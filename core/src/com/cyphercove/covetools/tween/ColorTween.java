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

import com.badlogic.gdx.graphics.Color;

import org.jetbrains.annotations.NotNull;

/** A tween for changing the R, G, and B components of a {@linkplain Color}. It does not modify the
 * alpha component.*/
public class ColorTween extends Tween<Color, ColorTween> {

    private float endR, endG, endB;

    public ColorTween (){
        super(2);
    }

    protected void begin () {
        //Set start and end values to take the shortest path around
        if (Math.abs(endR - target.r) > 0.5f){
            if (endR > target.r){
                setStartValue(0, target.r + 1f);
                setEndValue(0, endR);
            } else {
                setStartValue(0, target.r);
                setEndValue(0, endR + 1);
            }
        } else {
            setStartValue(0, target.r);
            setEndValue(0, endR);
        }

        if (Math.abs(endG - target.g) > 0.5f){
            if (endG > target.g){
                setStartValue(1, target.g + 1f);
                setEndValue(1, endG);
            } else {
                setStartValue(1, target.g);
                setEndValue(1, endG + 1);
            }
        } else {
            setStartValue(1, target.g);
            setEndValue(1, endG);
        }

        if (Math.abs(endB - target.b) > 0.5f){
            if (endB > target.b){
                setStartValue(2, target.b + 1f);
                setEndValue(2, endB);
            } else {
                setStartValue(2, target.b);
                setEndValue(2, endB + 1);
            }
        } else {
            setStartValue(2, target.b);
            setEndValue(2, endB);
        }
    }

    protected void apply (int vectorIndex, float value) {
        switch (vectorIndex){
            case 0:
                target.r = value % 1f;
                break;
            case 1:
                target.g = value % 1f;
                break;
            case 2:
                target.b = value % 1f;
                break;
        }
    }

    @NotNull
    public ColorTween end (float r, float g, float b){
        endR = r;
        endG = g;
        endB = b;
        return this;
    }

    public float getEndR (){
        return endR;
    }

    public float getEndG () {
        return endG;
    }

    public float getEndB () {
        return endB;
    }
}
