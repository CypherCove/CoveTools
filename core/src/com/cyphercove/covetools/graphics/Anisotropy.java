/*******************************************************************************
 * Copyright 2015 Cypher Cove, LLC
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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.BufferUtils;

import java.nio.FloatBuffer;

public class Anisotropy {

    private static boolean anisotropySupported = false;
    private static boolean checkComplete = false;
    private static float maxAnisotropySupported = 1.0f;

    /**
     * Applies the given anisotropic level to the texture. Returns the anisotropy value that was applied,
     * based on device's maximum capability. Note that this call binds the texture.
     *
     * @param texture    The texture to apply anisotropy to.
     * @param anisotropy The anisotropic level to apply. (Will be reduced if device capability is less.
     * @return The anisotropic level that was applied, or -1.0 if anisotropy is not supported by the device.
     */
    public static float setTextureAnisotropy (Texture texture, float anisotropy) {
        if (!checkComplete)
            isSupported();
        if (anisotropySupported) {
            texture.bind();
            float valueApplied = Math.min(maxAnisotropySupported, anisotropy);
            Gdx.gl20.glTexParameterf(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAX_ANISOTROPY_EXT, valueApplied);
            return valueApplied;
        } else {
            return -1f;
        }
    }

    /**
     * @return Whether the device supports anisotropic filtering.
     */
    public static boolean isSupported () {
        GL20 gl = Gdx.gl;
        if (gl != null) {
            if (Gdx.graphics.supportsExtension("GL_EXT_texture_filter_anisotropic")) {
                anisotropySupported = true;
                FloatBuffer buffer = BufferUtils.newFloatBuffer(16);
                buffer.position(0);
                buffer.limit(buffer.capacity());
                Gdx.gl20.glGetFloatv(GL20.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, buffer);
                maxAnisotropySupported = buffer.get(0);
            }
            checkComplete = true;
        }
        return anisotropySupported;
    }

    /**
     * @return The max anisotropic filtering level supported by device, or 1 if it isn't supported.
     */
    public static float getMaxAnisotropySupported () {
        if (!checkComplete)
            isSupported();
        return maxAnisotropySupported;
    }
}
