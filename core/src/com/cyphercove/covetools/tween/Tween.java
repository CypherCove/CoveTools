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

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;
import com.cyphercove.covetools.math.Ease;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Base class for tweens that can smoothly interrupt each other with TweenManager.
 * <p>
 * Derived from the {@linkplain com.badlogic.gdx.scenes.scene2d.actions.TemporalAction TemporalAction} class.
 * </p>
 * @param <T> The type of target the tween operates on.
 * @param <U> The type of this tween
 */
public abstract class Tween<T, U extends Tween> implements Pool.Poolable {
    private float delay, delayTime, duration, time;
    private Ease ease;
    /** Whether the TweenManager should set this tween's speed to the current speed of any tween it
     * interrupts, provided the Ease is Blendable. */
    private boolean shouldBlend = true;
    /** If true, the ease's start speeds should be overwritten with the values in the startSpeeds array as the ease is used. */
    private boolean isBlended;
    /** The start speed that was on the interpolation before blending. */
    private float originalStartSpeed;
    /** The start speeds in /s units. Only filled if isBlended.*/
    private final float[] startWorldSpeeds;
    /** The start speeds used in /duration units. Only filled if isBlended.*/
    private final float[] startSpeeds;
    /** The start values, copied from the target object when the interpolation begins. Subclasses fill this in the begin() method. */
    private final float[] startValues;
    /** The end values, filled in indirectly by the user before the tween is started. */
    private final float[] endValues;
    protected final int vectorSize;
    private boolean began, complete;
    protected T target;
    protected TweenCompletionListener<T> completionListener;
    protected TweenInterruptionListener<T> interruptionListener;
    private Pool pool;
    private U next;
    protected U head;
    protected int loops = 1, loopCount;

    /**
     * @param vectorSize The number of elements in the vector being modified, or 1 for a scalar.
     */
    protected Tween (int vectorSize) {
        this.vectorSize = vectorSize;
        startSpeeds = new float[vectorSize];
        startWorldSpeeds = new float[vectorSize];
        startValues = new float [vectorSize];
        endValues = new float[vectorSize];
        resetHead();
    }

    /**
     * Submits the tween to the tween manager to start it. Only one tween or tween chain can be running on
     * the same target. If the tween is marked {@linkplain #isShouldBlend()} and has a delay, it
     * will not interrupt any existing tween until its delay runs out but it will cancel any other
     * delayed tween in the same state. If {@linkplain #isShouldBlend()} is false, it will immediately
     * interrupt any existing tween on the same target before its delay starts.
     * <p>Any member of a tween chain can be submitted to the manager, but it will always start at
     * the head of the chain.</p>
     *
     * @param tweenManager The TweenManager to run the transition.
     */
    public void start (@NotNull TweenManager tweenManager){
        tweenManager.start(this);
    }

    private void resetHead (){
        try {
            head = (U) this;
        } catch (ClassCastException e){
            throw new GdxRuntimeException("The U type of the Tween class must be its own class.");
        }
    }

    /**
     * Steps forward on the delay only. Used to run out the delay on a deferred tween.
     * @param delta Time passed
     * @return If delay is complete.
     */
    boolean stepDelay (float delta){
        delayTime += delta;
        if (delayTime >= delay){
            time = delayTime - delay - delta; // seed leftover time minus the delta since it will be added back on first step
            return true;
        }
        return false;
    }

    /**
     * @return Whether delay time has run out. May not have {@link #began} the transition yet.
     */
    boolean isDelayComplete (){
        return delayTime >= delay;
    }

    boolean step (float delta) {
        if (complete) {
            if (next != null)
                return next.step(delta);
            return true;
        }
        if (delayTime < delay) {
            delayTime += delta;
            if (delayTime < delay)
                return false;
            delta = delayTime - delay; // can use remainder of time to begin first frame
        }
        if (!began) {
            if (head == this && loops >= 0){
                loopCount++;
                if (loopCount > loops)
                    return true;
                if (isBlended && loopCount == 2){
                    isBlended = false; // no longer want to use the start speeds after first loop complete
                    ((Ease.Blendable)ease).startSpeed(originalStartSpeed);
                }
            }
            for (int i = 0; i < vectorSize; i++) {
                float worldSpeed = startWorldSpeeds[i];
                if (duration != 0)
                    startSpeeds[i] = worldSpeed;
            }
            begin();
            began = true;
        }
        time += delta;
        complete = time >= duration;
        if (complete) {
            for (int i = 0; i < vectorSize; i++) {
                apply(i, endValues[i]);
            }
        } else if (isBlended) {
            float alpha = time / duration;
            Ease.Blendable blendable = (Ease.Blendable)ease;
            for (int i = 0; i < vectorSize; i++) {
                blendable.startSpeed(startSpeeds[i]);
                apply(i, ease.apply(alpha, startValues[i], endValues[i]));
            }
        } else if (ease != null){
            float alpha = time / duration;
            for (int i = 0; i < vectorSize; i++) {
                apply(i, ease.apply(alpha, startValues[i], endValues[i]));
            }
        } else {
            float alpha = time / duration;
            for (int i = 0; i < vectorSize; i++) {
                apply(i, MathUtils.lerp(startValues[i], endValues[i], alpha));
            }
        }
        if (complete) {
            if (next != null) {
                if (next.isComplete()) //In case this is in a repeat loop
                    next.restart();
                return next.step(time - duration); // Take the first step using remaining dt
            }
            return true;
        }
        return false;

    }

