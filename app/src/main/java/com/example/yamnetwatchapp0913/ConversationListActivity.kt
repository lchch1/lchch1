package com.example.yamnetwatchapp0913

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ConversationListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnBack: ImageView
    private lateinit var conversationAdapter: ConversationAdapter
    private val conversationList = mutableListOf<ConversationRecord>()

    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_list)

        btnBack = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.recyclerView)
        conversationAdapter = ConversationAdapter(conversationList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = conversationAdapter

        loadConversations()
    }

    private fun loadConversations() {
        val sharedPref = getSharedPreferences("conversations", MODE_PRIVATE)
        conversationList.clear()

        val allEntries = sharedPref.all
        val recordIds = mutableSetOf<Long>()

        allEntries.keys.forEach { key ->
            if (key.startsWith("title_")) {
                val recordId = key.substring(6).toLongOrNull()
                if (recordId != null) {
                    recordIds.add(recordId)
                }
            }
        }

        recordIds.sortedDescending().forEach { recordId ->
            val title = sharedPref.getString("title_$recordId", "제목 없음") ?: "제목 없음"
            val messagesJson = sharedPref.getString("messages_$recordId", "[]") ?: "[]"
            val timestamp = sharedPref.getLong("timestamp_$recordId", 0L)

            // [P2 Fix] 수동 split 파싱 → Gson 사용 (특수문자 포함 메시지 안전 처리)
            val messages = parseMessagesJson(messagesJson)
            val record = ConversationRecord(
                id = recordId,
                title = title,
                messages = messages,
                timestamp = timestamp
            )
            conversationList.add(record)
        }

        conversationAdapter.notifyDataSetChanged()
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
