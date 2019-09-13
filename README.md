# CoveTools
This is a set of utilities I use with LibGDX.

## Project Dependency
The current version of CoveTools is available via JCenter. **Note the new group ID! CoveTools has moved as of the 1.2.0 version.** You can add it to your LibGDX project's base `build.gradle` under the `core` module's dependencies:

    compile "com.cyphercove.covetools:covetools:1.2.1
    
If you are using the live wallpaper features, you also need to add **covetools-android** to your Android module dependencies:

    compile "com.cyphercove.covetools:covetools-android:1.2.1"
    
CoveTools is compatible with LibGDX 1.9.10. CoveTools does not (yet) support GWT.

See [CHANGES.md](Changes.md) for the change log, which lists breaking changes and LibGDX version increases.

## AssignmentAssetManager

AssignmentAssetManager is an AssetManager that is designed to reduce typing and redundancy as much as possible. You set up a container class for your assets by making fields for each asset:

	private static class Stage1Assets {
	    @Asset("egg.png") public Texture eggTexture;
	    @Asset("tree.png") public Texture treeTexture;
	    @Asset("pack") public TextureAtlas atlas;
	    @Assets({"arial-15.fnt", "arial-32.fnt", "arial-32-pad.fnt"}) public BitmapFont[] fonts;
	}

Precede each asset to be loaded with the `@Asset` annotation, providing a file path for the value. Then you can load all of the assets of your container class by passing an instance of the container. You can use `@Assets` to specify an array.

    stage1Assets = new Stage1Assets();
    assignmentAssetManager.loadAssetFields(stage1Assets);
    
Once the asset manager is finished loading (with `finishLoading()` or enough calls to `update()`), all the fields of the container are automatically assigned references to the loaded assets. You can immediately start using them.

    batch.draw(stage1Assets.eggTexture, 0, 0);
    
You can unload all the assets of a container with

    assignmentAssetManager.unloadAssetFields(stage1Assets);
    
The assets that are unique to the container will be unloaded, and all fields will be marked null. If an asset is part of some other loaded container, it will not be unloaded. This allows you to make container for multiple stages of your game and safely load and unload stages without unnecessary reloading of anything that is shared.

You can also specify an AssetLoader parameter by field name in the annotation:

	private static class Stage1Assets {
        TextureParameter mipmapParams = new TextureParameter(){{
            genMipMaps = true;
        }};
    	
        @Asset(value = "egg.png", parameter = "mipmapParams") public Texture eggTexture;
	}
	
The parameter must be a field of the container class, or can be a static member of any class if a fully qualified name is given.

Finally, you can optionally implement the AssetContainer interface to specify a directory for all the assets, or to get a callback once they're loaded and assigned:

    private static class Stage1Assets implements AssetContainer {
	    @Asset("atlas.pack") public TextureAtlas atlas;
	    public TextureRegion eggTextureRegion;
        
		@Override
		public String getAssetPathPrefix() {
			return "stage1/";
		}
		
		@Override
		public void onAssetsLoaded() {
			eggTextureRegion = atlas.findRegion("egg");
		}
    }
    
## JsonFieldUpdater

This class lets you update field values (by reflection) by modifying a Json file and loading it at runtime. I use this to tweak values quickly without rebuilding the game over and over. I create an input listener that calls `jsonFieldUpdater.readFieldsToObjects(...)` in response to a key press. For example:

    public class MyGlobalParameters {
        public static float gravity = 25f;
    }
    
If the above were one of your classes, you could create a Json file like this:

    {
        MyGlobalParameters: {
            gravity : 9.8
        }
    }

And cause the new value(s) to be loaded at runtime by calling:

    jsonFieldUpdater.readFieldsToObjects(Gdx.files.internal("myParameters.json"), MyGlobalParameters.class)
    
## Disposal

The Disposal class provides an easy way to clean up a class of Disposable objects, so you can easily avoid accidental leaks. Just make sure all your Disposable objects are referenced by member variables. Use the `@Group(int)` and `@Skip` annotations for controlled disposal.
    
