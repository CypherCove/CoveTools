/* ******************************************************************************
 * Copyright 2020 Cypher Cove, LLC
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

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * A wrapper for a FrameBuffer to make it easier to resize (by disposing and creating new). It also
 * may hold the previous FrameBuffer for resize in case the next resize restores the previous size.
 * This can allow mobile screen rotation to be done quickly without a pause.
 * <p>
 * ResizableFrameBuffer also automatically falls back to RGB565 if RGBA8888 is used but not supported
 * by the device.
 */
public class ResizableFrameBuffer implements Disposable {

    private FrameBuffer current, held;

    private final Pixmap.Format format;
    private final boolean hasDepth;
    private final boolean hasStencil;
    private final boolean keepPreviousSize;

    public ResizableFrameBuffer(Pixmap.Format format, boolean hasDepth, boolean hasStencil, boolean keepPreviousSize) {
        this.format = format;
        this.hasDepth = hasDepth;
        this.hasStencil = hasStencil;
        this.keepPreviousSize = keepPreviousSize;
    }

    /**
     * @return The FrameBuffer for the current size, or null if resize has not yet been called or
     * this ResizableFrameBuffer has been disposed.
     */
    public FrameBuffer getCurrent() {
        return current;
    }

    /**
     * Sets the size of the FrameBuffer returned by {@link #getCurrent()}. Texture filters and wrap
     * properties are maintained.
     * @param width Width of the FrameBuffer.
     * @param height Height of the FrameBuffer.
     */
    public void resize (int width, int height) {
        if (width == 0 || height == 0){
            return; // Invalid size, protect from Lwjgl3 minification if resize calls through to this.
        }

        if (current != null && current.getWidth() == width && current.getHeight() == height){
            return;
        }

        Texture previousTexture = current == null ? null : current.getColorBufferTexture();
        Texture.TextureFilter minFilter = null;
        Texture.TextureFilter magFilter = null;
        Texture.TextureWrap uWrap = null;
        Texture.TextureWrap vWrap = null;
        if (previousTexture != null) {
            minFilter = previousTexture.getMinFilter();
            magFilter = previousTexture.getMagFilter();
            uWrap = previousTexture.getUWrap();
            vWrap = previousTexture.getVWrap();
        }

        if (held != null && held.getWidth() == width && held.getHeight() == height){
            FrameBuffer oldCurrent = current;
            current = held;
            held = oldCurrent;
        } else {
            if (held != null) {
                held.dispose();
                held = null;
            }

            if (keepPreviousSize) {
                held = current;
            } else if (current != null) {
                current.dispose();
            }

            current = generate(width, height);
        }
        if (previousTexture != null) {
            Texture currentTexture = current.getColorBufferTexture();
            currentTexture.setFilter(minFilter, magFilter);
            currentTexture.setWrap(uWrap, vWrap);
        }
    }

    private FrameBuffer generate (int width, int height) {
        FrameBuffer frameBuffer;
        try {
            frameBuffer = new FrameBuffer(format, width, height, hasDepth, hasStencil);
        } catch (GdxRuntimeException e) {
            if (format == Pixmap.Format.RGBA8888) {
                frameBuffer = new FrameBuffer(Pixmap.Format.RGB565, width, height, hasDepth, hasStencil);
            } else {
                throw e;
            }
        }
        return frameBuffer;
    }

    @Override
    public void dispose() {
        if (current != null){
            current.dispose();
            current = null;
        }
        if (held != null){
            held.dispose();
            held = null;
        }
    }
}
