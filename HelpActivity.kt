package com.example.aplikacja_final

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.aplikacja_final.databinding.ActivityHelpBinding
import com.google.android.material.navigation.NavigationView

class HelpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHelpBinding
    lateinit var toggle: ActionBarDrawerToggle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDrawer()
        hideSystemUI()
    }
    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
    }
    private fun setupDrawer() {
        val navView: NavigationView = findViewById(R.id.navigationHelp)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawerlayouthelp)
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_home -> {
                    Toast.makeText(this, "Ekran główny", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, BocznyPanelWspolny::class.java))
                }
                R.id.nav_pozycje -> {
                    Toast.makeText(this, "Spis pozycji", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, SecondActivity::class.java))
                }
                R.id.nav_help -> Toast.makeText(this, "Okno wsparcia", Toast.LENGTH_SHORT).show()
                R.id.nav_sterowanie -> {
                    Toast.makeText(this, "Sterowanie", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsDelay::class.java))
                    Toast.makeText(this, "Ustawienia", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}