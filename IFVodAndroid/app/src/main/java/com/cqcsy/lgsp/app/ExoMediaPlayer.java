package com.cqcsy.lgsp.app;

import static com.google.android.exoplayer2.Player.DISCONTINUITY_REASON_SEEK;
import static com.google.android.exoplayer2.Player.REPEAT_MODE_ALL;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SeekParameters;
import com.google.android.exoplayer2.TracksInfo;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.video.VideoSize;

import java.io.File;
import java.io.FileDescriptor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.exo2.demo.EventLogger;
import tv.danmaku.ijk.media.player.AbstractMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.MediaInfo;
import tv.danmaku.ijk.media.player.misc.IjkTrackInfo;


/**
 * Created by guoshuyu on 2018/1/10.
 * Exo
 */
public class ExoMediaPlayer extends AbstractMediaPlayer implements Player.Listener, AnalyticsListener {

    public static int ON_POSITION_DISCOUNTINUITY = 2702;

    private static final String TAG = "IjkExo2MediaPlayer";

    protected Context mAppContext;
    protected ExoPlayer mInternalPlayer;
    protected EventLogger mEventLogger;
    protected DefaultRenderersFactory mRendererFactory;
    protected MediaSource mMediaSource;
    protected MappingTrackSelector mTrackSelector;
    protected LoadControl mLoadControl;
    protected String mDataSource;
    protected Surface mSurface;
    protected Map<String, String> mHeaders = new HashMap<>();
    protected PlaybackParameters mSpeedPlaybackParameters;
    protected int mVideoWidth;
    protected int mVideoHeight;
    protected int lastReportedPlaybackState;
    protected boolean isLastReportedPlayWhenReady;
    protected boolean isPreparing = true;
    protected boolean isBuffering = false;
    protected boolean isLooping = false;
    /**
     * 是否带上header
     */
    protected boolean isPreview = false;
    /**
     * 是否开启缓存
     */
    protected boolean isCache = false;
    /**
     * dataSource等的帮组类
     */
    protected ExoPlayerSourceManager mExoHelper;
    /**
     * 缓存目录，可以为空
     */
    protected File mCacheDir;
    /**
     * 类型覆盖
     */
    private String mOverrideExtension;

    protected int audioSessionId = C.AUDIO_SESSION_ID_UNSET;


    public ExoMediaPlayer(Context context) {
        mAppContext = context.getApplicationContext();
        lastReportedPlaybackState = Player.STATE_IDLE;
        mExoHelper = ExoPlayerSourceManager.newInstance(context, mHeaders);
    }


    private int getVideoRendererIndex() {
        if (mInternalPlayer != null) {
            for (int i = 0; i < mInternalPlayer.getRendererCount(); i++) {
                if (mInternalPlayer.getRendererType(i) == C.TRACK_TYPE_VIDEO) {
                    return i;
                }
            }
        }
        return 0;
    }

    @Override
    public void setDisplay(SurfaceHolder sh) {
        if (sh == null)
            setSurface(null);
        else
            setSurface(sh.getSurface());
    }

    @Override
    public void setSurface(Surface surface) {
        mSurface = surface;
        if (mInternalPlayer != null) {
            if (surface != null && !surface.isValid()) {
                mSurface = null;
            }
            mInternalPlayer.setVideoSurface(surface);
        }
    }


    @Override
    public void setDataSource(Context context, Uri uri, Map<String, String> headers) {
        if (headers != null) {
            mHeaders.clear();
            mHeaders.putAll(headers);
        }
        setDataSource(context, uri);
    }

    @Override
    public void setDataSource(String path) {
        setDataSource(mAppContext, Uri.parse(path));
    }

    @Override
    public void setDataSource(Context context, Uri uri) {
        mDataSource = uri.toString();
        mMediaSource = mExoHelper.getMediaSource(mDataSource, isPreview, isCache, isLooping, mCacheDir, mOverrideExtension);
    }

    @Override
    public void setDataSource(FileDescriptor fd) {
        throw new UnsupportedOperationException("no support");
    }

    @Override
    public String getDataSource() {
        return mDataSource;
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        if (mInternalPlayer != null)
            throw new IllegalStateException("can't prepare a prepared player");
        prepareAsyncInternal();
    }

