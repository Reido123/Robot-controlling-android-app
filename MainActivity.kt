package com.example.aplikacja_final

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.example.aplikacja_final.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import kotlin.math.abs
import android.os.Handler
import android.os.Looper
import android.view.View
import org.json.JSONException
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.ai.client.generativeai.type.content
import kotlin.math.sqrt

class MainActivity : BocznyPanelWspolny(), Joystick.JoystickListener {
    private lateinit var handler: Handler
    private var runnable: Runnable? = null
    private var runnable_roll: Runnable? = null
    private var runnable_c1: Runnable? = null
    private var delayWczytywaniaKatow: Long = GlobalDelays.global_delayWczytywaniaKatow
    private var delaySterowaniaManipulatorem: Long = GlobalDelays.global_delaySterowaniaManipulatorem
    private var delay_json: Long = GlobalDelays.global_delay_json
    private var delay_gotJsonFromServer: Long = 500
    private var roll_delay: Long = GlobalDelays.global_roll_delay
    private var c1_delay: Long = GlobalDelays.global_delayWczytywaniaKatow
    private lateinit var manipulatorView: ManipulatorView
    private lateinit var kinematykaManipulatora: KinematykaManipulatora
    private var currentAngles = KinematykaManipulatora.Angles(Math.toRadians(-60.0), Math.toRadians(20.0))
    lateinit var joystickView: Joystick
    lateinit var joystickView2: Joystick
    private lateinit var binding: ActivityMainBinding
    lateinit var togglemain: ActionBarDrawerToggle
    private var isUpdating = false
    private var blokadaJoystickaRight = false
    var komunikacjaWifi = KomunikacjaWifi()
    private var isJoystickVisible = false
    private var joystickXPercent: Double = 0.0
    private var joystickYPercent: Double = 0.0
    private var kier: Int = 0
    private var kier_1_motor: Int = 0
    private var kierunek_roll: Int = 0
    private var roll: Int = 96
    private var ktory_silnik: Int = 0
    private var c1_dir: Int = 0
    private var c1_step: Int = 180
    private var jazda_dir: Int = 0
    private var jazda_step: Int = 0
    private var os: Int = 96

    private var stary_kat2: Double = 90.0
    private var stary_kat1: Double = -90.0
    private var chwytak = false

    enum class Mode {
        HORIZONTAL,
        VERTICAL
    }
    enum class Mode_ster {
        MANUAL,
        AUTOMATIC
    }
    private var currentModeSter = Mode_ster.AUTOMATIC
    private var currentMode = Mode.HORIZONTAL
    private var isButtonPressed = false
    private lateinit var job: Job
    private var joystickJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        manipulatorView = findViewById(R.id.manipulatorView1)
        handler = Handler(Looper.getMainLooper())

        manipulatorView.updateAngles(currentAngles.theta1, currentAngles.theta2)
        kinematykaManipulatora = KinematykaManipulatora(
            15.0,
            15.0
        ) // Jak tu zmieni dlugości, to zmienić trzeba też w pliku Joystick.kt scaledX i Y odpwiednio * (l1+l2) czyli 30 aktualnie
        binding.TVroll.text = roll.toString()+"\u00B0 "
        binding.TVkatc3.text = (180 - Math.toDegrees(currentAngles.theta2).toInt()).toString()+"\u00B0 "
        binding.TVkatc2.text = Math.toDegrees(-currentAngles.theta1).toInt().toString()+"\u00B0 "
        binding.btnLewo.setOnTouchListener { _, event ->
            handleButtonTouch(event, 1)
        }
        binding.btnPrawo.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
        binding.btnLewo.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
        binding.btnPrawo.setOnTouchListener { _, event ->
            handleButtonTouch(event, -1)
        }

