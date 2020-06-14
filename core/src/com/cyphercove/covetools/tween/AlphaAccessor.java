package com.cyphercove.covetools.tween;

import com.badlogic.gdx.graphics.Color;

import org.jetbrains.annotations.NotNull;

public class AlphaAccessor implements AccessorTween.Accessor{
    private Color color;

    public AlphaAccessor (@NotNull Color color){
        this.color = color;
    }

    public float getValue (int index) {
        return color.a;
    }

    public void setValue (int index, float newValue) {
        color.a = newValue;
    }
}
