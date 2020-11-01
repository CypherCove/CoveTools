/* ******************************************************************************
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

import com.badlogic.gdx.graphics.Color;

/**
 * Allows access to Live Wallpaper-specific application methods from the core module.
 */
public interface LiveWallpaperApplicationDelegate {
    /**
     * Notify the wallpaper engine that the significant colors of the wallpaper have changed.
     * @param primaryColor The most visually significant color.
     * @param secondaryColor The second most visually significant color.
     * @param tertiaryColor The third most visually significant color.
     */
    void notifyColorsChanged (Color primaryColor, Color secondaryColor, Color tertiaryColor);
}
