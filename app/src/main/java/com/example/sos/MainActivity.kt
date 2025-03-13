// java/com/example/sos/MainActivity.kt
package com.example.sos

import android.os.Bundle
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import com.example.sos.login.LoginFragment
import com.example.sos.register.Register1Fragment
import com.google.firebase.FirebaseApp
import java.util.Locale
import android.content.res.Configuration

class MainActivity : AppCompatActivity() {

    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        val config = resources.configuration
        val locales = ConfigurationCompat.getLocales(config)
        if (locales.isEmpty || (locales[0] != Locale.ENGLISH && locales[0]?.language != "th")) {
            val newConfig = Configuration(config)
            val newLocales = LocaleListCompat.create(Locale("th"), Locale.ENGLISH)
            ConfigurationCompat.setLocales(newConfig, newLocales)
            resources.updateConfiguration(newConfig, resources.displayMetrics)
        }

        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)

        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)

        // Load LoginFragment initially
        loadFragment(LoginFragment())

        loginButton.setOnClickListener {
            // Check if LoginFragment is already in backstack
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack(LoginFragment::class.java.simpleName, 0)
            } else {
                loadFragment(LoginFragment())
            }
            loginButton.isEnabled = false
            loginButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.myred)
            registerButton.isEnabled = true
            registerButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.mylightred)
        }

        registerButton.setOnClickListener {
            loadFragment(Register1Fragment())
            registerButton.isEnabled = false
            registerButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.myred)
            loginButton.isEnabled = true
            loginButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.mylightred)
        }

        // Handle back button press with OnBackPressedCallback
        handleBackPress()
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(fragment::class.java.simpleName) // Use fragment's class name as tag
        transaction.commit()
    }

    // Add this method to handle back press with the new API
    private fun handleBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (supportFragmentManager.backStackEntryCount > 1) {
                    supportFragmentManager.popBackStack()

                    // Update button states based on current fragment
                    val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                    if (currentFragment is LoginFragment) {
                        loginButton.isEnabled = false
                        loginButton.backgroundTintList = ContextCompat.getColorStateList(this@MainActivity, R.color.myred)
                        registerButton.isEnabled = true
                        registerButton.backgroundTintList = ContextCompat.getColorStateList(this@MainActivity, R.color.mylightred)
                    } else if (currentFragment is Register1Fragment) {
                        registerButton.isEnabled = false
                        registerButton.backgroundTintList = ContextCompat.getColorStateList(this@MainActivity, R.color.myred)
                        loginButton.isEnabled = true
                        loginButton.backgroundTintList = ContextCompat.getColorStateList(this@MainActivity, R.color.mylightred)
                    }
                } else {
                    finish()
                }
            }
        })
    }
}