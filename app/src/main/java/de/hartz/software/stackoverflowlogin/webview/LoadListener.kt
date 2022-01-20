package de.hartz.software.stackoverflowlogin.webview

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebViewClient
import android.widget.Toast
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
        Log.e("WebView", "Called Back!" + badgeContent)
        Toast.makeText(context, badgeContent, Toast.LENGTH_LONG).show()
    }

    @JavascriptInterface
    fun commandFinished() {
        webViewClient.commandFinished()
    }
}