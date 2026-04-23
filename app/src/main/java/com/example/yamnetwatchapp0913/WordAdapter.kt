package com.example.yamnetwatchapp0913

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import android.widget.TextView

class WordAdapter(
    private val wordList: List<WordItem>,
    private val onToggleClick: (Int) -> Unit
) : RecyclerView.Adapter<WordAdapter.WordViewHolder>() {

    class WordViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val wordItemName: TextView = itemView.findViewById(R.id.wordItemName)
        val wordItemToggle: ImageView = itemView.findViewById(R.id.wordItemToggle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_word, parent, false)
        return WordViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val wordItem = wordList[position]
        holder.wordItemName.text = wordItem.name

        if (wordItem.isOn) {
            holder.wordItemToggle.setImageResource(R.drawable.wordinsikonswitch)
        } else {
            holder.wordItemToggle.setImageResource(R.drawable.wordinsikoffswitch)
        }

        holder.wordItemToggle.setOnClickListener {
            onToggleClick(position)
        }
    }

    override fun getItemCount(): Int = wordList.size
}

// WordInsikActivity.kt (수정 버전)
