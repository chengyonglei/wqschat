package com.wqs.wechat.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.appcompat.widget.AppCompatTextView;

import com.blankj.utilcode.util.ToastUtils;
import com.wqs.wechat.R;

import java.io.IOException;
import java.net.URI;

import butterknife.internal.ListenerClass;


/**
 * Incremental change is better than ambitious failure.
 *
 * @author : <a href="http://mysticcoder.coding.me">MysticCoder</a>
 * @date : 2018/3/21
 * @desc : 不用时一定要调用{{@link #release()}}
 */


public class AudioPlayerView extends AppCompatTextView {

    Uri mUrl ;

    public boolean isHasprepared() {
        return hasprepared;
    }

    /**
     * 在非初始化状态下调用setDataSource  会抛出IllegalStateException异常
     */
    boolean hasprepared =false;

    private MediaPlayer mediaPlayer;

    private Handler handler;
    private Runnable runnable;
    private int i;

    private int[] drawLefts = new int[]{R.mipmap.audio_icon1, R.mipmap.audio_icon2,R.mipmap.audio_icon3};

    public AudioPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initMediaPlayer();
    }
    private void initMediaPlayer(){
        setGravity(TEXT_ALIGNMENT_CENTER);
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        } catch (Exception e) {
            Log.e("mediaPlayer", " init error", e);
        }

        if (mediaPlayer != null){
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    hasprepared = true;
                    setText(getDuration());
                }
            });
        }
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mediaPlayer.reset();
                return false;
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                stopAnim();
            }
        });
        setClick();

    }

    public void setUrl(Uri url){

        mUrl = url;
        try {
            mediaPlayer.setDataSource(getContext(),url);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("mediaPlayer", " set dataSource error", e);
        } catch (IllegalStateException e){
            Log.e("mediaPlayer", " set dataSource error", e);
        }
    }

    /**
     * 用于需要设置不同的dataSource
     * 二次setdataSource的时候需要reset 将MediaPlayer恢复到Initialized状态
     * @param url
     */
    public void resetUrl(Uri url){

        if (TextUtils.isEmpty(mUrl.toString())||hasprepared){
            mediaPlayer.reset();
        }
        setUrl(url);
    }

    public String getDuration(){

        int duration = mediaPlayer.getDuration();

        if (duration == -1){
            return "";
        }else {

            int sec = duration/1000;

            int m = sec/60;
            int s = sec%60;
            return m+":"+s;
        }

    }
    private void startAnim() {

        i = 0;
        if (handler == null) {
            handler = new Handler();
        }
        if (runnable == null) {
            runnable = new Runnable() {
                @Override
                public void run() {

                    handler.postDelayed(this, 500);
                    setDrawableLeft(drawLefts[i % 3]);
                    i++;
                }
            };
        }

        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 500);
    }

    private void stopAnim(){

        setDrawableLeft(drawLefts[2]);
        if (handler != null){
            handler.removeCallbacks(runnable);
        }
    }

    /**
     *  设置 drawableLeft
     *
     * @param
     * @param id
     */
    private void setDrawableLeft(@DrawableRes int id) {
        Drawable leftDrawable = getResources().getDrawable(id);
        leftDrawable.setBounds(0, 0, leftDrawable.getMinimumWidth(), leftDrawable.getMinimumHeight());
        setCompoundDrawables(leftDrawable, null, null, null);
        setCompoundDrawablePadding(20);

    }
    private void removeDrawableLeft(){
        setCompoundDrawables(null,null,null,null);
    }

    private void setClick(){

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    removeDrawableLeft();
                    stopAnim();
                }else {
                    ToastUtils.showShort(mUrl.toString());
                    mediaPlayer.seekTo(0);
                    startAnim();
                    mediaPlayer.start();
                }
            }

        });
    }

    /**
     * 释放资源
     */
    public  void release() {
        if ( mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();

            mediaPlayer = null;
        }

        if (handler != null){
            handler.removeCallbacks(runnable);
        }
    }

}

