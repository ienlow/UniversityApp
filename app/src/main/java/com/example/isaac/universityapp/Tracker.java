package com.example.isaac.universityapp;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
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

import java.util.Locale;

import static com.example.isaac.universityapp.Profile.MY_PREFS;

public class Tracker extends Service implements GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private LocationCallback mLocationCallback;
    private LatLng lebanon = new LatLng(36.896034, -82.068117);
    private CircleOptions one;
    private LocationRequest mLocationRequest = new LocationRequest();
    private boolean mRequestingLocationUpdates;
    private boolean finished = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private DynamoDBMapper dynamoDBMapper;
    private LocationsDO locationItem;
    private int i, j, points = 0;
    private long time;
    private BroadcastReceiver br;
    private long timeLeftInMilliseconds = 0, startTime = 0, timeSwapBuff = 0, updateTime = 0;//10 mins
    boolean timerPaused = false, timerStarted = false;
    private int seconds, minutes, hours;
    private Handler handler;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Notification notification;
    public static final String MY_PREFS = "MyPrefs";

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        editor = prefs.edit();
        handler = new Handler();
        notification = new Notification();
        startForeground(1, notification);
        if (prefs != null) {
            editor.putBoolean("Tracking", true);
            editor.apply();
        }

        //AWSMobileClient.getInstance().initialize(this).execute();

        // Instantiate a AmazonDynamoDBMapperClient
        final AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();

        Long g = new Intent(this, MainMenu.class).getLongExtra("Time", 0);
        Log.d("Time", String.valueOf(g));

        new Thread(new Runnable() {
            @Override
            public void run() {

                locationItem = dynamoDBMapper.load(
                        LocationsDO.class,
                        "ienlow",
                        "1");

                // Item read
                // Log.d("News Item:", newsItem.toString());
                finished = true;
            }
        }).start();


        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                    LatLng mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    try {
                        if (((locationItem.getLongitude() - mCurrentLocation.longitude) < .001)
                                && ((locationItem.getLongitude() - mCurrentLocation.longitude) > -.001)
                                && ((locationItem.getLatitude() - mCurrentLocation.latitude) < .001)
                                && ((locationItem.getLatitude() - mCurrentLocation.latitude) > -.001)) {
                            j++;
                            Intent intentTwo = new Intent("Success");
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcastSync(intentTwo);
                            //Toast.makeText(getApplicationContext(), String.valueOf(j), Toast.LENGTH_SHORT).show();
                            i = 0;
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
                        else {
                            Intent intent = new Intent("Fail");
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcastSync(intent);
                            if (i == 0) {
                                timerPaused = true;
                            }
                            i++;
                        }
                    }
                    catch (Exception E){
                        Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                    }
                }
            };
        };

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationItem = new LocationsDO();
        startTracking();
    }

    public void startTracking () {
        createLocationRequest();
        startLocationUpdates();
    }
    /**
     * Create request to update location.
     */
    protected void createLocationRequest() {
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        mRequestingLocationUpdates = true;
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
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

                handler.post(this);
                Log.d("Time", String.valueOf(seconds));
            }
            else {
                editor = getSharedPreferences(MY_PREFS, MODE_PRIVATE).edit();
                if (prefs != null)
                    points = prefs.getInt("points", 0);
                editor.putInt("points", seconds + points);
                editor.apply();
            }
            //Log.d("tag", String.valueOf(timeLeftInMilliseconds));
        }
    };

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        Intent intent = new Intent("Fail");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcastSync(intent);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(br);
        Toast.makeText(this, "Destroy", Toast.LENGTH_SHORT).show();
        if (prefs != null) {
            editor.putInt("points", seconds + points);
            editor.putBoolean("tracking", false);
            editor.apply();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
