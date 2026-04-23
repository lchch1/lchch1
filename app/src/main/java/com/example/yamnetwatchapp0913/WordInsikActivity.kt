package com.example.yamnetwatchapp0913

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.view.LayoutInflater
import android.view.View
import android.util.Log
import android.os.Vibrator


class WordInsikActivity : AppCompatActivity(), RecognitionListener {

    private lateinit var addword1Button: ImageView
    private lateinit var addWordNameView: ImageView
    private lateinit var wordNameEditText: EditText
    private lateinit var addwordconfirmicon: ImageView
    private lateinit var wordListContainer: LinearLayout

    private lateinit var jjinhadanHome: ImageView

    private val wordList = mutableListOf<WordItem>()
    // [P1 Fix] sharedPreferences 중복 초기화 제거 - 한 번만 선언
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()

    private var speechRecognizer: SpeechRecognizer? = null
    private val REQ_RECORD_AUDIO = 101
    private var isSpeechRecognitionActive = false

    private var isYamnetEnabled = false

    private companion object {
        private const val PREFS_NAME = "word_prefs"
        private const val WORD_LIST_KEY = "word_list"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wordinsik)

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // [P1 Fix] sharedPreferences 한 번만 초기화
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        addword1Button = findViewById(R.id.addword1)
        addWordNameView = findViewById(R.id.addWordNameView)
        wordNameEditText = findViewById(R.id.wordNameEditText)
        addwordconfirmicon = findViewById(R.id.addwordconfirmicon)
        wordListContainer = findViewById(R.id.wordListContainer)
        jjinhadanHome = findViewById(R.id.jjinhadanhome)

        loadWordList()
        refreshWordList()

        addword1Button.setOnClickListener {
            showWordInputDialog()
        }

        addwordconfirmicon.setOnClickListener {
            confirmWordName()
        }

        addWordNameView.setOnClickListener {
            hideWordInputDialog()
        }

        jjinhadanHome.setOnClickListener {
            if (MainActivity.mainActivityInstance != null) {
                MainActivity.mainActivityInstance?.startDoorbellDetection()
            }
            finish()
        }

