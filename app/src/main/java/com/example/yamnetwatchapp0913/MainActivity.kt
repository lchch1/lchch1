package com.example.yamnetwatchapp0913

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.content.Intent
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import com.example.yamnetwatchapp0913.databinding.ActivityMainBinding
import android.view.View
import android.os.Vibrator
import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private lateinit var drivingIcon: ImageView
    private lateinit var workIcon: ImageView
    private lateinit var schoolIcon: ImageView
    private lateinit var transitIcon: ImageView
    private lateinit var homeIcon: ImageView
    private lateinit var mymenuIcon: ImageView
    private lateinit var smallBlue: ImageView
    private lateinit var bigHomeIcon: ImageView
    private lateinit var bigDrivingIcon: ImageView
    private lateinit var bigWorkIcon: ImageView
    private lateinit var bigSchoolIcon: ImageView
    private lateinit var bigTransitIcon: ImageView

    private lateinit var leftTalkImageView: ImageView
    private lateinit var rightHochulImageView: ImageView
    private lateinit var wordinsik: ImageView
    private lateinit var jjinhadansetting: ImageView

    private lateinit var binding: ActivityMainBinding
    private lateinit var doorbellDetectionImage: ImageView

    private lateinit var doorbellDetectionImage1: ImageView

    private lateinit var bigMymodeIcon: ImageView

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var alarmOnIcon: ImageView
    private lateinit var alarmOffIcon: ImageView
    private lateinit var soundSettingIcon: ImageView

    private var isHomeEnabled = true
    private var isDrivingEnabled = true
    private var isWorkEnabled = true
    private var isTransitEnabled = true
    private var isSchoolEnabled = true

    private var isAlarmOn = true

    private val REQ_AUDIO = 200
    private val modelPath = "1.tflite"
    private var threshold = 0.4f

    private lateinit var classifier: AudioClassifier
    private lateinit var audioRecord: AudioRecord
    private var isDoorbellDetectionRunning = false
    private var yamnetHandler: Handler? = null
    private var classifyTask: Runnable? = null

    private var isDoorbellShowing = false

    private lateinit var babycryingDetectionImage: ImageView
    private lateinit var babycryingDetectionImage1: ImageView

    private var isBabycryingShowing = false

    private var currentDisplayedIcon: String? = null

    private lateinit var vibrator: Vibrator

    private var isDoorbellDetectionEnabled = true
    private var isBabycryingDetectionEnabled = true

    private lateinit var messageClient: MessageClient
    private val DOORBELL_PATH = "/doorbell_alert"
    private val BABY_CRY_PATH = "/baby_cry_alert"

    private fun initializeSmallBluePosition() {
        val scale = resources.displayMetrics.density

        bigHomeIcon.visibility = ImageView.GONE
        bigDrivingIcon.visibility = ImageView.GONE
        bigWorkIcon.visibility = ImageView.GONE
        bigSchoolIcon.visibility = ImageView.GONE
        bigTransitIcon.visibility = ImageView.GONE
        bigMymodeIcon.visibility = ImageView.GONE

        when {
            isHomeEnabled -> {
                moveSmallBlue((89 * scale).toInt(), (199 * scale).toInt())
                currentDisplayedIcon = "home"
                bigHomeIcon.visibility = ImageView.VISIBLE
                adjustIconSize(bigHomeIcon, 134, 147, 131, 172, scale)
            }
            isDrivingEnabled -> {
                moveSmallBlue((181 * scale).toInt(), (133 * scale).toInt())
                currentDisplayedIcon = "driving"
                bigDrivingIcon.visibility = ImageView.VISIBLE
                adjustIconSize(bigDrivingIcon, 130, 170, 138, 172, scale)
            }
            isWorkEnabled -> {
                moveSmallBlue((282 * scale).toInt(), (203 * scale).toInt())
                currentDisplayedIcon = "work"
                bigWorkIcon.visibility = ImageView.VISIBLE
                adjustIconSize(bigWorkIcon, 134, 159, 131, 172, scale)
            }
            isTransitEnabled -> {
                moveSmallBlue((103 * scale).toInt(), (303 * scale).toInt())
                currentDisplayedIcon = "transit"
                bigTransitIcon.visibility = ImageView.VISIBLE
                adjustIconSize(bigTransitIcon, 134, 182, 131, 172, scale)
            }
            isSchoolEnabled -> {
                moveSmallBlue((263 * scale).toInt(), (308 * scale).toInt())
                currentDisplayedIcon = "school"
                bigSchoolIcon.visibility = ImageView.VISIBLE
                adjustIconSize(bigSchoolIcon, 134, 147, 131, 172, scale)
            }
            else -> {
                moveSmallBlue((89 * scale).toInt(), (199 * scale).toInt())
                currentDisplayedIcon = "home"
                bigHomeIcon.visibility = ImageView.VISIBLE
                adjustIconSize(bigHomeIcon, 134, 147, 131, 172, scale)
            }
        }
    }

    private lateinit var offAlarmOffIcon: ImageView

    companion object {
        var mainActivityInstance: MainActivity? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        mainActivityInstance = this

        messageClient = Wearable.getMessageClient(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadSwitchStates()
        loadModeSettings()

        drivingIcon = findViewById(R.id.driving)
        workIcon = findViewById(R.id.work)
        schoolIcon = findViewById(R.id.school)
        transitIcon = findViewById(R.id.transit)
        homeIcon = findViewById(R.id.home)
        mymenuIcon = findViewById(R.id.mymenu)
        smallBlue = findViewById(R.id.smallblue)
        bigHomeIcon = findViewById(R.id.bighome)
        bigDrivingIcon = findViewById(R.id.bigdriving)
        bigWorkIcon = findViewById(R.id.bigwork)
        bigSchoolIcon = findViewById(R.id.bigschool)
        bigTransitIcon = findViewById(R.id.bigtransit)
        leftTalkImageView = findViewById(R.id.lefttalk)
        rightHochulImageView = findViewById(R.id.righthochul)
        jjinhadansetting = findViewById(R.id.jjinhadansetting)
        wordinsik = findViewById(R.id.wordinsik)
        doorbellDetectionImage = findViewById(R.id.doorbellDetectionImage)
        doorbellDetectionImage1 = findViewById(R.id.doorbellDetectionImage1)
        bigMymodeIcon = findViewById(R.id.bigmymode)
        alarmOnIcon = findViewById(R.id.alarmOnIcon)
        alarmOffIcon = findViewById(R.id.alarmOffIcon)
        soundSettingIcon = findViewById(R.id.soundSettingIcon)
        offAlarmOffIcon = findViewById(R.id.offAlarmOffIcon)
        babycryingDetectionImage = findViewById(R.id.babycryingDetectionImage)
        babycryingDetectionImage1 = findViewById(R.id.babycryingDetectionImage1)

        updateModeIconsState()
        initializeSmallBluePosition()

        val scale = resources.displayMetrics.density

        babycryingDetectionImage.setOnClickListener {
            hideBabycryingImage()
        }

        babycryingDetectionImage1.setOnClickListener {
            hideBabycryingImage()
        }

        homeIcon.setOnClickListener {
            if (isAlarmOn && isHomeEnabled) {
                moveSmallBlue((89 * scale).toInt(), (199 * scale).toInt())
                showBigIcon("home")
            }
        }

        drivingIcon.setOnClickListener {
            if (isAlarmOn && isDrivingEnabled) {
                moveSmallBlue((181 * scale).toInt(), (133 * scale).toInt())
                showBigIcon("driving")
            }
        }

        workIcon.setOnClickListener {
            if (isAlarmOn && isWorkEnabled) {
                moveSmallBlue((282 * scale).toInt(), (203 * scale).toInt())
                showBigIcon("work")
            }
        }

        schoolIcon.setOnClickListener {
            if (isAlarmOn && isSchoolEnabled) {
                moveSmallBlue((263 * scale).toInt(), (308 * scale).toInt())
                showBigIcon("school")
            }
        }

        transitIcon.setOnClickListener {
            if (isAlarmOn && isTransitEnabled) {
                moveSmallBlue((103 * scale).toInt(), (303 * scale).toInt())
                showBigIcon("transit")
            }
        }

        mymenuIcon.setOnClickListener {
            if (isAlarmOn) {
                moveSmallBlue((186 * scale).toInt(), (342 * scale).toInt())
                showBigIcon("mymenu")
            }
        }

        leftTalkImageView.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        rightHochulImageView.setOnClickListener {
            val intent = Intent(this, HochulActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        wordinsik.setOnClickListener {
            val intent = Intent(this, WordInsikActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        jjinhadansetting.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        doorbellDetectionImage.setOnClickListener {
            hideDoorbellImage()
        }

        doorbellDetectionImage1.setOnClickListener {
            hideDoorbellImage()
        }

        alarmOnIcon.setOnClickListener {
            alarmOnIcon.visibility = View.GONE
            alarmOffIcon.visibility = View.VISIBLE
            isAlarmOn = false

            homeIcon.setImageResource(R.drawable.offhome)
            transitIcon.setImageResource(R.drawable.offtransit)
            schoolIcon.setImageResource(R.drawable.offschool)
            mymenuIcon.setImageResource(R.drawable.offmymode)
            drivingIcon.setImageResource(R.drawable.offdriving)
            workIcon.setImageResource(R.drawable.offwork)

            bigHomeIcon.visibility = View.GONE
            bigDrivingIcon.visibility = View.GONE
            bigWorkIcon.visibility = View.GONE
            bigSchoolIcon.visibility = View.GONE
            bigTransitIcon.visibility = View.GONE
            bigMymodeIcon.visibility = View.GONE

            smallBlue.visibility = View.GONE
            offAlarmOffIcon.visibility = View.VISIBLE

            stopDoorbellDetection()
        }

        alarmOffIcon.setOnClickListener {
            alarmOffIcon.visibility = View.GONE
            alarmOnIcon.visibility = View.VISIBLE
            isAlarmOn = true

            if (isHomeEnabled) {
                homeIcon.setImageResource(R.drawable.home)
            }
            if (isDrivingEnabled) {
                drivingIcon.setImageResource(R.drawable.driving)
            }
            if (isWorkEnabled) {
                workIcon.setImageResource(R.drawable.work)
            }
            if (isTransitEnabled) {
                transitIcon.setImageResource(R.drawable.transit)
            }
            if (isSchoolEnabled) {
                schoolIcon.setImageResource(R.drawable.school)
            }

            mymenuIcon.setImageResource(R.drawable.mymenu1)

            smallBlue.visibility = View.VISIBLE
            offAlarmOffIcon.visibility = View.GONE

            if (currentDisplayedIcon != null) {
                showBigIcon(currentDisplayedIcon!!)
            } else {
                initializeSmallBluePosition()
            }

            startDoorbellDetection()
        }

        soundSettingIcon.setOnClickListener {
            val activityClass = when (currentDisplayedIcon) {
                "driving" -> SoundSetting3Activity::class.java
                "work" -> SoundSetting4Activity::class.java
                "transit" -> SoundSetting2Activity::class.java
                "school" -> SoundSetting5Activity::class.java
                "mymenu" -> SoundSetting6Activity::class.java
                else -> SoundSettingActivity::class.java
            }

            val intent = Intent(this, activityClass)
            startActivity(intent)
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            Log.d("Sound", "모드: $currentDisplayedIcon, $activityClass 진입")
        }

        if (checkMicPermission()) {
            startDoorbellDetection()
        }
    }

    private fun loadSwitchStates() {
        val soundPref = getSharedPreferences("sound_settings", MODE_PRIVATE)
        isDoorbellDetectionEnabled = soundPref.getBoolean("sw1_state", true)

        val kidPref = getSharedPreferences("kid_settings", MODE_PRIVATE)
        isBabycryingDetectionEnabled = kidPref.getBoolean("sw5_state", false)

        Log.d("SwitchStates", "sw1(초인종): $isDoorbellDetectionEnabled, sw5(아기울음): $isBabycryingDetectionEnabled")
    }

    private fun sendDoorbellAlertToWatch() {
        Thread {
            try {
                val nodeId = getConnectedNodeId()
                if (nodeId != null) {
                    messageClient.sendMessage(
                        nodeId,
                        DOORBELL_PATH,
                        "doorbell".toByteArray()
                    )
                    Log.d("MainActivity", "Watch로 초인종 알림 전송됨")
                } else {
                    Log.w("MainActivity", "연결된 Watch 없음")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Watch 알림 전송 실패", e)
            }
        }.start()
    }

    private fun sendBabyCryAlertToWatch() {
        Thread {
            try {
                val nodeId = getConnectedNodeId()
                if (nodeId != null) {
                    messageClient.sendMessage(
                        nodeId,
                        BABY_CRY_PATH,
                        "baby_cry".toByteArray()
                    )
                    Log.d("MainActivity", "Watch로 아기울음 알림 전송됨")
                } else {
                    Log.w("MainActivity", "연결된 Watch 없음")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Watch 알림 전송 실패", e)
            }
        }.start()
    }

    // [P1 Fix] Tasks.await()에 타임아웃 추가 (무한 대기 방지)
    private fun getConnectedNodeId(): String? {
        return try {
            val connectedNodes = Tasks.await(
                Wearable.getNodeClient(this).connectedNodes,
                5,
                TimeUnit.SECONDS
            )
            if (connectedNodes.isNotEmpty()) {
                connectedNodes[0].id
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Watch 연결 확인 실패", e)
            null
        }
    }

    private fun vibrateDevice(duration: Long) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(android.os.VibrationEffect.createOneShot(duration, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    fun enableDoorbellDetection() {
        isDoorbellDetectionEnabled = true
        if (!isDoorbellDetectionRunning) {
            startDoorbellDetection()
        }
        Log.d("YAMNet", "초인종 감지 활성화")
    }

    fun disableDoorbellDetection() {
        isDoorbellDetectionEnabled = false
        Log.d("YAMNet", "초인종 감지 비활성화")
    }

    fun enableBabycryingDetection() {
        isBabycryingDetectionEnabled = true
        if (!isDoorbellDetectionRunning) {
            startDoorbellDetection()
        }
        Log.d("YAMNet", "아기울음 감지 활성화")
    }

    fun disableBabycryingDetection() {
        isBabycryingDetectionEnabled = false
        Log.d("YAMNet", "아기울음 감지 비활성화")
    }

    private fun loadModeSettings() {
        val sharedPref = getSharedPreferences("mode_prefs", MODE_PRIVATE)
        isHomeEnabled = sharedPref.getBoolean("home_enabled", true)
        isDrivingEnabled = sharedPref.getBoolean("driving_enabled", true)
        isWorkEnabled = sharedPref.getBoolean("work_enabled", true)
        isTransitEnabled = sharedPref.getBoolean("transit_enabled", true)
        isSchoolEnabled = sharedPref.getBoolean("school_enabled", true)
    }

    private fun updateModeIconsState() {
        if (!isHomeEnabled) {
            homeIcon.setImageResource(R.drawable.offhome)
            homeIcon.isClickable = false
            homeIcon.isEnabled = false
        }

        if (!isDrivingEnabled) {
            drivingIcon.setImageResource(R.drawable.offdriving)
            drivingIcon.isClickable = false
            drivingIcon.isEnabled = false
        }

        if (!isWorkEnabled) {
            workIcon.setImageResource(R.drawable.offwork)
            workIcon.isClickable = false
            workIcon.isEnabled = false
        }

        if (!isTransitEnabled) {
            transitIcon.setImageResource(R.drawable.offtransit)
            transitIcon.isClickable = false
            transitIcon.isEnabled = false
        }

        if (!isSchoolEnabled) {
            schoolIcon.setImageResource(R.drawable.offschool)
            schoolIcon.isClickable = false
            schoolIcon.isEnabled = false
        }
    }

    private fun checkMicPermission(): Boolean {
        val granted = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.RECORD_AUDIO), REQ_AUDIO
            )
        }
        return granted
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_AUDIO &&
            grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("Permission", "마이크 권한 승인됨")
            startDoorbellDetection()
        }
    }

    fun startDoorbellDetection() {
        // [P0 Fix] 중복 실행 방지 가드
        if (isDoorbellDetectionRunning) return

        try {
            if (!::classifier.isInitialized) {
                classifier = AudioClassifier.createFromFile(this, modelPath)
            }
            audioRecord = classifier.createAudioRecord().apply { startRecording() }
            isDoorbellDetectionRunning = true

            yamnetHandler = Handler(Looper.getMainLooper()).also { handler ->
                classifyTask = object : Runnable {
                    override fun run() {
                        // [P0 Fix] isHomeEnabled 조건 제거 - 모든 모드에서 감지 동작
                        if (isDoorbellDetectionRunning) {
                            val tensor = classifier.createInputTensorAudio().apply {
                                load(audioRecord)
                            }
                            val results = classifier.classify(tensor)

                            val doorbellResults = results
                                .flatMap { it.categories }
                                .filter { isDoorbellSound(it.label) }
                                .filter { it.score >= threshold }

                            val top = doorbellResults.maxByOrNull { it.score }

                            val babycryingResults = results
                                .flatMap { it.categories }
                                .filter { isBabycryingSound(it.label) }
                                .filter { it.score >= threshold }

                            val babycryingTop = babycryingResults.maxByOrNull { it.score }

                            top?.let { cat ->
                                val conf = (cat.score * 100).toInt()
                                Log.d("Doorbell", "초인종 감지됨! - ${cat.label} (${conf}%)")

                                if (!isDoorbellShowing && !isBabycryingShowing && isDoorbellDetectionEnabled) {
                                    showDoorbellImage()
                                    sendDoorbellAlertToWatch()
                                }
                            }

                            babycryingTop?.let { cat ->
                                val conf = (cat.score * 100).toInt()
                                Log.d("BabyCrying", "아기 울음소리 감지됨! - ${cat.label} (${conf}%)")

                                if (!isBabycryingShowing && !isDoorbellShowing && isBabycryingDetectionEnabled) {
                                    showBabycryingImage()
                                    sendBabyCryAlertToWatch()
                                }
                            }

                            handler.postDelayed(this, 500)
                        }
                    }
                }
                handler.post(classifyTask!!)
            }

            Log.d("YAMNet", "Doorbell & BabyCrying 감지 시작됨")
        } catch (e: Exception) {
            Log.e("YAMNet", "오류: ${e.message}")
        }
    }

    private fun showBabycryingImage() {
        runOnUiThread {
            vibrateDevice(300)

            isBabycryingShowing = true
            // 현재 표시 중인 큰 아이콘 숨기기
            hideCurrentBigIcon()
            babycryingDetectionImage1.visibility = View.VISIBLE
            babycryingDetectionImage.visibility = View.VISIBLE

            alarmOnIcon.isClickable = false
            alarmOffIcon.isClickable = false
            soundSettingIcon.isClickable = false
        }
    }

    private fun hideBabycryingImage() {
        runOnUiThread {
            babycryingDetectionImage.visibility = View.GONE
            babycryingDetectionImage1.visibility = View.GONE
            // [P0 Fix] bigHomeIcon 하드코딩 → 현재 모드에 맞는 아이콘 복원
            restoreCurrentBigIcon()
            isBabycryingShowing = false
            Log.d("BabyCrying", "아기 울음소리 이미지 닫힘")

            alarmOnIcon.isClickable = true
            alarmOffIcon.isClickable = true
            soundSettingIcon.isClickable = true
        }
    }

    private fun isBabycryingSound(label: String): Boolean {
        val babycryingLabels = setOf(
            "Baby",
            "Cry",
            "Crying",
            "Infant",
            "Wail",
            "Whimper",
            "Weep"
        )
        return babycryingLabels.any { label.contains(it, ignoreCase = true) }
    }

    private fun isDoorbellSound(label: String): Boolean {
        val doorbellLabels = setOf(
            "Doorbell",
            "Bell",
            "Ring",
            "Chime",
            "Ding",
            "Dinging",
            "Knock"
        )
        return doorbellLabels.any { label.contains(it, ignoreCase = true) }
    }

    fun stopDoorbellDetection() {
        isDoorbellDetectionRunning = false
        classifyTask?.let { yamnetHandler?.removeCallbacks(it) }
        if (::audioRecord.isInitialized) {
            try {
                audioRecord.stop()
                audioRecord.release()
            } catch (e: Exception) {
                Log.e("YAMNet", "오디오 정지 오류: ${e.message}")
            }
        }
        Log.d("YAMNet", "Doorbell 감지 중지됨")
    }

    private fun moveSmallBlue(left: Int, top: Int) {
        val params = smallBlue.layoutParams as android.widget.RelativeLayout.LayoutParams
        params.leftMargin = left
        params.topMargin = top
        smallBlue.layoutParams = params
    }

    private fun showBigIcon(iconType: String) {
        val scale = resources.displayMetrics.density

        bigHomeIcon.visibility = ImageView.GONE
        bigDrivingIcon.visibility = ImageView.GONE
        bigWorkIcon.visibility = ImageView.GONE
        bigSchoolIcon.visibility = ImageView.GONE
        bigTransitIcon.visibility = ImageView.GONE
        bigMymodeIcon.visibility = ImageView.GONE

        currentDisplayedIcon = iconType

        when(iconType) {
            "home" -> {
                bigHomeIcon.visibility = ImageView.VISIBLE
                adjustIconSize(bigHomeIcon, 134, 147, 131, 172, scale)
            }
            "driving" -> {
                bigDrivingIcon.visibility = ImageView.VISIBLE
                adjustIconSize(bigDrivingIcon, 130, 170, 138, 172, scale)
            }
            "work" -> {
                bigWorkIcon.visibility = ImageView.VISIBLE
                adjustIconSize(bigWorkIcon, 134, 159, 131, 172, scale)
            }
            "school" -> {
                bigSchoolIcon.visibility = ImageView.VISIBLE
                adjustIconSize(bigSchoolIcon, 134, 147, 131, 172, scale)
            }
            "transit" -> {
                bigTransitIcon.visibility = ImageView.VISIBLE
                adjustIconSize(bigTransitIcon, 134, 182, 131, 172, scale)
            }
            "mymenu" -> {
                bigMymodeIcon.visibility = ImageView.VISIBLE
                adjustIconSize(bigMymodeIcon, 138, 145, 131, 170, scale)
            }
        }
    }

    private fun adjustIconSize(imageView: ImageView, widthDp: Int, heightDp: Int, leftDp: Int, topDp: Int, scale: Float) {
        val params = imageView.layoutParams as android.widget.RelativeLayout.LayoutParams
        params.width = (widthDp * scale).toInt()
        params.height = (heightDp * scale).toInt()
        params.leftMargin = (leftDp * scale).toInt()
        params.topMargin = (topDp * scale).toInt()
        imageView.layoutParams = params
    }

    // 현재 표시 중인 큰 아이콘을 숨김 (감지 팝업 표시 전)
    private fun hideCurrentBigIcon() {
        bigHomeIcon.visibility = View.GONE
        bigDrivingIcon.visibility = View.GONE
        bigWorkIcon.visibility = View.GONE
        bigSchoolIcon.visibility = View.GONE
        bigTransitIcon.visibility = View.GONE
        bigMymodeIcon.visibility = View.GONE
    }

    // [P0 Fix] 감지 팝업 닫힐 때 현재 모드에 맞는 아이콘 복원
    private fun restoreCurrentBigIcon() {
        val scale = resources.displayMetrics.density
        when (currentDisplayedIcon) {
            "home" -> {
                bigHomeIcon.visibility = View.VISIBLE
                adjustIconSize(bigHomeIcon, 134, 147, 131, 172, scale)
            }
            "driving" -> {
                bigDrivingIcon.visibility = View.VISIBLE
                adjustIconSize(bigDrivingIcon, 130, 170, 138, 172, scale)
            }
            "work" -> {
                bigWorkIcon.visibility = View.VISIBLE
                adjustIconSize(bigWorkIcon, 134, 159, 131, 172, scale)
            }
            "school" -> {
                bigSchoolIcon.visibility = View.VISIBLE
                adjustIconSize(bigSchoolIcon, 134, 147, 131, 172, scale)
            }
            "transit" -> {
                bigTransitIcon.visibility = View.VISIBLE
                adjustIconSize(bigTransitIcon, 134, 182, 131, 172, scale)
            }
            "mymenu" -> {
                bigMymodeIcon.visibility = View.VISIBLE
                adjustIconSize(bigMymodeIcon, 138, 145, 131, 170, scale)
            }
            else -> {
                bigHomeIcon.visibility = View.VISIBLE
                adjustIconSize(bigHomeIcon, 134, 147, 131, 172, scale)
            }
        }
    }

    private fun showDoorbellImage() {
        runOnUiThread {
            vibrateDevice(500)

            isDoorbellShowing = true
            // 현재 표시 중인 큰 아이콘 숨기기
            hideCurrentBigIcon()
            doorbellDetectionImage1.visibility = View.VISIBLE
            doorbellDetectionImage.visibility = View.VISIBLE

            alarmOnIcon.isClickable = false
            alarmOffIcon.isClickable = false
            soundSettingIcon.isClickable = false
        }
    }

    private fun hideDoorbellImage() {
        runOnUiThread {
            doorbellDetectionImage.visibility = View.GONE
            doorbellDetectionImage1.visibility = View.GONE
            // [P0 Fix] binding.bighome 하드코딩 → 현재 모드에 맞는 아이콘 복원
            restoreCurrentBigIcon()
            isDoorbellShowing = false
            Log.d("Doorbell", "초인종 이미지 닫힘")

            alarmOnIcon.isClickable = true
            alarmOffIcon.isClickable = true
            soundSettingIcon.isClickable = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivityInstance = null
        stopDoorbellDetection()
        handler.removeCallbacksAndMessages(null)
    }
}
