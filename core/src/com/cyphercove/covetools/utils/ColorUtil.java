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
package com.cyphercove.covetools.utils;

import com.badlogic.gdx.graphics.Color;

public class ColorUtil {

    /**
     * @return The HSV-model hue of the RGB components, as a value between 0 and 1, divided evenly from red to green to blue and back to
     * red. If the color is desaturated, the hue is 0.
     */
    public static float getHue(Color color) {
        float r = color.r, g = color.g, b = color.b;
        float max = Math.max(b, Math.max(r, g));
        float min = Math.min(b, Math.min(r, g));
        if (max == min) {
            return 0;
        } else {
            float dem = max - min;
            float hue;
            if (r == max) {
                hue = (g - b / dem) % 6f;
            } else if (g == max) {
                hue = 2f + (b - r) / dem;
            } else {
                hue = 4f + (r - g) / dem;
            }
            if (hue < 0) {
                hue += 6f;
            }
            hue /= 6f;
            return hue;
        }
    }

    /**
     * @return The HSV-model saturation of the RGB components, as a value between 0 and 1, where 1 is fully saturated.
     */
    public static float getSaturation(Color color) {
        float r = color.r, g = color.g, b = color.b;
        float max = Math.max(b, Math.max(r, g));
        float min = Math.min(b, Math.min(r, g));
        if (max == min)
            return 0;
        return (max - min) / max;
    }

    /**
     * @return The HSV-model value of the RGB components, as a value between 0 and 1, where 1 is fully bright.
     */
    public static float getValue(Color color) {
        return Math.max(color.b, Math.max(color.r, color.g));
    }

    /**
     * Modifies the color to hold hue, saturation, and value in the RGB components. This is more efficient than calling
     * {@link #getHue(Color)}, {@link #getSaturation(Color)}, and {@link #getValue(Color)} separately.
     * <p>
     * This is private because none of the other methods are intended to work with a color that is storing data this way.
     * It is used internally as a convenient way to avoid allocating arrays.
     *
     * @param color The RGB color to transform
     * @return The modified color.
     */
    private static Color toHSV(Color color) {
        float r = color.r, g = color.g, b = color.b;
        float max = Math.max(b, Math.max(r, g));
        float min = Math.min(b, Math.min(r, g));
        if (max == min) {
            color.r = 0;
        } else {
            float dem = max - min;
            float hue;
            if (r == max) {
                hue = (g - b / dem) % 6f;
            } else if (g == max) {
                hue = 2f + (b - r) / dem;
            } else {
                hue = 4f + (r - g) / dem;
            }
            if (hue < 0) {
                hue += 6f;
            }
            hue /= 6f;
            color.r = hue;
        }
        if (max == min)
            color.g = 0;
        else
            color.g = (max - min) / max;
        color.b = max;
        return color;
    }

    public static Color fromHSVToRGB(Color color) {
        return setFromHSV(color, color.r, color.g, color.b);
    }

    /**
     * Sets the colors' R, G, and B values based on the given hue, saturation, and value.
     *
     * @param color      The color to modify
     * @param hue        The HSV-model hue of the RGB components, as a value between 0 and 1, divided evenly from red to
     *                   green to blue and back to red. If the color is desaturated, the hue is 0.
     * @param saturation The HSV-model saturation of the RGB components, as a value between 0 and 1, where 1 is fully saturated.
     * @param value      The HSV-model value of the RGB components, as a value between 0 and 1, where 1 is fully bright.
     * @return The modified input color.
     */
    public static Color setFromHSV(Color color, float hue, float saturation, float value) {
        float chroma = saturation * value;
        float huePrime = hue * 6f;
        float X = chroma * (1 - Math.abs(huePrime % 2 - 1));
        float r, g, b;
        if (huePrime < 1) {
            r = chroma;
            g = X;
            b = 0;
        } else if (huePrime < 2) {
            r = X;
            g = chroma;
            b = 0;
        } else if (huePrime < 3) {
            r = 0;
            g = chroma;
            b = X;
        } else if (huePrime < 4) {
            r = 0;
            g = X;
            b = chroma;
        } else if (huePrime < 5) {
            r = X;
            g = 0;
            b = chroma;
        } else if (huePrime < 6) {
            r = chroma;
            g = 0;
            b = X;
        } else {
            r = 0;
            g = 0;
            b = 0;
        }

        float m = value - chroma;
        color.r = r + m;
        color.g = g + m;
        color.b = b + m;

        return color;
    }

    /**
     * Shifts the hue of the color by the given amount, normalized across colors from 0 to 1.
     *
     * @return The modified input color.
     */
    public static Color shiftHue(Color color, float amount) {
        toHSV(color);
        return setFromHSV(color, (color.r + amount) % 1f, color.g, color.b);
    }

    /**
     * Sets the hue of the color to the given value, normalized across colors from 0 to 1.
     *
     * @return The modified input color.
     */
    public static Color setHue(Color color, float hue) {
        toHSV(color);
        return setFromHSV(color, hue % 1f, color.g, color.b);
    }

    /**
     * Scales the saturation of the color.
     *
     * @return The modified input color.
     */
    public static Color scaleSaturation(Color color, float scale) {
        toHSV(color);
        return setFromHSV(color, color.r, Math.min(color.g * scale, 1f), color.b);
    }

    /**
     * Sets the saturation of the color, leaving hue and value constant.
     *
     * @return The modified input color.
     */
    public static Color setSaturation(Color color, float saturation) {
        toHSV(color);
        return setFromHSV(color, color.r, saturation, color.b);
    }

    /**
     * Scales the value of the color.
     *
     * @return The modified input color.
     */
    public static Color scaleValue(Color color, float scale) {
        toHSV(color);
        return setFromHSV(color, color.r, color.g, Math.min(color.b * scale, 1f));
    }

    /**
     * Sets the value of the color, leaving the hue and saturation constant.
     *
     * @return The modified input color.
     */
    public static Color setValue(Color color, float value) {
        toHSV(color);
        return setFromHSV(color, color.r, color.g, value);
    }

    public static final Color TMP = new Color();
    /** Interpolates between two ARGB8888 colors using the RGB model, and then adjusting the final value by lerped saturation. */
    public static Color blendRGBWithSaturation(Color one, Color two, float amount){
        TMP.set(one).lerp(two, amount);
        float s1 = getSaturation(one);
        float s2 = getSaturation(two);
        toHSV(TMP);
        return setFromHSV(TMP, TMP.r, s1 + amount * (s2 - s1), TMP.b);
    }
}
