package com.example.yamnetwatchapp0913

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash2)


        // ✅ 백그라운드 스레드에서 초기화 (UI 블로킹 방지)
        Thread {
            clearAllSharedPreferences()
        }.start()

        Handler(Looper.getMainLooper()).postDelayed({
            // ✅ 항상 IntroActivity로 이동
            startActivity(Intent(this, IntroActivity::class.java))
            finish()
        }, 2000)  // 3초
    }

    // ✅ SharedPreferences 모두 삭제
    private fun clearAllSharedPreferences() {
        val prefs = listOf(
            "app_prefs",            // isFirstRun 플래그
            "mode_prefs",           // 모드 활성화 상태
            "dog_settings",         // sw3, sw4 상태
            "kid_settings",          // sw5 상태
            "dog2_settings",
            "kid2_settings",
            "dog3_settings",
            "kid3_settings",
            "sound_settings"

        )

        prefs.forEach { prefName ->
            val sharedPref = getSharedPreferences(prefName, MODE_PRIVATE)
            with(sharedPref.edit()) {
                clear()
                apply()
            }
        }
    }
}

