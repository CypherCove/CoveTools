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

/**
 * An application listener that provides additional functionality for live wallpapers. The additional methods are only
 * called if you wrap the listener in a LiveWallpaperWrapper when passing it to the {@code initialize()} method in the
 * Android project's live wallpaper service.
 *
 * @author cypherdare
 */
public interface LiveWallpaperListener extends ApplicationListener {
    /**
     * An alternate render method that provides additional live wallpaper data. This method is called immediately
     * following {@link #render()}.
     *
     * @param xOffset        The x offset reported by Android's live wallpaper engine
     * @param yOffset        The y offset reported by Android's live wallpaper engine
     * @param xOffsetLooping Some Android launchers can loop around from the last to the first home screen, and this provides
     *                       a graceful transition for that change by smoothly panning the offset back across.
     * @param xOffsetFake    A fake x offset based on screen swipes by the user. This can compensate for launchers that
     *                       do not change the xOffset.
     */
    void render(float xOffset, float yOffset, float xOffsetLooping, float xOffsetFake);

    /**
     * Called after 'isPreview' state had changed. This is generally called a single time at application initialization.
     *
     * @param isPreview Whether the current application is in preview mode.
     */
    void onPreviewStateChange(boolean isPreview);

    /**
     * Called in response to an icon dropping on the home screen. Not all Android launcher apps are guaranteed to support this.
     * @param x
     * @param y
     */
    void onIconDropped (int x, int y);

    /**
     * Called when the application's shared preferences are modified.
     */
    void onSettingsChanged();
}
