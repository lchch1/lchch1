package com.example.yamnetwatchapp0913

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SoundSetting5Activity : AppCompatActivity() {

    private lateinit var jjinhadanHome: ImageView

    private val switchStates = mutableMapOf(
        R.id.sw1 to true,
        R.id.sw2 to true,
        R.id.sw3 to true,
        R.id.sw4 to true,
        R.id.Sssssw5 to true,
        R.id.Sssssw6 to true,
        R.id.Sssssw7 to true
    )

    // [P1 Fix] onPause, onDestroy 추가 - 학교 모드 설정 저장
    override fun onPause() {
        super.onPause()
        saveSwitchStates()
    }

    override fun onDestroy() {
        super.onDestroy()
        saveSwitchStates()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_soundsetting5)

        // [P1 Fix] 저장된 상태 불러오기
        loadSwitchStates()

        initializeSwitchUI()
        setupSwitchListeners()

        jjinhadanHome = findViewById(R.id.jjinhadanhome)

        jjinhadanHome.setOnClickListener {
            if (MainActivity.mainActivityInstance != null) {
                MainActivity.mainActivityInstance?.startDoorbellDetection()
            }
            finish()
        }
    }

    // [P1 Fix] 모든 스위치 상태 저장
    private fun saveSwitchStates() {
        val soundPref = getSharedPreferences("school5_settings", MODE_PRIVATE)
        with(soundPref.edit()) {
            putBoolean("sw1_state", switchStates[R.id.sw1] ?: true)
            putBoolean("sw2_state", switchStates[R.id.sw2] ?: true)
            putBoolean("sw3_state", switchStates[R.id.sw3] ?: true)
            putBoolean("sw4_state", switchStates[R.id.sw4] ?: true)
            putBoolean("Sssssw7_state", switchStates[R.id.Sssssw7] ?: true)
            apply()
        }

        val dogPref = getSharedPreferences("dog5_settings", MODE_PRIVATE)
        dogPref.edit().putBoolean("Sssssw6_state", switchStates[R.id.Sssssw6] ?: true).apply()

        val kidPref = getSharedPreferences("kid5_settings", MODE_PRIVATE)
        kidPref.edit().putBoolean("Sssssw5_state", switchStates[R.id.Sssssw5] ?: true).apply()
    }

    // [P1 Fix] 저장된 상태 불러오기
    private fun loadSwitchStates() {
        val soundPref = getSharedPreferences("school5_settings", MODE_PRIVATE)
        switchStates[R.id.sw1] = soundPref.getBoolean("sw1_state", true)
        switchStates[R.id.sw2] = soundPref.getBoolean("sw2_state", true)
        switchStates[R.id.sw3] = soundPref.getBoolean("sw3_state", true)
        switchStates[R.id.sw4] = soundPref.getBoolean("sw4_state", true)
        switchStates[R.id.Sssssw7] = soundPref.getBoolean("Sssssw7_state", true)

        val dogPref = getSharedPreferences("dog5_settings", MODE_PRIVATE)
        switchStates[R.id.Sssssw6] = dogPref.getBoolean("Sssssw6_state", true)

        val kidPref = getSharedPreferences("kid5_settings", MODE_PRIVATE)
        switchStates[R.id.Sssssw5] = kidPref.getBoolean("Sssssw5_state", true)
    }

    private fun initializeSwitchUI() {
        val switchIds = listOf(R.id.sw1, R.id.sw2, R.id.sw3, R.id.sw4, R.id.Sssssw5, R.id.Sssssw6, R.id.Sssssw7)

        switchIds.forEach { id ->
            val view = findViewById<ImageView>(id)
            val isOn = switchStates[id] ?: true
            updateSwitchImage(view, isOn)
        }
    }

    private fun setupSwitchListeners() {
        val switchIds = listOf(R.id.sw1, R.id.sw2, R.id.sw3, R.id.sw4, R.id.Sssssw5, R.id.Sssssw6, R.id.Sssssw7)

        switchIds.forEach { id ->
            val view = findViewById<ImageView>(id)
            view.setOnClickListener {
                toggleSwitch(id, view)
            }
        }
    }

    private fun toggleSwitch(switchId: Int, view: ImageView) {
        val currentState = switchStates[switchId] ?: true
        val newState = !currentState
        switchStates[switchId] = newState

        updateSwitchImage(view, newState)
        handleSwitchAction(switchId, newState)
    }

    private fun updateSwitchImage(view: ImageView, isOn: Boolean) {
        if (isOn) {
            view.setImageResource(R.drawable.ssonswitch)
        } else {
            view.setImageResource(R.drawable.ssoffswitch)
        }
    }

    private fun handleSwitchAction(switchId: Int, isOn: Boolean) {
        when (switchId) {
            R.id.sw1 -> {}
            R.id.sw2 -> {}
            R.id.sw3 -> {}
            R.id.sw4 -> {}
            R.id.Sssssw5 -> {}
            R.id.Sssssw6 -> {}
            R.id.Sssssw7 -> {}
        }
    }

    fun isSwitchOn(switchId: Int): Boolean {
        return switchStates[switchId] ?: true
    }

    fun setSwitchState(switchId: Int, isOn: Boolean) {
        switchStates[switchId] = isOn
        val view = findViewById<ImageView>(switchId)
        updateSwitchImage(view, isOn)
        handleSwitchAction(switchId, isOn)
    }

    fun getAllSwitchStates(): Map<Int, Boolean> {
        return switchStates.toMap()
    }
}
