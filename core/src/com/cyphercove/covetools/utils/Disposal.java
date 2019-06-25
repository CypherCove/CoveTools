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

import java.lang.annotation.*;
import java.security.AccessControlException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.reflect.*;
import com.cyphercove.covetools.assets.Asset;

/**
 * Convenience methods for disposing and nulling fields for {@link Disposable} objects.
 * <p>
 * Do not use on fields referencing objects that came from an AssetManager. The AssetManager must handle disposal of
 * those objects.
 *
 * @author cypherdare
 */
public class Disposal {

    /** Disposes all {@link Disposable} objects referenced by fields in the specified object and sets those field
     * values to null. Beware using this on a class that is holding references to passed-in objects! Any
     * Disposables annotated with {@link Skip} or {@link Asset} are not disposed.
     * @param object The object containing references to Disposable objects.
     */
    public static void clear (Object object) {
        clearExcept(object);
    }

    /** Disposes all {@link Disposable} objects referenced by fields in the specified object and sets
     * those field values to null, except those specified. Any Disposables annotated with {@link Skip}
     * or {@link Asset} are not disposed.
     * @param object The object containing references to Disposable objects.
     * @param skippedGroup Fields annotated with {@link Group} with matching numbers will not be disposed.
     */
    public static void clearExcept (Object object, int... skippedGroup){
        Field[] fields = ClassReflection.getDeclaredFields(object.getClass());
        boolean checkGroups = skippedGroup != null && skippedGroup.length > 0;
        outer:
        for (Field field : fields){
            com.badlogic.gdx.utils.reflect.Annotation skipAnnotation = field.getDeclaredAnnotation(Skip.class);
            if (skipAnnotation != null)
                continue;
            com.badlogic.gdx.utils.reflect.Annotation assetAnnotation = field.getDeclaredAnnotation(Asset.class);
            if (assetAnnotation != null)
                continue;
            if (checkGroups) {
                com.badlogic.gdx.utils.reflect.Annotation groupAnnotation = field.getDeclaredAnnotation(Group.class);
                if (groupAnnotation != null) {
                    Group group = groupAnnotation.getAnnotation(Group.class);
                    for (int i : skippedGroup)
                        if (i == group.value())
                            continue outer;
                }
            }
            clearFieldIfDisposable(object, field);
        }
    }

    /** Disposes all {@link Disposable} objects referenced by fields in the specified object and sets those field
     * values to null, if they are tagged with a given group number. If any field is tagged both with
     * {@link Group} and either {@link Skip} or {@link Asset}, a GdxRuntimeException will be thrown.
     * @param object The object containing references to Disposable objects.
     * @param groupNumber The {@link Group} value(s) of fields that will be disposed.
     */
    public static void clear (Object object, int... groupNumber){
        if (groupNumber == null || groupNumber.length == 0)
            return;
        Field[] fields = ClassReflection.getDeclaredFields(object.getClass());
        outer:
        for (Field field : fields){
            com.badlogic.gdx.utils.reflect.Annotation groupAnnotation = field.getDeclaredAnnotation(Group.class);
            if (groupAnnotation == null)
                continue;
            com.badlogic.gdx.utils.reflect.Annotation skipAnnotation = field.getDeclaredAnnotation(Skip.class);
            com.badlogic.gdx.utils.reflect.Annotation assetAnnotation = field.getDeclaredAnnotation(Asset.class);
            if (skipAnnotation != null || assetAnnotation != null)
                throw new GdxRuntimeException("Cannot annotate a field as Group if it is also annotated with Skip or Asset.");
            int fieldGroup = groupAnnotation.getAnnotation(Group.class).value();
            for (int i : groupNumber){
                if (fieldGroup == i){
                    clearFieldIfDisposable(object, field);
                    continue outer;
                }
            }
        }
    }

    private static void clearFieldIfDisposable (Object object, Field field){
        makeAccessible(field);
        Object referenced = null;
        try {
            referenced = field.get(object);
        } catch (ReflectionException e) {
            if (field.getDeclaringClass().isAssignableFrom(Disposable.class)){
                throw new GdxRuntimeException("Failed to read Disposable field " + field.getName(), e);
            }
        }
        if (referenced instanceof Disposable){
            try {
                ((Disposable) referenced).dispose();
            } catch (GdxRuntimeException e){
                // Some classes such as Pixmap throw when disposed multiple times.
                Gdx.app.error("Disposal",
                        String.format("Field %s threw an exception when disposed. It may have been disposed already. Ignoring and continuing.",
                                field, e));
            }
            try {
                field.set(object, null);
            } catch (ReflectionException e) {
                throw new GdxRuntimeException("Failed to write null to Disposable field " + field.getName(), e);
            }
        }
    }

    private static void makeAccessible (Field field){
        if (!field.isAccessible()) {
            try {
                field.setAccessible(true);
            } catch (AccessControlException ex) {
                throw new GdxRuntimeException(String.format("Field %s cannot be made accessible", field.getName()));
            }
        }
    }

    /**
     * Annotation for a field to always skip disposing in all cases. Useful for marking references
     * to Disposable object references that were passed down to the constructor or in a setter.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Skip {}

    /**
     * Annotation for a field to assign it to a group for use with the {@link #clear(Object, int...)} method.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Group {
        int value();
    }
}