    /** Called the first time {@link #step(float)} is called. This is a good place to query the {@link #target target's} starting
     * state. */
    protected void begin () {
    }

    /** Called each frame. The new value is given and this method is responsible for applying it to the target.
     * @param vectorIndex The element of the target to apply
     * @param value The value to apply.
     */
    abstract protected void apply (int vectorIndex, float value);

    /** Skips to the end of the transition. If there is a chain, it skips to the end of the chain.*/
    public void finish () {
        time = duration;
        if (next != null && next != head)
            next.finish();
    }

    /** Prepares the tween for running again from the beginning. */
    public void restart (){
        time = 0;
        delayTime = 0;
        began = false;
        complete = false;
    }

    /** Restores the tween to its default state so it can be returned to a Pool. */
    public void reset () {
        restart();
        ease = null;
        shouldBlend = true;
        isBlended = false;
        duration = 0;
        delay = 0;
        pool = null;
        next = null;
        loops = 1;
        loopCount = 0;
        resetHead();
        Arrays.fill(endValues, 0);
    }

    /** @return The target object to be transitioned. */
    public T getTarget () {
        return target;
    }

    /**
     * Sets the target object that will be transitioned by the tween. The {@linkplain TweenManager}
     * only allows a single tween or tween chain per target object. Adding a new tween to the manager
     * with the same target will interrupt any ongoing tween with the same target.
     * @param target The target object to be transitioned.
     * @return This tween for building.
     */
    @NotNull
    public U target (@NotNull T target) {
        this.target = target;
        if (next != null && next != head)
            next.target(target);
        return (U)this;
    }

    /** @return The pool this tween will automatically be returned to when the tween completes or is
     * interrupted, or null if none has been set. */
    @Nullable
    public Pool getPool () {
        return pool;
    }

    /** Sets a pool that this tween will automatically be returned to when the tween or tween chain
     * is completed or interrupted.
     * @param pool The pool to return this tween to, or null to clear it.
     * @return This tween for building.
     */
    @NotNull
    public U pool (@Nullable Pool pool) {
        this.pool = pool;
        return (U)this;
    }

    /** @return Whether this tween transition is complete. There may be later tweens in the chain
     * that are incomplete, and this tween may be restarted later if in a loop.
     */
    public boolean isComplete (){
        return complete;
    }

    /** @return Whether this tween is part of a loop. */
    public boolean isInLoop (){
        return head.loops > -1;
    }

    /** @return The next tween in the chain, or null if none is set. */
    @Nullable
    public U getNextInChain (){
        return next;
    }

    /** Sets a tween to be chained after this one. If there is a completion listener or interruption
     * listener, they will be shared by the entire chain. The completion listener is only called when
     * the entire chain is complete.
     *
     * @param next Next tween to run in this sequential chain.
     * @return The next tween that was passed in.
     */
    public U next (U next){
        this.next = next;
        passChainParametersDown();
        return next;
    }

    protected void passChainParametersDown (){
        if (next != null && next != head){
            next.head = head;
            next.target = target;
            next.completionListener = completionListener;
            next.interruptionListener = interruptionListener;
            next.passChainParametersDown();
        }
    }

    /** Sets the head of the chain as the next in sequence, so it forms a repeating loop. Adding any
     * additional tweens to the chain will cancel this loop. This loop will continue indefinitely.
     * @return The head tween of the loop*/
    @NotNull
    public U loop (){
        next = head;
        head.loops = -1;
        return head;
    }

    /** Sets the head of the chain as the next in sequence, so it forms a repeating loop. Adding any
     * additional tweens to the chain will cancel this loop.
     * @param times The number of times the loop should be performed. If there is a completion
     * listener, it will be called when the final loop is completed.
     * @return The head tween of the loop. */
    @NotNull
    public U loop (int times){
        next = head;
        head.loops = times;
        return head;
    }

    /** Return this tween and its chained children to their pools if they have them. Drops all
     * external references. Frees any configurable eases.*/
    public void free (){
        if (next != null && next != head)
            next.free();
        if (ease != null)
            ease.free();
        completionListener = null;
        interruptionListener = null;
        if (pool != null)
            pool.free(this);
    }

    public boolean hasBegun(){
        return began;
    }

    @Nullable
    public TweenCompletionListener<T> getCompletionListener () {
        return completionListener;
    }

    @Nullable
    public TweenInterruptionListener<T> getInterruptionListener () {
        return interruptionListener;
    }

