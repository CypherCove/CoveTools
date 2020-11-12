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
package com.cyphercove.covetools.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.GridPoint2;

/**
 * Utility for cycling through a series of window resolutions. By default, the resolutions in the
 * queue are ones useful for creating screenshots for Android storefronts:
 * <table>
 *     <caption>Default queue</caption>
 *     <thead><tr><th>Description</th><th>Width</th><th>Height</th></tr></thead>
 *     <tbody>
 *         <tr><td>Portrait phone</td><td>480</td><td>800</td></tr>
 *         <tr><td>Landscape phone</td><td>800</td><td>480</td></tr>
 *         <tr><td>Landscape 7" tablet</td><td>1024</td><td>600</td></tr>
 *         <tr><td>Landscape 10" tablet</td><td>1280</td><td>800</td></tr>
 *         <tr><td>Feature graphic</td><td>1024</td><td>500</td></tr>
 *     </tbody>
 * </table>
 * Advance the window resolution using {@link #next()}.
 */
public final class ResolutionCycle {
    private ResolutionCycle() {
    }

    private static int index = -1;

    private static final GridPoint2[] SCREENSHOT_RESOLUTIONS = {
            new GridPoint2(480, 800),
            new GridPoint2(800, 480),
            new GridPoint2(1024, 600),
            new GridPoint2(1280, 800),
            new GridPoint2(1024, 500)
    };

    private static GridPoint2[] resolutions = SCREENSHOT_RESOLUTIONS;

    /**
     * @return The current queue of resolutions.
     */
    public static GridPoint2[] getResolutions() {
        return resolutions;
    }

    /**
     * Sets the queue of resolutions.
     * @param resolutions The series of resolutions to be used for the queue.
     */
    public static void setResolutions(GridPoint2... resolutions) {
        ResolutionCycle.resolutions = resolutions;
    }

    /**
     * Sets the window resolution to the next one in the queue.
     */
    public static void next() {
        index = (index + 1) % resolutions.length;
        GridPoint2 resolution = resolutions[index];
        Gdx.app.getGraphics().setWindowedMode(resolution.x, resolution.y);
    }

}