        binding.btnObrotLewo.setOnTouchListener { v: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    kierunek_roll = -1
                    roll_button()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    stop_roll()
                    true
                }
                else -> false
            }
        }
        binding.btnObrotPrawo.setOnTouchListener { v: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    kierunek_roll = 1
                    roll_button()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    stop_roll()
                    true
                }
                else -> false
            }
        }
        binding.btnSterMan.setBackgroundColor(ContextCompat.getColor(this,R.color.colorPrimary))
        binding.btnSterMan.setOnClickListener {
            if (isJoystickVisible) {
                currentModeSter = Mode_ster.AUTOMATIC
                binding.RightSideLayout.visibility = View.GONE
                binding.constrAutomat.visibility = View.VISIBLE
            } else {
                currentModeSter = Mode_ster.MANUAL
                binding.RightSideLayout.visibility = View.VISIBLE
                binding.constrAutomat.visibility = View.GONE
            }

            isJoystickVisible = !isJoystickVisible
        }

        binding.btnGora.setBackgroundColor(ContextCompat.getColor(this,R.color.colorPrimary))
        binding.btnGora.setOnClickListener {
            currentMode = Mode.VERTICAL
            binding.btnGora.setBackgroundColor(Color.rgb(50,205,50))
            binding.btnDol.setBackgroundColor(ContextCompat.getColor(this,R.color.colorPrimary))
        }
        binding.btnDol.setBackgroundColor(Color.rgb(50,205,50))
        binding.btnDol.setOnClickListener {
            currentMode = Mode.HORIZONTAL
            binding.btnGora.setBackgroundColor(ContextCompat.getColor(this,R.color.colorPrimary))
            binding.btnDol.setBackgroundColor(Color.rgb(50,205,50))
        }
        joystickView = findViewById(R.id.joystick)
        joystickView2 = findViewById(R.id.joystick2)
        joystickView.setJoystickListener(object : Joystick.JoystickListener {

            override fun onJoystickMoved(xPercent: Float, yPercent: Float) {
                joystickXPercent = xPercent.toDouble()
                joystickYPercent = yPercent.toDouble()
                if (joystickJob == null || joystickJob?.isActive == false) {
                    joystickJob = CoroutineScope(Dispatchers.Main).launch {
                        while (true) {
                            sendJson()
                            Log.d("json", "15")
                            delay(delay_json)
                        }
                    }
                }
            }
            override fun onJoystickReleased() {
                joystickJob?.cancel()
                sendJson()
                Log.d("json", "14")
            }
        })
        hideSystemUI()
        joystickView2.setJoystickListener(object : Joystick.JoystickListener {
            override fun onJoystickMoved(xPercent: Float, yPercent: Float) {
                joystickXPercent = xPercent.toDouble()//joystickXPercent = xPercent.toDouble() * 100
                joystickYPercent = yPercent.toDouble()//joystickYPercent = yPercent.toDouble() * 100
                var a = 1.0
                var strefa_srodka = 5.0//35.0
                if (joystickJob == null || joystickJob?.isActive == false) {
                    joystickJob = CoroutineScope(Dispatchers.Main).launch {
                        while (true) {
                            var distanceFromCenter = sqrt(joystickXPercent * joystickXPercent + joystickYPercent * joystickYPercent)
                            Log.e("distanceFromCenter", distanceFromCenter.toString())
                            if (distanceFromCenter <= strefa_srodka) {
                                println("środek" + joystickXPercent + "\t" + joystickYPercent)
                                kier = 0
                                c1_dir = 0
                                stop_c1()
//                                c1_button()
//                                c1_step = kier * 100
                            }
                            if (joystickYPercent > a * joystickXPercent && joystickYPercent > -a * joystickXPercent &&
                                (abs(joystickXPercent) > strefa_srodka || abs(joystickYPercent) > strefa_srodka)
                            ) {
                                println("gorna" + joystickXPercent + "\t" + joystickYPercent)
                                if(currentMode == Mode.HORIZONTAL){ kier = 1}
                                else {kier = -1 }
                                performAction2(blokadaJoystickaRight)
                            }
                            if (joystickYPercent < a * joystickXPercent && joystickYPercent < -a * joystickXPercent &&
                                (abs(joystickXPercent) > strefa_srodka || abs(joystickYPercent) > strefa_srodka)
                            ) {
                                println("dolna" + joystickXPercent + "\t" + joystickYPercent)
                                if(currentMode == Mode.HORIZONTAL){ kier = -1 }
                                else { kier = 1 }
                                performAction2(blokadaJoystickaRight)
                            }
                            if (joystickYPercent > a * joystickXPercent && joystickYPercent < -a * joystickXPercent &&
                                (abs(joystickXPercent) > strefa_srodka || abs(joystickYPercent) > strefa_srodka)
                            ) {
                                println("lewa" + joystickXPercent + "\t" + joystickYPercent)
                                c1_dir = 1
//                                c1_step = c1_dir * 100
                                c1_button()
                                Log.d("uhiha", "1")
                            }
                            if (joystickYPercent < a * joystickXPercent && joystickYPercent > -a * joystickXPercent &&
                                (abs(joystickXPercent) > strefa_srodka || abs(joystickYPercent) > strefa_srodka)
                            ) {
                                c1_dir = -1
//                                c1_step = c1_dir * 100
                                c1_button()
                                println("prawa" + joystickXPercent + "\t" + joystickYPercent)
                                Log.d("uhiha", "2")
                            }
                            sendJson()
                            Log.d("json", "13")
                            delay(delay_json)
                        }
                    }
                }
            }

            override fun onJoystickReleased() {
                joystickJob?.cancel()
                stop_c1()
                sendJson()
                Log.d("json", "12")
                println("X:"+joystickXPercent+"\t\t\tY:"+joystickYPercent)
            }
        })

        setupDrawer()

        updateSwitchAppearance()
        setupSwitchListener()
    }

    private fun updateSwitchAppearance() {
        var sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        chwytak = sharedPref.getBoolean("chwytak", false)

        binding.chwytakOnoff.isChecked = chwytak
        if (chwytak) {
            binding.chwytakOnoff.setBackgroundColor(Color.rgb(50, 205, 50))
            binding.chwytakOnoff.text = "OTWARTY"
        } else {
            binding.chwytakOnoff.setBackgroundColor(Color.rgb(255, 49, 49))
            binding.chwytakOnoff.text = "ZAMKNIĘTY"
        }
    }

    private fun setupSwitchListener() {
        binding.chwytakOnoff.setOnCheckedChangeListener { buttonView, isChecked ->
            chwytak = isChecked
            Log.d("BOMBA", "7: $chwytak")

            var sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            var editor = sharedPref.edit()
            editor.putBoolean("chwytak", chwytak)
            editor.apply()

            updateSwitchAppearance()

            sendJson()
            Log.d("json", "11")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
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
        var navViewMain: NavigationView = findViewById(R.id.navigationStartMain)
        var drawerLayoutMain: DrawerLayout = findViewById(R.id.drawerlayoutbocznymain)
        togglemain = ActionBarDrawerToggle(this, drawerLayoutMain, R.string.open, R.string.close)
        drawerLayoutMain.addDrawerListener(togglemain)
        togglemain.syncState()
        navViewMain.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    saveDataToSharedPreferences()
                    startActivity(Intent(this, BocznyPanelWspolny::class.java))
                    Toast.makeText(this, "Ekran główny", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_pozycje -> {
                    saveDataToSharedPreferences()
                    startActivity(Intent(this, SecondActivity::class.java))
                    Toast.makeText(this, "Spis pozycji", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_help -> {
                    saveDataToSharedPreferences()
                    startActivity(Intent(this, HelpActivity::class.java))
                    Toast.makeText(this, "Ekran wsparcia", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_sterowanie -> {
                    Toast.makeText(this, "Sterowanie", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsDelay::class.java))
                    Toast.makeText(this, "Ustawienia", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun handleButtonTouch(event: MotionEvent, direction: Int): Boolean {

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isButtonPressed = true
                    kier_1_motor = direction
                    job = startRepeatingTask()
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isButtonPressed = false
                    kier_1_motor = 0
                    stop_c1()
                    sendJson()
                    Log.d("json", "9")
                    job.cancel()
                    return true
                }

        }
        return false
    }

    private fun startRepeatingTask(): Job {
        return lifecycleScope.launch {
            while (isButtonPressed) {
                performAction()
                delay(delaySterowaniaManipulatorem)
            }
        }
    }

    private suspend fun performAction() {
        withContext(Dispatchers.IO) {
            if (currentModeSter == Mode_ster.MANUAL) {
                sterowanie()
            }
        }
    }

    private fun performAction2(blokada:Boolean) {
        var dx = 0.1
        var dy = 0.1
        if (!blokada && currentModeSter == Mode_ster.AUTOMATIC) {
            var newAngles = when (currentMode) {
                Mode.HORIZONTAL -> kinematykaManipulatora.przemiescPunktKoncowy(dx * kier, 0.0, currentAngles)

                Mode.VERTICAL -> kinematykaManipulatora.przemiescPunktKoncowy(0.0, dy * kier, currentAngles)

            }
            if (newAngles != null) {
                currentAngles = newAngles
                lifecycleScope.launch {
                    withContext(Dispatchers.Main) {
                        stary_kat1 = Math.toDegrees(-currentAngles.theta1)
//                    stary_kat2 = (Math.toDegrees(-(currentAngles.theta1+currentAngles.theta2))+ 90.0)
                        stary_kat2 = 180.0-Math.toDegrees(currentAngles.theta2)// 180.0 - Math.toDegrees(currentAngles.theta2)
                        binding.TVkatc2.text = stary_kat1.toInt().toString()+"\u00B0 "//(Math.toDegrees(-currentAngles.theta1)).toInt().toString()
                        binding.TVkatc3.text = (stary_kat2.toInt()).toString()+"\u00B0 "//(Math.toDegrees(-(currentAngles.theta1+currentAngles.theta2))+ 90.0).toInt().toString()
                        manipulatorView.updateAngles(currentAngles.theta1, currentAngles.theta2)
                    }
                }
            }
        }
    }

    private fun saveDataToSharedPreferences() {
        var sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        var editor = sharedPref.edit()
        editor.putInt("kier", kier)
        editor.putInt("ktory_silnik", ktory_silnik)
        editor.putInt("c1_dir", c1_dir)
        editor.putInt("c1_step", c1_step)
        editor.putInt("jazda_dir", jazda_dir)
        editor.putInt("jazda_step", jazda_step)
        editor.putInt("os", os)
        editor.putInt("roll", roll)
        editor.putFloat("stary_kat2", stary_kat2.toFloat())
        editor.putFloat("stary_kat1", stary_kat1.toFloat())
        Log.d("save", "$stary_kat2")
        editor.putFloat("joystickXPercent", joystickXPercent.toFloat())
        editor.putFloat("joystickYPercent", joystickYPercent.toFloat())
        editor.putBoolean("isJoystickVisible", isJoystickVisible)
        editor.putBoolean("chwytak", chwytak)
        editor.putBoolean("isUpdating", isUpdating)
        Log.d("BOMBA", "9: $chwytak")
        editor.putBoolean("blokadaJoystickaRight", blokadaJoystickaRight)
        editor.putFloat("currentAngles.theta1", currentAngles.theta1.toFloat())
        editor.putFloat("currentAngles.theta2", currentAngles.theta2.toFloat())
        editor.apply()
    }
    private fun loadDataFromSharedPreferences() {
        var sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        delayWczytywaniaKatow = sharedPref.getLong("global_delayWczytywaniaKatow", GlobalDelays.global_delayWczytywaniaKatow)
        delay_json = sharedPref.getLong("global_delay_json", GlobalDelays.global_delay_json)
        delaySterowaniaManipulatorem = sharedPref.getLong("global_delaySterowaniaManipulatorem", GlobalDelays.global_delaySterowaniaManipulatorem)
        roll_delay = sharedPref.getLong("global_roll_delay", GlobalDelays.global_roll_delay)
        kier = sharedPref.getInt("kier", 0)
        ktory_silnik=sharedPref.getInt("ktory_silnik", 0)
        c1_dir=sharedPref.getInt("c1_dir", 0)
        c1_step=sharedPref.getInt("c1_step", 180)
        jazda_dir=sharedPref.getInt("jazda_dir", 0)
        jazda_step=sharedPref.getInt("jazda_step", 0)
        os=sharedPref.getInt("os", 96)
        roll=sharedPref.getInt("roll", 96)
        stary_kat2 = sharedPref.getFloat("stary_kat2", 90f).toDouble()
        stary_kat1 = sharedPref.getFloat("stary_kat1", -90f).toDouble()
        joystickXPercent = sharedPref.getFloat("joystickXPercent", 0f).toDouble()
        joystickYPercent = sharedPref.getFloat("joystickYPercent", 0f).toDouble()
        currentAngles.theta1 = sharedPref.getFloat("currentAngles.theta1", Math.toRadians(-90.0).toFloat()).toDouble()
        currentAngles.theta2 = sharedPref.getFloat("currentAngles.theta2", Math.toRadians(90.0).toFloat()).toDouble()
        isJoystickVisible = sharedPref.getBoolean(" isJoystickVisible", false)
        chwytak = sharedPref.getBoolean("chwytak", false)
        isUpdating = sharedPref.getBoolean("isUpdating", false)
        Log.d("BOMBA", "10: $chwytak")
        blokadaJoystickaRight = sharedPref.getBoolean("blokadaJoystickaRight", false)
        binding.TVkatc2.text = stary_kat1.toInt().toString()+"\u00B0 "
        binding.TVkatc3.text = (stary_kat2.toInt()).toString()+"\u00B0 "
        binding.TVroll.text = roll.toString()+"\u00B0 "
        manipulatorView.updateAngles(currentAngles.theta1, currentAngles.theta2)
    }

    private fun sterowanie() {

        var theta1_przeksztalcenie_ze_starykat1 = -Math.toRadians(stary_kat1)
        var theta2_przeksztalcenie_ze_starykat2 = Math.toRadians(180.0-stary_kat2)//Math.toRadians(stary_kat2)
//        var theta2_przeksztalcenie_ze_starykat2 =  Math.toRadians(stary_kat2)
        manipulatorView.updateAngles(theta1_przeksztalcenie_ze_starykat1, theta2_przeksztalcenie_ze_starykat2)
        currentAngles.theta1 = theta1_przeksztalcenie_ze_starykat1
        currentAngles.theta2 = theta2_przeksztalcenie_ze_starykat2
        var position_man = kinematykaManipulatora.kinematyka_MANUAL(currentAngles.theta1, currentAngles.theta2)

        Log.d("manual", "$position_man")
        when (wybor_silnika()) {

            1 -> {
                c1_dir =kier_1_motor
                c1_button()
                Log.d("uhiha", "3")
                sendJson()
                Log.d("json", "7")
            }

            2 -> {


                lifecycleScope.launch {
                    withContext(Dispatchers.Main) {

                            // Przyszły kąt, który chcemy ustawić
                        val przyszly_kat1 = stary_kat1 + kier_1_motor * 2

                        // Obliczamy przyszłą pozycję manipulatora przy nowym kącie
                        val przyszla_pozycja = kinematykaManipulatora.kinematyka_MANUAL(
                            Math.toRadians(-przyszly_kat1),
                            currentAngles.theta2
                        )

                        // Jeśli przyszła pozycja y jest <= 0.0, aktualizuj kąt
                        if (przyszla_pozycja.y <= 0.0) {
                            stary_kat1 = przyszly_kat1
                        } else {
                            // Nie zmieniaj kąta, jeśli y > 0.0
                            Log.d(
                                "manual",
                                "Blokujemy zmianę kąta, position_man.y > 0: ${przyszla_pozycja.y}"
                            )
                        }
                        if (stary_kat1 > 100.0) {
                            stary_kat1 = 100.0
                        }
                        if (stary_kat1 < 0.0) {
                            stary_kat1 = 0.0
                        }
                        binding.TVkatc2.text = stary_kat1.toInt().toString() + "\u00B0 "
                        binding.TVkatc3.text = (stary_kat2.toInt()).toString() + "\u00B0 "
                        Log.d("json", "::::::::: $stary_kat2 ;;; $stary_kat1")
////                        delay(delaySterowaniaManipulatorem)
                    }
                }


                Log.d("json", "$stary_kat2 ;;; $stary_kat1")
                sendJson()
                Log.d("json", "5")

            }

            3 -> {

                lifecycleScope.launch {
                    withContext(Dispatchers.Main) {
                        // Przyszły kąt, który chcemy ustawić
                        val przyszly_kat2 = stary_kat2 + kier_1_motor * 2

                        // Obliczamy przyszłą pozycję manipulatora przy nowym kącie
                        val przyszla_pozycja = kinematykaManipulatora.kinematyka_MANUAL(currentAngles.theta1, Math.toRadians(180.0 - przyszly_kat2))

                        // Jeśli przyszła wartość y jest większa niż 0.0, nie aktualizujemy kąta
                        if (przyszla_pozycja.y <= 0.0) {
                            stary_kat2 = przyszly_kat2  // Aktualizuj kąt tylko, gdy y <= 0.0
                        }
                        if (stary_kat2 > 180.0) {
                            stary_kat2 = 180.0
                        }
                        if (stary_kat2 < 10.0) {
                            stary_kat2 = 10.0
                        }
                        binding.TVkatc2.text = stary_kat1.toInt().toString()+"\u00B0 "
                        binding.TVkatc3.text = stary_kat2.toInt().toString()+"\u00B0 "
////                        delay(delaySterowaniaManipulatorem)
                    }
                }

                Log.d("json", "$stary_kat2 ;;; $stary_kat1")
                sendJson()
                Log.d("json", "4")
            }
        }
    }

    private fun wybor_silnika(): Int {
        return when (binding.radioGroup.checkedRadioButtonId) {
            R.id.rbSilnik1 -> 1
            R.id.rbSilnik2 -> 2
            R.id.rbSilnik3 -> 3
//            R.id.rbSilnik4 -> 4
            else -> 0
        }
    }

    private fun przelicznik() {
        jazda_step = -(joystickView.joystickY/ joystickView.height * 100.0 * 2 - 100.0).toInt()
        Log.e("przelicznik","os_PRZED: $os")
        os = (90 - (joystickView.joystickX / joystickView.width * 100.0 * 2 / 5 - 26.0)).toInt()
        if (os == 0) os = 96
        Log.e("przelicznik","os_PO: $os")
    }

    private fun sendJson() {
        przelicznik()
        var jsonSterowanie = JSONObject()
            .put(
                "C1", JSONObject()
                    .put("dir", c1_dir)
                    .put("step", c1_step)
            )
            .put("C2", 180-stary_kat1.toInt())
            .put("C3", stary_kat2.toInt())
            .put(
                "Ch", JSONObject()
                    .put("roll", roll)
                    .put("close", chwytak)

            )
            .put(
                "Jazda", JSONObject()
                    .put("dir", jazda_dir)
                    .put("step", jazda_step)
            )
            .put("OS", os)
            .toString()

        Log.d("WiFII", "Wysyłany JSON: $jsonSterowanie")
        if (isInternetAvailable(this)) {
            komunikacjaWifi.sendJson_wifi(jsonSterowanie)
            blokadaJoystickaRight = false
        }
        else {
            blokadaJoystickaRight = true
            Toast.makeText(this, "Lack of Internet", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getJsonFromServer() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                var jsonObject = komunikacjaWifi.getJson()

                // aktualizacja UI na wątku głównym
                withContext(Dispatchers.Main) {
                    updateUI(jsonObject)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun updateUI(jsonObject: JSONObject) {
        try {
            // aktualizacja zmiennych
            stary_kat1 = jsonObject.getDouble("C2")
            stary_kat2 = jsonObject.getDouble("C3") // 180 - stary_kat2 --> w JSON PAMIETAJ ZE TAK JEST!
            chwytak = jsonObject.getBoolean("close")
            Log.d("BOMBA", "2: $chwytak")
            c1_dir = jsonObject.getJSONObject("C1").getInt("dir")
            c1_step = jsonObject.getJSONObject("C1").getInt("step")
            roll = jsonObject.getInt("roll")
            jazda_dir = jsonObject.getJSONObject("Jazda").getInt("dir")
            jazda_step = jsonObject.getJSONObject("Jazda").getInt("dir")
            os = jsonObject.getInt("OS")

            var theta1_przeksztalcenie = -Math.toRadians(stary_kat1)
            var theta2_przeksztalcenie = Math.toRadians(180.0 - stary_kat2)//Math.toRadians(stary_kat2)

            binding.TVkatc2.text = stary_kat1.toInt().toString()+"\u00B0 "//(Math.toDegrees(-currentAngles.theta1)).toInt().toString()
            binding.TVkatc3.text = (stary_kat2.toInt()).toString()+"\u00B0 "//Math.toDegrees(-(currentAngles.theta1+currentAngles.theta2)).toInt().toString()
            currentAngles = KinematykaManipulatora.Angles(theta1_przeksztalcenie, theta2_przeksztalcenie)
            manipulatorView.updateAngles(theta1_przeksztalcenie,theta2_przeksztalcenie )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onJoystickMoved(xPercent: Float, yPercent: Float) {
    }
    override fun onJoystickReleased() {
    }
    override fun onPause() {
        super.onPause()
//        currentAngles = KinematykaManipulatora.Angles(Math.toRadians(-stary_kat1), Math.toRadians(180.0-stary_kat2))//Math.toRadians(stary_kat2))
//        manipulatorView.updateAngles(currentAngles.theta1, currentAngles.theta2)
        Log.d("BOMBA", "21: $chwytak")
        Log.e("przelicznik","os_SAVE: $os")
        saveDataToSharedPreferences()
        var sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        isUpdating = sharedPreferences.getBoolean("isUpdating", false)

//        if (isUpdating) {
//            startUpdating()
//        } else {
//            stopUpdating()
//        }
    }
    override fun onResume() {
        super.onResume()
        Log.d("BOMBA", "23: $chwytak")
//        saveDataToSharedPreferences()
        loadDataFromSharedPreferences()
        Log.e("przelicznik","os_PO_LOAD: $os")
        Log.d("BOMBA", "24: $chwytak")
        var sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        isUpdating = sharedPreferences.getBoolean("isUpdating", false)

        if (isUpdating) {
            startUpdating()
        } else {
            stopUpdating()
        }
        delayWczytywaniaKatow = GlobalDelays.global_delayWczytywaniaKatow
        delaySterowaniaManipulatorem = GlobalDelays.global_delaySterowaniaManipulatorem
        delay_json = GlobalDelays.global_delay_json
        roll_delay = GlobalDelays.global_roll_delay
        Log.d("lub", "$delayWczytywaniaKatow")
    }

    override fun onDestroy() {
        super.onDestroy()
        stopUpdating()
        Log.d("BOMBA", "4: $chwytak")
        saveDataToSharedPreferences()
    }
    private fun c1_button(){
        runnable_c1 = object: Runnable{
            override fun run(){
                c1_step += c1_dir *1
//                handler.postDelayed(this, roll_delay)

                if (c1_step < 0) {
                    c1_step = 0
                    stop_c1()
                }
                if (c1_step > 360) {
                    c1_step = 360
                    stop_c1()
                }
//                sendJson()
//                Log.d("uhiha", "4")
            }
        }
        handler.post(runnable_c1 as Runnable)
    }

    private fun stop_c1() {
        runnable_c1?.let { handler.removeCallbacks(it) }
    }
    private fun roll_button(){
        runnable_roll = object: Runnable{
            override fun run(){
                roll += kierunek_roll *2

//                handler.postDelayed(this, roll_delay)

                if (roll < 0) {
                    roll = 0
                    stop_roll()
                }
                if (roll > 180) {
                    roll = 180
                    stop_roll()
                }
                binding.TVroll.text = roll.toString()+"\u00B0"
                sendJson()
                Log.d("json", "2")
            }
        }
        handler.post(runnable_roll as Runnable)
    }

    private fun stop_roll() {
        runnable_roll?.let { handler.removeCallbacks(it) }
    }
    private fun startUpdating() {
        runnable = object : Runnable {
            override fun run() {
                Log.d("BOMBA", "isUpdating222: $isUpdating")

                if (isUpdating) {
                    updateKaty()
                    handler.postDelayed(this, delayWczytywaniaKatow)
                }
            }
        }
        handler.post(runnable!!)
    }

    private fun updateKaty() {
        blokadaJoystickaRight = true
//        loadDataFromSharedPreferences()
        var theta1_przeksztalcenie_ze_starykat1 = -Math.toRadians(stary_kat1)
        var theta2_przeksztalcenie_ze_starykat2 = Math.toRadians(180.0-stary_kat2)

        var staryKat1 = getStaryKat1()
        Log.d("BOMBA", "stary_kat1: $staryKat1")
        var staryKat2 = getStaryKat2()
        Log.d("BOMBA", "stary_kat2: $staryKat2")
        var roll_chwytaka = getRollChwytaka()
        var c1_obrot = getC1Obrot()
        var sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        var checkFlag = sharedPref.getBoolean("isUpdating", false)
        Log.d("BOMBA", "checkflag: $checkFlag")
        stary_kat1 = staryKat1.toDouble()
        stary_kat2 = staryKat2.toDouble()
        roll = roll_chwytaka
        c1_step = c1_obrot
        // Zaktualizuj widok
        binding.TVkatc2.text = staryKat1.toString()+"\u00B0 "
        binding.TVkatc3.text = staryKat2.toString()+"\u00B0 "
        binding.TVroll.text = roll_chwytaka.toString()+"\u00B0 "
        manipulatorView.updateAngles(theta1_przeksztalcenie_ze_starykat1, theta2_przeksztalcenie_ze_starykat2)
        currentAngles.theta1 = theta1_przeksztalcenie_ze_starykat1
        currentAngles.theta2 = theta2_przeksztalcenie_ze_starykat2

        if (!checkFlag) {
            stopUpdating()
            return
        }
        Log.d("json", "3")
        sendJson()

    }
    private fun stopUpdating() {
        var sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        var editor = sharedPreferences.edit()
        editor.putBoolean("isUpdating", false)
        editor.apply()
        isUpdating = sharedPreferences.getBoolean("isUpdating", false)
        blokadaJoystickaRight = false
        Log.d("BOMBA", "3: $chwytak")

        runnable?.let {
            handler.removeCallbacks(it)
        }
        chwytak = sharedPreferences.getBoolean("chwytak", false)
        updateSwitchAppearance()
        setupSwitchListener()
        saveDataToSharedPreferences()
        loadDataFromSharedPreferences()
        sendJson()
        Log.d("json", "1")
    }

    private fun getStaryKat1(): Int {
        var sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPref.getFloat("stary_kat1", 0.0f).toInt()
    }
    private fun getStaryKat2(): Int {
        var sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPref.getFloat("stary_kat2", 0.0f).toInt()
    }
    private fun getRollChwytaka(): Int {
        var sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPref.getInt("roll", 96)
    }
    private fun getC1Obrot(): Int {
        var sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPref.getInt("c1_step", 0)
    }

    fun isInternetAvailable(context: Context): Boolean {
        var connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var network = connectivityManager.activeNetwork ?: return false
            var activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            var networkInfo = connectivityManager.activeNetworkInfo ?: return false
            return networkInfo.isConnected
        }
    }
}
