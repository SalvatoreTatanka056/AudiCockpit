package com.example.myapplication.shared

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat

data class MediaItemData(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val mediaUri: String,
    val durationMs: Long
) {
    fun toMediaItem(): MediaBrowserCompat.MediaItem {
        val description = MediaDescriptionCompat.Builder()
            .setMediaId(id)
            .setTitle(title)
            .setSubtitle(artist)
            .setDescription(album)
            .setMediaUri(Uri.parse(mediaUri))
            .build()
        return MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
    }
}

object MediaCatalog {
    val items = listOf(
        MediaItemData(
            id = "song_1",
            title = "Jazz In Paris",
            artist = "Media Right Productions",
            album = "YouTube Audio Library",
            mediaUri = "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3",
            durationMs = 126000L
        ),
        MediaItemData(
            id = "song_2",
            title = "Big Buck Bunny Splash",
            artist = "Blender Foundation",
            album = "Blender Audio",
            mediaUri = "https://storage.googleapis.com/exoplayer-test-media-1/mp3/bbb-splash.mp3",
            durationMs = 10000L
        ),
        MediaItemData(
            id = "song_3",
            title = "Synthetic Tone",
            artist = "Audio Test",
            album = "Test Tones",
            mediaUri = "https://storage.googleapis.com/exoplayer-test-media-1/m4a/synth.mp3",
            durationMs = 5000L
        )
    )

    fun findById(id: String?): MediaItemData? = items.firstOrNull { it.id == id }
}

class MyMusicService : MediaBrowserServiceCompat() {

    private lateinit var session: MediaSessionCompat
    private var mediaPlayer: MediaPlayer? = null
    private var currentIndex: Int = -1

    private val callback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            if (currentIndex in MediaCatalog.items.indices) {
                if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
                    mediaPlayer?.start()
                    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
                } else {
                    playMedia(MediaCatalog.items[currentIndex])
                }
            } else if (MediaCatalog.items.isNotEmpty()) {
                playMedia(MediaCatalog.items[0])
            }
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            val index = MediaCatalog.items.indexOfFirst { it.id == mediaId }
            if (index != -1) {
                playMedia(MediaCatalog.items[index])
            }
        }

        override fun onPause() {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
                }
            }
        }

        override fun onStop() {
            mediaPlayer?.let {
                it.stop()
                it.release()
            }
            mediaPlayer = null
            updatePlaybackState(PlaybackStateCompat.STATE_STOPPED)
        }

        override fun onSkipToNext() {
            if (MediaCatalog.items.isNotEmpty()) {
                val nextIndex = (currentIndex + 1) % MediaCatalog.items.size
                playMedia(MediaCatalog.items[nextIndex])
            }
        }

        override fun onSkipToPrevious() {
            if (MediaCatalog.items.isNotEmpty()) {
                val prevIndex = if (currentIndex - 1 < 0) MediaCatalog.items.size - 1 else currentIndex - 1
                playMedia(MediaCatalog.items[prevIndex])
            }
        }

        override fun onSeekTo(position: Long) {
            mediaPlayer?.let {
                it.seekTo(position.toInt())
                updatePlaybackState(if (it.isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        session = MediaSessionCompat(this, "MyMusicService").apply {
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
            setCallback(this@MyMusicService.callback)
            isActive = true
        }
        sessionToken = session.sessionToken
        updatePlaybackState(PlaybackStateCompat.STATE_NONE)
    }

    private fun playMedia(item: MediaItemData) {
        currentIndex = MediaCatalog.items.indexOf(item)

        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(applicationContext, Uri.parse(item.mediaUri))
            setOnPreparedListener { mp ->
                mp.start()
                updateMetadata(item)
                updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
            }
            setOnCompletionListener {
                callback.onSkipToNext()
            }
            setOnErrorListener { _, _, _ ->
                updatePlaybackState(PlaybackStateCompat.STATE_ERROR)
                true
            }
            prepareAsync()
        }
        updatePlaybackState(PlaybackStateCompat.STATE_BUFFERING)
    }

    private fun updateMetadata(item: MediaItemData) {
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, item.id)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, item.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, item.artist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, item.album)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, item.durationMs)
            .build()
        session.setMetadata(metadata)
    }

    private fun updatePlaybackState(state: Int) {
        val position = mediaPlayer?.currentPosition?.toLong() ?: 0L
        val actions = PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_SEEK_TO or
                PlaybackStateCompat.ACTION_STOP or
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID

        val stateBuilder = PlaybackStateCompat.Builder()
            .setActions(actions)
            .setState(state, position, 1.0f)
        session.setPlaybackState(stateBuilder.build())
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        session.isActive = false
        session.release()
        super.onDestroy()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot("root", null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        if ("root" == parentId) {
            val mediaItems = MediaCatalog.items.map { it.toMediaItem() }.toMutableList()
            result.sendResult(mediaItems)
        } else {
            result.sendResult(mutableListOf())
        }
    }
}
