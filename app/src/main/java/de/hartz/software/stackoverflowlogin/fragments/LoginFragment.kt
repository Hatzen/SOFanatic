package de.hartz.software.stackoverflowlogin.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import de.hartz.software.stackoverflowlogin.R
import de.hartz.software.stackoverflowlogin.activities.WebViewActivity
import de.hartz.software.stackoverflowlogin.helper.Helper
import de.hartz.software.stackoverflowlogin.helper.PersistenceHelper
import de.hartz.software.stackoverflowlogin.model.User


class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupView(view)
    }

    private fun setupView(view: View) {
        val context = requireContext()

        val user = PersistenceHelper.getUser(context)
        val userView = view.findViewById<EditText>(R.id.user)
        val passwordView = view.findViewById<EditText>(R.id.pw)
        userView.setText(user.userName)
        passwordView.setText(user.password)

        view.findViewById<Button>(R.id.save).setOnClickListener {
            val userName = userView.text.toString()
            val password = passwordView.text.toString()
            val user = User(userName, password)
            PersistenceHelper.setUser(context, user)
            Helper.startService(context)
        }

        view.findViewById<Button>(R.id.showWebview).setOnClickListener {
            startActivity(Intent(context, WebViewActivity::class.java))
        }
    }

}