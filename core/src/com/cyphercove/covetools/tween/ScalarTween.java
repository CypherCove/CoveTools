/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
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
package com.cyphercove.covetools.tween;

import com.cyphercove.covetools.math.Scalar;

public class ScalarTween extends Tween<Scalar, ScalarTween> {

    public ScalarTween (){
        super(1);
    }

    protected void begin () {
        setStartValue(0, target.x);
    }

    protected void apply (int vectorIndex, float value) {
        target.x = value;
    }

    public ScalarTween end (float end){
        setEndValue(0, end);
        return this;
    }

    public float getEnd (){
        return getEndValue(0);
    }

}
