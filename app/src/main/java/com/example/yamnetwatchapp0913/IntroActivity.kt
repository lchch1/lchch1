package com.example.yamnetwatchapp0913

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class IntroActivity : AppCompatActivity() {

    private val modeState = mutableMapOf(
        R.id.introhome to false,
        R.id.introdriving to false,
        R.id.introwork to false,
        R.id.introtransit to false,
        R.id.introschool to false
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        setupModeButtons()
        setupNextButton()
    }

    private fun setupModeButtons() {
        val modeIds = listOf(
            R.id.introhome,
            R.id.introdriving,
            R.id.introwork,
            R.id.introtransit,
            R.id.introschool
        )

        modeIds.forEach { modeId ->
            findViewById<ImageView>(modeId).setOnClickListener {
                toggleMode(modeId)
            }
        }
    }

    private fun toggleMode(modeId: Int) {
        modeState[modeId] = !(modeState[modeId] ?: false)

        val imageView = findViewById<ImageView>(modeId)
        val isOn = modeState[modeId] ?: false

        when (modeId) {
            R.id.introhome -> {
                imageView.setImageResource(
                    if (isOn) R.drawable.chooseintrohome else R.drawable.introhome
                )
            }
            R.id.introdriving -> {
                imageView.setImageResource(
                    if (isOn) R.drawable.chooseintrodriving else R.drawable.introdriving
                )
            }
            R.id.introwork -> {
                imageView.setImageResource(
                    if (isOn) R.drawable.chooseintrowork else R.drawable.introwork
                )
            }
            R.id.introtransit -> {
                imageView.setImageResource(
                    if (isOn) R.drawable.chooseintrotransit else R.drawable.introtransit
                )
            }
            R.id.introschool -> {
                imageView.setImageResource(
                    if (isOn) R.drawable.chooseintroschool else R.drawable.introschool
                )
            }
        }
    }

    private fun setupNextButton() {
        findViewById<ImageView>(R.id.intronextbutton).setOnClickListener {
            // ✅ 선택된 모드 저장
            val sharedPref = getSharedPreferences("mode_prefs", MODE_PRIVATE)
            sharedPref.edit().apply {
                putBoolean("home_enabled", modeState[R.id.introhome] ?: false)
                putBoolean("driving_enabled", modeState[R.id.introdriving] ?: false)
                putBoolean("work_enabled", modeState[R.id.introwork] ?: false)
                putBoolean("transit_enabled", modeState[R.id.introtransit] ?: false)
                putBoolean("school_enabled", modeState[R.id.introschool] ?: false)
            }.apply()

            startActivity(Intent(this, Intro2Activity::class.java))
            finish()
        }
    }
}