    @Override
    public void start() throws IllegalStateException {
        if (mInternalPlayer == null)
            return;
        mInternalPlayer.setPlayWhenReady(true);
    }

    @Override
    public void stop() throws IllegalStateException {
        if (mInternalPlayer == null)
            return;
        mInternalPlayer.release();
    }

    @Override
    public void pause() throws IllegalStateException {
        if (mInternalPlayer == null)
            return;
        mInternalPlayer.setPlayWhenReady(false);
    }

    @Override
    public void setWakeMode(Context context, int mode) {
        // FIXME: implement
    }

    @Override
    public void setScreenOnWhilePlaying(boolean screenOn) {
        // TODO: do nothing
    }

    @Override
    public IjkTrackInfo[] getTrackInfo() {
        // TODO: implement
        return null;
    }

    @Override
    public int getVideoWidth() {
        return mVideoWidth;
    }

    @Override
    public int getVideoHeight() {
        return mVideoHeight;
    }

    @Override
    public boolean isPlaying() {
        if (mInternalPlayer == null)
            return false;
        int state = mInternalPlayer.getPlaybackState();
        switch (state) {
            case Player.STATE_BUFFERING:
            case Player.STATE_READY:
                return mInternalPlayer.getPlayWhenReady();
            case Player.STATE_IDLE:
            case Player.STATE_ENDED:
            default:
                return false;
        }
    }

    @Override
    public void seekTo(long msec) throws IllegalStateException {
        if (mInternalPlayer == null)
            return;
        mInternalPlayer.seekTo(msec);
    }

    @Override
    public long getCurrentPosition() {
        if (mInternalPlayer == null)
            return 0;
        return mInternalPlayer.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        if (mInternalPlayer == null)
            return 0;
        return mInternalPlayer.getDuration();
    }

    @Override
    public int getVideoSarNum() {
        return 1;
    }

    @Override
    public int getVideoSarDen() {
        return 1;
    }

