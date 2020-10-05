/* ******************************************************************************
 * Copyright 2015 Cypher Cove, LLC
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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;

/** Renders a mesh rectangle that fills the screen if no projection matrix is used. The only vertex
 * attribute for the shader is {@code a_position}. To use it, a separate ShaderProgram is needed. The
 * {@code shaderProgram.begin()} and {@code shaderProgram.end()} should be called before and after
 * {@linkplain #render(ShaderProgram)}.
 * @author cypherdare
 */
public class FullScreenQuad implements Disposable {

    private Mesh mesh;
    private boolean blendingEnabled = false;
    private int blendSrcFunc = GL20.GL_SRC_ALPHA;
    private int blendDstFunc = GL20.GL_ONE_MINUS_SRC_ALPHA;
    private int blendSrcFuncAlpha = GL20.GL_SRC_ALPHA;
    private int blendDstFuncAlpha = GL20.GL_ONE_MINUS_SRC_ALPHA;

    private static final float[] VERTICES ={
            -1,-1,0,
            1,-1,0,
            1,1,0,
            -1,1,0
    };

    public FullScreenQuad (){
        mesh = new Mesh(true, 4, 0,
                new VertexAttribute(VertexAttributes.Usage.Position, 3,"a_position"));
        mesh.setVertices(VERTICES);
    }

    public void render (ShaderProgram shaderProgram){
        if (blendingEnabled) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            if (blendSrcFunc != -1) Gdx.gl.glBlendFuncSeparate(blendSrcFunc, blendDstFunc, blendSrcFuncAlpha, blendDstFuncAlpha);
        } else {
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        mesh.render(shaderProgram, GL20.GL_TRIANGLE_FAN);

        if (blendingEnabled)
            Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public void setBlending (boolean enabled){
        blendingEnabled = enabled;
    }

    public void setBlendFunction (int srcFunc, int dstFunc) {
        setBlendFunctionSeparate(srcFunc, dstFunc, srcFunc, dstFunc);
    }

    public void setBlendFunctionSeparate (int srcFuncColor, int dstFuncColor, int srcFuncAlpha, int dstFuncAlpha) {
        blendSrcFunc = srcFuncColor;
        blendDstFunc = dstFuncColor;
        blendSrcFuncAlpha = srcFuncAlpha;
        blendDstFuncAlpha = dstFuncAlpha;
    }

    @Override
    public void dispose () {
        mesh.dispose();
    }
}
