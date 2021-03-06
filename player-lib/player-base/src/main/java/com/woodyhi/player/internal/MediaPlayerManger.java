package com.woodyhi.player.internal;

import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.Surface;

import com.woodyhi.player.base.AbsPlayerManager;
import com.woodyhi.player.base.LogUtil;
import com.woodyhi.player.base.PlayInfo;
import com.woodyhi.player.base.PlayerCallback;

/**
 * @author June
 * @date 2019-06-07
 */
public class MediaPlayerManger extends AbsPlayerManager {
    private static final String TAG = MediaPlayerManger.class.getSimpleName();

    private MediaPlayer mMediaPlayer;
    private PlayInfo playInfo;
    private boolean isSeekable = true;
    private boolean endReached;

    public MediaPlayerManger() {
    }

    private MediaPlayer createMediaPlayer() {
        final MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(mp -> {
            LogUtil.d(TAG, "onPrepared: ---------");
//                if (lastPosition > 0) {
//                    mp.seekTo(lastPosition);
//                } else {
            mp.start();
//                }
        });
        mediaPlayer.setOnVideoSizeChangedListener((mp, width, height) -> {
            LogUtil.d(TAG, "onVideoSizeChanged: width=" + width + ", height=" + height);
            if (width * height == 0) return;
            for (PlayerCallback listener : playerCallbacks) {
                listener.onVideoSizeChanged(width, height);
            }
        });
        mediaPlayer.setOnBufferingUpdateListener((mp, percent) -> {
            LogUtil.d(TAG, "nBufferingUpdate: mediaplayer buffered progress : " + percent + "%");
            for (PlayerCallback listener : playerCallbacks) {
                listener.onBufferingUpdate(percent);
            }
        });
        mediaPlayer.setOnSeekCompleteListener(mp -> LogUtil.d(TAG, "onSeekComplete"));
        mediaPlayer.setOnCompletionListener(mp -> {
            LogUtil.d(TAG, "onCompletion: " + mp);
            for (PlayerCallback listener : playerCallbacks) {
                listener.onCompletion();
            }
            endReached = true;
        });
        mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            private boolean startFlag;
            private long start;
            private long end;

            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
//                Log.d(TAG, "onInfo what = " + what + ", extra = " + extra);
                switch (what) {
                    case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
//                        Log.d(TAG, "info MEDIA_INFO_BAD_INTERLEAVING");
                        break;
                    case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
//                        updateLoadingState(false);
                        for (PlayerCallback listener : playerCallbacks) {
                            listener.onPlaying();
                        }
                        break;
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                        /* 遇到过视频一直不动，mediaplayer状态是playing、网络正常，不知道是不是视频流的问题 */
                        LogUtil.d(TAG, "info MEDIA_INFO_BUFFERING_START");
                        start = System.currentTimeMillis();
                        for (PlayerCallback listener : playerCallbacks) {
                            listener.onBufferingStart();
                        }
                        break;
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        LogUtil.d(TAG, "info MEDIA_INFO_BUFFERING_END");
                        end = System.currentTimeMillis();
                        for (PlayerCallback listener : playerCallbacks) {
                            listener.onBufferingEnd();
                        }
                        break;
                    case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
//                        Log.d(TAG, "info MEDIA_INFO_METADATA_UPDATE");
                        break;
                    case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
//                        Log.d(TAG, "info MEDIA_INFO_NOT_SEEKABLE");
                        isSeekable = false;
                        break;
                    case MediaPlayer.MEDIA_INFO_UNKNOWN:
//                        Log.d(TAG, "info MEDIA_INFO_UNKNOWN");
                        break;
                    case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
//                        Log.d(TAG, "info MEDIA_INFO_VIDEO_TRACK_LAGGING");
                        break;
                }
                return false;
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
//                Log.e(TAG, "onError what = " + what + ", extra = " + extra);
                switch (what) {
                    case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
//                        Log.e(TAG, "error MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK");
                        break;
                    case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
//                        Log.e(TAG, "error MEDIA_ERROR_SERVER_DIED");
                        break;
                    case MediaPlayer.MEDIA_ERROR_UNKNOWN:
//                        Log.e(TAG, "error MEDIA_ERROR_UNKNOWN");
//                        Toast.makeText(getContext(), "未知错误", Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });
        return mediaPlayer;
    }

    private void loadMedia() {
        Surface sf = getSurface();
        if (sf == null) {
            LogUtil.d(TAG, "surface is not ready ------------");
            return;
        }

        if (mMediaPlayer == null) {
            mMediaPlayer = createMediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setSurface(sf);
            try {
                if (playInfo.path == null) {
                    AssetFileDescriptor afd = playInfo.assetFileDescriptor;
                    mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                } else {
                    mMediaPlayer.setDataSource(playInfo.path);
                }
                mMediaPlayer.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                mMediaPlayer.setSurface(sf);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onSurfaceCreated(Surface surface) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setSurface(surface);
        }
        if (!endReached && !pausedFromUser) {
            if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
                play();
            } else {
                loadMedia();
            }
        }
    }

    @Override
    protected void onSurfaceDestroyed() {
        pause();
        if (mMediaPlayer != null) {
            mMediaPlayer.setSurface(null);
        }
    }

    //-----------------------------------------Controller--------------------------------------
    public void play(PlayInfo info) {
        if (info == null) {
            if (mMediaPlayer == null) {
                loadMedia();
            } else {
                play();
            }
        } else {
            release();
            this.playInfo = info;
            loadMedia();
        }
        endReached = false;
    }

    @Override
    public void play() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    @Override
    public void pause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying())
            mMediaPlayer.pause();
    }

    @Override
    public void seekTo(int msec) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(msec);
        }
    }

    @Override
    public void fastForward(int msec) {
        // TODO
    }

    @Override
    public void fastRewind(int msec) {
        // TODO
    }

    @Override
    public boolean isPlaying() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying())
            return true;
        return false;
    }

    @Override
    public boolean isSeekable() {
        return isSeekable;
    }

    @Override
    public int getDuration() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            return mMediaPlayer.getDuration();
        }
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}
