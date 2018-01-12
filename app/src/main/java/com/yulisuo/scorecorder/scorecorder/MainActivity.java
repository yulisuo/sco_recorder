package com.yulisuo.scorecorder.scorecorder;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "recorder";
    private AudioManager am;
    private Button btnStart,btnStop;
    private MediaRecorder mediaRecorder;
    private int mScoState = AudioManager.SCO_AUDIO_STATE_DISCONNECTED;
    private MyReceiver mReceiver;
    private String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnStart = findViewById(R.id.button_start);
        btnStop = findViewById(R.id.button_stop);
        check();
    }

    private void check() {
        int grantedCount = 0;
        for (int i = 0;i < permissions.length; i++) {
            if (checkSelfPermission(permissions[i]) == PackageManager.PERMISSION_GRANTED) {
                grantedCount++;
            }
        }
        if (grantedCount == permissions.length) {
            setButtonClickListener();
        } else {
            requestPermissions(permissions, 100);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int grantResultSum = 0;
        for(int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                grantResultSum += grantResult;
            }
        }
        Log.i(TAG, "onRequestPermissionsResult,grantResultSum:" + grantResultSum);
        if (requestCode == 100 && (grantResultSum == permissions.length)) {
            setButtonClickListener();
        }
    }


    private void setButtonClickListener() {
        Log.i(TAG, "setButtonClickListener");
        if (btnStart != null && btnStop != null) {
            btnStart.setOnClickListener(this);
            btnStop.setOnClickListener(this);
        }
    }

    private void start() {
        Log.i(TAG, "start");
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mReceiver = new MyReceiver();
        registerReceiver(mReceiver, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));
        am.startBluetoothSco();
        if (mScoState == AudioManager.SCO_AUDIO_STATE_CONNECTED) {
            record();
        }
    }

    private void stop() {
        am.stopBluetoothSco();
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
        }
        unregisterReceiver(mReceiver);
        mScoState = AudioManager.SCO_AUDIO_STATE_DISCONNECTED;      //reset
    }

    private void record() {
        Log.i(TAG, "record");
        mediaRecorder = new MediaRecorder();
        mediaRecorder.reset();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        String filePath = Environment.getExternalStorageDirectory().getPath()
                + File.separator + "Music" + File.separator + "m1.3gp";
        Log.i(TAG, "record file path:" + filePath);
        mediaRecorder.setOutputFile(filePath);
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_start:
                start();
                break;
            case R.id.button_stop:
                stop();
                break;
            default:
                break;
        }
    }

    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "receiver:" + action);
            if (action.equals(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)) {
                mScoState = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, AudioManager.SCO_AUDIO_STATE_DISCONNECTED);
                Log.i(TAG, "receiver,mScoState:" + mScoState);
                if (mScoState == AudioManager.SCO_AUDIO_STATE_CONNECTED) {
                    record();
                }
            }
        }
    }
}
