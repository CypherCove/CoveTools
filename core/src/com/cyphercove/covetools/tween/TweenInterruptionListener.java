/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
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
package com.cyphercove.covetools.tween;

import org.jetbrains.annotations.NotNull;

/**
 *
 * @param <T> Tween target type
 */
public interface TweenInterruptionListener<T> {

    /** Called when another tween is interrupted by a tween with the same target. The associated tween will
     * not be completed. This method must not begin another tween with the same target.
     * @param tweenTarget The target object of the tween that was interrupted.
     */
    void onTweenInterrupted (@NotNull T tweenTarget);
}
