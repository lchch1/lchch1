package com.example.yamnetwatchapp0913

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ConversationDetailActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnBack: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<ChatMessage>()

    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_detail)

        // [P3 Fix] deprecated onBackPressed() → onBackPressedDispatcher 사용
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        btnBack = findViewById(R.id.btnBack)
        tvTitle = findViewById(R.id.tvTitle)
        recyclerView = findViewById(R.id.recyclerView)

        btnBack.setOnClickListener {
            finish()
        }

        chatAdapter = ChatAdapter(messageList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatAdapter

        val recordId = intent.getLongExtra("recordId", 0L)
        val title = intent.getStringExtra("title") ?: "대화"

        tvTitle.text = title

        loadConversationDetail(recordId)
    }

    private fun loadConversationDetail(recordId: Long) {
        val sharedPref = getSharedPreferences("conversations", MODE_PRIVATE)
        val messagesJson = sharedPref.getString("messages_$recordId", "[]") ?: "[]"

        messageList.clear()
        // [P2 Fix] 수동 split 파싱 → Gson 사용 (특수문자 포함 메시지 안전 처리)
        messageList.addAll(parseMessagesJson(messagesJson))
        chatAdapter.notifyDataSetChanged()

        if (messageList.isNotEmpty()) {
            recyclerView.scrollToPosition(messageList.size - 1)
        }
    }

    private fun parseMessagesJson(json: String): List<ChatMessage> {
        return try {
            val type = object : TypeToken<List<ChatMessage>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
