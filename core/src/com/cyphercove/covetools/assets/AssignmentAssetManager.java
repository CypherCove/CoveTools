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
package com.cyphercove.covetools.assets;

import java.lang.reflect.Array;
import java.security.AccessControlException;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.ShaderProgramLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;

/**
 * An AssetManager that can load and automatically populate annotated fields for assets in an asset container
 * class. Fields of a class can be annotated with {@link Asset} or {@link Assets} (for arrays of assets) to specify
 * their paths. They can be queued for loading by passing an instance of the containing class using
 * {@link AssignmentAssetManager#loadAssetFields(Object)}. When loading is complete, references to all the loaded
 * assets are automatically populated on the container class.
 * <p>
 * All annotated assets of a container class can be unloaded by calling {@link AssignmentAssetManager#unloadAssetFields(Object)}.
 * This unloads these assets and nulls their fields in the container class. If an asset is still referenced in another loaded
 * container, it will not be unloaded.
 *
 * @author cypherdare
 */
public class AssignmentAssetManager extends AssetManager {

    private final ObjectSet<Object> queuedContainers = new ObjectSet<Object>();
    private final ObjectSet<Object> loadedContainers = new ObjectSet<Object>();
    private final ObjectMap<Object, ObjectMap<Field, AssetDescriptor<?>>> containersFieldsToAssets = new ObjectMap<>();
    private final ObjectMap<Object, ObjectMap<Object[], AssetDescriptor<?>[]>> containersFieldsToAssetArrays = new ObjectMap<>();

    public AssignmentAssetManager() {
        super();
    }

    public AssignmentAssetManager(FileHandleResolver resolver, boolean defaultLoaders) {
        super(resolver, defaultLoaders);
    }

