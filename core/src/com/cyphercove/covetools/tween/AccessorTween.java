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

import org.jetbrains.annotations.NotNull;

public class AccessorTween extends Tween<AccessorTween.Accessor, AccessorTween> {

    public interface Accessor {
        float getValue (int index);
        void setValue (int index, float newValue);
    }

    public AccessorTween (int vectorSize){
        super(vectorSize);
    }

    public int getVectorSize (){
        return vectorSize;
    }

    protected void begin () {
        for (int i = 0; i < getVectorSize(); i++) {
            setStartValue(i, target.getValue(i));
        }
    }

    protected void apply (int vectorIndex, float value) {
        target.setValue(vectorIndex, value);
    }

    @NotNull
    public AccessorTween end (int vectorIndex, float value){
        super.setEndValue(vectorIndex, value);
        return this;
    }

    public float getEndValue (int vectorIndex){
        return super.getEndValue(vectorIndex);
    }

}
