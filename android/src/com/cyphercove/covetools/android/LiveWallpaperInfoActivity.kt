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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.app.Service
import android.content.ComponentName
import android.app.WallpaperManager
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import com.cyphercove.covetools.R

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
 *
 * If any of the properties with "onboarding" in the name are overridden to return something other
 * than 0, an on-boarding screen is shown if the currently running live wallpaper cannot be detected
 * or if the wallpaper preview cannot be directly resolved and opened, as seems to be the case on
 * Android 13 Tiramisu. The screen has a button for trying to open the wallpaper picker instead, a
 * checkbox to disable the screen, and a button to go on to the settings. If the user checks the box
 * for disabling the screen, it will skip to opening the settings from then on, or if there are no
 * settings, then the wallpaper picker.
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

    /** @return The resource ID for a message to show on the backup onboarding screen in the event the
     * currently running live wallpaper cannot be detected to be running and the wallpaper preview
     * cannot be resolved and opened. Suggested message is something like, "Live wallpapers should
     * be set using the Android wallpaper settings." Can return 0 to omit the message or if not using
     * a backup onboarding screen.
     */
    protected open val onboardingMessageStringResource: Int
        get() = 0

    /** @return The resource ID for labeling the button on the backup onboarding screen that opens whichever
     * wallpaper picker can be resolved. If no intent can be resolved, the button will not be shown.
     * Suggested label is something like, "Open Android wallpaper picker". Can return 0 to omit the
     * button or if not using a backup onboarding screen.
     */
    protected open val onboardingOpenWallpaperPickerButtonStringResource: Int
        get() = 0

    /** @return The resource ID for labeling the check box on the backup onboarding screen that will
     * disable the onboarding screen from being shown again. Suggested label is something like, "Don't
     * show again". Can return 0 to omit the check box or if not using a backup onboarding screen.
     */
    protected open val onboardingDontShowAgainCheckBoxStringResource: Int
        get() = 0

    /** @return The resource ID for labeling the button on the backup onboarding screen that opens the
     * settings. This button is only shown if the settings can be resolved. Can return 0 to omit the
     * button or if not using a backup onboarding screen.
     */
    protected open val onboardingOpenSettingsButtonStringResource: Int
        get() = 0

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
        if (isOnboardingBackupEnabled) {
            handleOnboardingBackupMode()
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

    protected open val canOpenWallpaperSetter: Boolean
        @SuppressLint("QueryPermissionsNeeded")
        get() = potentialLiveWallpaperSetters.any {
            it.isChooser && it.intent.resolveActivity(packageManager) != null
        }

            /**
     * Opens the found wallpaper setter. The direct wallpaper preview is preferred. If it cannot be
     * found, the live wallpaper selector (list of all live wallpapers) is picked next, followed by
     * the general wallpaper setter.
     * @return Whether the setter was found and opened.
     */
    @SuppressLint("QueryPermissionsNeeded")
    protected open fun tryOpenWallpaperSetter(previewOnly: Boolean = false): Boolean {
        for ((intent, isChooser) in potentialLiveWallpaperSetters) {
            try {
                if (previewOnly && isChooser) {
                    continue
                }
                startActivity(intent)
                if (isChooser) {
                    showLongToast(wallpaperChooserToastStringResource)
                }
                return true
            } catch (e: Exception) {
                continue
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

    protected open val canOpenWallpaperSettings: Boolean
        @SuppressLint("QueryPermissionsNeeded")
        get() {
            val settingsActivity = wallpaperSettingsClass?.name
                ?: WallpaperManager.getInstance(applicationContext).wallpaperInfo.settingsActivity
            if (settingsActivity != null) {
                val i = Intent(Intent.ACTION_MAIN).apply {
                    component = ComponentName(applicationContext.packageName, settingsActivity)
                }
                return i.resolveActivity(packageManager) != null
            }
            return false
        }

    /** Open an intent for the live wallpaper settings if it can be resolved.
     * @return whether the intent could be resolved and started.
     * */
    @SuppressLint("QueryPermissionsNeeded")
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

    private val isOnboardingBackupEnabled: Boolean
        get() = onboardingMessageStringResource != 0
                || onboardingOpenSettingsButtonStringResource != 0
                || onboardingOpenWallpaperPickerButtonStringResource != 0

    private val sharedPreferences by lazy {
        getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    private var isOnboardingScreenDisabledByUser: Boolean
        get()= sharedPreferences.getBoolean(ONBOARDING_DISABLED_KEY, false)
        set(value) { sharedPreferences.edit().putBoolean(ONBOARDING_DISABLED_KEY, value).apply() }

    /** Call only after we have been unable to detect this wallpaper as running. */
    @SuppressLint("QueryPermissionsNeeded")
    private fun handleOnboardingBackupMode() {
        if (tryOpenWallpaperSetter(true)) {
            finish()
            return
        }
        // Detecting running wallpaper and opening preview failed. Show the onboarding screen unless disabled.
        if (isOnboardingScreenDisabledByUser) {
            if (!tryOpenWallpaperSettings()) {
                if (!tryOpenWallpaperSetter()) {
                    showLongToast(wallpaperSetterNotFoundToastStringResource)
                }
            }
            finish()
            return
        }

        setContentView(R.layout.activity_live_wallpaper_info_onboarding)

        val messageView: TextView = findViewById(R.id.message)
        if (onboardingMessageStringResource != 0) {
            messageView.setText(onboardingMessageStringResource)
        } else {
            messageView.visibility = View.GONE
        }

        val openPickerButton: Button = findViewById(R.id.openPickerButton)
        val shouldShowPickerButton = onboardingOpenWallpaperPickerButtonStringResource != 0
                && canOpenWallpaperSetter
        if (shouldShowPickerButton) {
            openPickerButton.setText(onboardingOpenWallpaperPickerButtonStringResource)
            openPickerButton.setOnClickListener {
                tryOpenWallpaperSetter()
                finish()
            }
        } else {
            openPickerButton.visibility = View.GONE
        }

        val dontShowAgainCheckBox: CheckBox = findViewById(R.id.dontShowAgainCheckBox)
        if (onboardingDontShowAgainCheckBoxStringResource != 0) {
            dontShowAgainCheckBox.setText(onboardingDontShowAgainCheckBoxStringResource)
            dontShowAgainCheckBox.setOnCheckedChangeListener { _, isChecked ->
                isOnboardingScreenDisabledByUser = isChecked
            }
        } else {
            dontShowAgainCheckBox.visibility = View.GONE
        }

        val openSettingsButton: Button = findViewById(R.id.openSettingsButton)
        val shouldShowSettingsButton = onboardingOpenSettingsButtonStringResource != 0
                && canOpenWallpaperSettings
        if (shouldShowSettingsButton) {
            openSettingsButton.setText(onboardingOpenSettingsButtonStringResource)
            openSettingsButton.setOnClickListener {
                tryOpenWallpaperSettings()
                finish()
            }
        } else {
            openSettingsButton.visibility = View.GONE
        }

    }
}

private const val SHARED_PREFERENCES_NAME = "com.cyphercove.covetools.android.LiveWallpaperInfoActivity"
private const val ONBOARDING_DISABLED_KEY = "onboardingDisabled"