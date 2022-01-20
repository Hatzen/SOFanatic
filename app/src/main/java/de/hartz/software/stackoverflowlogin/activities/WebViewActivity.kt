package de.hartz.software.stackoverflowlogin.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.content.Context

import android.content.SharedPreferences
import android.util.Log
import android.webkit.*

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import de.hartz.software.stackoverflowlogin.R
import de.hartz.software.stackoverflowlogin.helper.Helper

class WebViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        setupTestView()
    }

    private fun setupTestView () {
        val wv = findViewById<WebView>(R.id.webView)
        Helper.applyWebViewHandler(wv, this)
    }
}