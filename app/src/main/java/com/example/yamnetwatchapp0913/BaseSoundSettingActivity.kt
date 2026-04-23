package com.example.yamnetwatchapp0913

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

/**
 * SoundSettingActivity 1~6의 공통 로직을 추상화한 베이스 클래스.
 * 각 SoundSettingActivity는 이 클래스를 상속하여 prefsName만 오버라이드하면 됩니다.
 *
 * 사용법:
 *   class SoundSettingXActivity : BaseSoundSettingActivity() {
 *       override val prefsName = "soundX_settings"
 *       override val switchIds = listOf(R.id.sw1, ...)
 *       override val layoutResId = R.layout.activity_soundsettingX
 *   }
 */
abstract class BaseSoundSettingActivity : AppCompatActivity() {

    abstract val layoutResId: Int
    abstract val switchIds: List<Int>
    abstract val prefsName: String

    protected val switchStates = mutableMapOf<Int, Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutResId)

        switchIds.forEach { switchStates[it] = true }

        loadSwitchStates()
        initializeSwitchUI()
        setupSwitchListeners()

        val homeBtn = findViewById<ImageView>(R.id.jjinhadanhome)
        homeBtn?.setOnClickListener {
            MainActivity.mainActivityInstance?.startDoorbellDetection()
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        saveSwitchStates()
    }

    override fun onDestroy() {
        super.onDestroy()
        saveSwitchStates()
    }

    protected open fun loadSwitchStates() {
        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        switchIds.forEach { id ->
            switchStates[id] = prefs.getBoolean("sw_${id}", true)
        }
    }

    protected open fun saveSwitchStates() {
        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        val editor = prefs.edit()
        switchIds.forEach { id ->
            editor.putBoolean("sw_${id}", switchStates[id] ?: true)
        }
        editor.apply()
    }

    private fun initializeSwitchUI() {
        switchIds.forEach { id ->
            val view = findViewById<ImageView>(id) ?: return@forEach
            updateSwitchImage(view, switchStates[id] ?: true)
        }
    }

    private fun setupSwitchListeners() {
        switchIds.forEach { id ->
            val view = findViewById<ImageView>(id) ?: return@forEach
            view.setOnClickListener {
                val newState = !(switchStates[id] ?: true)
                switchStates[id] = newState
                updateSwitchImage(view, newState)
                onSwitchChanged(id, newState)
            }
        }
    }

    private fun updateSwitchImage(view: ImageView, isOn: Boolean) {
        if (isOn) {
            view.setImageResource(R.drawable.ssonswitch)
        } else {
            view.setImageResource(R.drawable.ssoffswitch)
        }
    }

    protected open fun onSwitchChanged(switchId: Int, isOn: Boolean) {}
}
