package de.hartz.software.stackoverflowlogin.activities

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import de.hartz.software.stackoverflowlogin.helper.Helper
import de.hartz.software.stackoverflowlogin.helper.PersistenceHelper
import de.hartz.software.stackoverflowlogin.R
import de.hartz.software.stackoverflowlogin.helper.CheckPermissionsHelper
import de.hartz.software.stackoverflowlogin.model.TimeStampNames
import de.hartz.software.stackoverflowlogin.model.User
import de.hartz.software.stackoverflowlogin.schedule.AlarmReceiver
import de.hartz.software.stackoverflowlogin.service.BackgroundLoginService
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestOverlayPermission() // TODO: Request energy optimzer settings.
        Helper.setAlarm(this)
        setupView()
        Helper.showNotification(this, "Activity started")
    }

    private fun setupView() {
        val user = PersistenceHelper.getUser(this)

        val userView = findViewById<EditText>(R.id.user)
        val passwordView = findViewById<EditText>(R.id.pw)
        userView.setText(user.userName)
        passwordView.setText(user.password)

        val successView = findViewById<TextView>(R.id.timestamp_success)
        val errorView = findViewById<TextView>(R.id.timestamp_error)
        val alarmView = findViewById<TextView>(R.id.timestamp_alarm)
        successView.setText(PersistenceHelper.getTimeStamp(this, TimeStampNames.LAST_SUCCESS))
        errorView.setText(PersistenceHelper.getTimeStamp(this, TimeStampNames.LAST_ERROR))
        alarmView.setText(PersistenceHelper.getTimeStamp(this, TimeStampNames.LAST_ALARM))

        findViewById<Button>(R.id.save).setOnClickListener {
            val userName = userView.text.toString()
            val password = passwordView.text.toString()
            val user = User(userName, password)
            PersistenceHelper.setUser(this, user)
            Helper.startService(this)
        }

        findViewById<Button>(R.id.showWebview).setOnClickListener {
            startActivity(Intent(this, WebViewActivity::class.java))
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (!CheckPermissionsHelper.canDrawOverlayViews(this)) {
            Toast.makeText(this, "Permission required but denied. Finishing App.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun requestOverlayPermission() {
        if (!CheckPermissionsHelper.canDrawOverlayViews(this)) {
            val myIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            myIntent.data = Uri.parse("package:$packageName")
            startActivityForResult(myIntent, -1)
        }
    }
}