## Live Wallpapers

One of LibGDX's selling points is the ability to develop a game using desktop builds and then deploying to Android with minimal extra work. CoveTools provides some classes that help you do the same with live wallpapers.

The first step is to make your base application listener derive from LiveWallpaperListener instead of ApplicationListener. This gives you a few extra methods to fill out:

* Use `void render(float xOffset, float yOffset, float xOffsetLooping, float xOffsetFake);` instead of `render()`, and it will provide you with the xOffset provided by Android's launcher. On the desktop build, it will simulate the xOffset scrolling left and right when you drag in the window.

* Use `void onPreviewStateChange(boolean isPreview);` to determine if the wallpaper is in preview mode on Android.
    
* Use `onSettingsChanged()`to perform any necessary updates if the user has changed the settings on Android.

For your desktop build, wrap the listener in a DesktopLiveWallpaperWrapper when passing it to the LwjglApplication constructor:

    new LwjglApplication(new DesktopLiveWallpaperWrapper(new MyLiveWallpaperListener()), config);

For your Android build, wrap the listener in a LiveWallpaperWrapper before handing it to `initialize(...)`. The wrapper has some additional parameters to pass to the constructor. The application context is used to retrieve display parameters. The optional SharedPreferences will be listened to for changes. The optional WallpaperEventListener will allow you to respond to some live wallpaper events on the LibGDX (OpenGL) thread. I use this to initialize values from the Android settings right before the first render call, and to update those values when the settings change.

    initialize(new LiveWallpaperWrapper(new MyLiveWallpaperListener(), getApplicationContext(), 
        mySharedPreferences, myEventListener), config);

You can also easily use your wallpaper as a Daydream, aka screensaver by wrapping it in a DaydreamWrapper before passing it to `AndroidDaydream.initialize(...)` :

    initialize(new DaydreamWrapper(new MyLiveWallpaperListener(), myEventListener), config);
    
A daydream shouldn't be running while a user is changing settings, so there is no option to pass a SharedPreferences instance.

## GaussianBlur

This tool is for applying Gaussian blur post processing effects such as depth of field, bloom, or 2D light mapping. 

Instantiate one like any other Disposable object such as FrameBuffer or SpriteBatch. Make sure to dispose it in `dispose()`. Before you use it, you must call `resize()` on it at least once. Presumably you'll be calling this in your `resize()` method:

```
public void resize (int width, int height){
    //...
    
    // You can pass a value corresponding to the backing FrameBuffer's long dimension. The width and
    // height will be filled automatically with appropriate aspect ratio.
    gaussianBlur.resize(50, width, height);
}
```

Then in `render()`, you can draw the scene you want blurred between `begin()` and `end()` calls.

```
public void render (){
    //update game objects...
    
    gaussianBlur.begin();
    Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    
    // draw the scene that will be blurred
    
    gaussianBlur.end();
    
    //Draw the rest of the scene normally.
    Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    //...
    
    //Draw the gaussian blur over the scene
    gaussianBlur.setRadius(3f); // Can be anywhere between 0 and 8. The radius of the Gaussian.
    gaussianBlur.setBlending(true, GL20.GL_ONE, GL20.GL_ONE);
    gaussianBlur.render(); 
    
}
```

You can also use a custom shader. GaussianBlur internally uses a tiny single-quad-sized SpriteBatch 
for drawing, so it should be a shader that works with a SpriteBatch (same uniform names).

```
gaussianBlur.beginRender(myCustomShader);
myCustomShader.setUniformf(...); // Set any necessary uniforms here.
gaussianBlur.finishRender();
```

## Anisotropy

LibGDX doesn't natively provide convenience methods for setting Texture anisotropic filtering. Use the Anisotropy class like this:

    Anisotropy.setTextureAnisotropy(myTexture, 16);
    
The highest anisotropic filtering level available up to the value passed will be applied to the texture. The device might not support as high a value as was passed, or not support anisotropic filtering at all. The above method returns the actual value that was set.