    public AssignmentAssetManager(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public synchronized boolean update() {
        boolean done = super.update();
        if (done) {
            // assign references to Asset fields of queuedContainers
            for (Object assetContainer : queuedContainers) {
                ObjectMap<Field, AssetDescriptor<?>> fieldsToAssets = containersFieldsToAssets.get(assetContainer);
                for (ObjectMap.Entry<Field, AssetDescriptor<?>> fieldEntry : fieldsToAssets) {
                    Field field = fieldEntry.key;
                    makeAccessible(field);
                    try {
                        field.set(assetContainer, get(fieldEntry.value));
                    } catch (ReflectionException e) {
                        throw new GdxRuntimeException("Failed to assign loaded asset " + field.getName(), e);
                    }
                }
                ObjectMap<Object[], AssetDescriptor<?>[]> fieldsToAssetArrays = containersFieldsToAssetArrays.get(assetContainer);
                for (ObjectMap.Entry<Object[], AssetDescriptor<?>[]> arrayEntry : fieldsToAssetArrays) {
                    Object[] destinationArray = arrayEntry.key;
                    AssetDescriptor<?>[] descriptors = arrayEntry.value;
                    for (int i = 0; i < descriptors.length; i++) {
                        destinationArray[i] = get(descriptors[i]);
                    }
                }

                if (assetContainer instanceof AssetContainer)
                    ((AssetContainer) assetContainer).onAssetsLoaded();
            }

            loadedContainers.addAll(queuedContainers);
            queuedContainers.clear();
        }
        return done;
    }

    /**
     * Queues the corresponding assets of the {@link Asset} and {@link Assets} annotated fields of
     * the specified container for loading. When loading is complete, the fields will automatically
     * reference the loaded assets.
     * <p>
     * Fields that are not null are skipped to avoid the possibility of loading duplicate assets or
     * failing to dispose of existing assets.
     *
     * @param assetContainer An object containing fields annotated with {@link Asset} and
     *                       {@link Assets}. May optionally implement {@link AssetContainer} for
     *                       further customization.
     */
    public synchronized void loadAssetFields(Object assetContainer) {
        if (assetContainer == null)
            throw new GdxRuntimeException("Asset container cannot be null");
        if (queuedContainers.contains(assetContainer) || loadedContainers.contains(assetContainer))
            return;
        Class<?> containerType = assetContainer.getClass();
        String pathPrepend = "";
        if (assetContainer instanceof AssetContainer) {
            pathPrepend = ((AssetContainer) assetContainer).getAssetPathPrefix();
        }
        Field[] fields = ClassReflection.getDeclaredFields(containerType);
        ObjectMap<Field, AssetDescriptor<?>> containerAssets = new ObjectMap<Field, AssetDescriptor<?>>();
        ObjectMap<Object[], AssetDescriptor<?>[]> containerAssetArrays = new ObjectMap<Object[], AssetDescriptor<?>[]>();
        for (Field field : fields) {
            com.badlogic.gdx.utils.reflect.Annotation assetAnnotation = field.getDeclaredAnnotation(Asset.class);
            com.badlogic.gdx.utils.reflect.Annotation shaderProgramAssetAnnotation = field.getDeclaredAnnotation(ShaderProgramAsset.class);
            com.badlogic.gdx.utils.reflect.Annotation textureAssetAnnotation = field.getDeclaredAnnotation(TextureAsset.class);
            com.badlogic.gdx.utils.reflect.Annotation assetsAnnotation = field.getDeclaredAnnotation(Assets.class);

            if (assetAnnotation != null ||
                    shaderProgramAssetAnnotation != null ||
                    textureAssetAnnotation != null ||
                    assetsAnnotation != null) {
                try {
                    makeAccessible(field);
                    if (field.get(assetContainer) != null)
                        continue;
                } catch (ReflectionException e) {
                    throw new GdxRuntimeException("Cannot retrieve value of field " + field);
                }
            }

            if (assetAnnotation != null ||
                    shaderProgramAssetAnnotation != null ||
                    textureAssetAnnotation != null) {
                Class<?> assetType = field.getType();
                String fileName;
                AssetLoaderParameters<?> parameter;
                if (assetAnnotation != null) {
                    Asset asset = assetAnnotation.getAnnotation(Asset.class);
                    fileName = pathPrepend + asset.value();
                    parameter = findParameter(assetContainer, fields, asset.parameter(), field.getName());
                } else if (shaderProgramAssetAnnotation != null) {
                    ShaderProgramAsset asset = shaderProgramAssetAnnotation.getAnnotation(ShaderProgramAsset.class);
                    fileName = pathPrepend + asset.value();
                    parameter = generateParameter(pathPrepend, asset);
                } else {
                    TextureAsset asset = textureAssetAnnotation.getAnnotation(TextureAsset.class);
                    fileName = pathPrepend + asset.value();
                    parameter = generateParameter(asset);
                }
                @SuppressWarnings({"rawtypes", "unchecked"})
                AssetDescriptor<?> assetDescriptor = new AssetDescriptor(fileName, assetType, parameter);
                load(assetDescriptor);
                containerAssets.put(field, assetDescriptor);
                continue;
            }

            if (assetsAnnotation != null) {
                Class<?> assetType = field.getType().getComponentType();
                if (assetType == null) {
                    throw new GdxRuntimeException(String.format("@Assets may only be used with an array, and %s is not an array.", field.getName()));
                }
                Assets assets = assetsAnnotation.getAnnotation(Assets.class);
                String[] fileNames = assets.value();
                if (pathPrepend != null) {
                    for (int i = 0; i < fileNames.length; i++)
                        fileNames[i] = pathPrepend + fileNames[i];
                }
                AssetDescriptor<?>[] assetDescriptors = new AssetDescriptor[fileNames.length];

                String[] parameters = assets.parameters();
                if (parameters.length > 0) {
                    if (parameters.length != fileNames.length)
                        throw new GdxRuntimeException(String.format("For asset array %s, number of parameters does not match number of file name values.", field.getName()));
                    for (int i = 0; i < assetDescriptors.length; i++) {
                        AssetLoaderParameters<?> parameter = findParameter(assetContainer, fields, parameters[i], field.getName());
                        @SuppressWarnings({"rawtypes", "unchecked"})
                        AssetDescriptor<?> assetDescriptor = new AssetDescriptor(fileNames[i], assetType, parameter);
                        assetDescriptors[i] = assetDescriptor;
                        load(assetDescriptors[i]);
                    }
                } else {
                    // No parameters array specified, check for single parameter. Use that or null for parameters
                    AssetLoaderParameters<?> parameter = findParameter(assetContainer, fields, assets.parameter(), field.getName());
                    for (int i = 0; i < assetDescriptors.length; i++) {
                        @SuppressWarnings({"rawtypes", "unchecked"})
                        AssetDescriptor<?> assetDescriptor = new AssetDescriptor(fileNames[i], assetType, parameter);
                        assetDescriptors[i] = assetDescriptor;
                        load(assetDescriptors[i]);
                    }
                }

                Object[] containerArray = (Object[]) Array.newInstance(assetType, fileNames.length);
                try { // instantiate the empty array for the assets
                    makeAccessible(field);
                    field.set(assetContainer, containerArray);
                } catch (ReflectionException e) {
                    throw new GdxRuntimeException("Failed to assign generated array for " + field.getName(), e);
                }
                containerAssetArrays.put(containerArray, assetDescriptors);
            }
        }
        containersFieldsToAssets.put(assetContainer, containerAssets);
        containersFieldsToAssetArrays.put(assetContainer, containerAssetArrays);
        queuedContainers.add(assetContainer);
    }

    /**
     * @return The AssetLoaderParameters matching the given field name, or null if the field name is "" or null.
     * @throws GdxRuntimeException if the field value is null, the named field does not reference an AssetLoaderParameters, or the field does not exist.
     */
    AssetLoaderParameters<?> findParameter(Object container, Field[] containerFields, String parameterFieldName, String annotatedFieldName) {
        if (parameterFieldName == null || parameterFieldName.equals(""))
            return null;

        Field parameterField;

        if (parameterFieldName.contains(".")) { // assume fully qualified, statically accessed
            int lastDotIndex = parameterFieldName.lastIndexOf(".");
            String className = parameterFieldName.substring(0, lastDotIndex);
            parameterFieldName = parameterFieldName.substring(lastDotIndex + 1);
            try {
                Class<?> parameterContainerClass = ClassReflection.forName(className);
                parameterField = ClassReflection.getDeclaredField(parameterContainerClass, parameterFieldName);
                makeAccessible(parameterField);
                AssetLoaderParameters<?> parameter = (AssetLoaderParameters<?>) parameterField.get(null);
                if (parameter == null) {
                    throw new GdxRuntimeException(String.format("The specified parameter %s for asset %s cannot be null.", parameterFieldName, annotatedFieldName));
                }
                return parameter;
            } catch (ReflectionException e) {
                throw new GdxRuntimeException(String.format("Cannot retrieve parameter field %s of fully qualified class %s.", parameterFieldName, className));
            }
        }

        for (Field field : containerFields) {
            if (field.getName().equals(parameterFieldName)) {
                makeAccessible(field);
                try {
                    AssetLoaderParameters<?> parameter = (AssetLoaderParameters<?>) field.get(container);
                    if (parameter == null) {
                        throw new GdxRuntimeException(String.format("The specified parameter %s for asset %s cannot be null.", parameterFieldName, annotatedFieldName));
                    }
                    return parameter;
                } catch (ReflectionException e) {
                    throw new GdxRuntimeException(String.format("Specified parameter %s for asset %s is not an AssetLoaderParameter.", parameterFieldName, annotatedFieldName), e);
                }
            }
        }
        throw new GdxRuntimeException(String.format("The specified parameter %s for asset %s does not exist.", parameterFieldName, annotatedFieldName));
    }

    private void makeAccessible(Field field) {
        if (!field.isAccessible()) {
            try {
                field.setAccessible(true);
            } catch (AccessControlException ex) {
                throw new GdxRuntimeException(String.format("Field %s cannot be made accessible", field.getName()));
            }
        }
    }

    /**
     * Unloads the corresponding assets of the {@link Asset} and {@link Assets} annotated fields of the specified container,
     * if they are not referenced by any other containers. Nulls out these fields in the asset container.
     * <p>
     * If this container was not already loaded or queued for loading, nothing will be unloaded.
     *
     * @param assetContainer An object containing asset references that was previously loaded with {@link #loadAssetFields(Object)}.
     */
    public void unloadAssetFields(Object assetContainer) {
        unloadAssetFields(assetContainer, null);
    }

    /**
     * Unloads the corresponding assets of the {@link Asset} and {@link Assets} annotated fields of the specified container,
     * if they are not referenced by any other containers. Nulls out these fields in the asset container.
     * <p>
     * If this container was not already loaded or queued for loading, nothing will be unloaded.
     *
     * @param assetContainer An object containing asset references that was previously loaded with {@link #loadAssetFields(Object)}.
     * @param assetType      Only assets of the corresponding type will be unloaded.
     */
    public void unloadAssetFields(Object assetContainer, Class<?> assetType) {
        boolean isQueuedOrLoaded = false;
        if (queuedContainers.contains(assetContainer)) {
            queuedContainers.remove(assetContainer);
            isQueuedOrLoaded = true;
        }
        if (loadedContainers.contains(assetContainer)) {
            loadedContainers.remove(assetContainer);
            isQueuedOrLoaded = true;
        }
        if (!isQueuedOrLoaded)
            return;

        ObjectMap<Field, AssetDescriptor<?>> assets = containersFieldsToAssets.get(assetContainer);
        ObjectMap<Object[], AssetDescriptor<?>[]> assetArrays = containersFieldsToAssetArrays.get(assetContainer);
        containersFieldsToAssets.remove(assetContainer);
        containersFieldsToAssetArrays.remove(assetContainer);

        // unload asset fields if not in any other loaded asset containers
        for (AssetDescriptor<?> asset : assets.values()) {
            if (!isReferenced(asset) && (assetType == null || assetType == asset.type))
                unload(asset.fileName);
        }
        for (AssetDescriptor<?>[] assetArray : assetArrays.values()) {
            for (AssetDescriptor<?> asset : assetArray) {
                if (!isReferenced(asset) && (assetType == null || assetType == asset.type))
                    unload(asset.fileName);
            }
        }

        // null field references of asset container
        for (Field field : assets.keys()) {
            if (assetType != null && assetType != field.getType())
                continue;
            try {
                field.set(assetContainer, null);
            } catch (ReflectionException e) {
                throw new GdxRuntimeException("Failed to clear field " + field.getName(), e);
            }
        }
        for (Object[] array : assetArrays.keys()) {
            Field[] fields = ClassReflection.getDeclaredFields(assetContainer.getClass());
            for (Field field : fields) {
                if (assetType != null && assetType != field.getType().getComponentType())
                    continue;
                makeAccessible(field);
                try {
                    if (field.get(assetContainer) == array) {
                        field.set(assetContainer, null);
                    }
                } catch (ReflectionException e) {
                    throw new GdxRuntimeException("Failed to clear field " + field.getName(), e);
                }
            }
        }
    }

    private boolean isReferenced(AssetDescriptor<?> asset) {
        // Checks equality of file names of the AssetDescriptor (same behavior as AssetManager)
        for (ObjectMap<Field, AssetDescriptor<?>> assets : containersFieldsToAssets.values()) {
            for (AssetDescriptor<?> assetDescriptor : assets.values())
                if (assetDescriptor.fileName.equals(asset.fileName))
                    return true;
        }

        for (ObjectMap<Object[], AssetDescriptor<?>[]> assetArrays : containersFieldsToAssetArrays.values()) {
            for (AssetDescriptor<?>[] assetArray : assetArrays.values()) {
                for (AssetDescriptor<?> assetDescriptor : assetArray)
                    if (assetDescriptor.fileName.equals(asset.fileName))
                        return true;
            }
        }

        return false;
    }

    private static ShaderProgramLoader.ShaderProgramParameter generateParameter(String pathPrepend, ShaderProgramAsset asset) {
        ShaderProgramLoader.ShaderProgramParameter parameter =
                new ShaderProgramLoader.ShaderProgramParameter();
        if (!asset.vertexFile().equals("")) {
            parameter.vertexFile = pathPrepend + asset.vertexFile();
        }
        if (!asset.fragmentFile().equals("")) {
            parameter.fragmentFile = pathPrepend + asset.fragmentFile();
        }
        parameter.logOnCompileFailure = asset.logOnCompileFailure;
        String prependVertexCode = asset.prependAllCode() + asset.prependVertexCode();
        if (!prependVertexCode.equals("")) {
            parameter.prependVertexCode = prependVertexCode;
        }
        String prependFragmentCode = asset.prependAllCode() + asset.prependFragmentCode();
        if (!prependFragmentCode.equals("")) {
            parameter.prependFragmentCode = prependFragmentCode;
        }
        return parameter;
    }

    private static TextureLoader.TextureParameter generateParameter(TextureAsset asset) {
        TextureLoader.TextureParameter parameter = new TextureLoader.TextureParameter();
        parameter.format = asset.format();
        parameter.minFilter = asset.filter().minFilter;
        parameter.magFilter = asset.filter().magFilter;
        parameter.genMipMaps = asset.filter().usesMipMaps;
        parameter.wrapU = parameter.wrapV = asset.wrap();
        return parameter;
    }

}
