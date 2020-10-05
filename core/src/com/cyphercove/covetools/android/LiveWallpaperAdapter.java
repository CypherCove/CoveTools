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

import com.badlogic.gdx.ApplicationAdapter;

/**
 * Empty convenience implementation of {@link LiveWallpaperListener}.
 */
public class LiveWallpaperAdapter extends ApplicationAdapter implements LiveWallpaperListener {
    @Override
    public void render(float xOffset, float yOffset, float xOffsetLooping, float xOffsetFake) {

    }

    @Override
    public void onPreviewStateChange(boolean isPreview) {

    }

    @Override
    public void onIconDropped(int x, int y) {

    }

    @Override
    public void onSettingsChanged() {

    }
}
