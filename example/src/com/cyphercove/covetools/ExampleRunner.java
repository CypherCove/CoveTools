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
package com.cyphercove.covetools;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.cyphercove.covetools.examples.AssignmentAssetManagerTest;
import com.cyphercove.covetools.examples.TweenTest;

public class ExampleRunner extends Game {

    public static final Array<Class<? extends Example>> examples = new Array<Class<? extends Example>>();
    static {
    	examples.add(AssignmentAssetManagerTest.class);
    	examples.add(TweenTest.class);
    }

    SpriteBatch spriteBatch;
    Stage stage;
    Skin skin;
    SelectBox<Class<? extends Example>> selectBox;
    String[] arg;

    public ExampleRunner (String[] arg){
        this.arg = arg;
    }

    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        Table table = new Table(skin);
        table.setFillParent(true);
        selectBox = new SelectBox<Class<? extends Example>>(skin){
            protected String toString (Class<? extends Example> obj) {
                return obj.getSimpleName();
            }
        };
        selectBox.setItems(examples);
        selectBox.addListener(new InputListener(){
            @Override
            public boolean scrolled (InputEvent event, float x, float y, int amount) {
                selectBox.setSelectedIndex((selectBox.getSelectedIndex() + Integer.signum(amount) + examples.size) % examples.size);
                return true;
            }
        });
        table.add(selectBox).fillX().space(20).row();
        stage.addActor(table);
        stage.setScrollFocus(selectBox);
        TextButton startButton = new TextButton("Start example", skin);
        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                runSelectedExample();
            }
        });
        table.add(startButton).fillX();

        if (arg != null && arg.length > 0) {
            Class<? extends Example> specifiedExample = getExample(arg[0]);
            if (specifiedExample != null){
                startExample(specifiedExample);
                return;
            }
        }

        setScreen(menuScreen);
    }

    @Override
    public void render (){
        if (getScreen() instanceof Example && Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE))
            exitCurrentExample();
        super.render();
    }

    private Class<? extends Example> getExample (String simpleName){
        for (Class<? extends Example> example : examples){
            if (example.getSimpleName().equals(simpleName))
                return example;
        }
        return null;
    }

    private void runSelectedExample (){
        Class<? extends Example> exampleClass = selectBox.getSelected();
        if (exampleClass == null)
            return;
        startExample(exampleClass);
    }

    private void startExample (Class<? extends Example> exampleClass){
        try {
            Example example = exampleClass.newInstance();
            setScreen(example);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void exitCurrentExample (){
        Screen exitedScreen = getScreen();
        setScreen(menuScreen);
        if (exitedScreen != null)
            exitedScreen.dispose();
        selectBox.setSelected(((Example)exitedScreen).getClass());
    }

    @Override
    public void dispose (){
        Screen exitedScreen = getScreen();
        if (exitedScreen != null)
            exitedScreen.dispose();
        setScreen(null);
        spriteBatch.dispose();
        skin.dispose();
    }

    private Screen menuScreen = new Screen() {
        @Override
        public void show() {
            Gdx.input.setInputProcessor(stage);
        }

        @Override
        public void render(float delta) {
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            stage.act();
            stage.draw();
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER))
                runSelectedExample();
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP))
                selectBox.setSelectedIndex((selectBox.getSelectedIndex() + examples.size - 1) % examples.size);
            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN))
                selectBox.setSelectedIndex((selectBox.getSelectedIndex() + 1) % examples.size);
        }

        @Override
        public void resize(int width, int height) {
            stage.getViewport().update(width, height, true);
        }

        @Override
        public void pause() {

        }

        @Override
        public void resume() {

        }

        @Override
        public void hide() {

        }

        @Override
        public void dispose() {

        }
    };
}
