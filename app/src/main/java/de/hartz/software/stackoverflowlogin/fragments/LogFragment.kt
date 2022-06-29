package de.hartz.software.stackoverflowlogin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import de.hartz.software.stackoverflowlogin.R
import de.hartz.software.stackoverflowlogin.helper.PersistenceHelper
import de.hartz.software.stackoverflowlogin.model.TimeStampNames


class LogFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_log, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupView(view)
    }

    private fun setupView(view: View) {
        val context = requireContext()

        val successView = view.findViewById<TextView>(R.id.timestamp_success)
        val errorView = view.findViewById<TextView>(R.id.timestamp_error)
        val alarmView = view.findViewById<TextView>(R.id.timestamp_alarm)
        val numberOfDaysView = view.findViewById<TextView>(R.id.numberOfDays)
        successView.setText(PersistenceHelper.getTimeStamp(context, TimeStampNames.LAST_SUCCESS))
        errorView.setText(PersistenceHelper.getTimeStamp(context, TimeStampNames.LAST_ERROR))
        alarmView.setText(PersistenceHelper.getTimeStamp(context, TimeStampNames.LAST_ALARM))
        numberOfDaysView.setText("" + PersistenceHelper.getNumberOfDays(context))
    }

}