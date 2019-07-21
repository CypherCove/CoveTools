/*******************************************************************************
 * Copyright 2019 Cypher Cove, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.cyphercove.covetools.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class GaussianBlur implements Disposable{

    public static final int MAX_RADIUS = 8; //Increasing it beyond 8 would require extra vec4s for offsets and weights
    private int maxRadius;
    public static final float MIN_SIGMA = 0.28f; //sigma less than this means blurring will be unnoticeable.

    private boolean blendingEnabled = false;
    private int blendSrcFunc = GL20.GL_SRC_ALPHA;
    private int blendDstFunc = GL20.GL_ONE_MINUS_SRC_ALPHA;
    private Color clearColor = new Color(0,0,0,1);
    private SpriteBatch spriteBatch;
    private ShaderProgram blurPassShaderProgram;
    private FrameBuffer fboInitialTargetStandard;
    private FrameBuffer fboInitialTargetInverted;
    private boolean keepInverseTarget;
    private boolean useInverseTarget;
    private FrameBuffer fboPass1;
    private FrameBuffer fboPass2;
    private float horizontalOnePixelSize;
    private float verticalOnePixelSize;
    private final float[] tmpArray = new float[MAX_RADIUS+1];
    private float[] offsets = new float[4];
    private float weightAtCenter;
    private float[] weights = new float[4];
    private float sigma = -1;
    private int currentWidth = -1;
    private int currentHeight = -1;

    private Matrix4 fboToSceneProjectionMatrix;
    private Matrix4 fboToSceneProjectionMatrixLeft;
    private Matrix4 fboToSceneProjectionMatrixRight;

    GaussianBlurShaderProvider shaderProvider;

    private boolean hasDepth = true;
    private boolean depthTestingToScene = true;

    public interface CustomShaderPreparer {
        void applyCustomShaderParameters(SpriteBatch spriteBatch, boolean flipped);
    }

    private CustomShaderPreparer customShaderPreparer;

    /**
     *
     * @param initialAndMaxRadius The maximum blur radius this instance can support. The initial
     *                  radius is set to this value. The actual maximum blur radius will be rounded
     *                  up to the nearest even integer due to internal workings.
     * @param hasDepth
     * @param keepInverseTarget Whether, when resizing, to create two target frame buffers so a screen
     *                          rotation can be done quickly without a pause.
     */
    public GaussianBlur(float initialAndMaxRadius, boolean hasDepth, boolean keepInverseTarget){
        this(initialAndMaxRadius, hasDepth, keepInverseTarget, new GaussianBlurShaderProvider());
    }

    /**
     *
     * @param initialAndMaxRadius The maximum blur radius this instance can support. The initial
     *                  radius is set to this value. The actual maximum blur radius will be rounded
     *                  up to the nearest even integer due to internal workings.
     * @param hasDepth
     * @param keepInverseTarget Whether, when resizing, to create two target frame buffers so a screen
     *                          rotation can be done quickly without a pause.
     */
    public GaussianBlur(float initialAndMaxRadius, boolean hasDepth, boolean keepInverseTarget,
                        GaussianBlurShaderProvider shaderProvider){
        if (initialAndMaxRadius < 0 || initialAndMaxRadius > MAX_RADIUS){
            throw new GdxRuntimeException(
                    "Radius must be between 0 and " + MAX_RADIUS + " inclusive.");
        }

        this.hasDepth = hasDepth;
        this.keepInverseTarget = keepInverseTarget;
        this.useInverseTarget = false;
        spriteBatch = new SpriteBatch(1);

        this.maxRadius = (int)Math.ceil(initialAndMaxRadius);
        if (this.maxRadius % 2 != 0)
            this.maxRadius++; //round up to nearest even integer.
        offsets = new float[this.maxRadius /2];
        weights = new float[this.maxRadius /2];
        setRadius(initialAndMaxRadius);

        setTextureToSceneDepth(0.9999999f);//By default draw behind everything.

        this.shaderProvider = shaderProvider;

        if (initialAndMaxRadius > 0)
            blurPassShaderProgram = shaderProvider.obtainBlurPassShaderProgram(this.maxRadius);

    }

    @Override
    public void dispose() {
        if (fboInitialTargetStandard !=null) fboInitialTargetStandard.dispose();
        if (fboInitialTargetInverted !=null) fboInitialTargetInverted.dispose();
        if (fboPass1!=null) fboPass1.dispose();
        if (fboPass2!=null) fboPass2.dispose();
        spriteBatch.dispose();
        shaderProvider.disposeShader(blurPassShaderProgram);
    }

    /**
     * Must be called at least once. The texture size is matched with the larger of the screen width
     * and height.
     */
    public void resize(int textureSize, int screenWidth, int screenHeight){
        if (screenWidth > screenHeight){
            resize(textureSize, (int)(textureSize/(float)screenWidth*(float)screenHeight));
        } else {
            resize((int)(textureSize/(float)screenHeight*(float)screenWidth), textureSize);
        }
    }

    /**
     * Must be called at least once.
     */
    public void resize(int textureWidth, int textureHeight){
        if (fboInitialTargetStandard != null && currentWidth==textureWidth && currentHeight==textureHeight)
            return;

        if (keepInverseTarget && currentWidth==textureHeight && currentHeight==textureWidth){
            useInverseTarget = !useInverseTarget;
            currentWidth = textureWidth;
            currentHeight = textureHeight;
            return;
        }

        currentWidth = textureWidth;
        currentHeight = textureHeight;

        if (fboInitialTargetStandard != null)
            fboInitialTargetStandard.dispose();
        fboInitialTargetStandard = getLinearFrameBuffer(textureWidth, textureHeight, hasDepth);

        if (fboInitialTargetInverted != null)
            fboInitialTargetInverted.dispose();
        if (keepInverseTarget){
            fboInitialTargetInverted = getLinearFrameBuffer(textureHeight, textureWidth, hasDepth);
        }

        if (maxRadius > 0) {
            if (fboPass1 != null)
                fboPass1.dispose();
            fboPass1 = getLinearFrameBuffer(textureWidth, textureHeight, false);
            if (fboPass2 != null)
                fboPass2.dispose();
            fboPass2 = getLinearFrameBuffer(textureWidth, textureHeight, false);

            verticalOnePixelSize = 1f / (float) textureHeight;
            horizontalOnePixelSize = 1f / (float) textureWidth;
        }
    }

    private static boolean try8888 = true;
    private static FrameBuffer getLinearFrameBuffer(int width, int height, boolean hasDepth){

        if (try8888) {
            try {
                FrameBuffer frameBuffer = new FrameBuffer(
                        Pixmap.Format.RGBA8888, width, height, hasDepth);
                return frameBuffer;
            } catch (IllegalStateException e) {
                try8888 = false;
                Gdx.app.log("GaussianBlur.getLinearFrameBuffer",
                        "Could not create RGBA8888 FrameBuffer. Switching to RGB565.");
            }
        }

        return new FrameBuffer(
                Pixmap.Format.RGB565, width, height, hasDepth);
    }

    public int getMaxRadius() {
        return maxRadius;
    }

    //Private because untested.
    private void setMaxRadius(int maxRadius) {
        shaderProvider.disposeShader(blurPassShaderProgram);
        shaderProvider.obtainBlurPassShaderProgram(maxRadius);
        setRadius(maxRadius);
    }

    /**
     * Set the blur radius. It can be set higher than the max radius, but clipping will be visible
     * if it exceeds it by more than ~15%.
     * @param radius
     */
    public void setRadius (float radius){
        setSigma(radius / 3f);
    }

    public float getRadius (){
        return 3f * sigma;
    }

    /** Prepare to use linear filtering to sample two points by controlling offsets. Method described
     * here: http://rastergrid.com/blog/2010/09/efficient-gaussian-blur-with-linear-sampling/*/
    private void setSigma(float sigma){

        if (this.sigma != sigma){
            this.sigma = sigma;

            if (sigma < MIN_SIGMA)
                return;

            if (maxRadius == 0)
                return;

            //Calculate standard weights
            float twoSigmaSquared = 2*sigma*sigma;
            float weightSum = 0;
            for (int i=0; i <= maxRadius; i++){
                tmpArray[i] = (1.0f / (MathUtils.PI * twoSigmaSquared)) *
                        (float)Math.exp(-(double)(i*i) / (double)twoSigmaSquared);
                weightSum += i==0 ? tmpArray[i] : 2*tmpArray[i];
            }

            //Normalize them to avoid darkening
            for (int i=0; i <= maxRadius; i++){
                tmpArray[i] /= weightSum;
            }

            //The first weight doesn't use the linear sampling optimization because it is at the
            //center
            weightAtCenter = tmpArray[0];

            //Fill into the optimized arrays
            for (int i=0; i < offsets.length; i++)
            {
                float left = tmpArray[i*2 + 1];
                float right = tmpArray[i*2 + 2];
                weights[i] = left + right;
                offsets[i] = (left * (i*2 + 1) + right * (i*2 + 2)) / weights[i];
            }

        }
    }

    public float getSigma(){
        return sigma;
    }

    protected boolean shouldBlur(){
        return maxRadius != 0 && sigma > MIN_SIGMA;
    }

    /**
     * Sets a clear color for the base textures, which tends to bleed into the top or right edge (whichever is longer).
     */
    public void setClearColor(Color color){
        clearColor.set(color);
    }

    /**Sets whether and how to blend the texture into the scene
     *
     */
    public void setBlending(boolean enabled, int blendSrcFunc, int blendDstFunc){
        blendingEnabled = enabled;
        this.blendSrcFunc = blendSrcFunc;
        this.blendDstFunc = blendDstFunc;
    }

    /**
     * Set the normalized depth that the texture is rendered at if depthTestingToScene it true.
     */
    public void setTextureToSceneDepth(float depth){
        OrthographicCamera tempCam = new OrthographicCamera(2,2);
        tempCam.position.set(0,0,depth);
        tempCam.near = 0;
        tempCam.far = 1;
        tempCam.update();
        fboToSceneProjectionMatrix = new Matrix4(tempCam.combined);
        tempCam.up.set(1,0,0);
        tempCam.update();
        fboToSceneProjectionMatrixRight = new Matrix4(tempCam.combined);
        tempCam.up.set(-1,0,0);
        tempCam.update();
        fboToSceneProjectionMatrixLeft = new Matrix4(tempCam.combined);

    }

    public void begin(){

        if (fboInitialTargetStandard == null)
            throw new GdxRuntimeException("begin() called before resize().");

        GL20 gl = Gdx.gl20;
        if (useInverseTarget)
            fboInitialTargetInverted.begin();
        else
            fboInitialTargetStandard.begin();
        gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
        if (hasDepth){
            gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        } else {
            gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        }
    }

    private void doBlurPass(FrameBuffer fboInput, boolean vertical) {
        GL20 gl = Gdx.gl20;
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        spriteBatch.setShader(blurPassShaderProgram);
        gl.glDisable(GL20.GL_BLEND);
        spriteBatch.begin();
        if (useInverseTarget) {
            blurPassShaderProgram.setUniformf("u_size",
                    vertical ? horizontalOnePixelSize : verticalOnePixelSize,
                    0);
        } else {
            blurPassShaderProgram.setUniformf("u_size",
                    vertical ? 0 : horizontalOnePixelSize,
                    vertical ? verticalOnePixelSize : 0);
        }
        blurPassShaderProgram.setUniform4fv("u_offsets", offsets, 0, 4);
        blurPassShaderProgram.setUniformf("u_weightAtCenter", weightAtCenter);
        blurPassShaderProgram.setUniform4fv("u_weights", weights, 0, 4);

        spriteBatch.draw(fboInput.getColorBufferTexture(), -1, -1, 2, 2);
        spriteBatch.end();
        spriteBatch.setShader(null);
    }

    /**Disables depth testing. Must re-enable it if 3D API is expecting it in its RenderContext
     *
     */
    public void end(){
        FrameBuffer initialTargetBuffer;
        if (useInverseTarget)
            initialTargetBuffer = fboInitialTargetInverted;
        else
            initialTargetBuffer = fboInitialTargetStandard;
        initialTargetBuffer.end();

        if (shouldBlur()) {
            spriteBatch.disableBlending();
            Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);

            //rotate if using the inverse target
            if (useInverseTarget){
                spriteBatch.setProjectionMatrix(fboToSceneProjectionMatrixRight);
            } else {
                spriteBatch.setProjectionMatrix(fboToSceneProjectionMatrix);
            }
            fboPass1.begin();
            doBlurPass(initialTargetBuffer, false);
            fboPass1.end();
            if (useInverseTarget){ //don't rotate the second pass
                spriteBatch.setProjectionMatrix(fboToSceneProjectionMatrix);
            }
            fboPass2.begin();
            doBlurPass(fboPass1, true);
            fboPass2.end();
        }
    }

    public void render() {
        render(null);
    }

    public void render(ShaderProgram customShader){
        if (blendingEnabled){
            spriteBatch.setBlendFunction(blendSrcFunc, blendDstFunc);
            spriteBatch.enableBlending();
        } else{
            spriteBatch.disableBlending();
        }

        if (depthTestingToScene){
            Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        }

        FrameBuffer initialTargetBuffer;
        if (useInverseTarget) {
            initialTargetBuffer = fboInitialTargetInverted;
        } else {
            initialTargetBuffer = fboInitialTargetStandard;
        }

        spriteBatch.setShader(customShader);
        spriteBatch.setColor(Color.WHITE);

        if (shouldBlur()){
            Texture texture = fboPass2.getColorBufferTexture();
            if (useInverseTarget) {
                spriteBatch.setProjectionMatrix(fboToSceneProjectionMatrixLeft);
                spriteBatch.begin();
                applyCustomShaderParameters(true);
                spriteBatch.draw(texture, 1, -1, -2, 2);
            }else{
                spriteBatch.setProjectionMatrix(fboToSceneProjectionMatrix);
                spriteBatch.begin();
                applyCustomShaderParameters(false);
                spriteBatch.draw(texture, -1, 1, 2, -2);
            }
        } else{
            Texture texture = initialTargetBuffer.getColorBufferTexture();
            spriteBatch.setProjectionMatrix(fboToSceneProjectionMatrix);
            spriteBatch.begin();
            spriteBatch.draw(texture, -1, 1, 2, -2);
        }
        spriteBatch.end();

        spriteBatch.setShader(null);

        if (depthTestingToScene){
            Gdx.gl.glDisable(GL20.GL_DEPTH_TEST); //return to OpenGL default, as expected by other classes
        }
    }

    public CustomShaderPreparer getCustomShaderPreparer() {
        return customShaderPreparer;
    }

    public void setCustomShaderPreparer(CustomShaderPreparer customShaderPreparer) {
        this.customShaderPreparer = customShaderPreparer;
    }

    private void applyCustomShaderParameters(boolean flipped){
        if (customShaderPreparer != null)
            customShaderPreparer.applyCustomShaderParameters(spriteBatch, flipped);
    }


    /**
     * Set whether depth testing should be used when drawing the texture into the scene.
     * @param depthTestingToScene
     */
    public void setDepthTestingToScene(boolean depthTestingToScene) {
        this.depthTestingToScene = depthTestingToScene;
    }

}