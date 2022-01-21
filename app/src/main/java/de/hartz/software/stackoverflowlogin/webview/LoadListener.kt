package de.hartz.software.stackoverflowlogin.webview

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebViewClient
import android.widget.Toast
import de.hartz.software.stackoverflowlogin.helper.Helper
import de.hartz.software.stackoverflowlogin.helper.PersistenceHelper
import de.hartz.software.stackoverflowlogin.model.TimeStampNames
import java.util.regex.Matcher
import java.util.regex.Pattern

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
        }
        PersistenceHelper.storeNumberOfDays(context, numberOfDays)
    }

    @JavascriptInterface
    fun commandFinished() {
        webViewClient.commandFinished()
    }
}