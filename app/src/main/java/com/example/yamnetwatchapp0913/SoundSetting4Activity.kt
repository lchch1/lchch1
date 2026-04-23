package com.example.yamnetwatchapp0913

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SoundSetting4Activity : AppCompatActivity() {

    private lateinit var jjinhadanHome: ImageView

    private val switchStates = mutableMapOf(
        R.id.sw1 to true,
        R.id.sw2 to true,
        R.id.sw3 to true,
        R.id.sw4 to true,
        R.id.Ssssw5 to true,
        R.id.Ssssw6 to true,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_soundsetting4)

        initializeSwitchUI()
        setupSwitchListeners()

        jjinhadanHome = findViewById(R.id.jjinhadanhome)
        jjinhadanHome.setOnClickListener {
            MainActivity.mainActivityInstance?.startDoorbellDetection()
            finish()
        }
    }

    private fun initializeSwitchUI() {
        val switchIds = listOf(R.id.sw1, R.id.sw2, R.id.sw3, R.id.sw4, R.id.Ssssw5, R.id.Ssssw6)
        switchIds.forEach { id ->
            val view = findViewById<ImageView>(id)
            updateSwitchImage(view, switchStates[id] ?: true)
        }
    }

    private fun setupSwitchListeners() {
        val switchIds = listOf(R.id.sw1, R.id.sw2, R.id.sw3, R.id.sw4, R.id.Ssssw5, R.id.Ssssw6)
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
            R.id.Ssssw5 -> {}
            R.id.Ssssw6 -> {}
        }
    }

    fun isSwitchOn(switchId: Int): Boolean = switchStates[switchId] ?: true

    fun getAllSwitchStates(): Map<Int, Boolean> = switchStates.toMap()
}
