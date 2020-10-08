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
package com.cyphercove.covetools.graphics;

import com.badlogic.gdx.graphics.Texture;

/**
 * The most common pairings of minification and magnification filters.
 */
public enum TextureFilterPair {
    Nearest(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest, false),
    Linear(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear, false),
    Bilinear(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.Linear, true),
    Trilinear(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear, true);

    public final Texture.TextureFilter minFilter;
    public final Texture.TextureFilter magFilter;
    public final boolean usesMipMaps;

    TextureFilterPair(Texture.TextureFilter minFilter, Texture.TextureFilter magFilter, boolean usesMipMaps) {
        this.minFilter = minFilter;
        this.magFilter = magFilter;
        this.usesMipMaps = usesMipMaps;
    }
}
