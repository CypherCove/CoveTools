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

import com.badlogic.gdx.math.Vector2;

public class Vector2Tween extends Tween<Vector2, Vector2Tween> {

    public Vector2Tween (){
        super(2);
    }

    protected void begin () {
        setStartValue(0, target.x);
        setStartValue(1, target.y);
    }

    protected void apply (int vectorIndex, float value) {
        switch (vectorIndex){
            case 0:
                target.x = value;
                break;
            case 1:
                target.y = value;
                break;
        }
    }

    public Vector2Tween end (float endX, float endY){
        setEndValue(0, endX);
        setEndValue(1, endY);
        return this;
    }

    public float getEndX (){
        return getEndValue(0);
    }

    public float getEndY (){
        return getEndValue(1);
    }
}
