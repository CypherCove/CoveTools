package com.cyphercove.covetools.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

public class ClickRingIllustrator implements Disposable {
    Texture texture;
    Array<Click> clicks = new Array<>();
    float endSize;
    float appearTime = 0.15f;
    float fadeTime = 0.8f;

    private static class Click implements Pool.Poolable {
        public float x, y, age;

        public void reset () {
            age = 0;
        }
    }

    public ClickRingIllustrator (float endSize){
        texture = new Texture(Gdx.files.internal("clickring.png"), true);
        texture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        this.endSize = endSize;
    }

    public void render (float dt, Batch batch){
        batch.begin();
        for (Click click : clicks){
            click.age += dt;
            float size = click.age < appearTime ? Interpolation.fastSlow.apply(0, endSize, click.age / appearTime) : endSize;
            float alpha = click.age < appearTime ? 1 : Interpolation.fade.apply(MathUtils.clamp(1 - (click.age - appearTime) / fadeTime, 0, 1));
            batch.setColor(1, 1, 1, alpha);
            batch.draw(texture, click.x - size / 2, click.y - size / 2, size, size);
        }
        batch.end();
    }

    public void addClick (float x, float y){
        Click click = Pools.obtain(Click.class);
        click.x = x;
        click.y = y;
        clicks.add(click);
    }

    @Override
    public void dispose () {

    }
}
