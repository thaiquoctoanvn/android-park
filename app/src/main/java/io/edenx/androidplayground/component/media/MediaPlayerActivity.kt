package io.edenx.androidplayground.component.media

import android.content.ComponentName
import androidx.media3.common.C.TRACK_TYPE_TEXT
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import io.edenx.androidplayground.component.base.BaseActivity
import io.edenx.androidplayground.databinding.ActivityMediaPlayerBinding

class MediaPlayerActivity :
    BaseActivity<ActivityMediaPlayerBinding>(ActivityMediaPlayerBinding::inflate) {

    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private val controller: MediaController?
        get() = if (controllerFuture.isDone) controllerFuture.get() else null

    override fun onViewCreated() {

    }

    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(this, ComponentName(this, Media3PlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                initMediaController()
            },
            MoreExecutors.directExecutor()
        )
    }

    override fun setListener() {

    }

    private fun initMediaController() {
        controller?.let {
            binding.playerView.player = it
            binding.playerView.setShowSubtitleButton(it.currentTracks.isTypeSupported(TRACK_TYPE_TEXT))
            it.addListener(object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)
                }

                override fun onTracksChanged(tracks: Tracks) {
                    binding.playerView.setShowSubtitleButton(tracks.isTypeSupported(TRACK_TYPE_TEXT))
                }

                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    super.onPlayWhenReadyChanged(playWhenReady, reason)
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                }
            })
            it.playWhenReady = true
        }
    }

}