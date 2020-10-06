/* ******************************************************************************
 * Copyright 2020 See AUTHORS file.
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

/**
 * A listener that provides convenient non-core project access to events on the GLThread.
 */
public interface WallpaperEventListener {
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
     * @param deltaTime     The frame delta time reported by libGDX.
     */
    void onRender(LiveWallpaperListener liveWallpaper, float deltaTime);

    /**
     * Called on the GL thread immediately after the SharedPreferences have changed.
     *
     * @param liveWallpaper The wrapped live wallpaper.
     */
    void onSettingsChanged(LiveWallpaperListener liveWallpaper);

    /**
     * Called when the user taps multiple times in the same spot within a small time window.
     *
     * @param tapCount The number of taps that occurred in the same location.
     * @return True if the tap count should be reset.
     */
    boolean onMultiTap(int tapCount);
}
