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
package com.cyphercove.covetools.utils;

public class FrameRateLimiter {

    private static long lastTime;

    /** Call at the end of the {@code render()} method to impose a limit on the frame rate by sleeping the thread.
     * @param maxFPS The target maximum frame rate.*/
    public static void limit (int maxFPS){
        if (maxFPS > 0){
            int deltaMillisMin = 1000 / maxFPS;
            long deltaMillis = System.currentTimeMillis() - lastTime;
            if (deltaMillis < deltaMillisMin){
                try {Thread.sleep(deltaMillisMin - deltaMillis);}
                catch (InterruptedException e) {}
            }
        }

        lastTime = System.currentTimeMillis();
    }
}
