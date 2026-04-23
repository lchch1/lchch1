package com.example.yamnetwatchapp0913

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConversationAdapter(private val conversations: List<ConversationRecord>) :
    RecyclerView.Adapter<ConversationAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleView: TextView = view.findViewById(R.id.tvTitle)
        private val dateView: TextView = view.findViewById(R.id.tvDate)
        private val messageCountView: TextView = view.findViewById(R.id.tvMessageCount)

        fun bind(record: ConversationRecord) {
            titleView.text = record.title
            dateView.text = formatDate(record.timestamp)
            messageCountView.text = "${record.messages.size}개 메시지"

            // 아이템 클릭 리스너
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, ConversationDetailActivity::class.java)
                intent.putExtra("recordId", record.id)
                intent.putExtra("title", record.title)
                itemView.context.startActivity(intent)
            }
        }

        private fun formatDate(timestamp: Long): String {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREAN)
            return formatter.format(Date(timestamp))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_conversation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(conversations[position])
    }

    override fun getItemCount(): Int = conversations.size
}