    /**
     * Sets a listener to be called when the tween or tween chain is completed.
     * @param listener The listener to call when the tween completes. It is a functional interface
     *                 and the sole parameter is the target object of the tween.
     * @return This tween for building.
     */
    @NotNull
    public U onComplete (@Nullable TweenCompletionListener<T> listener) {
        this.completionListener = listener;
        if (next != null && next != head)
            next.onComplete(listener);
        return (U)this;
    }

    /**
     * Sets a listener to be called when the tween or tween chain is interrupted by another tween.
     * @param listener The listener to call when the tween is interrupted. It is a functional interface
     *                 and the sole parameter is the target object of the tween.
     * @return This tween for building.
     */
    public U onInterrupted (@Nullable TweenInterruptionListener<T> listener){
        this.interruptionListener = listener;
        if (next != null && next != head)
            next.onInterrupted(listener);
        return (U)this;
    }

    /** @return The transition time so far. Does not account for later tweens in the chain.*/
    public float getTime () {
        return time;
    }

    /** @return The length of the tween. */
    public float getDuration () {
        return duration;
    }

    /** Sets the length of the transition in seconds.
     * @param duration How long the translation will take.
     * @return This tween for building.*/
    @NotNull
    public U duration (float duration) {
        this.duration = duration;
        return (U)this;
    }

    public float getDelay () {
        return delay;
    }

    /**
     * Sets amount of time to wait before beginning the tween.
     * @param delay Wait time.
     * @return This tween for building.
     */
    @NotNull
    public U delay (float delay) {
        this.delay = delay;
        return (U)this;
    }

    @Nullable
    public Ease getEase () {
        return ease;
    }

    /**
     * Sets the ease function to use for the transition. If the Ease is a
     * {@link com.cyphercove.covetools.math.Ease.Blendable Blendable}, this tween interrupts
     * an ongoing tween, and {@linkplain #isShouldBlend()} is true (default true), then the tween
     * will begin at the speed of the tween it is interrupting, ignoring any start speed that is
     * set on the ease. If the tween loops, the original start speed of the ease will be used when
     * it repeats.
     * @param ease The ease function to use.
     * @return This tween for building.
     */
    @NotNull
    public U ease (@Nullable Ease ease) {
        this.ease = ease;
        return (U)this;
    }

    public boolean isShouldBlend () {
        return shouldBlend;
    }

    /** Sets whether the TweenManager should make this tween start at the same speed as any tween
     * that it is interrupting. This is only possible if the Ease is Blendable. If this is set true,
     * any starting speed set on the ease will be ignored if this tween interrupts another tween. If
     * this occurs but the tween loops, the original start speed will be used when it repeats. Also,
     * if this is set true and it has a delay, any currently runninng tween on the same target will
     * not be interrupted until the delay runs out, regardless of the type of ease.
     * @param shouldBlend Whether this TweenManager should attempt to blend the speeds from any
     *                    interrupted tween.
     * @return This tween for building.
     */
    @NotNull
    public U shouldBlend (boolean shouldBlend) {
        this.shouldBlend = shouldBlend;
        return (U)this;
    }

    /** Called by subclasses in {@link #begin()} to set up the starting values.
     * @param vectorIndex The index of the vector of values to set the start value of.
     * @param value The value to set the given element of the vector to.*/
    protected void setStartValue (int vectorIndex, float value){
        startValues[vectorIndex] = value;
    }

    /** Called by subclasses to set the end values. Protected so more intuitive methods can be exposed.
     * @param vectorIndex The index of the vector of values to set the end value of.
     * @param value The value to set the given element of the vector to.*/
    protected void setEndValue (int vectorIndex, float value){
        endValues[vectorIndex] = value;
    }

    /** Called by subclasses to get end values. Protected so more intuitive methods can be exposed.
     * @param vectorIndex The index of the vector of values to get the end value of.
     * @return The end value set for the given index.*/
    protected float getEndValue (int vectorIndex){
        return endValues[vectorIndex];
    }

    protected float getWorldSpeed (int vectorIndex){
        if (complete){
            if (next != null && next != head)
                return next.getWorldSpeed(vectorIndex);
            return 0;
        }
        if (!began)
            return 0; // still in delay
        if (duration == 0) {
            if (isBlended)
                return startWorldSpeeds[vectorIndex];
            return 0;
        }
        if (isBlended)
            ((Ease.Blendable)ease).startSpeed(startSpeeds[vectorIndex]);
        float speed = ease.speed(time / duration, startValues[vectorIndex], endValues[vectorIndex]) * duration;
        return speed;
    }

    /** Called by TweenManger when interrupting another tween. If conditions are right for blending,
     * the blend will be set up.
     * @param tween The tween that is being interrupted.
     */
    void interrupt (@NotNull U tween){
        if (getDuration() == 0 || !shouldBlend || !(ease instanceof Ease.Blendable))
            return;
        isBlended = true;
        originalStartSpeed = ((Ease.Blendable) ease).getStartSpeed();
        for (int i = 0; i < vectorSize; i++) {
            float startingWorldSpeed = tween.getWorldSpeed(i);
            startWorldSpeeds[i] = startingWorldSpeed;
            startSpeeds[i] = startingWorldSpeed / getDuration();
        }
    }
}
