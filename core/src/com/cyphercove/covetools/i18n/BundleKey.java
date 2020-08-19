/*******************************************************************************
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
package com.cyphercove.covetools.i18n;

import com.badlogic.gdx.utils.I18NBundle;
import java.util.MissingResourceException;

/**
 * A representation of an I18NBundle key, for use with {@link SafeBundle}. Calling
 * {@code toString()} should return the actual key.
 * */
interface BundleKey {
    /**
     * Gets the String for this key
     *
     * @exception NullPointerException if a backing I18NBundle has not been set.
     * @exception MissingResourceException if no string for this key can be found and
     * {@link I18NBundle#getExceptionOnMissingKey()} returns {@code true}.
     * @return the string for the given key or the key surrounded by {@code ???} if it cannot be
     * found and {@link I18NBundle#getExceptionOnMissingKey()} returns {@code false}.
     * */
    String get();

    /**
     * Gets the string for this key after replacing the given arguments if they occur.
     *
     * @param args the arguments to be replaced in the String associated to this key.
     * @exception NullPointerException if a backing I18NBundle has not been set.
     * @exception MissingResourceException if no string for this key can be found.
     * @return the string for the given key formatted with the given arguments
     * */
    String format(Object... args);
}
