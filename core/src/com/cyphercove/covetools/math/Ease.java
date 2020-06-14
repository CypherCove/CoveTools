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
package com.cyphercove.covetools.math;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.cyphercove.covetools.tween.Tween;

import org.jetbrains.annotations.NotNull;

/**
 * Easing functions that can be used with {@linkplain Tween Tweens}. Non-configurable eases are provided
 * as static immutable members. Configurable eases are provided via function calls. These configurable
 * eases are pulled from {@linkplain Pool Pools} and can automatically be returned to their pools by
 * calling{@link #free()}. Do not assign a single configurable ease to multiple Tweens because the
 * Tweens automatically free them to their pools when complete.
 */
abstract public class Ease {
    /**
     * @param a     The progress in the interpolation (for example, elapsed time over total duration).
     *              The value is clamped between 0 and 1.
     * @param start Where the value should be when {@code a} is 0
     * @param end   Where the value should be when {@code a} is 1
     * @return the interpolated value between the given start and end values
     */
    abstract public float apply (float a, float start, float end);

    /**
     * Provides the derivative of the ease function at a given amount of progress.
     *
     * @param a     The progress in the interpolation (for example, elapsed time over total duration).
     *              The value is clamped between 0 and 1.
     * @param start Where the eased value should be when {@code a} is 0
     * @param end   Where the eased value should be when {@code a} is 1
     * @return the speed of the easing at the current amount of progress {@code a} in units of
     * value change over fraction of total duration. Multiplying this value by the total duration of
     * the ease gives the world speed of the ease.
     */
    abstract public float speed (float a, float start, float end);

    /**
     * If the ease is poolable, this returns it to its pool. Call only when it will no longer be used.
     */
    public void free () {
    }

    /**
     * Eases that are blendable support setting the starting speed of the function so it can smoothly
     * blend from another Ease that it is interrupting.
     */
    public interface Blendable {
        /**
         * Set the start speed of the function, in units of value change over total duration. World
         * speed should be divided by total ease duration before passing it in.
         * @param startSpeed The beginning speed for the transition.
         * @return The Ease for building.
         */
        @NotNull Ease startSpeed (float startSpeed);

        /** @return the start speed of the function. */
        float getStartSpeed ();
    }

    /* Immutable static eases --------------------------------------------------------------------*/

    public static Ease linear = new Ease() {

        public float apply (float a, float start, float end) {
            if (a <= 0)
                return start;
            if (a >= 1)
                return end;
            return a * (end - start) + start;
        }

        public float speed (float a, float start, float end) {
            return end - start;
        }
    };

    /**
     * A cubic Hermite spline that starts and ends with zero speed.
     */
    public static Ease smoothstep = new Ease() {
        @Override
        public float apply (float a, float start, float end) {
            if (a <= 0)
                return start;
            if (a >= 1)
                return end;
            return a * a * (3 - 2 * a) * (end - start) + start;
        }

        @Override
        public float speed (float a, float start, float end) {
            if (a <= 0 || a >= 1)
                return 0;
            return a * (6 - 6 * a) * (end - start);
        }
    };

    /**
     * A quintic Hermite spline that starts and ends with zero speed and zero acceleration. By Ken Perlin.
     */
    public static Ease smootherstep = new Ease() {
        @Override
        public float apply (float a, float start, float end) {
            if (a <= 0)
                return start;
            if (a >= 1)
                return end;
            return a * a * a * (a * (a * 6 - 15) + 10) * (end - start) + start;
        }

        @Override
        public float speed (float a, float start, float end) {
            if (a <= 0 || a >= 1)
                return 0;
            return a * a * (a * (a * 30 - 60) + 30) * (end - start);
        }
    };

    /* Blendable eases --------------------------------------------------------------------------*/

    public static class CubicHermite extends Ease implements Blendable, Pool.Poolable {
        float startSpeed, endSpeed;

        public CubicHermite startSpeed (float startSpeed) {
            this.startSpeed = startSpeed;
            return this;
        }

        /**
         * Set the end speed of the function, in units of value change over total duration. World
         * speed should be divided by total ease duration before passing it in.
         * @param endSpeed The final speed for the transition.
         * @return The Ease for building.
         */
        public CubicHermite endSpeed (float endSpeed) {
            this.endSpeed = endSpeed;
            return this;
        }

        public float getStartSpeed () {
            return startSpeed;
        }

        public float getEndSpeed () {
            return endSpeed;
        }

        public void free () {
            Pools.free(this);
        }

        public void reset () {
            startSpeed = 0;
            endSpeed = 0;
        }

        public float apply (float a, float start, float end) {
            if (startSpeed == 0 && endSpeed == 0)
                return smoothstep.apply(a, start, end);
            if (a <= 0)
                return start;
            if (a >= 1)
                return end;

            float a2 = a * a;
            float a3 = a2 * a;
            return start * (2 * a3 - 3 * a2 + 1) +
                    startSpeed * (a3 - 2 * a2 + a) +
                    end * (-2 * a3 + 3 * a2) +
                    endSpeed * (a3 - a2);
        }

