package com.github.warren_bank.rtsp_ipcam_viewer.list_view.recycler_view;

import com.github.warren_bank.rtsp_ipcam_viewer.R;
import com.github.warren_bank.rtsp_ipcam_viewer.common.data.VideoType;
import com.github.warren_bank.rtsp_ipcam_viewer.fullscreen_view.activities.VideoActivity;

import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.TextView;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.rtsp.RtspDefaultClient;
import com.google.android.exoplayer2.source.rtsp.RtspMediaSource;
import com.google.android.exoplayer2.source.rtsp.core.Client;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public final class RecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener, View.OnLongClickListener {

    private PlayerView view;
    private TextView title;
    private SimpleExoPlayer exoPlayer;
    private DefaultHttpDataSourceFactory dataSourceFactory;

    private VideoType data;

    public RecyclerViewHolder(View view) {
        super(view);

        this.view  = (PlayerView) view;
        this.title = (TextView) view.findViewById(R.id.exo_title);

        Context context = view.getContext();
        DefaultTrackSelector trackSelector = new DefaultTrackSelector();
        RenderersFactory renderersFactory = new DefaultRenderersFactory(context);
        this.exoPlayer = ExoPlayerFactory.newSimpleInstance(context, renderersFactory, trackSelector);

        String userAgent = context.getResources().getString(R.string.user_agent);
        this.dataSourceFactory = new DefaultHttpDataSourceFactory(userAgent);

        this.view.setOnTouchListener(this);
        this.view.setOnLongClickListener(this);
        this.view.setUseController(false);
        this.view.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
        this.view.setPlayer(this.exoPlayer);

        this.exoPlayer.setVolume(0f);  // mute all videos in list view
    }

    public void bind(VideoType data) {
        this.data = data;
        this.title.setText(data.title);

        stop();

        Uri uri = Uri.parse(data.URL_low_res);
        MediaSource source;

        if (Util.isRtspUri(uri)) {
            source = new RtspMediaSource.Factory(RtspDefaultClient.factory()
                .setFlags(Client.FLAG_ENABLE_RTCP_SUPPORT)
                .setNatMethod(Client.RTSP_NAT_DUMMY))
                .createMediaSource(uri);
        } else {
            source = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
        }

        exoPlayer.prepare(source);

        play();
    }

    public void play() {
        try {
            exoPlayer.setPlayWhenReady(true);
        }
        catch (Exception e){}
    }

    public void pause() {
        try {
            exoPlayer.setPlayWhenReady(false);
        }
        catch (Exception e){}
    }

    public void stop() {
        try {
            exoPlayer.stop(true);
        }
        catch (Exception e){}
    }

    public void release() {
        try {
            exoPlayer.release();
        }
        catch (Exception e){}
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            long click_duration = event.getEventTime() - event.getDownTime();  // milliseconds

            int max_duration = ViewConfiguration.getLongPressTimeout();
            if (click_duration <= max_duration) {
                doOnClick();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onLongClick(View v) {
        doOnLongClick();
        return true;
    }

    // open selected video in fullscreen view
    private void doOnClick() {
        VideoActivity.open(
            view.getContext(),
            (data.URL_high_res != null) ? data.URL_high_res : data.URL_low_res
        );
    }

    // toggle play/pause of selected video
    private void doOnLongClick() {
        try {
            exoPlayer.setPlayWhenReady(
                !exoPlayer.getPlayWhenReady()
            );
        }
        catch (Exception e){}
    }

}
