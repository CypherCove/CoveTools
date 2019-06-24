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

import com.badlogic.gdx.math.Vector3;

public class Vector3Tween extends Tween<Vector3, Vector3Tween> {

    public Vector3Tween (){
        super(2);
    }

    protected void begin () {
        setStartValue(0, target.x);
        setStartValue(1, target.y);
        setStartValue(2, target.z);
    }

    protected void apply (int vectorIndex, float value) {
        switch (vectorIndex){
            case 0:
                target.x = value;
                break;
            case 1:
                target.y = value;
                break;
            case 2:
                target.z = value;
                break;
        }
    }

    public Vector3Tween end (float endX, float endY, float endZ){
        setEndValue(0, endX);
        setEndValue(1, endY);
        setEndValue(2, endZ);
        return this;
    }

    public float getEndX (){
        return getEndValue(0);
    }

    public float getEndY (){
        return getEndValue(1);
    }

    public float getEndZ () {
        return getEndValue(2);
    }
}
