package com.example.yamnetwatchapp0913

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AudioAdapter(
    private val audioList: MutableList<AudioFile>,
    private val onPlayClick: (AudioFile) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<AudioAdapter.AudioViewHolder>() {

    inner class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileNameTextView: TextView = itemView.findViewById(R.id.fileNameTextView)
        val playMp3Button: ImageView = itemView.findViewById(R.id.playMp3Button)
        val deleteMp3Button: ImageView = itemView.findViewById(R.id.deleteMp3Button)

        fun bind(audioFile: AudioFile, position: Int) {
            fileNameTextView.text = audioFile.name

            playMp3Button.setOnClickListener {
                onPlayClick(audioFile)
            }

            // ✅ 중요: position을 정확하게 사용
            deleteMp3Button.setOnClickListener {
                onDeleteClick(adapterPosition)  // position 대신 adapterPosition 사용!
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_audio_file, parent, false)
        return AudioViewHolder(view)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        holder.bind(audioList[position], position)
    }

    override fun getItemCount(): Int = audioList.size
}
