package com.chitram.tv

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.widget.FrameLayout
import android.widget.TextView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Plays only a separately supplied source URL. Catalogue and TMDB data never generate video URLs. */
class PlayerActivity : Activity() {
    companion object {
        const val MOVIE_ID = "movie_id"
        const val PROFILE_ID = "profile_id"
        const val TITLE = "title"
        const val START_POSITION_MS = "start_position_ms"
    }

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var message: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playerView = PlayerView(this).apply {
            useController = true
            controllerShowTimeoutMs = 5_000
            keepScreenOn = true
            isFocusable = true
            isFocusableInTouchMode = true
        }
        message = TextView(this).apply {
            setTextColor(Color.WHITE)
            textSize = 18f
            setBackgroundColor(0xDD10141F.toInt())
            setPadding(24, 16, 24, 16)
            visibility = android.view.View.GONE
        }
        setContentView(FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
            addView(playerView, FrameLayout.LayoutParams(-1, -1))
            addView(message, FrameLayout.LayoutParams(-2, -2, Gravity.CENTER))
        })
        playerView.requestFocus()
    }

    override fun onStart() {
        super.onStart()
        val url = BuildConfig.TEST_PLAYBACK_URL
        if (!url.startsWith("https://") || BuildConfig.TEST_PLAYBACK_SOURCE_ID.isBlank()) {
            showError("No valid test stream is configured.")
            return
        }
        val instance = ExoPlayer.Builder(this).build()
        player = instance
        playerView.player = instance
        instance.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                showError("The stream could not play. It may have expired or become unavailable. Press Back to return.")
            }
        })
        instance.setMediaItem(MediaItem.fromUri(url))
        instance.prepare()
        instance.seekTo(intent.getLongExtra(START_POSITION_MS, 0L).coerceAtLeast(0L))
        instance.playWhenReady = true
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean =
        if (::playerView.isInitialized && playerView.dispatchKeyEvent(event)) true else super.dispatchKeyEvent(event)

    override fun onStop() {
        val current = player
        if (current != null) {
            val profileId = intent.getLongExtra(PROFILE_ID, 0L)
            val movieId = intent.getLongExtra(MOVIE_ID, 0L)
            val position = current.currentPosition.coerceAtLeast(0L)
            val duration = current.duration.coerceAtLeast(0L)
            if (profileId > 0 && movieId > 0 && duration > 0) {
                val repository = (application as ChitramApplication).repository
                CoroutineScope(Dispatchers.IO).launch { repository.saveProgress(profileId, movieId, position, duration) }
            }
            playerView.player = null
            current.release()
            player = null
        }
        super.onStop()
    }

    private fun showError(text: String) {
        message.text = text
        message.visibility = android.view.View.VISIBLE
    }
}
