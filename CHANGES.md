# 1.2.4
 - **[Breaking]** `AssignmentAssetManager.loadAssetFields()` now skips fields that have non-null values.
 - Added `ShaderProgramAsset` and `TextureAsset` for creating loading parameters within the annotation.

# 1.2.3
 - Update to LibGDX 1.9.11
 - Deprecated `Anisotropy`, as its features are now available from Texture directly.
 - **[Breaking (imports only)]** WallpaperEventListener is no longer an inner interface of LiveWallpaperWrapper.
 - Added WallpaperEventAdapter, empty implementation of WallpaperEventListener.
 - Added LiveWallpaperAdapter, empty implementation of LiveWallpaperListener.
 - Fleshed out CoveTools.gwt.xml, for using CoveTools with GWT.
 - Tweens removed. See [gdx-tween](https://github.com/CypherCove/gdx-tween).
 - **[Breaking]** Change raw return type of `LiveWallpaperInfoActivity.getWallpaperServiceClass()` to `Class<? extends Service>`.
 - Deprecated `ColorUtil.blendRGBWithSaturation(...)` as there are some cases where it produces sudden jumps in interpolation.

# 1.2.2
 - Add TextureAtlasCacher.

# 1.2.1 
 - Update to LibGDX 1.9.10
 - **[Breaking]** `ColorUtil.blendRGBWithSaturation(...)` changed signature to include result parameter to avoid ambiguity
 - **[Breaking]** ColorUtil methods removed if they duplicated functionality from the Color class.
 - **[Breaking]** ColorUtil methods involving hue changed from 0-1 scale to 0-360 scale.
 - Added tweens. API not yet stable.
 - **[Breaking]** `Anisotropy.setTextureAnisotropy()` returns 1 rather than -1 if anisotropy is unsupported
 - **[Breaking]** `GaussianBlur.depthTestingToScene` defaults false instead of true (less surprising behavior).