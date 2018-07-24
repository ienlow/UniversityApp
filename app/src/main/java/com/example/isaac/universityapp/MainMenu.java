package com.example.isaac.universityapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.example.isaac.universityapp.MapsActivity;
import com.example.isaac.universityapp.R;

import java.util.Locale;

/**
 * Created by isaac on 2/24/2018.
 */

public class MainMenu extends AppCompatActivity {
    Intent intent;
    private int i;
    private DynamoDBMapper dynamoDBMapper;
    private Button button6;
    private CountDownTimer timer;
    private long timeLeftInMilliseconds = 0, startTime = 0, timeSwapBuff = 0, updateTime = 0;//10 mins
    private TextView timerText;
    private ProgressBar progress;
    private BroadcastReceiver br;
    boolean timerPaused = false;
    boolean timerStarted = false;
    private int seconds, minutes, hours;
    private long timeHolder = 0;
    private Tag tag;
    private Object lock;
    private Handler handler;
    //Radford long = -80.5764477 lat = 37.1318

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);

        AWSMobileClient.getInstance().initialize(this).execute();
        button6 = findViewById(R.id.button6);
        button6.setText("vs. Georgia Southern \n @ Dedmon Center");

        // Instantiate a AmazonDynamoDBMapperClient
        final AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();

        Intent intent = new Intent(this, Tracker.class);
        startService(intent);
        lock = new Object();

        timerText = findViewById(R.id.timer);
        handler = new Handler();
        //progress = findViewById(R.id.progressBar2);
        //progress.setVisibility(View.VISIBLE);

        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("Success")) {
                    i = 0;
                    //progress.setVisibility(View.INVISIBLE);
                    if (!timerStarted) {
                        startTime = SystemClock.uptimeMillis();
                        handler.post(updateTimer);
                    }
                    if (timerPaused) {
                        startTime = SystemClock.uptimeMillis();
                        handler.post(updateTimer);
                        timerPaused = false;
                    }
                }
                else if (intent.getAction().equals("Fail")) {
                    if (i == 0) {
                        handler.removeCallbacks(updateTimer);
                        timeSwapBuff += timeLeftInMilliseconds;
                        timerPaused = true;
                    }
                    i++;
                    // progress.setVisibility(View.INVISIBLE);
                    Log.d("fail", "fail");
                }
            }
        };
    }

    Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            timerStarted = true;
            if (!timerPaused) {
                timeLeftInMilliseconds = SystemClock.uptimeMillis() - startTime;
                updateTime = timeSwapBuff + timeLeftInMilliseconds;
                seconds = (int) (updateTime / 1000);
                minutes = (int) (seconds / 60);
                hours = minutes / 60;
                seconds = seconds % 60;

                String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);

                timerText.setText(timeLeftFormatted);
                handler.post(this);
            }
            Log.d("tag", String.valueOf(timeLeftInMilliseconds));
        }
    };

    protected void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(br, new IntentFilter("Started"));
        LocalBroadcastManager.getInstance(this).registerReceiver(br, new IntentFilter("Success"));
        LocalBroadcastManager.getInstance(this).registerReceiver(br, new IntentFilter("Fail"));
    }

    protected void onPause () {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(br);
    }

    public void mapsStart(View view) {
        intent = new Intent(this, Timer.class);
        startActivity(intent);
    }

    public void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(this, Tracker.class);
        stopService(intent);
    }
}
