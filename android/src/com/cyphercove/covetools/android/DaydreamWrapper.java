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
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidDaydream;

/**
 * Wrap a {@link LiveWallpaperListener} in a DaydreamWrapper when passing it to
 * {@link AndroidDaydream#initialize(ApplicationListener, AndroidApplicationConfiguration) initialize()} to enable
 * its {@link LiveWallpaperListener#render(float, float, float, float)} method, which will be passed 0.5 for all of the
 * offset values. This allows a {@link LiveWallpaperListener} to be easily repurposed as a daydream.
 *
 * @author cypherdare
 */
public class DaydreamWrapper implements ApplicationListener {

    /**
     * A listener that provides convenient non-core project access to events on the GLThread
     */
    public interface DaydreamEventListener {
        /**
         * Called immediately after {@code create()} is called on the live wallpaper.
         *
         * @param liveWallpaper The wrapped live wallpaper.
         */
        void onPostCreate(LiveWallpaperListener liveWallpaper);

        /**
         * Called once per render loop immediately before {@code render()} is called on the live wallpaper.
         *
         * @param liveWallpaper The wrapped live wallpaper.
         * @param deltaTime     The frame delta time reported by LibGDX.
         */
        void onRender(LiveWallpaperListener liveWallpaper, float deltaTime);

    }

    private LiveWallpaperListener liveWallpaper;
    private DaydreamEventListener daydreamEventListener;

    /**
     * Wraps a LiveWallpaperListener in an ApplicationAdapter that can be passed to
     * {@link AndroidDaydream#initialize(ApplicationListener, AndroidApplicationConfiguration)}.
     *
     * @param liveWallpaper The live wallpaper daydreamEventListener to wrap
     */
    public DaydreamWrapper(LiveWallpaperListener liveWallpaper) {
        this(liveWallpaper, null);
    }

    /**
     * Wraps a LiveWallpaperListener in an ApplicationAdapter that can be passed to
     * {@link AndroidDaydream#initialize(ApplicationListener, AndroidApplicationConfiguration)}.
     *
     * @param liveWallpaper         The live wallpaper daydreamEventListener to wrap
     * @param daydreamEventListener Optional listener to events related to the live wallpaper.
     */
    public DaydreamWrapper(LiveWallpaperListener liveWallpaper, DaydreamEventListener daydreamEventListener) {
        this.liveWallpaper = liveWallpaper;
        this.daydreamEventListener = daydreamEventListener;
    }

    public void setDaydreamEventListener(DaydreamEventListener daydreamEventListener) {
        this.daydreamEventListener = daydreamEventListener;
    }

    @Override
    public void render() {

        if (daydreamEventListener != null)
            daydreamEventListener.onRender(liveWallpaper, Gdx.graphics.getDeltaTime());
        liveWallpaper.render();
        liveWallpaper.render(0.5f, 0.5f, 0.5f, 0.5f);
    }

    @Override
    public void dispose() {
        liveWallpaper.dispose();
    }

    @Override
    public void create() {
        liveWallpaper.create();
        if (daydreamEventListener != null) {
            daydreamEventListener.onPostCreate(liveWallpaper);
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

}

