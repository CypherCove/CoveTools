#1.2.12
 - Update to libGDX 1.13.1
 - Now building with Java 11.
 - Add SafeDaydream class.
 - Add ContextUser and SharedPreferences get operator functions.
 - Update Kotlin version in -android module to 2.2.0

#1.2.11
 - Update to libGDX 1.12.0
 - Update Android target to SDK 34.

#1.2.10
 - Update Android target to SDK 33.

#1.2.9
 - Fix LiveWallpaperInfoActivity failing to open wallpaper preview.

#1.2.8
 - Add optional onboarding screen to LiveWallpaperInfoActivity

#1.2.7
 - Update to libGDX 1.11.0
 - Update to Android SDK 33 and increase minSdkVersion to 17
 - Add Kotlin to Android module.
 - **[Breaking]** Revised LiveWallpaperInfoActivity behavior to handle the new Android SDK 33 live wallpaper picker and to 
handle explicitly setting the settings Activity. The resources for your toast messages should be
changed to reflect the new behavior. As of SDK 33, it is no longer possible to reliably detect if
live wallpapers are supported by finding the live wallpaper chooser.

#1.2.6
 - Update to libGDX 1.9.14.
 - Added ResolutionCycle

#1.2.5
 - Update to libGDX 1.9.12.
 - **[Breaking]** Added `LiveWallpaperListener.setLiveWallpaperApplicationDelegate()`.
 - Added LiveWallpaperApplicationDelegate for accessing `AndroidLiveWallpaper.notifyColorsChanged()`
 from the core module.

# 1.2.4
 - Update Android target to SDK 30.
 - **[Breaking]** `AssignmentAssetManager.loadAssetFields()` now skips fields that have non-null values.
 - Added `ShaderProgramAsset` and `TextureAsset` for creating loading parameters within the annotation.
 - **[Breaking]** `GaussianBlur` no longer disables depth testing on `end()` and `render()`, but rather
 restores the prior depth testing state.
 - Added `GaussianBlur.getTexture()` for drawing the blurred image externally.
 - `GaussianBlurShaderProvider` is no longer public, and is now shared automatically among separate
 `GaussianBlur` objects.
 - **[Behavior change]** `GaussianBlur` now more properly performs the blur in approximately linear 
 color space, which prevents the blurred image from being noticeably darker than the source image.
 - **[Behavior change]** `LiveWallpaperInfoActivity` also checks for matching package name when 
 determining if the currently running wallpaper is a match for the project's package name. Previously
 there were false positives if the service name matched but the package was different, which can 
 occur when different flavors of a project are installed on the same device.

# 1.2.3
 - Update to libGDX 1.9.11.
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
 - Update to libGDX 1.9.10.
 - **[Breaking]** `ColorUtil.blendRGBWithSaturation(...)` changed signature to include result parameter to avoid ambiguity
 - **[Breaking]** ColorUtil methods removed if they duplicated functionality from the Color class.
 - **[Breaking]** ColorUtil methods involving hue changed from 0-1 scale to 0-360 scale.
 - Added tweens. API not yet stable.
 - **[Breaking]** `Anisotropy.setTextureAnisotropy()` returns 1 rather than -1 if anisotropy is unsupported
 - **[Breaking]** `GaussianBlur.depthTestingToScene` defaults false instead of true (less surprising behavior).