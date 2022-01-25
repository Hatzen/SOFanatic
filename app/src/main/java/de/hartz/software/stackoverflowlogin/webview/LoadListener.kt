package de.hartz.software.stackoverflowlogin.webview

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast
import de.hartz.software.stackoverflowlogin.helper.Helper
import de.hartz.software.stackoverflowlogin.helper.PersistenceHelper
import de.hartz.software.stackoverflowlogin.model.TimeStampNames

class LoadListener(val context: Context, val webViewClient: WebViewLoginHandler) {
    @JavascriptInterface
    fun login(loginWorked: Boolean) {
        var timestampName = TimeStampNames.LAST_ERROR
        var message = "Login failed"
        Log.e(javaClass.simpleName, message)
        if (loginWorked) {
            message = "Login success"
            timestampName = TimeStampNames.LAST_SUCCESS
        }
        PersistenceHelper.storeTimeStamp(context, timestampName)
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    @JavascriptInterface
    fun badges(badgeContent: String) {
        val numberOfDays = Integer.parseInt(
            badgeContent.substring(badgeContent.indexOf("-") + 2, badgeContent.indexOf("/"))
        )
        val storedNumberOfDays = PersistenceHelper.getNumberOfDays(context)
        if (numberOfDays != storedNumberOfDays) {
            if (numberOfDays <= 1) {
                Helper.showNotification(context, "Number of Days got resetted! :(")
            }
            Helper.showNotification(context, "Number of days logged in: " + numberOfDays)
            PersistenceHelper.storeNumberOfDays(context, numberOfDays)
            PersistenceHelper.storeTimeStamp(context, TimeStampNames.LAST_DAY_CHANGED)
        } else {
            // If the days count is the same check wether there is need to set the Alarm to every hour.
            // Especially needed when started after booting.
            Helper.setAlarm(context)
        }
    }

    @JavascriptInterface
    fun commandFinished() {
        webViewClient.commandFinished()
    }
}