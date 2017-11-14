package com.mobapptuts.mediaplayerstreamingapp

import android.graphics.Point
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Surface
import android.view.SurfaceHolder
import android.view.View
import android.widget.FrameLayout
import android.widget.MediaController
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity(), SurfaceHolder.Callback, MediaController.MediaPlayerControl {
    override fun isPlaying(): Boolean {
        return mediaPlayer.isPlaying
    }

    override fun canSeekForward(): Boolean {
        return true
    }

    override fun getDuration(): Int {
        return mediaPlayer.duration
    }

    override fun pause() {
        mediaPlayer.pause()
    }

    override fun getBufferPercentage(): Int {
        return 0
    }

    override fun seekTo(p0: Int) {
        mediaPlayer.seekTo(p0)
    }

    override fun getCurrentPosition(): Int {
        return mediaPlayer.currentPosition
    }

    override fun canSeekBackward(): Boolean {
        return true
    }

    override fun start() {
        mediaPlayer.start()
    }

    override fun getAudioSessionId(): Int {
        return mediaPlayer.audioSessionId
    }

    override fun canPause(): Boolean {
        return true
    }

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        val surface = holder.surface
        setupMediaPlayer(surface)
        prepareMediaPlayer()
    }

    private lateinit var mediaController: MediaController
    private lateinit var mediaPlayer: MediaPlayer
    private var playbackPosition = 0
    private val rtspUrl = "rtsp://184.72.239.149/vod/mp4:BigBuckBunny_175k.mov"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val holder = surfaceView.holder
        holder.addCallback(this)

    }

    override fun onPause() {
        super.onPause()

        playbackPosition = mediaPlayer.currentPosition
    }

    override fun onStop() {
        mediaPlayer.stop()
        mediaPlayer.release()

        super.onStop()
    }

    private fun createAudioAttributes(): AudioAttributes {
        val builder = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
        return builder.build()
    }

    private fun setupMediaPlayer(surface: Surface) {
        mediaController = MediaController(this)
        progressBar.visibility = View.VISIBLE
        mediaPlayer = MediaPlayer()
        mediaPlayer.setSurface(surface)
        val audioAttributes = createAudioAttributes()
        mediaPlayer.setAudioAttributes(audioAttributes)
        val uri = Uri.parse(rtspUrl)
        try {
            mediaPlayer.setDataSource(this, uri)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    private fun setSurfaceDimensions(player: MediaPlayer, width: Int, height: Int) {
        if(width > 0 && height > 0) {
            val aspectRatio = height.toFloat() / width.toFloat()
            val screenDimensions = Point()
            windowManager.defaultDisplay.getSize(screenDimensions)
            val surfaceWidth = screenDimensions.x
            val surfaceHeight = (surfaceWidth * aspectRatio).toInt()
            val params = FrameLayout.LayoutParams(surfaceWidth, surfaceHeight)
            surfaceView.layoutParams = params
            val holder = surfaceView.holder
            player.setDisplay(holder)
        }
    }

    private fun prepareMediaPlayer() {
        try {
            mediaPlayer.prepareAsync()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

        mediaPlayer.setOnPreparedListener {
            mediaController.setMediaPlayer(this)
            mediaController.setAnchorView(surfaceContainer)
            progressBar.visibility = View.INVISIBLE
            mediaPlayer.seekTo(playbackPosition)
            mediaPlayer.start()
            displayMediaController()
        }

        mediaPlayer.setOnVideoSizeChangedListener { player, width, height ->
            setSurfaceDimensions(player, width, height)
        }

        surfaceView.setOnClickListener {
            displayMediaController()
        }
    }

    private fun displayMediaController() {
        mediaController.isEnabled = true
        mediaController.show()
    }
}
