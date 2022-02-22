package com.example.gestureinteraction;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.gestureinteraction.databinding.ActivityMusicLayoutBinding;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MusicLayout extends GestureActivity{
    private ActivityMusicLayoutBinding binding;
    private TextView mTextViewTitle, mTextViewTime, mTextViewDuration;
    private ImageView mImageViewArt;
    private CircleProgressBar mProgressBar;
    private ImageButton mImageButtonControl;
    private CountDownTimer timer;
    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private AudioManager audioManager;
    private View v;

    private String title;
    private int duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMusicLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        v = this.findViewById(android.R.id.content);

        mTextViewTitle = binding.textViewHeader;
        mTextViewTime = binding.textViewTime;
        mTextViewDuration = binding.textViewDuration;
        mImageViewArt = binding.imageViewArt;
        mImageButtonControl = binding.imageButtonControl;
        mProgressBar = binding.progressBar;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            title = extras.getString("song_name");
            loadItem();
        }
        updateUI("Null");
    }

    public void loadItem(){
        String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "GestInteraction" + File.separator + "music";
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(path + File.separator + title);
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(path + File.separator + title);
            mMediaPlayer.prepare();
            String albumName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            duration = (int) Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

            byte[] art = mmr.getEmbeddedPicture();
            Bitmap songImage = BitmapFactory.decodeByteArray(art, 0, art.length);
            mTextViewTime.setText(String.format("%02d:%02d", 0, 0));
            mTextViewDuration.setText(String.format("-%02d:%02d", (duration/1000)/60, (duration/1000)%60));
            mProgressBar.setMax(duration);
            mProgressBar.setProgress(0);
            /*
            mProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                    int remainTime = duration - progress;
                    int remainMin = (remainTime/1000)/60;
                    int remainSec = (remainTime/1000)%60;
                    mTextViewTime.setText(String.format("%02d:%02d", (progress/1000)/60, (progress/1000)%60));
                    mTextViewDuration.setText(String.format("-%02d:%02d", remainMin, remainSec));
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    if(timer != null) timer.cancel();
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int remainTime = duration - seekBar.getProgress();
                    if(timer != null){
                        start(remainTime);
                        mImageButtonControl.setImageResource(android.R.drawable.ic_media_pause);
                        mImageButtonControl.setContentDescription("Pause");
                    }
                }
            });*/
            mTextViewTitle.setText(albumName);
            mTextViewTitle.setTextColor(Color.CYAN);
            mImageViewArt.setImageBitmap(songImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mImageButtonControl.setImageResource(android.R.drawable.ic_media_play);
        mImageButtonControl.setContentDescription("Play");
        mImageButtonControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mImageButtonControl.getContentDescription().equals("Play")){
                    mImageButtonControl.setImageResource(android.R.drawable.ic_media_pause);
                    mImageButtonControl.setContentDescription("Pause");
                    int currTime = (int) mProgressBar.getProgress();
                    start(duration - currTime);
                    setPopupWindow(view, "Play", android.R.drawable.ic_media_play);
                } else {
                    timer.cancel();
                    mImageButtonControl.setImageResource(android.R.drawable.ic_media_play);
                    mImageButtonControl.setContentDescription("Play");
                    mMediaPlayer.pause();
                    setPopupWindow(view, "Pause", android.R.drawable.ic_media_pause);
                }

            }
        });
    }

    private void start(int timeStamp){
        mMediaPlayer.seekTo(duration - timeStamp);
        mMediaPlayer.start();
        timer = new CountDownTimer(timeStamp, 1000) {
            @Override
            public void onTick(long l) {
                mProgressBar.setProgress(duration - (int) l);
                int remainTime = duration - (int) l;
                int remainMin = (remainTime/1000)/60;
                int remainSec = (remainTime/1000)%60;
                mTextViewTime.setText(String.format("%02d:%02d", remainMin, remainSec));
                mTextViewDuration.setText(String.format("-%02d:%02d", ((int) l/1000)/60, ((int) l/1000)%60));
            }
            @Override
            public void onFinish() {
                mImageButtonControl.setImageResource(android.R.drawable.ic_media_play);
                mImageButtonControl.setContentDescription("Play");
                timer.cancel();
            }
        }.start();
    }

    private void prevSong(){
        List<String> items = new ArrayList<>();
        if(timer != null){
            timer.cancel();
            mProgressBar.setProgress(0);
        }
        try {
            items = Arrays.asList(getAssets().list("music"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        int pIndex = items.indexOf(title) - 1;
        if (pIndex < 0){
            pIndex = items.size() - 1;
        }
        title = items.get(pIndex);
        loadItem();
        setPopupWindow(v, "Previous Song", android.R.drawable.ic_media_previous);
    }

    private void nextSong(){
        List<String> items = new ArrayList<>();
        if(timer != null){
            timer.cancel();
            mProgressBar.setProgress(0);
        }
        try {
            items = Arrays.asList(getAssets().list("music"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        int nIndex = items.indexOf(title) + 1;
        if (nIndex > items.size() - 1){
            nIndex = 0;
        }
        title = items.get(nIndex);
        loadItem();
        setPopupWindow(v, "Next Song", android.R.drawable.ic_media_next);
    }

    private void turnUpVolume(){
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int currVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int newVolume = currVolume + 1;
        if (newVolume > maxVolume){
            newVolume = maxVolume;
        }
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
        setPopupWindow(v, String.format("Volume: %d", newVolume), R.drawable.ic_baseline_volume_up, maxVolume, newVolume);
    }

    private void turnDownVolume(){
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int currVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int newVolume = currVolume - 1;
        if (newVolume < 0){
            newVolume = 0;
        }
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
        setPopupWindow(v, String.format("Volume: %d", newVolume), R.drawable.ic_baseline_volume_down, maxVolume, newVolume);
    }

    private void forward(){
        if(timer != null){
            timer.cancel();
            int remainTime = duration - (int) mProgressBar.getProgress() - 5000;
            if(remainTime > duration){ remainTime = duration; }
            start(remainTime);
            mImageButtonControl.setImageResource(android.R.drawable.ic_media_pause);
            mImageButtonControl.setContentDescription("Pause");
            setPopupWindow(v, "Forward 5s", android.R.drawable.ic_media_ff);
        }
    }

    private void rewind(){
        if(timer != null){
            timer.cancel();
            int remainTime = duration - (int) mProgressBar.getProgress() + 5000;
            if(remainTime < 0) remainTime = 0;
            start(remainTime);
            mImageButtonControl.setImageResource(android.R.drawable.ic_media_pause);
            mImageButtonControl.setContentDescription("Pause");
            setPopupWindow(v, "Rewind 5s", android.R.drawable.ic_media_rew);
        }
    }

    public void setPopupWindow(View view, String message, int type, int max, int progress){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_icon, null);
        PopupWindow popupWindow = new PopupWindow(popupView, ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);
        ImageView icon = (ImageView) popupWindow.getContentView().findViewById(R.id.imageView_popupIcon);
        TextView text = (TextView) popupWindow.getContentView().findViewById(R.id.textView_popupMessage);
        ProgressBar bar = (ProgressBar) popupWindow.getContentView().findViewById(R.id.progressBar_popup);
        if (type != R.drawable.ic_baseline_volume_down && type != R.drawable.ic_baseline_volume_up){
            bar.setVisibility(View.GONE);
        } else {
            bar.setVisibility(View.VISIBLE);
            bar.setMax(max);
            bar.setProgress(progress);
        }
        icon.setImageResource(type);
        text.setText(message);
        popupWindow.setWidth(size.x);
        popupWindow.setHeight(size.y);
        view.post(new Runnable() {
            @Override
            public void run() {
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
                new Handler().postDelayed(new Runnable(){
                    public void run() {
                        popupWindow.dismiss();
                    }
                }, 500);
            }
        });
    }

    @Override
    public void updateUI(String result) {
        switch (result){
            case "Finger Pinching":
                mImageButtonControl.callOnClick();
                break;
            case "Wrist Lifting":
                forward();
                break;
            case "Wrist Dropping":
                rewind();
                break;
            case "CW Circling":
                nextSong();
                break;
            case "CCW Circling":
                prevSong();
                break;
            case "Hand Right Flipping":
                turnUpVolume();
                break;
            case "Hand Left Flipping":
                turnDownVolume();
                break;
            case "Hand Rotation":
                finish();
                break;
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        mMediaPlayer.reset();
    }
}