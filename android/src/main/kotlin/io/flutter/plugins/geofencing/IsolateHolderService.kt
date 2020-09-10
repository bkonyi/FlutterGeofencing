// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.geofencing

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import android.util.Log
import io.flutter.embedding.engine.FlutterEngine

class IsolateHolderService : Service() {
    companion object {
        @JvmStatic
        val ACTION_SHUTDOWN = "SHUTDOWN"
        @JvmStatic
        private val WAKELOCK_TAG = "IsolateHolderService::WAKE_LOCK"
        @JvmStatic
        private val TAG = "IsolateHolderService"
        @JvmStatic
        private var sBackgroundFlutterEngine: FlutterEngine? = null

        @JvmStatic
        fun setBackgroundFlutterEngine(engine: FlutterEngine?) {
            sBackgroundFlutterEngine = engine
        }
    }

    override fun onBind(p0: Intent) : IBinder? {
        return null;
    }

    override fun onCreate() {
        super.onCreate()
        val CHANNEL_ID = "geofencing_plugin_channel"
        val channel = NotificationChannel(CHANNEL_ID,
                "Flutter Geofencing Plugin",
                NotificationManager.IMPORTANCE_LOW)
        val imageId = getResources().getIdentifier("ic_launcher", "mipmap", getPackageName())

        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Almost home!")
                .setContentText("Within 1KM of home. Fine location tracking enabled.")
                .setSmallIcon(imageId)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()

        (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG).apply {
                setReferenceCounted(false)
                acquire()
            }
        }
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int) : Int {
        if (intent.getAction() == ACTION_SHUTDOWN) {
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG).apply {
                    if (isHeld()) {
                        release()
                    }
                }
            }
            stopForeground(true)
            stopSelf()
        }
        return START_STICKY;
    }
}
