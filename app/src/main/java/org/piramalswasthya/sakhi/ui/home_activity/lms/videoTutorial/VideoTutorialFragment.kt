package org.piramalswasthya.sakhi.ui.home_activity.lms.videoTutorial

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.VideoView
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity

class VideoTutorialFragment:Fragment() {
    private lateinit var seekBar: SeekBar
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var videoView: VideoView
    private lateinit var tapZone: View
    private lateinit var gestureDetector: GestureDetectorCompat
    private lateinit var btnPlayPause: ImageView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_video_tutorial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

         videoView = view.findViewById(R.id.videoView)
         seekBar  = view.findViewById(R.id.seekBar )
         tapZone = view.findViewById(R.id.tapZone)
         btnPlayPause = view.findViewById(R.id.btnPlayPause)
         val videoUri = Uri.parse("android.resource://${requireContext().packageName}/${R.raw.lms_video}")
        videoView.setVideoURI(videoUri)
        videoView.setOnPreparedListener { mediaPlayer ->
            seekBar.max = mediaPlayer.duration
            seekBar.visibility = View.VISIBLE
            updateSeekBar()
            mediaPlayer.isLooping = true
            videoView.start()
            hidePlayPauseButtonWithDelay()
        }
        videoView.setOnClickListener {
            if (videoView.isPlaying) {
                videoView.pause()
                btnPlayPause.setImageResource(R.drawable.ic_play)
                btnPlayPause.visibility = View.VISIBLE
                hidePlayPauseButtonWithDelay()
            } else {
                videoView.start()
                btnPlayPause.setImageResource(R.drawable.ic_pause)
                btnPlayPause.visibility = View.VISIBLE
                hidePlayPauseButtonWithDelay()
            }
        }
        btnPlayPause.setOnClickListener {
            if (videoView.isPlaying) {
                videoView.pause()
                btnPlayPause.setImageResource(R.drawable.ic_play)
                btnPlayPause.visibility = View.VISIBLE

            } else {
                videoView.start()
                btnPlayPause.setImageResource(R.drawable.ic_pause)
                hidePlayPauseButtonWithDelay()
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    videoView.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        gestureDetector = GestureDetectorCompat(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                forwardVideo(10000)
                return true
            }
        })
        tapZone.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
    }

    private fun forwardVideo(milliseconds: Int) {
        val newPosition = videoView.currentPosition + milliseconds
        if (newPosition < videoView.duration) {
            videoView.seekTo(newPosition)
            seekBar.progress = newPosition
        } else {
            videoView.seekTo(videoView.duration)
            seekBar.progress = videoView.duration
        }
    }
    private fun updateSeekBar() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (videoView.isPlaying) {
                    seekBar.progress = videoView.currentPosition
                    handler.postDelayed(this, 500)
                }
            }
        }, 500)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic_video_tutorial_icon,
                getString(R.string.video_tutorial)
            )
        }
    }
    private fun hidePlayPauseButtonWithDelay() {
        handler.postDelayed({
            if (videoView.isPlaying) {
                btnPlayPause.visibility = View.GONE
            }

        }, 3000)
    }
}