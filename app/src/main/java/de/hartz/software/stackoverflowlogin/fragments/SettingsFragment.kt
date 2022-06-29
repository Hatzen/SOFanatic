package de.hartz.software.stackoverflowlogin.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import de.hartz.software.stackoverflowlogin.R


class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val packageName: String = requireActivity().packageName

        view.findViewById<Button>(R.id.overlay).setOnClickListener {
            val myIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            myIntent.data = Uri.parse("package:$packageName")
            startActivityForResult(myIntent, -1)
        }

        val button = view.findViewById<Button>(R.id.energySaver)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            button.setOnClickListener {
                val intent = Intent()
                val pm = requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager?
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                startActivityForResult(intent, -1)
            }
        } else {
            button.visibility = View.GONE
        }

    }

}