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

import java.lang.annotation.*;

/**
 * Annotation for a field to be populated with a reference to the specified asset. A path for the asset must be provided. An
 * AssetManagerParameters may be provided using {@link #parameter()}. Parameters are specified by field name, and should already be
 * populated before loading. The parameter field is assumed to be in the same class as the asset, unless a fully qualified name is given
 * for a static field.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Asset {
    String value (); // the path

    String parameter () default "";
}
