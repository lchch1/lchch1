package com.example.yamnetwatchapp0913

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.util.Log

class HochulActivity : AppCompatActivity() {

    private lateinit var jjinhadansetting: ImageView
    private lateinit var addVoiceFileButton: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var addmp3nameView: android.view.View
    private lateinit var mp3NameEditText: EditText
    private lateinit var addmp3confirmicon: ImageView
    private lateinit var jjinhadanhome: ImageView

    private val audioList = mutableListOf<AudioFile>()
    private lateinit var audioAdapter: AudioAdapter
    private var mediaPlayer: MediaPlayer? = null
    private var selectedAudioUri: Uri? = null

    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private companion object {
        private const val PREFS_NAME = "audio_prefs"
        private const val AUDIO_LIST_KEY = "audio_list"
    }

    private val pickAudioLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedAudioUri = it
            showNameInputDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hochul)

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        jjinhadansetting = findViewById(R.id.jjinhadansetting)
        addVoiceFileButton = findViewById(R.id.addvoicefile)
        recyclerView = findViewById(R.id.audioRecyclerView)
        addmp3nameView = findViewById(R.id.addmp3name)
        mp3NameEditText = findViewById(R.id.mp3NameEditText)
        addmp3confirmicon = findViewById(R.id.addmp3confirmicon)
        jjinhadanhome = findViewById(R.id.jjinhadanhome)

        loadAudioList()

        audioAdapter = AudioAdapter(
            audioList,
            { audioFile -> playAudio(audioFile) },
            { position -> deleteAudio(position) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = audioAdapter

        addVoiceFileButton.setOnClickListener {
            pickAudioLauncher.launch("audio/*")
        }

        addmp3confirmicon.setOnClickListener {
            confirmMp3Name()
        }

        jjinhadanhome.setOnClickListener {
            if (MainActivity.mainActivityInstance != null) {
                MainActivity.mainActivityInstance?.startDoorbellDetection()
            }
            finish()
        }
        jjinhadansetting.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        addmp3nameView.setOnClickListener {
            hideNameInputDialog()
        }
    }

    private fun showNameInputDialog() {
        addmp3nameView.visibility = android.view.View.VISIBLE
        mp3NameEditText.visibility = android.view.View.VISIBLE
        addmp3confirmicon.visibility = android.view.View.VISIBLE
        mp3NameEditText.requestFocus()
        mp3NameEditText.setText("")
    }

    private fun hideNameInputDialog() {
        addmp3nameView.visibility = android.view.View.GONE
        mp3NameEditText.visibility = android.view.View.GONE
        addmp3confirmicon.visibility = android.view.View.GONE
    }

    private fun confirmMp3Name() {
        val mp3Name = mp3NameEditText.text.toString().trim()

        if (mp3Name.isEmpty()) {
            return
        }

        selectedAudioUri?.let { uri ->
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val fileName = "$mp3Name.mp3"
                // [P2 Fix] cacheDir → filesDir (시스템이 임의 삭제하지 않는 영구 저장소)
                val savedFile = java.io.File(filesDir, fileName)

                inputStream?.use { input ->
                    savedFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                val newUri = Uri.fromFile(savedFile)
                val audioFile = AudioFile(mp3Name, newUri)
                audioList.add(audioFile)
                audioAdapter.notifyItemInserted(audioList.size - 1)

                saveAudioList()
                hideNameInputDialog()

                Log.d("AudioSave", "파일 저장됨: ${savedFile.absolutePath}")
            } catch (e: Exception) {
                Log.e("AudioSave", "파일 복사 오류: ${e.message}")
                e.printStackTrace()
            }
        }
    }


    private fun deleteAudio(position: Int) {
        if (position < 0 || position >= audioList.size) {
            return
        }

        audioList.removeAt(position)
        audioAdapter.notifyDataSetChanged()

        saveAudioList()
    }

    private fun playAudio(audioFile: AudioFile) {
        try {
            audioFile.uri?.let { uri ->
                val inputStream = contentResolver.openInputStream(uri)

                if (inputStream == null) {
                    Log.e("AudioPlay", "파일을 열 수 없습니다: $uri")
                    return
                }

                inputStream.close()

                mediaPlayer?.release()
                mediaPlayer = null

                mediaPlayer = MediaPlayer().apply {
                    try {
                        setDataSource(this@HochulActivity, uri)
                        prepare()
                        start()
                    } catch (e: Exception) {
                        Log.e("AudioPlay", "재생 오류: ${e.message}")
                        e.printStackTrace()
                    }
                }
            } ?: run {
                Log.e("AudioPlay", "URI가 null입니다.")
            }
        } catch (e: Exception) {
            Log.e("AudioPlay", "오디오 파일 오류: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun saveAudioList() {
        try {
            val audioListToSave = audioList.map {
                AudioFileData(it.name, it.uri.toString())
            }
            val json = gson.toJson(audioListToSave)
            sharedPreferences.edit().putString(AUDIO_LIST_KEY, json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadAudioList() {
        try {
            val json = sharedPreferences.getString(AUDIO_LIST_KEY, null)
            if (json != null) {
                val type = object : TypeToken<List<AudioFileData>>() {}.type
                val savedList: List<AudioFileData> = gson.fromJson(json, type)
                audioList.clear()
                audioList.addAll(savedList.map {
                    AudioFile(it.name, Uri.parse(it.uriString))
                })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
