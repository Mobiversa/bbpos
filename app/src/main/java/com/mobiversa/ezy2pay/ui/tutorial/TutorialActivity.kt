package com.mobiversa.ezy2pay.ui.tutorial

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.mobiversa.ezy2pay.R
import kotlinx.android.synthetic.main.activity_tutorial.*

class TutorialActivity : YouTubeBaseActivity(), YouTubePlayer.OnInitializedListener {

    private var API_KEY: String = "AIzaSyAGXTPRUz_TqzjvHWgA2sxHCjlnlItWa1Q"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        actionBar?.title = "Contact US"
        actionBar?.setDisplayHomeAsUpEnabled(true)

        try {
            //Initializing YouTube player view
            youtube_view.initialize(API_KEY, this)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onInitializationSuccess(
        provider: YouTubePlayer.Provider?,
        player: YouTubePlayer?,
        wasRestored: Boolean
    ) {
        if (null == player) return
        // Start buffering
        if (!wasRestored) {
            player.cueVideo(resources.getString(R.string.VIDEO_ID))
        }
    }

    override fun onInitializationFailure(
        provider: YouTubePlayer.Provider?,
        result: YouTubeInitializationResult?
    ) {
        Toast.makeText(this, "Failed to initialize.", Toast.LENGTH_LONG).show()
    }
}
