package de.hartz.software.stackoverflowlogin.helper

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import de.hartz.software.stackoverflowlogin.BuildConfig
import de.hartz.software.stackoverflowlogin.R
import de.hartz.software.stackoverflowlogin.activities.MainActivity
import de.hartz.software.stackoverflowlogin.model.TimeStampNames
import de.hartz.software.stackoverflowlogin.schedule.AlarmReceiver
import de.hartz.software.stackoverflowlogin.service.BackgroundLoginService
import de.hartz.software.stackoverflowlogin.webview.LoadListener
import de.hartz.software.stackoverflowlogin.webview.WebViewLoginHandler
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*


object Helper {

    fun startService (context: Context) {
        val intent = Intent(context, BackgroundLoginService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun applyWebViewHandler(webView: WebView, context: Context, loginOnly: Boolean = false) {
        val webViewClient = WebViewLoginHandler(context, webView, loginOnly)
        webView.webViewClient = webViewClient
        // TODO: Webview still seems to cache login somehow.. invalidating password still leads to being logged in..
        webView.getSettings().setAppCacheEnabled(false)
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE)
        webView.clearCache(true)

        webView.setWebChromeClient(object: WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage ): Boolean {
                    consoleMessage.messageLevel()
                    Log.e("WebView", consoleMessage.message());
                    return true;
                }
            });

        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(LoadListener(context, webViewClient), WebViewLoginHandler.ANDROID_CALLBACK)
        val googleUrl =
            "https://www.google.com"
        webView.loadUrl(googleUrl) // Trial showing google first for clearing cache..
        val stackoverflowUrl =
            "https://stackoverflow.com/users/login?ssrc=head&returnurl=https%3a%2f%2fstackoverflow.com%2f"
        webView.loadUrl(stackoverflowUrl)
    }

    // https://stackoverflow.com/a/7846622/8524651
    // https://stackoverflow.com/a/60212603/8524651
    // TODO: Listen to connectivity changes and set accurate.
    //      Maybe we can use this tutorial? Or is it outdated?? https://www.schuermann.eu/2012/05/service-daily/
    fun setAlarm(context: Context) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = AlarmReceiver.DAILY_LOGIN
        val flags =  PendingIntent.FLAG_CANCEL_CURRENT
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, flags)
        val calendar: Calendar = Calendar.getInstance()

        val result: Instant
        // Start within the next hour when success is more than a day ago.  Otherwise next day midday.
        val lastTimestamp = PersistenceHelper.getTimeStamp(context, TimeStampNames.LAST_DAY_CHANGED)

        var wasErroneousLastTime = true
        if (lastTimestamp != "") {
            val lastTimestampDate = PersistenceHelper.DATE_FORMAT.parse(lastTimestamp)
            wasErroneousLastTime = lastTimestampDate == null ||
                    lastTimestampDate.toInstant().plus(1, ChronoUnit.DAYS).isBefore(Instant.now())
        }
        if (!wasErroneousLastTime) {
            // Set alarm for 12AM to not accidentially pass Stackoverflows UTC Time.
            var localDateTime: LocalDateTime =
                LocalDateTime.ofInstant(Instant.now().plus(1, ChronoUnit.DAYS), ZoneId.systemDefault())
            localDateTime = localDateTime.withHour(12)
            result = localDateTime.atZone(ZoneId.systemDefault()).toInstant()
        } else {
            if (lastTimestamp != "") {
                Helper.showNotification(context, "Login count didnt changed since" + lastTimestamp)
            }
            result = Instant.now().plus(1, ChronoUnit.HOURS)
        }

        calendar.setTimeInMillis(result.toEpochMilli())
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(pendingIntent)

        val time = calendar.getTimeInMillis()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarm.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        } else {
            alarm.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        }
    }

    fun showNotification(context: Context, title: String) {
        val channelId = "ACTIVATION_INFO"
        val notification = NotificationCompat.Builder(context, channelId)
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        notification.setContentIntent(pendingIntent)
        notification.setSmallIcon(R.mipmap.ic_launcher_round)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notification.setSmallIcon(R.mipmap.ic_stat_notification)
            notification.setColor(context.getResources().getColor(R.color.white))
        } else {
            notification.setSmallIcon(R.mipmap.ic_stat_notification)
        }

        notification.setContentTitle(title)
        notification.setContentText("StackoverflowLogin step finished")
        notification.setPriority(Notification.PRIORITY_MAX)
        val notificationManager = (context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Information about last login.",
                NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
            notification.setChannelId(channelId)
        }

        val notificationResult = notification.build()
        notificationManager.notify(187, notificationResult)
    }

    fun showDeveloperNotification(context: Context, title: String) {
        val debug = false // TODO: Make a UI Checkbox?
        if (!debug) {
            return
        }
        val channelId = "ACTIVATION_INFO"
        val notification = NotificationCompat.Builder(context, channelId)
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        notification.setContentIntent(pendingIntent)
        notification.setSmallIcon(R.mipmap.ic_launcher_round)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notification.setSmallIcon(R.mipmap.ic_stat_notification)
            notification.setColor(context.getResources().getColor(R.color.white))
        } else {
            notification.setSmallIcon(R.mipmap.ic_stat_notification)
        }

        notification.setContentTitle(title)
        notification.setContentText("StackoverflowLogin step finished")
        notification.setPriority(Notification.PRIORITY_MAX)
        val notificationManager = (context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Information about last login.",
                NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
            notification.setChannelId(channelId)
        }

        val notificationResult = notification.build()
        if (BuildConfig.DEBUG) {
            notificationResult.flags = notificationResult.flags or (Notification.FLAG_NO_CLEAR or Notification.FLAG_ONGOING_EVENT)
        }

        notificationManager.notify(12312, notificationResult)

    }
}