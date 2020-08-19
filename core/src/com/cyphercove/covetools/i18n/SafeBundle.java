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
package com.cyphercove.covetools.i18n;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.I18NBundle;

import java.util.Locale;
import java.util.MissingResourceException;

/**
 * A wrapper for {@link I18NBundle} that uses {@link BundleKey} to represent the keys so String
 * constants don't need to be passed around.
 * */
class SafeBundle {
    private final I18NBundle bundle;

    /**
     * Wraps an existing {@link I18NBundle}.
     * @param bundle The bundle to be wrapped.
     */
    public SafeBundle(I18NBundle bundle) {
        this.bundle = bundle;
    }

    /**
     * Creates a new bundle using the specified <code>baseFileHandle</code>; the default locale and
     * the default encoding "UTF-8".
     *
     * @param baseFileHandle the file handle to the base of the bundle
     * @exception MissingResourceException if no bundle for the specified base file handle can be found.
     * */
    public SafeBundle(FileHandle baseFileHandle) {
        bundle = I18NBundle.createBundle(baseFileHandle);
    }

    /**
     * Creates a new bundle using the specified <code>baseFileHandle</code> and <code>locale</code>;
     * the default encoding "UTF-8" is used.
     *
     * @param baseFileHandle the file handle to the base of the bundle
     * @param locale the locale for which a bundle is desired
     * @exception MissingResourceException if no bundle for the specified base file handle can be found.
     * */
    public SafeBundle(FileHandle baseFileHandle, Locale locale) {
        bundle = I18NBundle.createBundle(baseFileHandle, locale);
    }

    /**
     * Creates a new bundle using the specified <code>baseFileHandle</code> and <code>encoding</code>;
     * the default locale is used.
     *
     * @param baseFileHandle the file handle to the base of the bundle
     * @param encoding the charter encoding
     * @exception MissingResourceException if no bundle for the specified base file handle can be found.
     * */
    public SafeBundle(FileHandle baseFileHandle, String encoding) {
        bundle = I18NBundle.createBundle(baseFileHandle, encoding);
    }

    /**
     * Creates a new bundle using the specified <code>baseFileHandle</code>, <code>locale</code> and
     * <code>encoding</code>.
     *
     * @param baseFileHandle the file handle to the base of the bundle
     * @param locale the locale for which a bundle is desired
     * @param encoding the charter encoding
     * @exception MissingResourceException if no bundle for the specified base file handle can be
     * found.
     * */
    public SafeBundle(FileHandle baseFileHandle, Locale locale, String encoding) {
        bundle = I18NBundle.createBundle(baseFileHandle, locale, encoding);
    }

    /**
     * Returns the wrapped {@link I18NBundle}.
     * @return the wrapped bundle
     */
    public I18NBundle getWrappedBundle() {
        return bundle;
    }

    /**
     * Returns the locale of this bundle. This method can be used after instantiation to determine
     * whether the resource bundle really corresponds to the requested locale or is a fallback.
     *
     * @return the locale of this bundle
     * */
    public Locale getLocale() {
        return bundle.getLocale();
    }

    /**
     * Gets a string for the given key from this bundle.
     *
     * @param key the key for the desired string.
     * @exception NullPointerException if <code>key</code> is <code>null</code>
     * @exception MissingResourceException if no string for the given key can be found and
     * {@link I18NBundle#getExceptionOnMissingKey()} returns {@code true}.
     * @return the string for the given key or the key surrounded by {@code ???} if it cannot be
     * found and {@link I18NBundle#getExceptionOnMissingKey()} returns {@code false}.
     * */
    public final String get (BundleKey key) {
        return bundle.get(key.toString());
    }

    /**
     * Gets the string with the specified key from this bundle after replacing he given arguments if
     * they occur.
     *
     * @param key the key for the desired string
     * @param args the arguments to be replaced in the string associated to the given key.
     * @exception NullPointerException if <code>key</code> is <code>null</code>
     * @exception MissingResourceException if no string for the given key can be found
     * @return the string for the given key formatted with the given arguments.
     * */
    public String format (BundleKey key, Object... args) {
        return bundle.format(bundle.get(key.toString()), args);
    }
}
