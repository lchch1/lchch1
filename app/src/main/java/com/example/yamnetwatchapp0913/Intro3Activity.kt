package com.example.yamnetwatchapp0913

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.view.MotionEvent

class Intro3Activity : AppCompatActivity() {

    private lateinit var kidYes: ImageView
    private lateinit var kidNo: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro3)

        kidYes = findViewById(R.id.kidyes)
        kidNo = findViewById(R.id.kidno)

        kidYes.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 버튼 누른 느낌 - 투명도 감소
                    kidYes.alpha = 0.7f
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // 버튼 뗀 느낌 - 투명도 복구
                    kidYes.alpha = 1.0f
                    // ✅ Yes 클릭: sw5 ON으로 저장
                    saveSw5State(true)
                    saveSsw5State(true)
                    saveSssw5State(true)
                    navigateToIntro4(true)
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    // 취소되면 원래대로
                    kidYes.alpha = 1.0f
                    true
                }
                else -> false
            }
        }

        kidNo.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    kidNo.alpha = 0.7f
                    true
                }
                MotionEvent.ACTION_UP -> {
                    kidNo.alpha = 1.0f
                    // ✅ No 클릭: sw5 OFF로 저장
                    saveSw5State(false)
                    saveSsw5State(false)
                    saveSssw5State(false)
                    navigateToIntro4(false)
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    kidNo.alpha = 1.0f
                    true
                }
                else -> false
            }
        }
    }

    // ✅ sw5 상태 저장
    private fun saveSw5State(isOn: Boolean) {
        val sharedPref = getSharedPreferences("kid_settings", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("sw5_state", isOn)
            apply()
        }
    }

    private fun saveSsw5State(isOn: Boolean) {
        val sharedPref = getSharedPreferences("kid2_settings", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("Ssw5_state", isOn)
            apply()
        }
    }

    private fun saveSssw5State(isOn: Boolean) {
        val sharedPref = getSharedPreferences("kid3_settings", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("Sssw5_state", isOn)
            apply()
        }
    }

    private fun navigateToIntro4(hasKid: Boolean) {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, Intro4Activity::class.java)
            intent.putExtra("HAS_KID", hasKid)
            startActivity(intent)
            finish()  // ← 이거 추가!
        }, 500)
    }
}
