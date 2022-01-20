package de.hartz.software.stackoverflowlogin.service

import android.app.Notification
import android.content.Intent
import android.view.WindowManager
import android.graphics.PixelFormat
import android.view.Gravity
import android.os.IBinder
import android.app.Service
import android.util.Log
import android.view.ViewGroup
import android.webkit.*
import de.hartz.software.stackoverflowlogin.helper.CheckPermissionsHelper
import de.hartz.software.stackoverflowlogin.helper.Helper
import de.hartz.software.stackoverflowlogin.helper.PersistenceHelper
import de.hartz.software.stackoverflowlogin.model.TimeStampNames
import androidx.core.app.NotificationCompat

import android.app.NotificationManager

import android.app.NotificationChannel
import android.content.Context

import android.os.Build


// https://stackoverflow.com/a/68354848/8524651
class BackgroundLoginService : Service() {

    private lateinit var wv: WebView

    override fun onCreate() {
        super.onCreate()
        // Notification needed for foreground service.
        // https://stackoverflow.com/a/46449975/8524651
        if (Build.VERSION.SDK_INT >= 26) {
            val CHANNEL_ID = "my_channel_01"
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
            val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("")
                .setContentText("").build()
            startForeground(1, notification)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (!CheckPermissionsHelper.canDrawOverlayViews(this)) {
            Log.e(javaClass.simpleName, "Cannot login permission not granted.")
            PersistenceHelper.storeTimeStamp(this, TimeStampNames.LAST_ERROR)
            return START_NOT_STICKY
        }
        wv = WebView(this)

        Helper.applyWebViewHandler(wv, this)

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val invisibleLayoutParams = getInvisibleWebViewParams()
        windowManager.addView(wv, invisibleLayoutParams)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // TODO: Do we need to remove the view? Most probably we need to..
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.removeView(wv)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun getInvisibleWebViewParams(): WindowManager.LayoutParams {
        val params = WindowManager.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // TYPE_PHONE, // Android>7 https://stackoverflow.com/questions/52059033/permission-denied-for-window-type-2002-in-android-studio
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 0
        params.width = 0
        params.height = 0
        return params
    }
}
