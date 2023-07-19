package com.emuneee.marshmallowfm;

import static java.lang.Thread.sleep;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ServiceConnection {

    private MediaController mMediaController;
    private ImageButton mPlayButton;
    private TextView mTitle;

    private Handler mHandler;
    private HandlerThread mHandlerThread;

    private MediaController.Callback mMediaControllerCallback = new MediaController.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackState state) {
            Log.e("marshmallowfm-activity", "onPlaybackStateChanged state " + state.getState());
            switch (state.getState()) {
                case PlaybackState.STATE_NONE:
                    mPlayButton.setImageResource(R.mipmap.ic_play);
                    break;
                case PlaybackState.STATE_PLAYING:
                    mPlayButton.setImageResource(R.mipmap.ic_pause);
                    break;
                case PlaybackState.STATE_PAUSED:
                    mPlayButton.setImageResource(R.mipmap.ic_play);
                    break;
                case PlaybackState.STATE_FAST_FORWARDING:
                    break;
                case PlaybackState.STATE_REWINDING:
                    break;
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            super.onMetadataChanged(metadata);
            Log.e("marshmallowfm-activity", "onMetadataChanged title " + metadata.getString(MediaMetadata.METADATA_KEY_TITLE));
            mTitle.setText(metadata.getString(MediaMetadata.METADATA_KEY_TITLE));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPlayButton = (ImageButton) findViewById(R.id.play);
        mTitle = (TextView) findViewById(R.id.title);
        mPlayButton.setOnClickListener(this);
        findViewById(R.id.rewind).setOnClickListener(this);
        findViewById(R.id.forward).setOnClickListener(this);
        mHandlerThread = new HandlerThread("just test");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        Intent intent = new Intent(this, AudioPlayerService.class);
        getApplicationContext().bindService(intent, this, 0);
        Log.e("marshmallowfm-activity", "onCreate");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play:
                if (mMediaController.getPlaybackState().getState() == PlaybackState.STATE_PLAYING) {
                    Log.e("marshmallowfm-activity", "onClick pause");
                    mMediaController.getTransportControls().pause();
                } else if(mMediaController.getPlaybackState().getState() == PlaybackState.STATE_PAUSED){
                    Log.e("marshmallowfm-activity", "onClick play");
                    mMediaController.getTransportControls().play();
                } else {
                    Uri uri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath() + "//yeliangxing.mp3");
                    Log.e("marshmallowfm-activity", "onClick other");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        mMediaController.getTransportControls().playFromUri(uri, null);
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(AudioPlayerService.PARAM_TRACK_URI, uri);
                        mMediaController.getTransportControls().playFromSearch("", bundle);
                    }


                }
                break;
            case R.id.rewind:
                mMediaController.getTransportControls().rewind();
                Log.e("marshmallowfm-activity", "onClick rewind");
                break;
            case R.id.forward:
                Log.e("marshmallowfm-activity", "onClick forward");
                mMediaController.getTransportControls().fastForward();
                break;
        }

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.e("marshmallowfm-activity", "onServiceConnected");
        if (service instanceof AudioPlayerService.ServiceBinder) {
            mMediaController = new MediaController(MainActivity.this,
                    ((AudioPlayerService.ServiceBinder) service).getService().getMediaSessionToken());
            mMediaController.registerCallback(mMediaControllerCallback);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.e("marshmallowfm-activity", "onServiceDisconnected");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("marshmallowfm-activity", "onStart");
        Intent intent = new Intent(this, AudioPlayerService.class);
        startService(intent);
    }
}