        public float speed (float a, float start, float end) {
            if (a <= 0)
                return startSpeed;
            if (a >= 1)
                return endSpeed;

            float a2 = a * a;
            return start * (6 * a2 - 6 * a) +
                    startSpeed * (3 * a2 - 4 * a + 1) +
                    end * (-6 * a2 + 6 * a) +
                    endSpeed * (3 * a2 - 2 * a);
        }
    }

    /**
     * A cubic Hermite function whose starting and ending speeds can be specified.  The
     * function is equivalent to smoothstep if the starting and ending speeds are 0. This ease can
     * be freed to a pool. Do not assign a single instance to multiple {@linkplain Tween Tweens}.
     *
     * @return A cubic hermite ease
     */
    public static CubicHermite cubic () {
        return Pools.obtain(CubicHermite.class);
    }

    public static class QuinticHermite extends CubicHermite {

        public QuinticHermite startSpeed (float startSpeed) {
            super.startSpeed(startSpeed);
            return this;
        }

        public QuinticHermite endSpeed (float startSpeed) {
            super.endSpeed(startSpeed);
            return this;
        }

        public float apply (float a, float start, float end) {
            if (startSpeed == 0 && endSpeed == 0)
                return smootherstep.apply(a, start, end);
            if (a <= 0)
                return start;
            if (a >= 1)
                return end;

            float a3 = a * a * a;
            float a4 = a3 * a;
            float a5 = a4 * a;
            return start * (-6 * a5 + 15 * a4 - 10 * a3 + 1) +
                    startSpeed * (-3 * a5 + 8 * a4 - 6 * a3 + a) +
                    end * (6 * a5 - 15 * a4 + 10 * a3) +
                    endSpeed * (-3 * a5 + 7 * a4 - 4 * a3) +
                    a4 - 0.5f * (a5 + a3);
        }

        public float speed (float a, float start, float end) {
            if (a <= 0)
                return startSpeed;
            if (a >= 1)
                return endSpeed;

            float a2 = a * a;
            float a3 = a2 * a;
            float a4 = a2 * a2;
            return 30 * (end - start) * (a4 - 2 * a3 + a2) +
                    startSpeed * (-15 * a4 + 32 * a3 - 18 * a2 + 1) +
                    endSpeed * (-15 * a4 + 28 * a3 - 12 * a2) +
                    2.5f * a4 + 4 * a3 - 1.5f * a2;
        }
    }

    /**
     * A quintic Hermite function whose starting and ending speeds can be specified. The starting and
     * ending acceleration will be zero. The function is equivalent to smootherstep if the starting
     * and ending speeds are 0. This ease can be freed to a pool. Do not assign a single instance to
     * multiple {@linkplain Tween Tweens}.
     *
     * @return A quintic hermite ease
     */
    public static QuinticHermite quintic () {
        return Pools.obtain(QuinticHermite.class);
    }

    public static class InterpolationWrapper extends Ease implements Pool.Poolable {
        Interpolation interpolation;
        float precision = 0.001f;
        float halfPrecision = 0.0005f;

        public void setInterpolation (Interpolation interpolation) {
            this.interpolation = interpolation;
        }

        public void free () {
            Pools.free(this);
        }

        public void reset () {
            interpolation = null;
            precision = 0.001f;
            halfPrecision = 0.0005f;
        }

        /**
         * Sets the precision for calculating this interpolation's speed. This is the fraction of total
         * duration used for calculating the speed.
         *
         * @param precision The new precision.
         */
        public void setSpeedPrecision (float precision) {
            this.precision = precision;
            halfPrecision = 0.5f * precision;
        }

        public float apply (float a, float start, float end) {
            return interpolation.apply(start, end, MathUtils.clamp(a, 0f, 1f));
        }

        @Override
        public float speed (float a, float start, float end) {
            if (a <= halfPrecision)
                return interpolation.apply(precision) * (end - start) / precision;

            if (a >= 1f - halfPrecision)
                return interpolation.apply(1f - precision) * (end - start) / precision;

            return (interpolation.apply(a + halfPrecision) - interpolation.apply(a - halfPrecision))
                    * (end - start) / precision;
        }
    }

    /**
     * A wrapper for {@linkplain Interpolation Interpolations} so they can be used as Eases. This ease can
     * be freed to a pool. Do not assign a single instance to multiple {@linkplain Tween Tweens}.
     *
     * @param interpolation The Interpolation to wrap.
     * @return An Ease that uses the function of an Interpolation.
     */
    public static InterpolationWrapper wrap (Interpolation interpolation) {
        InterpolationWrapper ease = Pools.obtain(InterpolationWrapper.class);
        ease.setInterpolation(interpolation);
        return ease;
    }

    /**
     * A wrapper for {@linkplain Interpolation Interpolations} so they can be used as Eases. This ease can
     * be freed to a pool. Do not assign a single instance to multiple {@linkplain Tween Tweens}.
     *
     * @param interpolation  The Interpolation to wrap.
     * @param speedPrecision The precision to use when calculating the speed of this ease. This is the
     *                       step size to use as a fraction of total duration.
     * @return An Ease that uses the function of an Interpolation.
     */
    public static InterpolationWrapper wrap (Interpolation interpolation, float speedPrecision) {
        InterpolationWrapper ease = Pools.obtain(InterpolationWrapper.class);
        ease.setInterpolation(interpolation);
        ease.setSpeedPrecision(speedPrecision);
        return ease;
    }
}
