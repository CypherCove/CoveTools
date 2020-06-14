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

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IdentityMap;
import com.badlogic.gdx.utils.ObjectMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An added tween interrupts any tween that is currently running on the same target.
 * <p>A tween that has a sequence chain under it is treated as a single tween on that target.</p>
 */
public class TweenManager {
    private IdentityMap<Object, Tween> tweens = new IdentityMap<>();
    private IdentityMap<Object, Tween> delayedTweens = new IdentityMap<>();
    private Array<Tween> tmpTweens = new Array<>();
    private IdentityMap<Object, TweenCompletionListener> tmpListeners = new IdentityMap<>();

    /** Adds a tween or tween chain to the manager. Only one tween or tween chain can be running on
     * the same target. If the tween is marked {@linkplain Tween#isShouldBlend()} and has a delay, it
     * will not interrupt any existing tween until its delay runs out but it will cancel any other
     * delayed tween in the same state. If {@linkplain Tween#isShouldBlend()} is false, it will immediately
     * interrupt any existing tween on the same target before its delay starts.
     *
     * @param tween The tween or member of a tween chain to start.
     */
    public void start (@NotNull Tween tween){
        tween = tween.head; // Any tween in a chain can be submitted but always want to start at the head.
        Object target = tween.getTarget();
        Tween interruptedTween = tweens.get(target);

        if (interruptedTween != null &&
                tween.isShouldBlend() &&
                !tween.isDelayComplete()){
            //defer submission to allow existing tween to continue running until delay runs out.
            Tween cancelledTween = delayedTweens.get(tween.target);
            if (cancelledTween != null)
                cancelledTween.free();
            delayedTweens.put(tween.target, tween);
            return;
        }

        Tween cancelledTween = delayedTweens.get(tween.target);
        if (cancelledTween != null)
            cancelledTween.free();
        delayedTweens.put(tween.target, tween);
        delayedTweens.remove(tween.target);

        TweenInterruptionListener interruptedTweenListener = null;
        if (interruptedTween != null){
            if (tween.isShouldBlend()){
                tween.interrupt(interruptedTween);
            }
            interruptedTweenListener = interruptedTween.getInterruptionListener();
            tweens.remove(target);
            interruptedTween.free();
        }

        tweens.put(target, tween);
        if (interruptedTweenListener != null){
            interruptedTweenListener.onTweenInterrupted(target);
        }
    }

    /**
     * Removes a tween or tween chain from the tween manager. No listener will be called.
     * @param target The target object whose tween or tween chain is to be removed.
     * @return Whether a tween or tween chain existed and was removed.
     */
    public boolean clearTween (@Nullable Object target){
        boolean removed = false;
        if (delayedTweens.containsKey(target)){
            Tween tween = delayedTweens.get(target);
            delayedTweens.remove(target);
            tween.free();
            removed = true;
        }
        if (tweens.containsKey(target)){
            Tween tween = tweens.get(target);
            tweens.remove(target);
            tween.free();
            removed = true;
        }
        return removed;
    }

    /** Must be called for every frame of animation to advance all of the tweens.
     * @param delta The time passed since the last step.*/
    public void step (float delta){
        for (Tween tween : delayedTweens.values()){
            if(tween.stepDelay(delta)) {
                delayedTweens.remove(tween.target);
                tmpTweens.add(tween);
            }
        }
        for (Tween tween : tmpTweens){
            start(tween);
        }
        tmpTweens.clear();

        for (Tween tween : tweens.values()){
            if (tween.step(delta)){
                if (tween.getCompletionListener() != null){
                    tmpListeners.put(tween.target, tween.getCompletionListener());
                }
                tmpTweens.add(tween);
            }
        }

        for (Tween tween : tmpTweens){
            tweens.remove(tween.target);
            tween.free();
        }
        tmpTweens.clear();

        for (ObjectMap.Entry<Object, TweenCompletionListener> entry : tmpListeners){
            entry.value.onTweenComplete(entry.key);
        }
        tmpListeners.clear();
    }
}
