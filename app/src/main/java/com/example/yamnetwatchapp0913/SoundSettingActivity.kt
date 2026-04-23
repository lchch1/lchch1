package com.example.yamnetwatchapp0913

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

import android.util.Log




class SoundSettingActivity : AppCompatActivity() {



    private lateinit var jjinhadanHome: ImageView


    // 각 스위치의 ON/OFF 상태를 저장
    private val switchStates = mutableMapOf(
        R.id.sw1 to true,   // 초기값: ON
        R.id.sw2 to true,  // 초기값: OFF
        R.id.sw3 to true,   // 초기값: ON
        R.id.sw4 to true,   // 초기값: off
        R.id.sw5 to true,  // 초기값: on
        R.id.sw6 to true,   // 초기값: ON
        R.id.sw7 to true    // 초기값: ON
    )

    override fun onDestroy() {
        super.onDestroy()
        // ✅ Activity 종료 시에도 저장
        saveSwitchStates()
    }


    override fun onPause() {
        super.onPause()
        saveSwitchStates()
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_soundsetting)


        // ✅ sw1, sw2 불러오기
        val soundPref = getSharedPreferences("sound_settings", MODE_PRIVATE)
        val sw1State = soundPref.getBoolean("sw1_state", true)
        val sw2State = soundPref.getBoolean("sw2_state", false)
        val sw6State = soundPref.getBoolean("sw6_state", true)    // ✅ sw6 불러오기
        val sw7State = soundPref.getBoolean("sw7_state", true)    // ✅ sw7 불러오기


        switchStates[R.id.sw1] = sw1State
        switchStates[R.id.sw2] = sw2State
        switchStates[R.id.sw6] = sw6State
        switchStates[R.id.sw7] = sw7State


        // ✅ SharedPreferences에서 sw3, sw4 상태 불러오기
        val sharedPref = getSharedPreferences("dog_settings", MODE_PRIVATE)
        val sw3State = sharedPref.getBoolean("sw3_state", true)   // 기본값: ON
        val sw4State = sharedPref.getBoolean("sw4_state", true)   // 기본값: ON

        // ✅ switchStates에 적용
        switchStates[R.id.sw3] = sw3State
        switchStates[R.id.sw4] = sw4State

        val kidPref = getSharedPreferences("kid_settings", MODE_PRIVATE)
        val sw5State = kidPref.getBoolean("sw5_state", true)

        switchStates[R.id.sw5] = sw5State




        // 초기 UI 설정
        initializeSwitchUI()

        // 스위치 클릭 리스너 설정
        setupSwitchListeners()

        jjinhadanHome = findViewById(R.id.jjinhadanhome)  // ✅ 추가

