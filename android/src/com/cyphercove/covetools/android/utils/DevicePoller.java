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
package com.cyphercove.covetools.android.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

/**
 * Periodically checks on various device states. For each type of parameter, a poll interval can be set. The {@code get}
 * methods can be called as frequently as desired without causing unnecessary repeated polling. For polling to occur,
 * the {@link #update(float)} method should be called repeatedly. It is set up this way so the game can easily keep it
 * updated only while running, and the speed can be adjusted from core project code if desired.
 *
 * @author cypherdare
 */
public class DevicePoller {

    private float batteryLevelCountdown = 0;
    private float chargeStateCountdown = 0;
    private float batteryLevelInterval = 1;
    private float chargeStateInterval = 1;
    private float batteryLevel = 1f;
    private boolean chargeState = true;

    Context context;

    public DevicePoller (Context context, float batteryLevelInterval, float chargeStateInterval){
        this.context = context;
        this.batteryLevelInterval = batteryLevelInterval;
        this.chargeStateInterval = chargeStateInterval;
    }

    public void update (float deltaTime){
        batteryLevelCountdown -= deltaTime;
        chargeStateCountdown -= deltaTime;
    }

    /** @return The battery level on a 0-1 scale. */
    public float getBatteryLevel(){
        if (batteryLevelCountdown <= 0){
            batteryLevelCountdown = batteryLevelInterval;
            Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            batteryLevel =
                    (float)intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 50)/
                            (float)intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);

        }

        return batteryLevel;
    }

    /** @return Whether the device is currently charging. */
    public boolean getChargingState(){
        if (chargeStateCountdown <= 0){
            chargeStateCountdown = chargeStateInterval;
            Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int pluggedExtra = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
            chargeState = !(pluggedExtra == 0);
        }

        return chargeState;
    }

}

