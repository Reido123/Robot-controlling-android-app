
package com.example.aplikacja_final

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.aplikacja_final.databinding.ActivityBocznyPanelWspolnyBinding
import com.google.android.material.navigation.NavigationView

open class BocznyPanelWspolny: AppCompatActivity() {
    //    private lateinit var binding: ActivitySecondBinding
    lateinit var toggle: ActionBarDrawerToggle
    private lateinit var binding: ActivityBocznyPanelWspolnyBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_boczny_panel_wspolny)
        setupDrawer()
        var pomoc: Button = findViewById(R.id.btnPomoc)
        pomoc.setOnClickListener {startActivity(Intent(this, HelpActivity::class.java))}
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
        val navView: NavigationView = findViewById(R.id.navigationStart)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawerlayoutboczny)
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_home -> {
                    Toast.makeText(this, "Ekran startowy", Toast.LENGTH_SHORT).show()

                }
                R.id.nav_pozycje -> {
                    Toast.makeText(this, "Pozycje", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, SecondActivity::class.java))
                }
                R.id.nav_help -> {
                    Toast.makeText(this, "Help", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HelpActivity::class.java))
                }
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
