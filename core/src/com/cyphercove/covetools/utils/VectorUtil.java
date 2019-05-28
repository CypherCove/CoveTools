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
package com.cyphercove.covetools.utils;

import static com.badlogic.gdx.math.MathUtils.*;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class VectorUtil {
    private static final Vector3 tmp1 = new Vector3();
    private static final Vector3 tmp2 = new Vector3();

    public static Vector3 getPlaneIntersection(Vector3 vec, Vector3 planeNormal, Vector3 p2, Vector3 p0) {
        float t = planeNormal.dot(tmp1.set(p2).sub(p0)) / planeNormal.dot(vec);
        return tmp1.set(p0).add(tmp2.set(vec).scl(t));
    }

    public static Vector3 rotateX(Vector3 vec, float angRad) {
        float sin = sin(angRad);
        float cos = cos(angRad);
        return vec.set(vec.x, vec.y * cos - vec.z * sin, vec.z * cos + vec.y * sin);
    }

    public static Vector3 rotateY(Vector3 vec, float angRad) {
        float sin = sin(angRad);
        float cos = cos(angRad);
        return vec.set(vec.z * sin + vec.x * cos, vec.y, vec.z * cos - vec.x * sin);
    }

    public static Vector3 rotateZ(Vector3 vec, float angRad) {
        float sin = sin(angRad);
        float cos = cos(angRad);
        return vec.set(vec.x * cos - vec.y * sin, vec.x * sin + vec.y * cos, vec.z);
    }

    public static Vector3 rotate(Vector3 vec, Vector3 axis, float angRad) {
        float sin = sin(angRad);
        float cos = cos(angRad);
        float com = (axis.x * vec.x + axis.y * vec.y + axis.z * vec.z) * (1.0F - cos);
        float x0 = axis.x * com + vec.x * cos + (-axis.z * vec.y + axis.y * vec.z) * sin;
        float y0 = axis.y * com + vec.y * cos + (axis.z * vec.x - axis.x * vec.z) * sin;
        float z0 = axis.z * com + vec.z * cos + (-axis.y * vec.x + axis.x * vec.y) * sin;
        vec.x = x0;
        vec.y = y0;
        vec.z = z0;
        return vec;
    }

    public static Vector3 rotateDeg(Vector3 vec, Vector3 axis, float angDeg) {
        float sin = sinDeg(angDeg);
        float cos = cosDeg(angDeg);
        float com = (axis.x * vec.x + axis.y * vec.y + axis.z * vec.z) * (1.0F - cos);
        float x0 = axis.x * com + vec.x * cos + (-axis.z * vec.y + axis.y * vec.z) * sin;
        float y0 = axis.y * com + vec.y * cos + (axis.z * vec.x - axis.x * vec.z) * sin;
        float z0 = axis.z * com + vec.z * cos + (-axis.y * vec.x + axis.x * vec.y) * sin;
        vec.x = x0;
        vec.y = y0;
        vec.z = z0;
        return vec;
    }

    public static Vector3 rotate(Vector3 vec, Vector3 axis, float sin, float cos) {
        float x0, y0, z0;
        if(axis.z == 1.0F) {
            x0 = vec.x * cos - vec.y * sin;
            y0 = vec.x * sin + vec.y * cos;
            z0 = vec.z;
        } else {
            float f = (axis.x * vec.x + axis.y * vec.y + axis.z * vec.z) * (1.0F - cos);
            x0 = axis.x * f + vec.x * cos + (-axis.z * vec.y + axis.y * vec.z) * sin;
            y0 = axis.y * f + vec.y * cos + (axis.z * vec.x - axis.x * vec.z) * sin;
            z0 = axis.z * f + vec.z * cos + (-axis.y * vec.x + axis.x * vec.y) * sin;
        }

        vec.x = x0;
        vec.y = y0;
        vec.z = z0;
        return vec;
    }

    public static float getAngleRad(Vector3 v1, Vector3 v2) {
        return (float)Math.acos((double)v1.dot(v2));
    }

    public static float getAngleDeg(Vector3 v1, Vector3 v2) {
        return (float)Math.acos((double)v1.dot(v2)) * 57.295776F;
    }

    public static Vector2 averageInto(Vector2 first, Vector2 second) {
        return first.set((first.x + second.x) / 2.0F, (first.y + second.y) / 2.0F);
    }

    public static Vector3 averageInto(Vector3 first, Vector3 second) {
        return first.set((first.x + second.x) / 2.0F, (first.y + second.y) / 2.0F, (first.z + second.z) / 2.0F);
    }
}
