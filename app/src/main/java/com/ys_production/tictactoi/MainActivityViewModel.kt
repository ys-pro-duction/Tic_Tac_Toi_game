package com.ys_production.tictactoi

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivityViewModel: ViewModel() {
    private var mediaPlayer: MediaPlayer? = null
    fun bubbleSound(context: Context) {
        CoroutineScope(Dispatchers.Default).launch {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(context, R.raw.liquid_bubble)
            }
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    it.seekTo(0)
                }
                it.start()
            }
        }
    }
}