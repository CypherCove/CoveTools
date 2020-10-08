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
package com.cyphercove.covetools.assets;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.cyphercove.covetools.graphics.TextureFilterPair;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for a field to be populated with a reference to the specified Texture asset. A
 * path for the asset must be provided. Other parameters are used to generate a
 * {@link com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter}. Note that the parameters
 * do not have one-to-one correspondence with TextureParameter fields. If the following are too
 * restrictive, use {@link Asset} instead to specify an explicit parameter object.
 * <ul>
 *     <li>{@code format} is the Pixmap.Format of the Texture. Beware that it defaults to RGBA8888
 *     instead of defaulting to automatically detecting the image type from the file!</li>
 *     <li>{@code filter} uses a {@link TextureFilterPair} to specify both {@code TextureParameter.minFilter}
 *     and {@code TextureParameter.magFilter}, and the minification filter is used to set the value
 *     of {@code TextureParameter.genMipMaps} automatically. Default is {@code Nearest}.</li>
 *     <li>{@code wrap} corresponds to both {@code TextureParameter.wrapU} and
 *     {@code TextureParameter.wrapV}. Default is {@code ClampToEdge}.</li>
 * </ul>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TextureAsset {
    String value(); // the path
    Pixmap.Format format() default Pixmap.Format.RGBA8888;
    TextureFilterPair filter() default TextureFilterPair.Nearest;
    Texture.TextureWrap wrap() default Texture.TextureWrap.ClampToEdge;
}
