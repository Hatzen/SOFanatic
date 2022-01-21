package de.hartz.software.stackoverflowlogin.activities

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import de.hartz.software.stackoverflowlogin.R
import de.hartz.software.stackoverflowlogin.helper.Helper

class WebViewActivity : AppCompatActivity() {
    companion object {
        val KEY_EXTRA_LOGIN_ONLY = "LOGIN_ONLY"
    }

    var loginOnly = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        loginOnly = savedInstanceState?.getBoolean(KEY_EXTRA_LOGIN_ONLY, false) ?: false
        setupTestView()
    }

    private fun setupTestView () {
        val wv = findViewById<WebView>(R.id.webView)
        Helper.applyWebViewHandler(wv, this, loginOnly)
    }
}