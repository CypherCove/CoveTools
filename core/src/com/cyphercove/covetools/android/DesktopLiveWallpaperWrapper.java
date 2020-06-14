/*******************************************************************************
 * Copyright 2017 See AUTHORS file.
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
package com.cyphercove.covetools.android;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;

import org.jetbrains.annotations.NotNull;

/** Wrap a {@link LiveWallpaperListener} in a DesktopLiveWallpaperWrapper when passing it to
 * {@code initialize()} to enable a fake version of the {@code xOffset} features for desktop testing.
 *
 * @author cypherdare
 */
public class DesktopLiveWallpaperWrapper implements ApplicationListener {

    private LiveWallpaperListener liveWallpaper;

    public DesktopLiveWallpaperWrapper(@NotNull LiveWallpaperListener liveWallpaper) {
        this.liveWallpaper = liveWallpaper;
    }

    /**
     * Adjust these parameters to control the motion of the xOffset. These correspond to the smooth looping parameters
     * in the Android LiveWallpaperWrapper.
     *
     * @param minVelocity Minimum xOffset pan speed in units per second. Default is 0.4.
     * @param maxVelocity Maximum xOffset pan speed in units per second. Default is 1.5.
     */
    public void setSmoothLoopingXOffsetParams(float minVelocity, float maxVelocity) {
        offsetMinVelocity = minVelocity;
        offsetMaxVelocity = maxVelocity;
    }

    @Override
    public void create() {
        liveWallpaper.create();
    }

    float xOffset = 0.5f;
    float xOffsetTarget = 0.5f;
    protected float xOffsetFake = 0.5f;
    float xOffsetFakeTarget = 0.5f;
    static final float PIXEL_TO_XOFFSET_FAKE_RATIO = 750f;
    boolean fingerDown = false;
    private float offsetMaxVelocity = 1.5f; //per second
    private float offsetMinVelocity = .4f; //per second

    @Override
    public void render() {
        if (Gdx.input.justTouched() && Gdx.input.getY() < 100) {
            if (Gdx.input.getX() < 100) {
                xOffsetTarget = Math.max(xOffsetTarget - 0.2f, 0);
            } else if (Gdx.input.getX() > (Gdx.graphics.getWidth() - 100)) {
                xOffsetTarget = Math.min(xOffsetTarget + 0.2f, 1);
            }
            MathUtils.clamp(xOffsetTarget, 0, 1);
        }
        xOffset += (xOffsetTarget - xOffset) * 2 * Gdx.graphics.getDeltaTime();

        handleFakeTouch();

        liveWallpaper.render();
        liveWallpaper.render(xOffsetFake, 0.5f, xOffsetFake, xOffsetFake);
    }

    void handleFakeTouch() {
        if (Gdx.input.justTouched()) {
            fingerDown = true;
        }
        if (fingerDown) {
            if (Gdx.input.getDeltaX() != 0) {
                float swipeDelta = -Gdx.input.getDeltaX();
                xOffsetFakeTarget += swipeDelta / PIXEL_TO_XOFFSET_FAKE_RATIO;
                if (xOffsetFakeTarget < 0)
                    xOffsetFakeTarget = 0;
                else if (xOffsetFakeTarget > 1)
                    xOffsetFakeTarget = 1;
            }
        }

        if (!Gdx.input.isTouched())
            fingerDown = false;

        float offsetDelta = xOffsetFakeTarget - xOffsetFake;
        float offsetVelocity = offsetMaxVelocity * MathUtils.sin(offsetDelta * MathUtils.PI);
        if (offsetDelta < 0) { //moving left
            offsetVelocity -= offsetMinVelocity;
        } else if (offsetDelta > 0) {//moving right
            offsetVelocity += offsetMinVelocity;
        }
        float offsetAdder = offsetVelocity * Gdx.graphics.getDeltaTime();
        if (Math.abs(offsetAdder) >= Math.abs(offsetDelta)) {
            xOffsetFake = xOffsetFakeTarget;
        } else {
            xOffsetFake += offsetAdder;
        }
    }

    @Override
    public void resize(int width, int height) {
        liveWallpaper.resize(width, height);
    }

    @Override
    public void pause() {
        liveWallpaper.pause();
    }

    @Override
    public void resume() {
        liveWallpaper.resume();
    }

    @Override
    public void dispose() {
        liveWallpaper.dispose();
    }
}