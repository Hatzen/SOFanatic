package de.hartz.software.stackoverflowlogin.schedule

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.Context
import android.util.Log
import de.hartz.software.stackoverflowlogin.helper.Helper
import de.hartz.software.stackoverflowlogin.helper.PersistenceHelper
import de.hartz.software.stackoverflowlogin.model.TimeStampNames
import de.hartz.software.stackoverflowlogin.service.BackgroundLoginService

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        val DAILY_LOGIN = "de.hartz.software.stackoverflowlogin.DAILY_LOGIN"
        private val BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (DAILY_LOGIN == action || BOOT_COMPLETED == action) {
            Log.e(this.javaClass.simpleName, "Action triggered $action")
            Helper.startService(context)
            Helper.setAlarm(context) // Needed for both on Boot AND IdleAndExact reset alarms.
            PersistenceHelper.storeTimeStamp(context, TimeStampNames.LAST_ALARM)
            Helper.showNotification(context, "AlarmReceiver")
        }
    }
}