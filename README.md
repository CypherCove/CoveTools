# CoveTools
This is a set of utilities I use with LibGDX.

![Maven Central](https://img.shields.io/maven-central/v/com.cyphercove.covetools/covetools?color=6d7ac5)
![libGDX](https://img.shields.io/badge/libgdx-1.1.1-e74a45?link=http%3A%2F%2Fwww.libgdx.com%2F)

## Project Dependency
As of 1.2.10, CoveTools is available on Maven Central.

You can add it to your LibGDX project's base `build.gradle` under the `core` module's dependencies:

    implementation "com.cyphercove.covetools:covetools:1.2.10"
    
If you are using the live wallpaper features, you also need to add **covetools-android** to your 
Android module dependencies:

    implementation "com.cyphercove.covetools:covetools-android:1.2.10"
    
To use with GWT, add this to the `.gwt.xml` file:

    <inherits name="com.cyphercove.covetools"/>

Your libGDX version should be at least as high as the one noted in the badge above.


See [CHANGES.md](CHANGES.md) for the change log. There may be breaking changes between releases.

## AssignmentAssetManager

AssignmentAssetManager is an AssetManager that is designed to reduce typing and redundancy as much 
as possible. You set up a container class for your assets by making fields for each asset:

```java
private static class Stage1Assets {
    @Asset("egg.png") public Texture eggTexture;
    @Asset("tree.png") public Texture treeTexture;
    @Asset("pack") public TextureAtlas atlas;
    @Assets({"arial-15.fnt", "arial-32.fnt", "arial-32-pad.fnt"}) public BitmapFont[] fonts;
}
```

Precede each asset to be loaded with the `@Asset` annotation, providing a file path for the value. 
Then you can load all of the assets of your container class by passing an instance of the container.
You can use `@Assets` to specify an array.

    stage1Assets = new Stage1Assets();
    assignmentAssetManager.loadAssetFields(stage1Assets);
    
Once the asset manager is finished loading (with `finishLoading()` or enough calls to `update()`), 
all the fields of the container are automatically assigned references to the loaded assets. You can 
immediately start using them.

    batch.draw(stage1Assets.eggTexture, 0, 0);
    
You can unload all the assets of a container with

    assignmentAssetManager.unloadAssetFields(stage1Assets);
    
The assets that are unique to the container will be unloaded, and all fields will be marked null. If
an asset is part of some other loaded container, it will not be unloaded. This allows you to make 
container for multiple stages of your game and safely load and unload stages without unnecessary 
reloading of anything that is shared.

You can also specify an AssetLoader parameter by field name in the annotation:

```java
private static class Stage1Assets {
    TextureParameter mipmapParams = new TextureParameter(){{
        genMipMaps = true;
    }};
    
    @Asset(value = "egg.png", parameter = "mipmapParams") public Texture eggTexture;
}
```
	
The parameter must be a field of the container class, or can be a static member of any class if a 
fully qualified name is given.

Finally, you can optionally implement the AssetContainer interface to specify a directory for all 
the assets, or to get a callback once they're loaded and assigned:

```java
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
```

### Proguard/R8 tip

Asset loader parameter fields that you reference only by name in annotations are at risk of being 
stripped during minification. If you define these fields only in AssetContainers, you can use a 
single rule to protect all of these fields:

    -keep class * implements com.cyphercove.covetools.assets.AssetContainer { *; }
    
## TextureAtlasCacher

TextureAtlasCacher uses the names of an object's fields to pull regions from a TextureAtlas and 
assign them automatically. Field names can also have suffixes like "Region", "AtlasRegion" or 
"TextureRegion" that are automatically omitted when finding the regions in the atlas. Or, the 
`@RegionName` annotation can be used to use an explicit name.

In this example, this class's fields are all assigned automatically from the TextureAtlas passed to 
its constructor.

```java
public class MyTextureRegions {
    public TextureRegion door;
    public TextureRegion[] swayingTree;
    @RegionName("egg") public AtlasRegion rollingEgg;

    public MyTextureRegions(TextureAtlas atlas) {
        TextureAtlasCacher.cacheRegions(atlas, this, true);
    }
}
```
## JsonFieldUpdater

This class lets you update field values (by reflection) by modifying a Json file and loading it at 
runtime. I use this to tweak values quickly without rebuilding the game over and over. I create an 
input listener that calls `jsonFieldUpdater.readFieldsToObjects(...)` in response to a key press. 
For example:

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

The Disposal class provides an easy way to clean up a class of Disposable objects, so you can easily
avoid accidental leaks. Just make sure all your Disposable objects are referenced by member 
variables. Use the `@Group(int)` and `@Skip` annotations for controlled disposal.
    
## Android Live Wallpapers

One of libGDX's selling points is the ability to develop a game using desktop builds and then 
deploying to Android with minimal extra work. CoveTools provides some classes that help you do the 
same with live wallpapers.

The first step is to make your base application listener derive from LiveWallpaperListener (or 
LiveWallpaperAdapter) instead of ApplicationListener. This gives you a few extra methods to fill out:

* Use `void render(float xOffset, float yOffset, float xOffsetLooping, float xOffsetFake);` instead 
of `render()`, and it will provide you with the xOffset provided by Android's launcher. On the 
desktop build, it will simulate the xOffset scrolling left and right when you drag in the window.

* Use `void onPreviewStateChange(boolean isPreview);` to determine if the wallpaper is in preview 
mode on Android.
    
* Use `onSettingsChanged()` to perform any necessary updates if the user has changed the settings 
on Android.

* Use `onIconDropped (int x, int y)` to react to an icon being dropped at the specified location 
(in screen coordinates) in the Android launcher.

For your desktop build, wrap the listener in a DesktopLiveWallpaperWrapper when passing it to the 
LwjglApplication constructor:

    new LwjglApplication(new DesktopLiveWallpaperWrapper(new MyLiveWallpaperListener()), config);

For your Android build, wrap the listener in a LiveWallpaperWrapper before handing it to 
`initialize(...)`. The wrapper has some additional parameters to pass to the constructor. The 
application context is used to retrieve display parameters. The optional SharedPreferences will be 
listened to for changes. The optional WallpaperEventListener will allow you to respond to some 
live wallpaper events on the libGDX (OpenGL) thread. I use this to initialize values from the 
Android settings right before the first render call, and to update those values when the settings 
change.

    initialize(new LiveWallpaperWrapper(new MyLiveWallpaperListener(), getApplicationContext(), 
        mySharedPreferences, myEventListener), config);

You can also easily use your wallpaper as a Daydream, aka Screen Saver, by wrapping it in a 
DaydreamWrapper before passing it to `AndroidDaydream.initialize(...)` :

    initialize(new DaydreamWrapper(new MyLiveWallpaperListener(), myEventListener), config);
    
A daydream shouldn't be running while a user is changing settings, so there is no option to pass a 
SharedPreferences instance.

## GaussianBlur

This tool is for applying Gaussian blur post processing effects such as depth of field, bloom, or 2D 
light mapping. 

Instantiate one like any other Disposable object such as FrameBuffer or SpriteBatch. Make sure to 
dispose it in `dispose()`. Before you use it, you must call `resize()` on it at least once. 
Presumably you'll be calling this in your `resize()` method:

```java
public void resize (int width, int height){
    //...
    
    // You can pass a value corresponding to the backing FrameBuffer's long dimension. The width and
    // height will be filled automatically with appropriate aspect ratio.
    gaussianBlur.resize(50, width, height);
}
```

Then in `render()`, you can draw the scene you want blurred between `begin()` and `end()` calls.

```java
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

```java
gaussianBlur.beginRender(myCustomShader);
myCustomShader.setUniformf(...); // Set any necessary uniforms here.
gaussianBlur.finishRender();
```
