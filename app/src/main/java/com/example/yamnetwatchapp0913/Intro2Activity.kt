package com.example.yamnetwatchapp0913

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.view.MotionEvent


class Intro2Activity : AppCompatActivity() {

    private lateinit var introDogYes: ImageView
    private lateinit var introDogNo: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro2)

        introDogYes = findViewById(R.id.introdogyes)
        introDogNo = findViewById(R.id.introdogno)

        introDogYes.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 버튼 누른 느낌 - 투명도 감소
                    introDogYes.alpha = 0.7f
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // 버튼 뗀 느낌 - 투명도 복구
                    introDogYes.alpha = 1.0f
                    // ✅ Yes 클릭: sw3, sw4 ON으로 저장
                    saveSw3Sw4State(true)
                    saveSsw6State(true)
                    saveSssw6State(true)
                    navigateToIntro3(true)
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    // 취소되면 원래대로
                    introDogYes.alpha = 1.0f
                    true
                }
                else -> false
            }
        }

        introDogNo.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    introDogNo.alpha = 0.7f
                    true
                }
                MotionEvent.ACTION_UP -> {
                    introDogNo.alpha = 1.0f
                    // ✅ No 클릭: sw3, sw4 OFF로 저장
                    saveSw3Sw4State(false)
                    saveSsw6State(false)
                    saveSssw6State(false)
                    navigateToIntro3(false)
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    introDogNo.alpha = 1.0f
                    true
                }
                else -> false
            }
        }
    }

    private fun saveSw3Sw4State(isOn: Boolean) {
        val sharedPref = getSharedPreferences("dog_settings", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("sw3_state", isOn)
            putBoolean("sw4_state", isOn)
            apply()
        }
    }

    private fun saveSsw6State(isOn: Boolean) {
        val sharedPref = getSharedPreferences("dog2_settings", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("Ssw6_state", isOn)
            apply()
        }
    }

    private fun saveSssw6State(isOn: Boolean) {
        val sharedPref = getSharedPreferences("dog3_settings", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("Sssw6_state", isOn)
            apply()
        }
    }

    private fun navigateToIntro3(hasPet: Boolean) {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, Intro3Activity::class.java)
            intent.putExtra("HAS_PET", hasPet)
            startActivity(intent)
            finish()  // ← 이거 추가! IntroActivity처럼 현재 액티비티 종료
        }, 500)
    }
}
