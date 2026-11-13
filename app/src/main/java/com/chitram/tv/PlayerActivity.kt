package com.chitram.tv

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.lifecycle.lifecycleScope
import com.chitram.tv.data.ConfiguredTestVideoUrlResolver
import com.chitram.tv.data.VideoFormat
import com.chitram.tv.data.VideoSourceResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Plays only a separately supplied source URL. Catalogue and TMDB data never generate video URLs. */
class PlayerActivity : ComponentActivity() {
    companion object {
        const val MOVIE_ID = "movie_id"
        const val PROFILE_ID = "profile_id"
        const val TITLE = "title"
        const val START_POSITION_MS = "start_position_ms"
    }

    private var player: ExoPlayer? = null
    private var resolveJob: Job? = null
    private var retryAllowed = false
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
            isFocusable = true
            setOnClickListener { if (retryAllowed) resolveAndStart() }
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
        resolveAndStart()
    }

    private fun resolveAndStart() {
        resolveJob?.cancel()
        playerView.player = null
        player?.release()
        player = null
        showLoading("Finding video source…")
        resolveJob = lifecycleScope.launch {
            val result = try {
                withContext(Dispatchers.IO) {
                    val movieId = intent.getLongExtra(MOVIE_ID, 0L)
                    val movie = (application as ChitramApplication).repository.findMovie(movieId)
                        ?: return@withContext VideoSourceResult.Unavailable("Film not found in the local catalogue.")
                    ConfiguredTestVideoUrlResolver(BuildConfig.TEST_PLAYBACK_SOURCE_ID, BuildConfig.TEST_PLAYBACK_URL).resolve(movie)
                }
            } catch (_: CancellationException) {
                return@launch
            } catch (_: Exception) {
                VideoSourceResult.Unavailable("Video source could not be loaded.")
            }
            if (!isActive) return@launch
            when (result) {
                is VideoSourceResult.Unavailable -> showError(result.message)
                is VideoSourceResult.Ready -> startPlayer(result)
            }
        }
    }

    private fun startPlayer(source: VideoSourceResult.Ready) {
        val instance = ExoPlayer.Builder(this).build()
        player = instance
        playerView.player = instance
        instance.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_BUFFERING -> showLoading("Loading video…")
                    Player.STATE_READY -> { message.visibility = android.view.View.GONE; playerView.requestFocus() }
                }
            }
            override fun onPlayerError(error: PlaybackException) {
                showError("Playback failed or the source expired.")
            }
        })
        val mimeType = if (source.format == VideoFormat.HLS) MimeTypes.APPLICATION_M3U8 else MimeTypes.VIDEO_MP4
        instance.setMediaItem(MediaItem.Builder().setUri(source.url).setMimeType(mimeType).build())
        instance.prepare()
        instance.seekTo(intent.getLongExtra(START_POSITION_MS, 0L).coerceAtLeast(0L))
        instance.playWhenReady = true
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean =
        if (::playerView.isInitialized && !message.hasFocus() && playerView.dispatchKeyEvent(event)) true else super.dispatchKeyEvent(event)

    override fun onStop() {
        resolveJob?.cancel()
        resolveJob = null
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
        retryAllowed = true
        message.text = "$text\nPress OK to retry or Back to return."
        message.visibility = android.view.View.VISIBLE
        message.requestFocus()
    }

    private fun showLoading(text: String) {
        retryAllowed = false
        message.text = text
        message.visibility = android.view.View.VISIBLE
    }
}