        initializeSpeechRecognizer()
        checkAudioPermission()
    }

    private fun initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer?.setRecognitionListener(this)
    }

    private fun checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQ_RECORD_AUDIO
            )
        } else {
            startListening()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_RECORD_AUDIO && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startListening()
        }
    }

    override fun onResume() {
        super.onResume()
        stopYamnetOnEntry()
        startListening()
    }

    override fun onPause() {
        super.onPause()
        isSpeechRecognitionActive = false
        speechRecognizer?.stopListening()
        startYamnet()
    }

    private fun stopYamnetOnEntry() {
        if (MainActivity.mainActivityInstance != null && !isYamnetEnabled) {
            MainActivity.mainActivityInstance?.stopDoorbellDetection()
            isYamnetEnabled = true
            Log.d("YAMNet", "WordInsikActivity 진입: YAMNet 자동 중지")
        }
    }

    private fun startYamnet() {
        if (MainActivity.mainActivityInstance != null && isYamnetEnabled) {
            MainActivity.mainActivityInstance?.startDoorbellDetection()
            isYamnetEnabled = false
            Log.d("YAMNet", "WordInsikActivity 종료: YAMNet 자동 재시작")
        }
    }

    private fun startListening() {
        if (isSpeechRecognitionActive) return

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        isSpeechRecognitionActive = true
        speechRecognizer?.startListening(intent)
    }

    override fun onReadyForSpeech(params: Bundle?) {}

    override fun onBeginningOfSpeech() {}

    override fun onRmsChanged(rmsdB: Float) {}

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {}

    // [P1 Fix] onError에서 즉시 재시도 대신 1500ms 딜레이 추가 (배터리 소모 방지)
    override fun onError(error: Int) {
        isSpeechRecognitionActive = false
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isSpeechRecognitionActive) {
                startListening()
            }
        }, 1500)
    }

    override fun onResults(results: Bundle?) {
        Log.d("STT_DEBUG", "onResults 호출됨!")

        results?.let {
            val matches = it.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
            Log.d("STT_DEBUG", "결과: $matches")

            if (!matches.isNullOrEmpty()) {
                val recognizedText = matches[0].lowercase()
                Log.d("STT_DEBUG", "감지된 텍스트: '$recognizedText'")

                checkForMatchingWords(recognizedText)
            }
        }

        isSpeechRecognitionActive = false

        Handler(Looper.getMainLooper()).postDelayed({
            if (!isSpeechRecognitionActive) {
                Log.d("STT_DEBUG", "다시 리스닝 시작")
                startListening()
            }
        }, 3000)
    }

    override fun onPartialResults(partialResults: Bundle?) {}

    override fun onEvent(eventType: Int, params: Bundle?) {}

    private fun checkForMatchingWords(recognizedText: String) {
        Log.d("STT_DEBUG", "단어 비교 시작 - 감지: '$recognizedText'")

        for (word in wordList) {
            if (word.isOn) {
                if (recognizedText.contains(word.name.lowercase())) {
                    Log.d("STT_DEBUG", "단어 일치! '${word.name}' 팝업 표시 준비")
                    showWordDetectionDialog(word.name)
                    return
                }
            }
        }

        Log.d("STT_DEBUG", "일치하는 단어 없음")
    }

    private fun showWordDetectionDialog(detectedWord: String) {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(android.os.VibrationEffect.createOneShot(400, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }

        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_word_detection, null)

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvGuidanceTitle)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tvWordDetected)
        val btnUnderstand = dialogView.findViewById<Button>(R.id.btnUnderstand)

        tvTitle.text = "단어 인식 알림"
        tvMessage.text = "$detectedWord 단어 감지!"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnUnderstand.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showWordInputDialog() {
        addWordNameView.visibility = View.VISIBLE
        wordNameEditText.visibility = View.VISIBLE
        addwordconfirmicon.visibility = View.VISIBLE
        wordNameEditText.requestFocus()
        wordNameEditText.setText("")
    }

    private fun hideWordInputDialog() {
        addWordNameView.visibility = View.GONE
        wordNameEditText.visibility = View.GONE
        addwordconfirmicon.visibility = View.GONE
    }

    private fun confirmWordName() {
        val wordName = wordNameEditText.text.toString().trim()

        if (wordName.isEmpty()) {
            return
        }

        wordList.add(WordItem(wordName, false))
        saveWordList()
        refreshWordList()

        hideWordInputDialog()
    }

    private fun refreshWordList() {
        wordListContainer.removeAllViews()

        for ((index, wordItem) in wordList.withIndex()) {
            val itemView = layoutInflater.inflate(R.layout.item_word, wordListContainer, false)
            val wordNameTextView = itemView.findViewById<TextView>(R.id.wordItemName)
            val toggleSwitch = itemView.findViewById<ImageView>(R.id.wordItemToggle)
            val deleteButton = itemView.findViewById<ImageView>(R.id.wordItemDelete)

            wordNameTextView.text = wordItem.name

            if (wordItem.isOn) {
                toggleSwitch.setImageResource(R.drawable.wordinsikonswitch)
            } else {
                toggleSwitch.setImageResource(R.drawable.wordinsikoffswitch)
            }

            toggleSwitch.setOnClickListener {
                wordList[index].isOn = !wordList[index].isOn
                saveWordList()
                refreshWordList()
            }

            deleteButton.setOnClickListener {
                wordList.removeAt(index)
                saveWordList()
                refreshWordList()
            }

            wordListContainer.addView(itemView)
        }
    }

    private fun saveWordList() {
        try {
            val json = gson.toJson(wordList)
            sharedPreferences.edit().putString(WORD_LIST_KEY, json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadWordList() {
        try {
            val json = sharedPreferences.getString(WORD_LIST_KEY, null)
            if (json != null) {
                val type = object : TypeToken<List<WordItem>>() {}.type
                val savedList: List<WordItem> = gson.fromJson(json, type)
                wordList.clear()
                wordList.addAll(savedList)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
        startYamnet()
    }
}
