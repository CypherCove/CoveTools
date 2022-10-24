/*******************************************************************************
 * Copyright 2017 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cyphercove.covetools.android

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.app.Service
import android.content.ComponentName
import android.app.WallpaperManager
import android.util.Log

/**
 * An activity that opens the live wallpaper chooser list, or a specific live wallpaper's preview on
 * Jelly Bean and later. Intended to be used as the application's `android.intent.category.INFO`
 * Activity if the application does not have a launcher Activity, which a typical situation for a
 * primarily live wallpaper application. If a wallpaper chooser cannot be found, a Toast will be
 * displayed via [wallpaperSetterNotFoundToastStringResource]. If on an API before Jelly Bean, a
 * toast will instruct the user to select the live wallpaper from the list.
 *
 * If the wallpaper is determined to be currently running, and if it has a settings activity, then
 * that settings activity is opened. It is determined to be running by matching both the service
 * class name and the package name.
 */
abstract class LiveWallpaperInfoActivity : Activity() {

    companion object {
        private val originalChooserIntent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
        private val sdk33ChooserIntent = Intent("com.android.wallpaper.livepicker")
        private val generalWallpaperChooserIntent = Intent(Intent.ACTION_SET_WALLPAPER)
        private val googleGeneralWallpaperPickerIntent = Intent("com.google.android.apps.wallpaper")
    }

    /** @return The class representing the live wallpaper.
     */
    protected abstract val wallpaperServiceClass: Class<out Service>

    /** @return The class representing the live wallpaper's settings, or null if it has none.
     */
    protected abstract val wallpaperSettingsClass: Class<out Activity>?

    /** @return The resource ID for the message that is shown if the a live wallpaper setting Activity cannot be bound.
     */
    protected abstract val wallpaperSetterNotFoundToastStringResource: Int

    /** @return The resource ID for the message that is shown if the user is brought to the live wallpaper
     * chooser list because the explicit wallpaper preview could not be directly opened.
     */
    protected abstract val wallpaperChooserToastStringResource: Int

    /** @return The resource ID for the message that is shown if the user opens the activity when
     * the live wallpaper is already running and no settings screen can be opened. Can be 0 if this
     * wallpaper has settings, because it will never be used.
     */
    protected abstract val alreadyRunningNoSettingsToastStringResource: Int

    private val wallpaperPreviewIntent: Intent
        get() {
            val componentName = ComponentName(applicationContext, wallpaperServiceClass)
            return Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, componentName)
            }
        }

    data class LiveWallpaperSetter(val intent: Intent, val isChooser: Boolean)

    private val potentialLiveWallpaperSetters: List<LiveWallpaperSetter>
        get() =
            listOf(
                LiveWallpaperSetter(wallpaperPreviewIntent, false),
                LiveWallpaperSetter(sdk33ChooserIntent, true),
                LiveWallpaperSetter(originalChooserIntent, true),
                LiveWallpaperSetter(generalWallpaperChooserIntent, true),
                LiveWallpaperSetter(googleGeneralWallpaperPickerIntent, true)
            )

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isWallpaperRunning) {
            if (!tryOpenWallpaperSettings()) {
                showLongToast(alreadyRunningNoSettingsToastStringResource)
            }
            finish()
            return
        }
        if (tryOpenWallpaperSetter()) {
            finish()
            return
        }
        showLongToast(wallpaperSetterNotFoundToastStringResource)
        tryOpenWallpaperSettings()
        finish()
    }

    /**
     * Opens the found wallpaper setter. The direct wallpaper preview is preferred. If it cannot be
     * found, the live wallpaper selector (list of all live wallpapers) is picked next, followed by
     * the general wallpaper setter.
     * @return Whether the setter was found and opened.
     */
    protected open fun tryOpenWallpaperSetter(): Boolean {
        for ((intent, isChooser) in potentialLiveWallpaperSetters) {
            if (intent.resolveActivity(packageManager) != null) {
                if (isChooser) {
                    showLongToast(wallpaperChooserToastStringResource)
                }
                startActivity(intent)
                return true
            }
        }
        return false
    }

    /** Whether the live wallpaper is already set as the system wallpaper.  */
    protected val isWallpaperRunning: Boolean
        get() {
            val info = WallpaperManager.getInstance(applicationContext).wallpaperInfo
            return info?.serviceName == wallpaperServiceClass.name && info?.packageName == application.packageName
        }

    /** Open an intent for the live wallpaper settings if it can be resolved.
     * @return whether the intent could be resolved and started.
     * */
    protected open fun tryOpenWallpaperSettings(): Boolean {
        val settingsActivity = wallpaperSettingsClass?.name
            ?: WallpaperManager.getInstance(applicationContext).wallpaperInfo.settingsActivity
        if (settingsActivity != null) {
            val i = Intent(Intent.ACTION_MAIN).apply {
                component = ComponentName(applicationContext.packageName, settingsActivity)
            }
            if (i.resolveActivity(packageManager) != null) {
                startActivity(i)
                return true
            }
        }
        return false
    }

    private fun showLongToast(stringRes: Int) {
        if (stringRes != 0) {
            Toast.makeText(
                this, stringRes, Toast.LENGTH_LONG
            ).show()
        }
    }
}