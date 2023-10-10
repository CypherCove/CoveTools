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
package com.cyphercove.covetools.android.preferences

import android.content.SharedPreferences

/**
 * Retrieves the value of the preference for the given [key]. A default return value of O, false, or
 *  an empty String is used.
 */
inline operator fun <reified T : Any> SharedPreferences.get(key: String): T {
    return when (T::class) {
        Int::class -> getInt(key, 0)
        Boolean::class -> getBoolean(key, false)
        String::class -> getString(key, "")
        Long::class -> getLong(key, 0L)
        Float::class -> getFloat(key, 0f)
        else -> error("Unsupported type")
    } as T
}

/**
 * Retrieves the value of the preference for the given [key].
 */
inline operator fun <reified T : Any> SharedPreferences.get(key: String, defaultValue: T): T {
    return when (T::class) {
        Int::class -> getInt(key, defaultValue as Int)
        Boolean::class -> getBoolean(key, defaultValue as Boolean)
        String::class -> getString(key, defaultValue as String)
        Long::class -> getLong(key, defaultValue as Long)
        Float::class -> getFloat(key, defaultValue as Float)
        else -> error("Unsupported type")
    } as T
}