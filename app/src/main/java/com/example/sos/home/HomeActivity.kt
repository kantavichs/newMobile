//java/com/example/sos/home/HomeActivity.kt
package com.example.sos.home

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.sos.R
import com.example.sos.account.AccountFragment
import com.example.sos.message.MessageFragment
import com.example.sos.pending.PendingFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private val homeFragment = HomeFragment()
    private val pendingFragment = PendingFragment()
    private val messageFragment = MessageFragment()
    private val accountFragment = AccountFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        if (savedInstanceState == null) {
            setCurrentFragment(homeFragment)
        }

        bottomNavigation = findViewById(R.id.bottomNavigation)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> setCurrentFragment(homeFragment)
                R.id.nav_pending -> setCurrentFragment(pendingFragment)
                R.id.nav_message -> setCurrentFragment(messageFragment)
                R.id.nav_account -> setCurrentFragment(accountFragment)
            }
            true
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragment_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // ฟังก์ชันเปลี่ยน Fragment แบบป้องกันซ้ำซ้อน + Animation
    private fun setCurrentFragment(fragment: Fragment) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment != fragment) {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    android.R.anim.fade_in,  // Animation เวลาเข้า
                    android.R.anim.fade_out  // Animation เวลาออก
                )
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }
}