package com.example.yamnetwatchapp0913

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SoundSetting3Activity : AppCompatActivity() {

    private lateinit var jjinhadanHome: ImageView

    private val switchStates = mutableMapOf(
        R.id.sw1 to true,
        R.id.sw2 to true,
        R.id.sw3 to true,
        R.id.sw4 to true,
        R.id.Sssw5 to true,
        R.id.Sssw6 to true,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_soundsetting3)

        val sharedPref = getSharedPreferences("dog3_settings", MODE_PRIVATE)
        switchStates[R.id.Sssw6] = sharedPref.getBoolean("Sssw6_state", true)

        val kidPref = getSharedPreferences("kid3_settings", MODE_PRIVATE)
        switchStates[R.id.Sssw5] = kidPref.getBoolean("Sssw5_state", true)

        initializeSwitchUI()
        setupSwitchListeners()

        jjinhadanHome = findViewById(R.id.jjinhadanhome)
        jjinhadanHome.setOnClickListener {
            MainActivity.mainActivityInstance?.startDoorbellDetection()
            finish()
        }
    }

    private fun initializeSwitchUI() {
        val switchIds = listOf(R.id.sw1, R.id.sw2, R.id.sw3, R.id.sw4, R.id.Sssw5, R.id.Sssw6)
        switchIds.forEach { id ->
            val view = findViewById<ImageView>(id)
            updateSwitchImage(view, switchStates[id] ?: true)
        }
    }

    private fun setupSwitchListeners() {
        val switchIds = listOf(R.id.sw1, R.id.sw2, R.id.sw3, R.id.sw4, R.id.Sssw5, R.id.Sssw6)
        switchIds.forEach { id ->
            val view = findViewById<ImageView>(id)
            view.setOnClickListener {
                val newState = !(switchStates[id] ?: true)
                switchStates[id] = newState
                updateSwitchImage(view, newState)
                handleSwitchAction(id, newState)
            }
        }
    }

    private fun updateSwitchImage(view: ImageView, isOn: Boolean) {
        view.setImageResource(if (isOn) R.drawable.ssonswitch else R.drawable.ssoffswitch)
    }

    private fun handleSwitchAction(switchId: Int, isOn: Boolean) {
        when (switchId) {
            R.id.sw1 -> {}
            R.id.sw2 -> {}
            R.id.sw3 -> {}
            R.id.sw4 -> {}
            R.id.Sssw5 -> {}
            R.id.Sssw6 -> {}
        }
    }

    fun isSwitchOn(switchId: Int): Boolean = switchStates[switchId] ?: true

    fun getAllSwitchStates(): Map<Int, Boolean> = switchStates.toMap()
}
