package com.example.yamnetwatchapp0913

data class ConversationRecord(
    val id: Long = System.currentTimeMillis(),
    val title: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)
