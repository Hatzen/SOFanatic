package de.hartz.software.stackoverflowlogin.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import de.hartz.software.stackoverflowlogin.R
import de.hartz.software.stackoverflowlogin.fragments.LogFragment
import de.hartz.software.stackoverflowlogin.fragments.LoginFragment
import de.hartz.software.stackoverflowlogin.fragments.SettingsFragment
import de.hartz.software.stackoverflowlogin.helper.CheckPermissionsHelper
import de.hartz.software.stackoverflowlogin.helper.Helper


class MainActivity : AppCompatActivity() {
    private lateinit var tabStateAdapter: TabStateAdapter
    private lateinit var viewPager: ViewPager2

    private val tabs = listOf("Log", "Login & Test", "Settings")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestOverlayPermission() // TODO: Request energy optimzer settings.
        Helper.setAlarm(this)

        tabStateAdapter = TabStateAdapter(supportFragmentManager, lifecycle)
        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = tabStateAdapter

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        TabLayoutMediator(tabLayout, viewPager, true) { tab, position ->
            tab.text = tabs[position]
        }.attach()
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

class TabStateAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> LogFragment()
            1 -> LoginFragment()
            2 -> SettingsFragment()
            else -> throw RuntimeException("No Fragment supplied")
        }
    }
}