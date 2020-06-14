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
package com.cyphercove.covetools.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.loaders.ShaderProgramLoader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;

import org.jetbrains.annotations.NotNull;

public class ShaderProgramReloader {

    /**
     * Reloads all the ShaderPrograms of the asset container, but only if none of them have errors. To see logs of any
     * errors, the asset manager should have a logger set.
     * <p>
     * Error checking for correct usage of AssignmentAssetManager annotations is skipped, with the assumption that the
     * container has already been used for loading.
     *
     * @param assetManager The AssignmentAssetManager with shader programs to reload.
     * @param assetContainer The asset container whose shader programs are to be reloaded.
     * @return Whether ShaderPrograms were reloaded.
     */
    public static boolean reloadAllShadersGuarded (@NotNull AssignmentAssetManager assetManager, @NotNull Object assetContainer) {

        Class<?> containerType = assetContainer.getClass();
        Field[] fields = ClassReflection.getDeclaredFields(containerType);
        String pathPrepend = null;
        if (assetContainer instanceof AssetContainer) {
            pathPrepend = ((AssetContainer) assetContainer).getAssetPathPrefix();
            if (pathPrepend != null && pathPrepend.equals(""))
                pathPrepend = null;
        }

        boolean error = false;
        ShaderProgramLoader loader = (ShaderProgramLoader)assetManager.getLoader(ShaderProgram.class);

        // Load redundant copies of the shaders to check for errors, then unload. Abort on any error.
        outer:
        for (Field field : fields) {
            com.badlogic.gdx.utils.reflect.Annotation assetAnnotation = field.getDeclaredAnnotation(Asset.class);
            if (assetAnnotation != null && field.getType() == ShaderProgram.class) {
                Asset asset = assetAnnotation.getAnnotation(Asset.class);
                String fileName = asset.value();
                if (pathPrepend != null)
                    fileName = pathPrepend + fileName;
                ShaderProgramLoader.ShaderProgramParameter parameter =
                        (ShaderProgramLoader.ShaderProgramParameter)assetManager.findParameter(assetContainer, fields, asset.parameter(), field.getName());
                ShaderProgram newProgram = loader.loadSync(assetManager, fileName, assetManager.getFileHandleResolver().resolve(fileName), parameter);
                if (!newProgram.isCompiled()){
                    error = true;
                    break;
                }
                newProgram.dispose();
                continue;
            }
            com.badlogic.gdx.utils.reflect.Annotation assetsAnnotation = field.getDeclaredAnnotation(Assets.class);
            if (assetsAnnotation != null) {
                Class<?> assetType = field.getType().getComponentType();
                Assets assets = assetsAnnotation.getAnnotation(Assets.class);
                String[] fileNames = assets.value();
                if (pathPrepend != null) {
                    for (int i = 0; i < fileNames.length; i++)
                        fileNames[i] = pathPrepend + fileNames[i];
                }
                AssetDescriptor<?>[] assetDescriptors = new AssetDescriptor[fileNames.length];

                String[] parameters = assets.parameters();
                if (parameters != null && parameters.length > 0) {
                    for (int i = 0; i < assetDescriptors.length; i++) {
                        ShaderProgramLoader.ShaderProgramParameter parameter =
                                (ShaderProgramLoader.ShaderProgramParameter)assetManager.findParameter(assetContainer, fields, parameters[i], field.getName());
                        ShaderProgram newProgram = loader.loadSync(assetManager, fileNames[i], assetManager.getFileHandleResolver().resolve(fileNames[i]), parameter);
                        if (!newProgram.isCompiled()){
                            error = true;
                            break outer;
                        }
                        newProgram.dispose();
                    }
                } else {
                    // No parameters array specified, check for single parameter. Use that or null for parameters
                    ShaderProgramLoader.ShaderProgramParameter parameter =
                            (ShaderProgramLoader.ShaderProgramParameter)assetManager.findParameter(assetContainer, fields, assets.parameter(), field.getName());
                    for (int i = 0; i < assetDescriptors.length; i++) {
                        ShaderProgram newProgram = loader.loadSync(assetManager, fileNames[i], assetManager.getFileHandleResolver().resolve(fileNames[i]), parameter);
                        if (!newProgram.isCompiled()){
                            error = true;
                            break outer;
                        }
                        newProgram.dispose();
                    }
                }
                continue;
            }
        }

        if (error){
            Gdx.app.error("ShaderProgramReloader", "An error was encountered in one of the shaders. Shaders will not be reloaded.");
            return false;
        }

        assetManager.unloadAssetFields(assetContainer, ShaderProgram.class);
        assetManager.loadAssetFields(assetContainer);
        assetManager.finishLoading();
        return true;
    }
}
