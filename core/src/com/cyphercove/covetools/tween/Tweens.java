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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.cyphercove.covetools.math.Ease;
import com.cyphercove.covetools.math.Scalar;

public class Tweens {
    static private final IntMap<Pool<AccessorTween>> accessorPools = new IntMap<>();

    static public <T extends Tween> T tween (Class<T> type) {
        Pool<T> pool = Pools.get(type);
        T tween = pool.obtain();
        tween.pool(pool);
        return tween;
    }

    static private Pool<AccessorTween> getAccessorPool (final int vectorSize){
        Pool<AccessorTween> pool = accessorPools.get(vectorSize);
        if (pool == null){
            pool = new Pool<AccessorTween>(100) {
                @Override
                protected AccessorTween newObject () {
                    return new AccessorTween(vectorSize);
                }
            };
            accessorPools.put(vectorSize, pool);
        }
        return pool;
    }

    static public AccessorTween accessorTween (int vectorSize){
        Pool<AccessorTween> pool = getAccessorPool(vectorSize);
        AccessorTween tween = pool.obtain();
        tween.pool(pool);
        return tween;
    }

    static public AccessorTween accessor (AccessorTween.Accessor target, int vectorSize, float duration, Ease ease){
        return accessorTween(vectorSize)
            .target(target)
            .duration(duration)
            .ease(ease);
    }

    static public ScalarTween to (Scalar target, float endX, float duration, Ease ease){
        return tween(ScalarTween.class)
            .target(target)
            .end(endX)
            .duration(duration)
            .ease(ease);
    }

    static public Vector2Tween to (Vector2 target, float endX, float endY, float duration, Ease ease){
        return tween(Vector2Tween.class)
            .target(target)
            .end(endX, endY)
            .duration(duration)
            .ease(ease);
    }

    static public Vector3Tween to (Vector3 target, float endX, float endY, float endZ, float duration, Ease ease){
        return tween(Vector3Tween.class)
            .target(target)
            .end(endX, endY, endZ)
            .duration(duration)
            .ease(ease);
    }

    static public ColorTween to (Color target, float endR, float endG, float endB, float duration, Ease ease){
            return tween(ColorTween.class)
            .target(target)
            .end(endR, endG, endB)
            .duration(duration)
            .ease(ease);
    }

    static public AccessorTween to (AlphaAccessor target, float endA, float duration, Ease ease){
        AccessorTween tween = accessor(target, 1, duration, ease);
        tween.end(0, endA);
        return tween;
    }
}
