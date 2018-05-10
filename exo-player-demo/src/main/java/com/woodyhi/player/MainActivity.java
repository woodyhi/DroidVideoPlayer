package com.woodyhi.player;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class MainActivity extends AppCompatActivity {

    private PlayerView playerView;

    private SimpleExoPlayer simpleExoPlayer;



            Uri uri = Uri.parse("http://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_320x180.mp4");
//    Uri uri = Uri.parse("rtmp://live.hkstv.hk.lxdns.com/live/hks");
//    Uri uri = Uri.parse("rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerView = findViewById(R.id.player_view);


        // ----- Create the player
        simpleExoPlayer = creatingPlayer(getApplicationContext());

        // ----- Bind the player to the view.
        playerView.setPlayer(simpleExoPlayer);

        // ----- Prepare the player with the source.
        simpleExoPlayer.prepare(createMediaSource(uri));

        // play automaticly when ready
        simpleExoPlayer.setPlayWhenReady(true);
    }

    private SimpleExoPlayer creatingPlayer(Context context){
        // 1. Create a default TrackSelector
        Handler mainHandler = new Handler();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        // 2. Create the player
        SimpleExoPlayer player =
                ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        return player;
    }

    private MediaSource createMediaSource(Uri uri){
        // Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter bandwidthMeter1 = new DefaultBandwidthMeter();
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(),
                Util.getUserAgent(getApplicationContext(), "yourApplicationName"), bandwidthMeter1);
        // This is the MediaSource representing the media to be played.
        MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);
        // Prepare the player with the source.
//        player.prepare(videoSource);
        return videoSource;
    }


//    private void play(Context context){
//
//        RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory();
//
//        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
//
//        ExtractorMediaSource.Factory mediaSourceFactory = new ExtractorMediaSource.Factory(rtmpDataSourceFactory);
//        mediaSourceFactory.setExtractorsFactory(extractorsFactory);
//
//        MediaSource mediaSource = mediaSourceFactory.createMediaSource(Uri.parse("rtmp://live.hkstv.hk.lxdns.com/live/hks"));
//
//    }


}