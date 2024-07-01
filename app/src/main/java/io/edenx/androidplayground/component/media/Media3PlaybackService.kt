package io.edenx.androidplayground.component.media

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

class Media3PlaybackService : MediaSessionService() {

    private lateinit var customCommands: List<CommandButton>

    private var customLayout = ImmutableList.of<CommandButton>()
    private var mediaSession: MediaSession? = null
    private val customCommandToggleShuffleModeOn =
        "android.media3.session.demo.SHUFFLE_ON"
    private val customCommandToggleShuffleModeOff =
        "android.media3.session.demo.SHUFFLE_OFF"

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player).setSessionActivity(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MediaPlayerActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
        ).build()
        customCommands =
            listOf(
                getShuffleCommandButton(
                    SessionCommand(customCommandToggleShuffleModeOn, Bundle.EMPTY)
                ),
                getShuffleCommandButton(
                    SessionCommand(customCommandToggleShuffleModeOff, Bundle.EMPTY)
                )
            )
        customLayout = ImmutableList.of(customCommands[0])
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    private fun getShuffleCommandButton(sessionCommand: SessionCommand): CommandButton {
        val isOn = sessionCommand.customAction == customCommandToggleShuffleModeOn
        return CommandButton.Builder()
            .setDisplayName(
                if (isOn) "Disable shuffle mode"
                else "Enable shuffle mode"
            )
            .setSessionCommand(sessionCommand)
            .setIconResId(if (isOn) io.edenx.androidplayground.R.drawable.ic_shuffle_off else io.edenx.androidplayground.R.drawable.ic_shuffle_on)
            .build()
    }

    private inner class MySessionCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val connectionResult = super.onConnect(session, controller)
            val availableSessionCommands = connectionResult.availableSessionCommands.buildUpon()
            for (commandButton in customCommands) {
                // Add custom command to available session commands.
                commandButton.sessionCommand?.let { availableSessionCommands.add(it) }
            }
//            // Enable custom command
//            return MediaSession.ConnectionResult.accept(
//                availableSessionCommands.build(),
//                connectionResult.availablePlayerCommands
//            )
            return super.onConnect(session, controller)
        }

        override fun onPostConnect(session: MediaSession, controller: MediaSession.ControllerInfo) {
            if (!customLayout.isEmpty() && controller.controllerVersion != 0) {
                // Let Media3 controller (for instance the MediaNotificationProvider) know about the custom
                // layout right after it connected.
                ignoreFuture(mediaSession?.setCustomLayout(controller, customLayout))
            }
        }

        private fun ignoreFuture(customLayout: ListenableFuture<SessionResult>?) {
            /* Do nothing. */
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            if (customCommandToggleShuffleModeOn == customCommand.customAction) {
                // Enable shuffling.
                session.player.shuffleModeEnabled = true
                // Change the custom layout to contain the `Disable shuffling` command.
                customLayout = ImmutableList.of(customCommands[1])
                // Send the updated custom layout to controllers.
                session.setCustomLayout(customLayout)
            } else if (customCommandToggleShuffleModeOff == customCommand.customAction) {
                // Disable shuffling.
                session.player.shuffleModeEnabled = false
                // Change the custom layout to contain the `Enable shuffling` command.
                customLayout = ImmutableList.of(customCommands[0])
                // Send the updated custom layout to controllers.
                session.setCustomLayout(customLayout)
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }
}