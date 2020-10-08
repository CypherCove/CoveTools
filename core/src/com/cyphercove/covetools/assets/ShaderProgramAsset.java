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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for a field to be populated with a reference to the specified ShaderProgram asset. A
 * path for the asset must be provided. Other parameters match those of
 * {@link com.badlogic.gdx.assets.loaders.ShaderProgramLoader.ShaderProgramParameter}, and take the
 * place of specifying the parameter object. {@link #prependAllCode()} can be additionally be used to
 * prepend code to both shader stages ahead of {@code prependVertexCode} and {@code prependFragmentCode}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ShaderProgramAsset {
    String value(); // the path
    String vertexFile() default "";
    String fragmentFile() default "";
    boolean logOnCompileFailure = true;
    String prependVertexCode() default "";
    String prependFragmentCode() default "";
    String prependAllCode() default "";
}
