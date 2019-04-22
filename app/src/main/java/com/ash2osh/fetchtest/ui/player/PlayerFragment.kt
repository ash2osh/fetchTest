package com.ash2osh.fetchtest.ui.player


import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.ash2osh.fetchtest.R
import com.ash2osh.fetchtest.ui.base.ScopedFragment
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util.getUserAgent
import kotlinx.android.synthetic.main.fragment_player.*
import java.io.File


class PlayerFragment : ScopedFragment() {
    private var player: SimpleExoPlayer? = null
    override val TAG: String get() = "PlayerFragment-->"
    private val args: PlayerFragmentArgs by navArgs()


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        player = ExoPlayerFactory.newSimpleInstance(context)
        player?.playWhenReady = true
        player?.volume = 1f
        //set the view
        playerView.player = player

        // Produces DataSource instances through which media data is loaded.
        val dataSourceFactory = DefaultDataSourceFactory(
            context,
            getUserAgent(context, "fetchtest")
        )
// This is the MediaSource representing the media to be played.
        val file = Uri.fromFile(File(args.file))
        val videoSource = ExtractorMediaSource.Factory(dataSourceFactory)
            .createMediaSource(file)
// Prepare the player with the source.
        player?.prepare(videoSource)
    }


    override fun onStop() {
        super.onStop()
        player?.release()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
}
