package com.cyphercove.covetools.examples;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.cyphercove.covetools.Example;
import com.cyphercove.covetools.math.Ease;
import com.cyphercove.covetools.math.Scalar;
import com.cyphercove.covetools.tween.TweenManager;
import com.cyphercove.covetools.utils.ClickRingIllustrator;
import com.cyphercove.covetools.utils.Disposal;

import static com.cyphercove.covetools.tween.Tweens.*;

public class TweenTest extends Example {

    private Texture texture;
    private TextureRegion region;
    private SpriteBatch batch;
    private Viewport viewport;
    private GameObject gameObject;
    private TweenManager tweenManager;
    private ClickRingIllustrator clickRingIllustrator;
    private Vector2 tmp = new Vector2();

    private class GameObject {
        Scalar alpha = new Scalar(1f);
        float angle;
        float size = 80f;
        Vector2 position = new Vector2();
        public void draw (){
            batch.setColor(1f, 1f, 1f, alpha.x);
            batch.draw(region, position.x - size / 2, position.y- size / 2,  size / 2, size / 2, size, size, 1f, 1f, angle);
        }
    }

    public TweenTest(){
        texture = new Texture(Gdx.files.internal("cyphercove.png"), true);
        texture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        batch = new SpriteBatch(5);
        viewport = new ExtendViewport(500, 500);
        region = new TextureRegion(texture);
        gameObject = new GameObject();
        gameObject.position.set(-200, -200);
        tweenManager = new TweenManager();
        clickRingIllustrator = new ClickRingIllustrator(35);
        makeSquareLoop();
        Gdx.input.setInputProcessor(inputProcessor);
    }

    void makeSquareLoop (){
        to(gameObject.position, -150, 150, 1f, Ease.wrap(Interpolation.bounceOut)).delay(0.3f)
                .next(to(gameObject.position, 150, 150, .5f, Ease.quintic()))
                .next(to(gameObject.position, 150, -150, 0.4f, Ease.quintic()))
                .next(to(gameObject.position, -150, -150, 0.4f, Ease.cubic()))
                .loop()
                .start(tweenManager);
    }

    void makeDoubleTriLoopAndRestartSquare (){
        to(gameObject.position, 0, 140, 0.5f, Ease.quintic())
                .next(to(gameObject.position, 120, -30, 0.25f, Ease.quintic()))
                .next(to(gameObject.position, -120, -30, 0.25f, Ease.quintic()))
                .loop(2)
                .onComplete(tweenTarget -> makeSquareLoop())
                .start(tweenManager);
    }

    void makeToPointAndResume (float x, float y){
        to(gameObject.position, x, y, 1f, Ease.quintic())
                .onComplete(tweenTarget -> makeSquareLoop())
                .start(tweenManager);
    }

    InputAdapter inputProcessor = new InputAdapter(){
        @Override
        public boolean touchDown (int screenX, int screenY, int pointer, int button) {
            viewport.unproject(tmp.set(screenX, screenY));
            if (button == Input.Buttons.LEFT){
                makeToPointAndResume(tmp.x, tmp.y);
                clickRingIllustrator.addClick(tmp.x, tmp.y);
                return true;
            }
            else if (button == Input.Buttons.RIGHT){
                makeDoubleTriLoopAndRestartSquare();
                clickRingIllustrator.addClick(tmp.x, tmp.y);
            }
            return false;
        }
    };

    @Override
    public void render (float delta) {
        tweenManager.step(delta);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        gameObject.draw();
        batch.end();
        clickRingIllustrator.render(delta, batch);
    }

    @Override
    public void resize (int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose () {
        Disposal.clear(this);
    }
}
