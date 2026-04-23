package com.example.yamnetwatchapp0913

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale
import android.view.LayoutInflater
import android.view.View
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class ChatActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var btnSpeak: Button
    private lateinit var btnRecordVoice: Button
    private lateinit var btnBack: ImageView
    private lateinit var btnSave: Button
    private lateinit var btnList: Button
    private lateinit var btnUnderstand: Button
    private lateinit var guidanceOverlay: FrameLayout

    private var guidanceTTS: TextToSpeech? = null

    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<ChatMessage>()

    private lateinit var textToSpeech: TextToSpeech
    private var speechRecognizer: SpeechRecognizer? = null
    private lateinit var speechIntent: Intent

    private var isRecording = false
    private var lastMyMessage: String = ""
    private var isTTSInitialized = false

    private val gson = Gson()

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private const val TAG = "ChatActivity_DEBUG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        Log.d(TAG, "=== ChatActivity 시작 ===")

        checkAndRequestPermissions()
        initializeViews()
        setupDiagnosticButtons()

        guidanceOverlay = findViewById(R.id.guidanceOverlay)
        btnUnderstand = findViewById(R.id.btnUnderstand)

        initGuidanceTTS()

        if (guidanceOverlay.visibility == View.VISIBLE) {
            playGuidanceTTS()
        }

        btnUnderstand.setOnClickListener {
            guidanceOverlay.visibility = View.GONE
            if (guidanceTTS?.isSpeaking == true) {
                guidanceTTS?.stop()
            }
        }

        if (MainActivity.mainActivityInstance != null) {
            MainActivity.mainActivityInstance?.stopDoorbellDetection()
            Log.d(TAG, "ChatActivity 진입: YAMNet 자동 중지")
        }

        initTextToSpeech()
        initializeSpeechIntent()
        setupRecyclerView()
        setupButtonListeners()
    }

    private fun initGuidanceTTS() {
        guidanceTTS = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val langResult = guidanceTTS?.setLanguage(Locale.KOREAN)

                if (langResult == TextToSpeech.LANG_MISSING_DATA ||
                    langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "한국어 TTS 지원 안 됨")
                } else {
                    Log.d("TTS", "TTS 초기화 성공")
                    playGuidanceTTS()
                }
            } else {
                Log.e("TTS", "TTS 초기화 실패")
            }
        }
    }

    private fun playGuidanceTTS() {
        val guidanceText = "저는 청각장애인입니다. " +
                "제가 소리를 들을 수 없는 상황이라 " +
                "여기서 저와 대화해주시면 감사드리겠습니다."

        guidanceTTS?.let { tts ->
            if (tts.isSpeaking) {
                tts.stop()
            }

            tts.setPitch(1.0f)
            tts.setSpeechRate(1.0f)

            val result = tts.speak(guidanceText, TextToSpeech.QUEUE_FLUSH, null, "guidanceID")

            if (result == TextToSpeech.SUCCESS) {
                Log.d("TTS", "TTS 재생 시작!")
            } else {
                Log.e("TTS", "TTS 재생 실패")
            }
        } ?: run {
            Log.e("TTS", "TTS가 초기화되지 않음")
        }
    }


    private fun initializeViews() {
        btnBack = findViewById(R.id.btnBack)
        guidanceOverlay = findViewById(R.id.guidanceOverlay)
        btnUnderstand = findViewById(R.id.btnUnderstand)
        recyclerView = findViewById(R.id.recyclerView)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        btnSpeak = findViewById(R.id.btnSpeak)
        btnRecordVoice = findViewById(R.id.btnRecordVoice)
        btnSave = findViewById(R.id.btnSave)
        btnList = findViewById(R.id.btnList)

        Log.d(TAG, "모든 View 초기화 완료")
    }


    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messageList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatAdapter
    }

    private fun setupButtonListeners() {
        btnBack.setOnClickListener {
            if (MainActivity.mainActivityInstance != null) {
                MainActivity.mainActivityInstance?.startDoorbellDetection()
                Log.d("YAMNet", "ChatActivity 종료: YAMNet 자동 재시작")
            }
            finish()
        }

        btnUnderstand.setOnClickListener { guidanceOverlay.visibility = android.view.View.GONE }
        btnSend.setOnClickListener { sendMessage() }
        btnSpeak.setOnClickListener { replayMessage() }
        btnRecordVoice.setOnClickListener { toggleVoiceRecording() }
        btnSave.setOnClickListener { showSaveDialog() }
        btnList.setOnClickListener { startActivity(Intent(this, ConversationListActivity::class.java)) }
    }

    private fun setupDiagnosticButtons() {
        btnSpeak.setOnLongClickListener {
            showDiagnosticDialog()
            true
        }

        btnRecordVoice.setOnLongClickListener {
            showDiagnosticDialog()
            true
        }
    }

    private fun showDiagnosticDialog() {
        val diagnosticInfo = """
            === 진단 정보 ===
            TTS 초기화: $isTTSInitialized
            음성 인식 가능: ${SpeechRecognizer.isRecognitionAvailable(this)}
            마이크 권한: ${ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED}

            === 테스트 ===
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("진단 메뉴")
            .setMessage(diagnosticInfo)
            .setPositiveButton("TTS 테스트") { _, _ ->
                testTTS()
            }
            .setNegativeButton("STT 테스트") { _, _ ->
                testSTT()
            }
            .setNeutralButton("닫기", null)
            .show()
    }

    private fun testTTS() {
        Log.d(TAG, "TTS 테스트 시작")

        if (isTTSInitialized) {
            try {
                textToSpeech.speak("테스트입니다", TextToSpeech.QUEUE_FLUSH, null, null)
                Log.d(TAG, "TTS 재생 성공")
            } catch (e: Exception) {
                Log.e(TAG, "TTS 재생 실패: ${e.message}")
            }
        } else {
            Log.e(TAG, "TTS 초기화되지 않음")
        }
    }

    private fun testSTT() {
        Log.d(TAG, "STT 테스트 시작")

        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            try {
                speechRecognizer?.destroy()

                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
                speechRecognizer?.setRecognitionListener(testSpeechListener)
                speechRecognizer?.startListening(speechIntent)
                Log.d(TAG, "STT 시작 성공")
            } catch (e: Exception) {
                Log.e(TAG, "STT 시작 실패: ${e.message}")
            }
        } else {
            Log.e(TAG, "음성 인식 지원 안 함")
        }
    }

    private val testSpeechListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d(TAG, "STT onReadyForSpeech")
        }
        override fun onBeginningOfSpeech() {
            Log.d(TAG, "STT onBeginningOfSpeech")
        }
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray) {}
        override fun onEndOfSpeech() {
            Log.d(TAG, "STT onEndOfSpeech")
        }
        override fun onError(error: Int) {
            Log.e(TAG, "STT onError: $error")
        }
        override fun onResults(results: Bundle) {
            val list = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            Log.d(TAG, "STT onResults: $list")
        }
        override fun onPartialResults(partialResults: Bundle) {
            val list = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            Log.d(TAG, "STT onPartialResults: $list")
        }
        override fun onEvent(eventType: Int, params: Bundle) {}
    }

    private fun sendMessage() {
        val messageText = etMessage.text.toString().trim()
        if (messageText.isNotEmpty()) {
            val myMessage = ChatMessage(messageText, isMine = true)
            chatAdapter.addMessage(myMessage)
            lastMyMessage = messageText
            etMessage.text.clear()
            recyclerView.scrollToPosition(messageList.size - 1)

            if (isTTSInitialized) {
                playTTS(messageText)
            }
        }
    }

    private fun replayMessage() {
        if (lastMyMessage.isNotEmpty()) {
            if (isTTSInitialized) {
                playTTS(lastMyMessage)
            }
        }
    }

    private fun toggleVoiceRecording() {
        if (!isRecording) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
                checkAndRequestPermissions()
                return
            }

            if (SpeechRecognizer.isRecognitionAvailable(this)) {
                startSpeech()
                btnRecordVoice.text = "⏹"
                btnRecordVoice.alpha = 0.7f
                isRecording = true
            }
        } else {
            stopSpeech()
            btnRecordVoice.text = "🎤"
            btnRecordVoice.alpha = 1.0f
            isRecording = false
        }
    }

    private fun initTextToSpeech() {
        Log.d(TAG, "TTS 초기화 시작...")
        textToSpeech = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        Log.d(TAG, "onInit 호출됨 - status: $status")

        if (status == TextToSpeech.SUCCESS) {
            Log.d(TAG, "TTS 초기화 성공")

            val langResult = textToSpeech.setLanguage(Locale.KOREAN)
            Log.d(TAG, "setLanguage 결과: $langResult")

            when (langResult) {
                TextToSpeech.LANG_MISSING_DATA -> {
                    Log.w(TAG, "한국어 데이터 없음")
                    startActivity(Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA))
                }
                TextToSpeech.LANG_NOT_SUPPORTED -> {
                    Log.w(TAG, "한국어 미지원")
                    textToSpeech.language = Locale.ENGLISH
                    isTTSInitialized = true
                }
                else -> {
                    isTTSInitialized = true
                    Log.d(TAG, "TTS 준비 완료")
                }
            }
        } else {
            Log.e(TAG, "TTS 초기화 실패 - status: $status")
        }
    }

    private fun playTTS(text: String) {
        Log.d(TAG, "playTTS 호출 - text: $text, initialized: $isTTSInitialized")

        if (!isTTSInitialized) {
            Log.w(TAG, "TTS 미초기화")
            return
        }

        try {
            textToSpeech.setPitch(1.0f)
            textToSpeech.setSpeechRate(1.0f)

            if (textToSpeech.isSpeaking) {
                textToSpeech.stop()
            }

            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            Log.d(TAG, "TTS 재생 시작: $text")
        } catch (e: Exception) {
            Log.e(TAG, "TTS 재생 오류: ${e.message}")
        }
    }

    private fun initializeSpeechIntent() {
        speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5000L)
        }
        Log.d(TAG, "Speech Intent 초기화 완료")
    }

    private fun startSpeech() {
        Log.d(TAG, "startSpeech 호출")
        try {
            speechRecognizer?.destroy()

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer?.setRecognitionListener(speechListener)
            speechRecognizer?.startListening(speechIntent)

            Log.d(TAG, "음성 인식 시작 성공")
        } catch (e: Exception) {
            Log.e(TAG, "음성 인식 시작 실패: ${e.message}", e)
            resetRecordButton()
        }
    }

    private fun stopSpeech() {
        Log.d(TAG, "stopSpeech 호출")
        try {
            speechRecognizer?.stopListening()
            Log.d(TAG, "음성 인식 중지 성공")
        } catch (e: Exception) {
            Log.e(TAG, "음성 인식 중지 오류: ${e.message}")
        }
    }

    private val speechListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d(TAG, "onReadyForSpeech")
        }

        override fun onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech - 음성 감지됨")
        }

        override fun onRmsChanged(rmsdB: Float) {}

        override fun onBufferReceived(buffer: ByteArray) {}

        override fun onEndOfSpeech() {
            Log.d(TAG, "onEndOfSpeech - 음성 입력 종료")
        }

        override fun onError(error: Int) {
            Log.e(TAG, "STT 에러: $error")
            resetRecordButton()
        }

        override fun onResults(results: Bundle) {
            val list = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            Log.d(TAG, "STT 결과: $list")

            if (!list.isNullOrEmpty()) {
                val text = list[0]
                Log.d(TAG, "인식된 텍스트: $text")

                runOnUiThread {
                    val receivedMessage = ChatMessage(text, isMine = false)
                    chatAdapter.addMessage(receivedMessage)
                    recyclerView.scrollToPosition(messageList.size - 1)
                }
            }
            resetRecordButton()
        }

        override fun onPartialResults(partialResults: Bundle) {
            val list = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            Log.d(TAG, "부분 결과: $list")
        }

        override fun onEvent(eventType: Int, params: Bundle) {
            Log.d(TAG, "onEvent: $eventType")
        }
    }

    private fun resetRecordButton() {
        runOnUiThread {
            btnRecordVoice.text = "🎤"
            btnRecordVoice.alpha = 1.0f
            isRecording = false
        }
    }

    private fun showSaveDialog() {
        val editText = EditText(this)
        editText.hint = "대화 제목을 입력하세요"
        editText.setPadding(40, 20, 40, 20)

        AlertDialog.Builder(this)
            .setTitle("대화 저장")
            .setView(editText)
            .setPositiveButton("저장") { _, _ ->
                val title = editText.text.toString().trim()
                if (title.isNotEmpty()) {
                    saveConversation(title)
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun saveConversation(title: String) {
        val sharedPref = getSharedPreferences("conversations", MODE_PRIVATE)
        val recordId = System.currentTimeMillis()
        // [P2 Fix] 수동 JSON 생성 → Gson 사용 (특수문자 안전하게 처리)
        val messagesJson = gson.toJson(messageList)

        with(sharedPref.edit()) {
            putString("title_$recordId", title)
            putString("messages_$recordId", messagesJson)
            putLong("timestamp_$recordId", recordId)
            apply()
        }
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "마이크 권한 승인")
            } else {
                Log.w(TAG, "마이크 권한 거부")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy - 리소스 정리")

        if (MainActivity.mainActivityInstance != null) {
            MainActivity.mainActivityInstance?.startDoorbellDetection()
            Log.d("YAMNet", "ChatActivity 종료: YAMNet 자동 재시작")
        }

        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }

        speechRecognizer?.destroy()
        speechRecognizer = null
        guidanceTTS?.stop()
        guidanceTTS?.shutdown()
    }

    override fun onPause() {
        super.onPause()
        if (isRecording) {
            stopSpeech()
            resetRecordButton()
        }
    }
}
