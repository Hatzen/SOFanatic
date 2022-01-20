package de.hartz.software.stackoverflowlogin.helper

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import de.hartz.software.stackoverflowlogin.model.TimeStampNames
import de.hartz.software.stackoverflowlogin.model.User
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object PersistenceHelper {
    val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'") // Quoted "Z" to indicate UTC, no timezone offset

    private val FILE_NAME = "dump.tmp"
    private val USER = "USER"
    private val PW = "PW"

    init {
        val tz: TimeZone = TimeZone.getTimeZone("UTC")
        DATE_FORMAT.setTimeZone(tz)
    }


    fun getUser(context: Context): User {
         val sharedPreferences = getSharedPref(context)

        val email = sharedPreferences.getString(USER, "")!!
        val password = sharedPreferences.getString(PW, "")!!

        return User(email, password)
    }

    fun setUser (context: Context, user: User) {
        val sharedPreferences = getSharedPref(context)
        val editor = sharedPreferences.edit()
        editor.putString(USER, user.userName)
        editor.putString(PW, user.password)
        editor.apply()
    }

    fun storeTimeStamp (context: Context, name: TimeStampNames) {
        val sharedPreferences = getSharedPref(context)

        val nowAsISO: String = DATE_FORMAT.format(Date())

        val editor = sharedPreferences.edit()
        editor.putString(name.timeStampName, nowAsISO)
        editor.apply()
    }

    fun getTimeStamp(context: Context, name: TimeStampNames): String {
        val sharedPreferences = getSharedPref(context)

        return sharedPreferences.getString(name.timeStampName, "")!!
    }

    private fun getSharedPref (context: Context): SharedPreferences {
        val masterKeyAlias: String = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
            FILE_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        return sharedPreferences
    }
}