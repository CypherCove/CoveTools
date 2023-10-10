/*******************************************************************************
 * Copyright 2023 See AUTHORS file.
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
package com.cyphercove.covetools.android

import android.content.res.Configuration
import com.badlogic.gdx.backends.android.AndroidDaydream

/**
 * AndroidDaydream appears to have a bug in `onConfigurationChanged` on devices running Android 11
 * or later. This class is an attempted workaround, and can be used instead of AndroidDaydream. If
 * it is successful in CypherCove apps for some time, I will make a PR to put it in libGDX and
 * retire this class.
 *
 * [onKeyboardConfigurationChangeAttempted] can be overridden to react to a failed or successful
 * configuration change.
 *
 * See [issue 6633](https://github.com/libgdx/libgdx/issues/6633) on Github.
 */
open class SafeDaydream: AndroidDaydream() {
    override fun onConfigurationChanged(config: Configuration) {
        val keyboardAvailable = config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO
        try {
            super.onConfigurationChanged(config)
            onKeyboardConfigurationChangeAttempted(true)
        } catch (e: NullPointerException) {
            // Attempt to update the state on next loop of main thread, in case it is ready by then.
            // If not even the handler is available, log as an error and do nothing.
            handler?.post {
                input?.setKeyboardAvailable(keyboardAvailable)
                    ?: onKeyboardConfigurationChangeAttempted(false)
            } ?: onKeyboardConfigurationChangeAttempted(false)
        }
    }

    /**
     * Called after each configuration change, indicating whether the change was successfully made
     * to the Keyboard state.
     */
    open fun onKeyboardConfigurationChangeAttempted(success: Boolean) { }
}