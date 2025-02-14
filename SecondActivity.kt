package com.example.aplikacja_final

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplikacja_final.databinding.ActivitySecondBinding
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SecondActivity : AppCompatActivity(), MyFirstAdapter.OnItemClickListener {

    private lateinit var binding: ActivitySecondBinding
    private lateinit var togglesecond: ActionBarDrawerToggle
    private lateinit var myAdapter: MyFirstAdapter
    private lateinit var itemList: MutableList<Dane>
    private var handler = Handler(Looper.getMainLooper())
    private var isIncrementing = false
    private var delay_inc_dec = GlobalDelays.global_delay_inc_dec
    private var runnable: Runnable? = null
    private var isUpdating = false
    private var kat1Reached = false
    private var kat2Reached = false
    private var rollReached = false
    private var c1Reached = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Wczytaj dane z SharedPreferences
        itemList = loadData() ?: createDane()
        myAdapter = MyFirstAdapter(itemList, this)
        setupDrawer()
        hideSystemUI()
        binding.recycleView1.layoutManager = LinearLayoutManager(this)
        binding.recycleView1.adapter = myAdapter

        binding.btnDodaj.setOnClickListener {
            var sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
            addNewRow(
                sharedPreferences.getInt("c1_step", 0),
                sharedPreferences.getFloat("stary_kat1", 0.0f).toInt(),
                sharedPreferences.getFloat("stary_kat2", 0.0f).toInt(),
                sharedPreferences.getInt("roll", 96),
                sharedPreferences.getBoolean("chwytak", false)
            )
        }

        binding.btnWczytaj.setOnClickListener {
            var selectedItem = getSelectedItem()
            if (selectedItem != null) {
                Toast.makeText(this, "Wybrano: ${selectedItem.id}", Toast.LENGTH_SHORT).show()

                var sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                var editor = sharedPreferences.edit()
                editor.putBoolean("isUpdating", true)
                editor.apply()
                MakeToPosition()
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                Toast.makeText(this, "Nie wybrano żadnego elementu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun Starykat1ReachedTarget(stary_kat1:Int, target_kat1:Int): Boolean {
        return stary_kat1 == target_kat1
    }
    private fun Starykat2ReachedTarget(stary_kat2:Int, target_kat2: Int): Boolean {
        return  stary_kat2 == target_kat2
    }
    private fun RollReachedTarget(roll: Int, target_roll: Int): Boolean {
        Log.d("BOMBA12", "RollReachedTarget - roll: $roll, target_roll: $target_roll")
        return roll == target_roll
    }
    private fun C1ReachedTarget(c1_step: Int, target_c1: Int): Boolean {
        Log.d("BOMBA12", "C1ReachedTarget - c1: $c1_step, target_c1: $target_c1")
        return c1_step == target_c1
    }

    private fun MakeToPosition() {
        var sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        var kat1 = getSelectedItem()?.c1kat
        var kat2 = getSelectedItem()?.c2kat
        var roll_docelowy = getSelectedItem()?.roll_chwyt
        var c1_docelowy = getSelectedItem()?.c1
        var staryKat1 = sharedPreferences.getFloat("stary_kat1", 90f).toInt()
        var staryKat2 = sharedPreferences.getFloat("stary_kat2", -90f).toInt()
        var roll_aktualny = sharedPreferences.getInt("roll", 96)
        var c1_aktualny = sharedPreferences.getInt("c1_step", 0)
        isUpdating = true
        kat1Reached = false
        kat2Reached = false
        rollReached = false
        c1Reached = false
        if (roll_aktualny == roll_docelowy) rollReached = true
        if (c1_aktualny == c1_docelowy) c1Reached = true
        if (kat1 == staryKat1) kat1Reached = true
        if (kat2 == staryKat2) kat2Reached = true

        if (kat1 == staryKat1 && kat2 == staryKat2 && roll_docelowy == roll_aktualny && c1_docelowy == c1_aktualny) {
            kat1Reached = true
            kat2Reached = true
            rollReached = true
            c1Reached = true
            checkAndStopUpdating()
        }
        else {
            kat1?.let {
                startUpdatingStaryKat(staryKat1, kat1, ::saveStaryKat1, ::Starykat1ReachedTarget)
            }
            kat2?.let {
                startUpdatingStaryKat(staryKat2, kat2, ::saveStaryKat2, ::Starykat2ReachedTarget)
            }
            roll_docelowy?.let {
                startUpdatingStaryKat(roll_aktualny, roll_docelowy, ::saveRoll, ::RollReachedTarget)
            }
            c1_docelowy?.let {
                startUpdatingStaryKat(c1_aktualny, c1_docelowy, ::saveC1, ::C1ReachedTarget)
            }
        }
    }

    private fun startUpdatingStaryKat(
        initialValue: Int,
        targetValue: Int,
        saveFunction: (Int) -> Unit,
        reachedTargetFunction: (Int, Int) -> Boolean
    ) {
        var currentValue = initialValue
        isIncrementing = true

        runnable = object : Runnable {
            override fun run() {
                if (!isIncrementing) {
                    Log.d("BOMBA12", "Pętla zatrzymana - isIncrementing jest false")
                    return  // Wyjście z pętli, jeśli inkrementacja została zatrzymana
                }

                if (isIncrementing && currentValue != targetValue) {
                    currentValue += if (targetValue > currentValue) 1 else -1
                    saveFunction(currentValue)
                    Log.e("KatUpdate", "Current: $currentValue, Target: $targetValue")
                    handler.postDelayed(this, delay_inc_dec.toLong())
                    Log.d("BOMBA12", "2: BOM")
                } else {
                    if (reachedTargetFunction(currentValue, targetValue)) {
                        if (saveFunction == ::saveStaryKat1) kat1Reached = true
                        if (saveFunction == ::saveStaryKat2) kat2Reached = true
                        if (saveFunction == ::saveRoll) rollReached = true
                        if (saveFunction == ::saveC1) c1Reached = true
                        Log.d("BOMBA12", "3: BOM")
                        var sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                        var pies = sharedPreferences.getInt("roll", 0)
                        Log.d("BOMBA12", "$kat1Reached i $kat2Reached i $rollReached = $pies / $targetValue / $currentValue")
                        checkAndStopUpdating()
                    }
                    checkAndStopUpdating()
                }
            }
        }
        Log.d("BOMBA12", "Czy wartości są różne? ${currentValue != targetValue}")

        if (!reachedTargetFunction(currentValue, targetValue)) {
            Log.d("BOMBA12", "reachedTargetFunction_PO: ${reachedTargetFunction(currentValue, targetValue)}")
            handler.post(runnable!!)
        }

    }

    private fun checkAndStopUpdating() {
        if (kat1Reached && kat2Reached && rollReached && c1Reached)
        {
            Log.d("BOMBA12", "1: BOM")
            stopIncrementing()
        }
    }

    private fun stopIncrementing() {
        isIncrementing = false

        runnable?.let {
            handler.removeCallbacks(it)
        }
//        isUpdating = false
        var sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        var editor = sharedPref.edit()
        editor.putBoolean("isUpdating", false)
        editor.apply()
        isUpdating = sharedPref.getBoolean("isUpdating", false)
        Log.d("BOMBA", "isUpdating_SECONDACTIVITY: $isUpdating")
        var chwytak_tabela = getSelectedItem()?.chwytak
        var chwyt_aktualny = sharedPref.getBoolean("chwytak", false)
        if (chwytak_tabela != chwyt_aktualny)
        {
            if (chwytak_tabela != null) {
                editor.putBoolean("chwytak", chwytak_tabela)
                editor.apply()
            }
        }
    }

    private fun saveStaryKat1(value: Int) {
        var sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        var editor = sharedPreferences.edit()
        editor.putFloat("stary_kat1", value.toFloat())
        editor.apply()
    }

    private fun saveStaryKat2(value: Int) {
        var sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        var editor = sharedPreferences.edit()
        editor.putFloat("stary_kat2", value.toFloat())
        editor.apply()
    }
    private fun saveRoll(value: Int) {
        var sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        var editor = sharedPreferences.edit()
        editor.putInt("roll", value)
        editor.apply()
        Log.d("BOMBA12", "saveRoll - zapisano roll: $value")
    }
    private fun saveC1(value: Int) {
        var sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        var editor = sharedPreferences.edit()
        editor.putInt("c1_step", value)
        editor.apply()
        Log.d("BOMBA12", "saveC1 - zapisano roll: $value")
    }

    override fun onDestroy() {
        super.onDestroy()
        isIncrementing = false
        runnable?.let {
            handler.removeCallbacks(it)
        }
    }

    private fun addNewRow(c1: Int, c1kat: Int, c2kat: Int, roll_chwyt: Int, chwytak: Boolean) {
//        var newRow = Dane(itemList.size + 1, c1x0, c1y0, c1kat, c2x0, c2y0, c2kat, chwytak)
        var newRow = Dane(itemList.size + 1, c1, c1kat, c2kat, roll_chwyt, chwytak)
        myAdapter.addItem(newRow)
        saveData()  // Zapisz dane po dodaniu nowego wiersza
    }

    private fun getSelectedItem(): Dane? {
        var selectedPosition = myAdapter.getSelectedPosition()
        return if (selectedPosition != RecyclerView.NO_POSITION) {
            itemList[selectedPosition]
        } else {
            null
        }
    }

    private fun saveData() {
        var sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        var editor = sharedPreferences.edit()
        var gson = Gson()
        var json = gson.toJson(itemList)
        editor.putString("itemList", json)
        editor.apply()
    }

    private fun loadData(): MutableList<Dane>? {
        var sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        var gson = Gson()
        var json = sharedPreferences.getString("itemList", null)
        var type = object : TypeToken<MutableList<Dane>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun createDane(): MutableList<Dane> = mutableListOf()

    override fun onItemClick(position: Int) {
        var selectedPosition = myAdapter.getSelectedPosition()
        if (position == selectedPosition) {
            myAdapter.removeItem(position)
            Toast.makeText(this, "Usunięto element", Toast.LENGTH_SHORT).show()
            saveData()  // Zapisz dane po usunięciu wiersza
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (togglesecond.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemSelected(position: Int) {
        // Handle item selection
    }

    private fun setupDrawer() {
        var navViewSecond: NavigationView = findViewById(R.id.navigationSecond)
        var drawerLayoutSecond: DrawerLayout = findViewById(R.id.drawerlayoutsecond)

        togglesecond = ActionBarDrawerToggle(this, drawerLayoutSecond, R.string.open, R.string.close)
        drawerLayoutSecond.addDrawerListener(togglesecond)
        togglesecond.syncState()

        navViewSecond.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, BocznyPanelWspolny::class.java))
                    Toast.makeText(this, "Ekran główny", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_pozycje -> {
                    Toast.makeText(this, "Spis Pozycji", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_help -> {
                    Toast.makeText(this, "Ekran wsparcia", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HelpActivity::class.java))
                }
                R.id.nav_sterowanie -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    Toast.makeText(this, "Sterowanie", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsDelay::class.java))
                    Toast.makeText(this, "Ustawienia", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }
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
}