        // ✅ 홈 버튼 클릭 리스너
        jjinhadanHome.setOnClickListener {
            if (MainActivity.mainActivityInstance != null) {
                MainActivity.mainActivityInstance?.startDoorbellDetection()
            }
            finish()
        }
    }


    private fun saveSwitchStates() {
        // ✅ sw1, sw2 저장 (새로운 SharedPreferences)
        val soundPref = getSharedPreferences("sound_settings", MODE_PRIVATE)
        with(soundPref.edit()) {
            putBoolean("sw1_state", switchStates[R.id.sw1] ?: true)
            putBoolean("sw2_state", switchStates[R.id.sw2] ?: false)
            putBoolean("sw6_state", switchStates[R.id.sw6] ?: true)    // ✅ sw6 저장
            putBoolean("sw7_state", switchStates[R.id.sw7] ?: true)    // ✅ sw7 저장
            apply()
        }

        // ✅ sw3, sw4 저장 (dog_settings)
        val dogPref = getSharedPreferences("dog_settings", MODE_PRIVATE)
        with(dogPref.edit()) {
            putBoolean("sw3_state", switchStates[R.id.sw3] ?: true)
            putBoolean("sw4_state", switchStates[R.id.sw4] ?: true)
            apply()
        }

        // ✅ sw5만 저장 (kid_settings)
        val kidPref = getSharedPreferences("kid_settings", MODE_PRIVATE)
        kidPref.edit().apply {
            putBoolean("sw5_state", switchStates[R.id.sw5] ?: false)
            apply()
        }


        Log.d("SaveSwitch", "sw1: ${switchStates[R.id.sw1]}, sw5: ${switchStates[R.id.sw5]}")

    }




    /**
     * 초기 상태에 따라 스위치 이미지 설정
     */
    private fun initializeSwitchUI() {
        val switchIds = listOf(R.id.sw1, R.id.sw2, R.id.sw3, R.id.sw4, R.id.sw5, R.id.sw6, R.id.sw7)

        switchIds.forEach { id ->
            val view = findViewById<ImageView>(id)
            val isOn = switchStates[id] ?: true
            updateSwitchImage(view, isOn)
        }
    }

    /**
     * 모든 스위치에 클릭 리스너 설정
     */
    private fun setupSwitchListeners() {
        val switchIds = listOf(R.id.sw1, R.id.sw2, R.id.sw3, R.id.sw4, R.id.sw5, R.id.sw6, R.id.sw7)

        switchIds.forEach { id ->
            val view = findViewById<ImageView>(id)
            view.setOnClickListener {
                toggleSwitch(id, view)
            }
        }
    }

    /**
     * 특정 스위치를 토글 (ON <-> OFF)
     */
    private fun toggleSwitch(switchId: Int, view: ImageView) {
        // 현재 상태 토글
        val currentState = switchStates[switchId] ?: true
        val newState = !currentState
        switchStates[switchId] = newState

        // UI 업데이트
        updateSwitchImage(view, newState)

        // 각 스위치별 동작 처리 (필요에 따라 추가)
        handleSwitchAction(switchId, newState)
    }

    /**
     * 스위치 상태에 따라 이미지 업데이트
     */
    private fun updateSwitchImage(view: ImageView, isOn: Boolean) {
        if (isOn) {
            view.setImageResource(R.drawable.ssonswitch)
        } else {
            view.setImageResource(R.drawable.ssoffswitch)
        }
    }

    /**
     * 각 스위치별 실제 동작 처리
     * (예: 알림음 ON/OFF, 진동 ON/OFF 등)
     */
    private fun handleSwitchAction(switchId: Int, isOn: Boolean) {
        when (switchId) {
            R.id.sw1 -> {
                // 스위치 1 동작
                if (isOn) {
                    // ON 상태 처리
                } else {
                    // OFF 상태 처리
                }
            }
            R.id.sw2 -> {
                // 스위치 2 동작
            }
            R.id.sw3 -> {
                // 스위치 3 동작
            }
            R.id.sw4 -> {
                // 스위치 4 동작
            }
            R.id.sw5 -> {
                if (isOn) {
                    MainActivity.mainActivityInstance?.enableBabycryingDetection()
                } else {
                    MainActivity.mainActivityInstance?.disableBabycryingDetection()
                }
            }
            R.id.sw6 -> {
                // 스위치 6 동작
            }
            R.id.sw7 -> {
                // 스위치 7 동작
            }
        }
    }

    /**
     * 특정 스위치의 현재 상태 조회
     */
    fun isSwitchOn(switchId: Int): Boolean {
        return switchStates[switchId] ?: true
    }

    /**
     * 특정 스위치의 상태 직접 설정 (외부에서 프로그래밍으로 제어할 때)
     */
    fun setSwitchState(switchId: Int, isOn: Boolean) {
        switchStates[switchId] = isOn
        val view = findViewById<ImageView>(switchId)
        updateSwitchImage(view, isOn)
        handleSwitchAction(switchId, isOn)
    }

    /**
     * 모든 스위치 상태 조회 (필요시 SharedPreferences 저장 등에 사용)
     */
    fun getAllSwitchStates(): Map<Int, Boolean> {
        return switchStates.toMap()
    }
}

