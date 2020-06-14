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

import org.jetbrains.annotations.NotNull;

public class ColorUtil {

    private static final float[] hsv = new float[3];

    /**
     * @param color The color to determine the hue of.
     * @return The HSV-model hue of the RGB components, as a value between 0 and 1, divided evenly from red to green to blue and back to
     * red. If the color is desaturated, the hue is 0.
     */
    public static float getHue(@NotNull Color color) {
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
     * @param color The color to determine the saturation of.
     * @return The HSV-model saturation of the RGB components, as a value between 0 and 1, where 1 is fully saturated.
     */
    public static float getSaturation(@NotNull Color color) {
        float r = color.r, g = color.g, b = color.b;
        float max = Math.max(b, Math.max(r, g));
        float min = Math.min(b, Math.min(r, g));
        if (max == min)
            return 0;
        return (max - min) / max;
    }

    /**
     * @param color The color to determine the value of.
     * @return The HSV-model value of the RGB components, as a value between 0 and 1, where 1 is fully bright.
     */
    public static float getValue(@NotNull Color color) {
        return Math.max(color.b, Math.max(color.r, color.g));
    }

    /**
     * Shifts the hue of the color by the given amount, normalized across colors from 0 to 360.
     * @param color The source color to modify.
     * @param amount How much to shift The hue of the resulting color.
     * @return The modified input color.
     */
    @NotNull
    public static Color shiftHue(@NotNull Color color, float amount) {
        color.toHsv(hsv);
        hsv[0] = (hsv[0] + amount) % 360f;
        return color.fromHsv(hsv);
    }

    /**
     * Sets the hue of the color to the given value, normalized across colors from 0 to 360 and leaving
     * saturation and value unchanged.
     * @param color The source color to modify.
     * @param hue The hue of the resulting color.
     * @return The modified input color.
     */
    @NotNull
    public static Color setHue(@NotNull Color color, float hue) {
        color.toHsv(hsv);
        hsv[0] = hue;
        return color.fromHsv(hsv);
    }

    /**
     * Scales the saturation of the color (of the HSV model), leaving hue and value constant.
     * @param color The source color to modify.
     * @param scale How much to multiply the saturation of the color by.
     * @return The modified input color.
     */
    @NotNull
    public static Color scaleSaturation(@NotNull Color color, float scale) {
        color.toHsv(hsv);
        hsv[1] = Math.min(1, scale * hsv[1]);
        return color.fromHsv(hsv);
    }

    /**
     * Sets the saturation of the color (of the HSV model), leaving hue and value constant.
     * @param color The source color to modify.
     * @param saturation The saturation of the resulting color, from 0 to 1.
     * @return The modified input color.
     */
    @NotNull
    public static Color setSaturation(@NotNull Color color, float saturation) {
        color.toHsv(hsv);
        hsv[1] = saturation;
        return color.fromHsv(hsv);
    }

    /**
     * Scales the value (of the HSV model) of the color.
     * @param color The source color to modify.
     * @param scale How much to multiply the value of the color by.
     * @return The modified input color.
     */
    @NotNull
    public static Color scaleValue(@NotNull Color color, float scale) {
        color.toHsv(hsv);
        hsv[2] = Math.min(1, scale * hsv[2]);
        return color.fromHsv(hsv);
    }

    /**
     * Sets the value of the color (of the HSV model), leaving the hue and saturation constant.
     * @param color The source color to modify.
     * @param value The value of the resulting color, from 0 to 1.
     * @return The modified input color.
     */
    @NotNull
    public static Color setValue(@NotNull Color color, float value) {
        color.toHsv(hsv);
        hsv[2] = value;
        return color.fromHsv(hsv);
    }

    private static final Color TMP = new Color();
    /** Interpolates between two colors using the RGB model, and then adjusts the saturation of the
     * result to the interpolated saturation of the source colors.
     * @param result The color to place the result in. Can be one of the inputs
     * @param one The beginning color for the blend.
     * @param two The end color for the blend.
     * @param amount The progress of the blend, from 0 to 1.
     * @return The result*/
    @NotNull
    public static Color blendRGBWithSaturation(@NotNull Color result, @NotNull Color one, @NotNull Color two, float amount){
        TMP.set(one).lerp(two, amount);
        result.a = TMP.a;
        float s1 = getSaturation(one);
        setSaturation(TMP, s1 + amount * (getSaturation(two) - s1));
        result.set(TMP);
        return result;
    }
}
