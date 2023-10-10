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
package com.cyphercove.covetools.android.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import com.cyphercove.covetools.android.preferences.get

/**
 * A type that holds a reference to an Android [Context].
 *
 * **Planned obsolescence.** When Kotlin context receivers becomes a stable feature, this will be
 * converted to an interface, and the member functions will be moved into other packages.
 *
 * Currently holds convenience functions for loading SharedPreference values using a key defined by
 * a String resource.
 */
open class ContextUser(val context: Context) {

    /**
     * Retrieves the value of the preference for the given [keyId], a String resource defining the
     * key for the preference. A default return value of O, false, or an empty String is used.
     */
    inline operator fun <reified T : Any> SharedPreferences.get(@StringRes keyId: Int): T =
        get(context.getString(keyId))

    /**
     * Retrieves the value of the preference for the given [keyId], a String resource defining the
     * key for the preference.
     */
    inline operator fun <reified T : Any> SharedPreferences.get(@StringRes keyId: Int, defaultValue: T): T =
        get(context.getString(keyId), defaultValue)
}