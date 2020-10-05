/*******************************************************************************
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
package com.cyphercove.covetools.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.List;

/**
 * See {@link TextureAtlasCacher#cacheRegions(TextureAtlas, Object, boolean)}.
 *
 * @author cypherdare
 */
public final class TextureAtlasCacher {
    private static final String[] singularSuffixes = {"AtlasRegion", "TextureRegion", "Region"};
    private static final String[] pluralSuffixes = {"AtlasRegions", "TextureRegions", "Regions", "es", "s"};

    /**
     * Assigns the regions of the given TextureAtlas to the fields with matching names in the target
     * object. The fields must be one of the following types:
     * <ul>
     *     <li>{@code TextureRegion}</li>
     *     <li>{@code AtlasRegion}</li>
     *     <li>{@code TextureRegion[]}</li>
     *     <li>{@code AtlasRegion[]}</li>
     *     <li>{@code Array<>}</li>
     *     <li>{@code List<>}</li>
     * </ul>
     * Due to type erasure, <em>any</em> {@code Array<>} or {@code List<>} with a name matching a name in the
     * atlas will be used, so be sure the collections are of type {@code TextureRegion} or
     * {@code AtlasRegion}.
     * <p>
     * If the field is an array, Array, or List, it will contain all regions with that name, ordered
     * by their {@link AtlasRegion#index}. Any existing collection assigned to the field is overwritten,
     * not added to.
     * <p>
     * If the field name cannot be found in the atlas, variations of the field name are also attempted:
     * <ul>
     *     <li>For AtlasRegion and TextureRegion fields, if the field name ends in "AtlasRegion",
     *     "TextureRegion", or "Region", they will be tried without these suffixes.</li>
     *     <li>For array and collection fields, if the field name ends in "AtlasRegions",
     *     "TextureRegions", "Regions", "es", or "s" they will be tried without these suffixes.</li>
     * </ul>
     * An explicit name can be used by annotating the field with {@link RegionName @RegionName}.
     *
     * @param atlas      The TextureAtlas to retrieve regions from.
     * @param target     The target object whose fields will be assigned region references.
     * @param logUnfound If true, logs any regions for which no matching field was found.
     */
    public static void cacheRegions (TextureAtlas atlas, Object target, boolean logUnfound) {
        final Array<String> missingNames = new Array<>();
        final Class<?> targetType = target.getClass();
        final Field[] fields = ClassReflection.getDeclaredFields(targetType);
        for (Field field : fields) {
            final Class<?> type = field.getType();
            if (type.isArray() &&
                    !(type.getComponentType() == TextureRegion.class
                            || type.getComponentType() == AtlasRegion.class))
                continue;
            String name = field.getName();
            boolean checkSuffixes = true;
            com.badlogic.gdx.utils.reflect.Annotation nameAnnotation = field.getDeclaredAnnotation(RegionName.class);
            if (nameAnnotation != null) {
                name = nameAnnotation.getAnnotation(RegionName.class).value();
                checkSuffixes = false;
            }
            if (type.isAssignableFrom(AtlasRegion.class) && type != Object.class) {
                AtlasRegion region = atlas.findRegion(name);
                if (checkSuffixes) {
                    for (String suffix : singularSuffixes) {
                        if (region != null)
                            break;
                        if (name.endsWith(suffix))
                            region = atlas.findRegion(name.substring(0, name.length() - suffix.length()));
                    }
                }
                if (region == null) {
                    missingNames.add(name);
                } else {
                    try {
                        makeAccessible(field);
                        field.set(target, region);
                    } catch (ReflectionException e) {
                        throw new GdxRuntimeException("Failed to assign region for " + name, e);
                    }
                }
            } else if (type == Array.class || type == List.class || type.isArray()) {
                Array<AtlasRegion> regions = atlas.findRegions(name);
                if (checkSuffixes) {
                    for (String suffix : pluralSuffixes) {
                        if (!regions.isEmpty())
                            break;
                        if (name.endsWith(suffix))
                            regions = atlas.findRegions(name.substring(0, name.length() - suffix.length()));
                    }
                }
                if (regions.isEmpty()) {
                    missingNames.add(name);
                } else {
                    try {
                        makeAccessible(field);
                        if (type == Array.class) {
                            field.set(target, regions);
                        } else if (type == List.class) {
                            List<AtlasRegion> regionsList = new ArrayList<>();
                            for (AtlasRegion region : regions)
                                regionsList.add(region);
                            field.set(target, regionsList);
                        } else { // field is array
                            AtlasRegion[] atlasRegionArray = regions.toArray();
                            if (type.getComponentType() == AtlasRegion.class) {
                                field.set(target, atlasRegionArray);
                            } else if (type.getComponentType() == TextureRegion.class) {
                                TextureRegion[] textureRegionArray = new TextureRegion[atlasRegionArray.length];
                                System.arraycopy(atlasRegionArray, 0, textureRegionArray, 0, atlasRegionArray.length);
                                field.set(target, textureRegionArray);
                            }
                        }
                    } catch (ReflectionException e) {
                        throw new GdxRuntimeException("Failed to assign regions for " + name, e);
                    }
                }
            }
        }
        if (logUnfound && !missingNames.isEmpty()) {
            Gdx.app.log("TextureAtlasCacher",
                    "Could not find regions matching the name(s): " + missingNames.toString());
        }
    }

    private static void makeAccessible (Field field) {
        if (!field.isAccessible()) {
            try {
                field.setAccessible(true);
            } catch (AccessControlException ex) {
                throw new GdxRuntimeException(String.format("Field %s cannot be made accessible", field.getName()));
            }
        }
    }

}