    @Override
    public void reset() {
        if (mInternalPlayer != null) {
            mInternalPlayer.release();
            mInternalPlayer = null;
        }
        //这样会清除掉缓存，导致小视频预加载失效
        /*if (mExoHelper != null) {
            mExoHelper.release();
        }*/
        mSurface = null;
        mDataSource = null;
        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    @Override
    public void onCues(List<Cue> cues) {

    }

    @Override
    public void onMetadata(Metadata metadata) {

    }

    @Override
    public void setLooping(boolean looping) {
        isLooping = looping;
    }

    @Override
    public boolean isLooping() {
        return isLooping;
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        if (mInternalPlayer != null)
            mInternalPlayer.setVolume((leftVolume + rightVolume) / 2);
    }

    @Override
    public int getAudioSessionId() {
        return audioSessionId;
    }

    @Override
    public MediaInfo getMediaInfo() {
        return null;
    }

    @Override
    public void setLogEnabled(boolean enable) {
        // do nothing
    }

    @Override
    public boolean isPlayable() {
        return true;
    }

    @Override
    public void setAudioStreamType(int streamtype) {
        // do nothing
    }

    @Override
    public void setKeepInBackground(boolean keepInBackground) {
        // do nothing
    }

    @Override
    public void release() {
        if (mInternalPlayer != null) {
            reset();
            mEventLogger = null;
        }
    }

    protected void prepareAsyncInternal() {
        new Handler(Looper.myLooper()).post(
                new Runnable() {
                    @Override
                    public void run() {
                        if (mTrackSelector == null) {
                            mTrackSelector = new DefaultTrackSelector(mAppContext);
                        }
                        mEventLogger = new EventLogger(mTrackSelector);
                        boolean preferExtensionDecoders = true;
                        boolean useExtensionRenderers = true;//是否开启扩展
                        @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode = useExtensionRenderers
                                ? (preferExtensionDecoders ? DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                                : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
                                : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;
                        if (mRendererFactory == null) {
                            mRendererFactory = new DefaultRenderersFactory(mAppContext);
                            mRendererFactory.setExtensionRendererMode(extensionRendererMode);
                        }
                        if (mLoadControl == null) {
                            mLoadControl = new DefaultLoadControl();
                        }
                        mInternalPlayer = new ExoPlayer.Builder(mAppContext, mRendererFactory)
                                .setLooper(Looper.myLooper())
                                .setTrackSelector(mTrackSelector)
                                .setLoadControl(mLoadControl).build();
                        mInternalPlayer.addListener(ExoMediaPlayer.this);
                        mInternalPlayer.addAnalyticsListener(ExoMediaPlayer.this);
                        mInternalPlayer.addListener(mEventLogger);
                        if (mSpeedPlaybackParameters != null) {
                            mInternalPlayer.setPlaybackParameters(mSpeedPlaybackParameters);
                        }
                        if (isLooping) {
                            mInternalPlayer.setRepeatMode(REPEAT_MODE_ALL);
                        }

                        if (mSurface != null)
                            mInternalPlayer.setVideoSurface(mSurface);
                        mInternalPlayer.setMediaSource(mMediaSource);
                        mInternalPlayer.prepare();
                        mInternalPlayer.setPlayWhenReady(false);
                    }
                }
        );
    }

    public String getOverrideExtension() {
        return mOverrideExtension;
    }

    public void setOverrideExtension(String overrideExtension) {
        this.mOverrideExtension = overrideExtension;
    }

    public void stopPlayback() {
        mInternalPlayer.stop();
    }

    /**
     * 是否需要带上header
     * setDataSource之前生效
     */
    public void setPreview(boolean preview) {
        isPreview = preview;
    }

    public boolean isPreview() {
        return isPreview;
    }

    public boolean isCache() {
        return isCache;
    }


    /**
     * 设置seek 的临近帧。
     **/
    public void setSeekParameter(@Nullable SeekParameters seekParameters) {
        mInternalPlayer.setSeekParameters(seekParameters);
    }


    /**
     * 是否开启cache
     * setDataSource之前生效
     */
    public void setCache(boolean cache) {
        isCache = cache;
    }

    public File getCacheDir() {
        return mCacheDir;
    }

    /**
     * cache文件的目录
     * setDataSource之前生效
     */
    public void setCacheDir(File cacheDir) {
        this.mCacheDir = cacheDir;
    }

    public MediaSource getMediaSource() {
        return mMediaSource;
    }

    public void setMediaSource(MediaSource mediaSource) {
        this.mMediaSource = mediaSource;
    }

    public ExoPlayerSourceManager getExoHelper() {
        return mExoHelper;
    }

    /**
     * 倍速播放
     *
     * @param speed 倍速播放，默认为1
     * @param pitch 音量缩放，默认为1，修改会导致声音变调
     */
    public void setSpeed(@Size(min = 0) float speed, @Size(min = 0) float pitch) {
        PlaybackParameters playbackParameters = new PlaybackParameters(speed, pitch);
        mSpeedPlaybackParameters = playbackParameters;
        if (mInternalPlayer != null) {
            mInternalPlayer.setPlaybackParameters(playbackParameters);
        }
    }

    public float getSpeed() {
        return mInternalPlayer.getPlaybackParameters().speed;
    }

    public int getBufferedPercentage() {
        if (mInternalPlayer == null)
            return 0;

        return mInternalPlayer.getBufferedPercentage();
    }

    public MappingTrackSelector getTrackSelector() {
        return mTrackSelector;
    }

    public void setTrackSelector(MappingTrackSelector trackSelector) {
        this.mTrackSelector = trackSelector;
    }

    public LoadControl getLoadControl() {
        return mLoadControl;
    }

    public void setLoadControl(LoadControl loadControl) {
        this.mLoadControl = loadControl;
    }

    public DefaultRenderersFactory getRendererFactory() {
        return mRendererFactory;
    }

    public void setRendererFactory(DefaultRenderersFactory rendererFactory) {
        this.mRendererFactory = rendererFactory;
    }

    @Override
    public void onTracksInfoChanged(@NonNull TracksInfo tracksInfo) {

    }

    @Override
    public void onIsLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlaybackStateChanged(int state) {
        onPlayWhenReadyChanged(isLastReportedPlayWhenReady, state);
    }

    @Override
    public void onPlayWhenReadyChanged(boolean playWhenReady, int playbackState) {
        //重新播放状态顺序为：STATE_IDLE -》STATE_BUFFERING -》STATE_READY
        //缓冲时顺序为：STATE_BUFFERING -》STATE_READY
        //Log.e(TAG, "onPlayerStateChanged: playWhenReady = " + playWhenReady + ", playbackState = " + playbackState);
        if (isLastReportedPlayWhenReady != playWhenReady || lastReportedPlaybackState != playbackState) {
            int buffer = 0;
            if (mInternalPlayer != null) {
                buffer = mInternalPlayer.getBufferedPercentage();
            }
            if (isBuffering) {
                switch (playbackState) {
                    case Player.STATE_ENDED:
                    case Player.STATE_READY:
                        notifyOnInfo(IMediaPlayer.MEDIA_INFO_BUFFERING_END, buffer);
                        isBuffering = false;
                        break;
                }
            }

            if (isPreparing) {
                switch (playbackState) {
                    case Player.STATE_READY:
                        notifyOnPrepared();
                        isPreparing = false;
                        break;
                }
            }

            switch (playbackState) {
                case Player.STATE_BUFFERING:
                    notifyOnInfo(IMediaPlayer.MEDIA_INFO_BUFFERING_START, buffer);
                    isBuffering = true;
                    break;
                case Player.STATE_READY:
                    break;
                case Player.STATE_ENDED:
                    notifyOnCompletion();
                    break;
                default:
                    break;
            }
        }
        isLastReportedPlayWhenReady = playWhenReady;
        lastReportedPlaybackState = playbackState;
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(@NonNull PlaybackException error) {
        notifyOnError(IMediaPlayer.MEDIA_ERROR_UNKNOWN, IMediaPlayer.MEDIA_ERROR_UNKNOWN);
    }

    @Override
    public void onPositionDiscontinuity(Player.PositionInfo oldPosition, Player.PositionInfo newPosition, int reason) {
        notifyOnInfo(ON_POSITION_DISCOUNTINUITY, reason);
        if (reason == DISCONTINUITY_REASON_SEEK) {
            notifyOnSeekComplete();
        }
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    /////////////////////////////////////AudioRendererEventListener/////////////////////////////////////////////


    @Override
    public void onPlayWhenReadyChanged(EventTime eventTime, boolean playWhenReady, int playbackState) {

    }

    @Override
    public void onTimelineChanged(EventTime eventTime, int reason) {

    }


    @Override
    public void onPlaybackParametersChanged(EventTime eventTime, PlaybackParameters playbackParameters) {

    }

    @Override
    public void onRepeatModeChanged(EventTime eventTime, int repeatMode) {

    }

    @Override
    public void onShuffleModeChanged(EventTime eventTime, boolean shuffleModeEnabled) {

    }

    @Override
    public void onIsLoadingChanged(EventTime eventTime, boolean isLoading) {

    }

    @Override
    public void onBandwidthEstimate(EventTime eventTime, int totalLoadTimeMs, long totalBytesLoaded, long bitrateEstimate) {

    }

    @Override
    public void onMetadata(EventTime eventTime, Metadata metadata) {

    }

    @Override
    public void onAudioDisabled(EventTime eventTime, DecoderCounters counters) {
        audioSessionId = C.AUDIO_SESSION_ID_UNSET;
    }

    @Override
    public void onAudioUnderrun(EventTime eventTime, int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {

    }

    @Override
    public void onDroppedVideoFrames(EventTime eventTime, int droppedFrames, long elapsedMs) {

    }

    @Override
    public void onVideoSizeChanged(EventTime eventTime, VideoSize videoSize) {
        mVideoWidth = (int) (videoSize.width * videoSize.pixelWidthHeightRatio);
        mVideoHeight = videoSize.height;
        notifyOnVideoSizeChanged((int) (videoSize.width * videoSize.pixelWidthHeightRatio), videoSize.height, 1, 1);
        if (videoSize.unappliedRotationDegrees > 0)
            notifyOnInfo(IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED, videoSize.unappliedRotationDegrees);
    }


    @Override
    public void onRenderedFirstFrame(EventTime eventTime, Object output, long renderTimeMs) {

    }

    @Override
    public void onDrmKeysLoaded(EventTime eventTime) {

    }

    @Override
    public void onDrmSessionManagerError(EventTime eventTime, Exception error) {

    }

    @Override
    public void onDrmKeysRestored(EventTime eventTime) {

    }

    @Override
    public void onDrmKeysRemoved(EventTime eventTime) {

    }
}
