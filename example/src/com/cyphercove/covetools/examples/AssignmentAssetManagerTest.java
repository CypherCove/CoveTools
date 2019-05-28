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
package com.cyphercove.covetools.examples;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.cyphercove.covetools.Example;
import com.cyphercove.covetools.assets.AssignmentAssetManager;
import com.cyphercove.covetools.assets.Asset;
import com.cyphercove.covetools.assets.Assets;
import com.cyphercove.covetools.assets.AssetContainer;

public class AssignmentAssetManagerTest extends Example {

    AssignmentAssetManager assetManager;
    SpriteBatch batch;
    Viewport viewport;
    Stage stage;
    Label listLabel;
    PersistantAssets persistantAssets = new PersistantAssets();
    Stage1Assets stage1Assets = new Stage1Assets();
    Stage2Assets stage2Assets = new Stage2Assets();
    GameStage gameStage;
    
    static TextureParameter mipmapParams = new TextureParameter(){{
		genMipMaps = true;
	}};
    
    private enum GameStage {
    	Stage1, Stage2
    }

    private static class PersistantAssets {
        @Asset("uiskin.json") public Skin skin;
        @Asset("arial-32.fnt") public BitmapFont font;
    }

    private static class Stage1Assets {
        @Asset(value = "egg.png", parameter = "com.cyphercove.covetools.examples.AssignmentAssetManagerTest.mipmapParams") public Texture eggTexture;
        @Asset("tree.png") public Texture treeTexture;
        @Asset("pack") public TextureAtlas atlas;
        @Assets({"arial-15.fnt", "arial-32.fnt", "arial-32-pad.fnt"}) public BitmapFont[] fonts;
    }
	
	private static class Stage2Assets implements AssetContainer {
		@Asset("tree.png") public Texture treeTexture;
        @Asset("pack") public TextureAtlas atlas;
        @Asset("Jump.wav") public Sound jumpSound;
        
		@Override
		public String getAssetPathPrefix() {
			return null;
		}
		@Override
		public void onAssetsLoaded() {
			jumpSound.play();
		}
	}

    public AssignmentAssetManagerTest (){
        batch = new SpriteBatch();
        viewport = new ExtendViewport(640, 480);
        stage = new Stage(viewport, batch);

        assetManager = new AssignmentAssetManager();
        assetManager.loadAssetFields(persistantAssets);
        assetManager.loadAssetFields(stage1Assets);
        assetManager.finishLoading();
        
        gameStage = GameStage.Stage1;
        Table table = new Table();
        table.pad(20);
        table.setFillParent(true);
        final SelectBox<GameStage> selectBox = new SelectBox<GameStage>(persistantAssets.skin);
        selectBox.setItems(GameStage.values());
        selectBox.setSelected(gameStage);
        selectBox.addListener(new ChangeListener(){
			public void changed(ChangeEvent event, Actor actor) {
				switch (selectBox.getSelected()){
				case Stage1:
					assetManager.unloadAssetFields(stage2Assets);
					assetManager.loadAssetFields(stage1Assets);
					assetManager.finishLoading();
					break;
				case Stage2:
					assetManager.unloadAssetFields(stage1Assets);
					assetManager.loadAssetFields(stage2Assets);
					assetManager.finishLoading();
					break;
				}
				gameStage = selectBox.getSelected();
			}
        });
        table.add(selectBox).expandX().left().row();
        
        TextButton relistButton = new TextButton("Refresh loaded assets list", persistantAssets.skin);
        relistButton.addListener(new ChangeListener(){
        	public void changed(ChangeEvent event, Actor actor) {
				refreshList();
			}
        });
        table.add(relistButton).expandX().left().space(5).row();
        
        listLabel = new Label("", persistantAssets.skin);
        table.add(listLabel).expand().left().top().row();
        refreshList();
        
        stage.addActor(table);
        Gdx.input.setInputProcessor(stage);
    }
    
    private void refreshList (){
    	StringBuilder sb = new StringBuilder();
		Array<String> assetNames = assetManager.getAssetNames();
		for (String name : assetNames){
			sb.append(name);
			sb.append("\n");
		}
		listLabel.setText(sb.toString());
    }

    @Override
    public void render(float delta) {
        stage.act();
        
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        batch.setColor(1, 1, 1, 1);
        persistantAssets.font.draw(batch, gameStage + " Assets",
                viewport.getWorldWidth() - 250, viewport.getWorldHeight() - 20);
        switch (gameStage){
		case Stage1:
			batch.draw(stage1Assets.eggTexture, 20, 20);
			batch.draw(stage1Assets.treeTexture, 40 + stage1Assets.eggTexture.getWidth(), 20);
			break;
		case Stage2:
			batch.draw(stage2Assets.treeTexture, 20, 20);
			break;
        }
        batch.end();
        
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
    	batch.dispose();
    	assetManager.dispose();
    }
}
