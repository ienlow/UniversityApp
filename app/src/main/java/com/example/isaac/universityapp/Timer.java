package com.example.isaac.universityapp;

import android.*;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.ServiceState;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedList;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.s3.model.transform.Unmarshallers;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by isaac on 3/20/2018.
 */


public class Timer extends AppCompatActivity {
    private CountDownTimer timer;
    private long timeLeftInMilliseconds = 600000;//10 mins
    private TextView timerText;
    private ProgressBar progress;
    private BroadcastReceiver br;
    boolean timerPaused = false;
    private int seconds;
    private int minutes;
    private long timeHolder = 0;
    private Tag tag;
    private Object lock;
    //Radford long = -80.5764477 lat = 37.1318

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, Tracker.class);
        startService(intent);
        setContentView(R.layout.timer_layout);
        lock = new Object();

        timerText = findViewById(R.id.timer);
        progress = findViewById(R.id.progressBar2);
        progress.setVisibility(View.VISIBLE);

            br = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals("Success")) {
                        progress.setVisibility(View.INVISIBLE);
                        startTimer();
                        timerPaused = false;
                    }
                    else if (intent.getAction().equals("Fail")) {
                        progress.setVisibility(View.INVISIBLE);
                        pauseTimer();
                        timerPaused = true;
                        Log.d("fail", "fail");
                        if (timerPaused) {
                            synchronized (lock) {
                                try {
                                    timer.wait();
                                } catch (InterruptedException e) {
                                }
                            }
                        }
                    }
                }
            };
    }

    public void startTimer() {
            timer = new CountDownTimer(timeLeftInMilliseconds, 1000) {
                @Override
                public void onTick(long l) {
                    timeLeftInMilliseconds = l;
                    updateTimer();
                    timeHolder = l; // Update the time holder to the current time left
                    Log.d("tag", String.valueOf(timeHolder));
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                }

                @Override
                public void onFinish() {

                }
            }.start();
    }

    public void pauseTimer() {
        timeLeftInMilliseconds = timeHolder; // Update time left to time holder if timer paused
        timer.cancel();
    }

    public void updateTimer() {
        //if (!timerPaused) {
            seconds = (int) (timeLeftInMilliseconds / 1000);
            minutes = (int) (seconds / 60);
            seconds = seconds % 60;

            String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

            timerText.setText(timeLeftFormatted);
        //}
    }

    protected void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(br, new IntentFilter("Started"));
        LocalBroadcastManager.getInstance(this).registerReceiver(br, new IntentFilter("Success"));
        LocalBroadcastManager.getInstance(this).registerReceiver(br, new IntentFilter("Fail"));
    }

    protected void onPause (){
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(br);
    }
}
