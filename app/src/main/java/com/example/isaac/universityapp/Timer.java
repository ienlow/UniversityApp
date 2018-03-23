package com.example.isaac.universityapp;

import android.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import java.util.Map;
import java.util.Set;

/**
 * Created by isaac on 3/20/2018.
 */


public class Timer extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private LocationCallback mLocationCallback;
    private LatLng lebanon = new LatLng(36.896034, -82.068117);
    private CircleOptions one;
    private LocationRequest mLocationRequest = new LocationRequest();
    private boolean mRequestingLocationUpdates;
    private FusedLocationProviderClient mFusedLocationClient;
    private DynamoDBMapper dynamoDBMapper;
    private LocationsDO locationItem;
    private int i, j;
    private String tracking;
    Intent intent;
    private Bundle b;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = new Intent(getApplicationContext(), MainMenu.class);
//        savedInstanceState.putBoolean(tracking, mRequestingLocationUpdates);
        b = this.getIntent().getExtras();
        if (b != null)
            j = b.getInt("Counter");

        setContentView(R.layout.timer_layout);
        AWSMobileClient.getInstance().initialize(this).execute();

        Button button = (Button) findViewById(R.id.button4);
        button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (j == 0) {
                        startTracking();
                        j = 1;
                    }
                }
            });

        // Instantiate a AmazonDynamoDBMapperClient
        final AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
        dynamoDBClient.setRegion(Region.getRegion(Regions.US_EAST_1));
         dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();

        // Initialize Google API
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationItem = new LocationsDO();

        new Thread(new Runnable() {
            @Override
            public void run() {

                locationItem = dynamoDBMapper.load(
                        LocationsDO.class,
                        "ienlow",
                        "1");

                // Item read
                // Log.d("News Item:", newsItem.toString());
            }
        }).start();
        //Toast.makeText(getApplicationContext(), "Create", Toast.LENGTH_SHORT).show();
//        savedInstanceState.putBoolean("tracking", mRequestingLocationUpdates);
    }

    public void startTracking () {
        createLocationRequest();
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                    LatLng mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    if (((locationItem.getLongitude() - mCurrentLocation.longitude) < .001)
                            && ((locationItem.getLongitude() - mCurrentLocation.longitude) > -.001)
                            && ((locationItem.getLatitude() - mCurrentLocation.latitude) < .001)
                            && ((locationItem.getLatitude() - mCurrentLocation.latitude) > -.001)) {
                        i++;
                        Toast.makeText(getApplicationContext(), String.valueOf(i), Toast.LENGTH_SHORT).show();
                    }
                }
            };
        };
        startLocationUpdates();
    }
    /**
     * Create request to update location.
     */
    protected void createLocationRequest() {
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        mRequestingLocationUpdates = true;
    }

    /*@Override
    protected void onResume() {
        super.onResume();
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        //Toast.makeText(getApplicationContext(), "Resume", Toast.LENGTH_SHORT).show();

    }*/

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

    @Override
    protected void onPause() {
        super.onPause();
        //stopLocationUpdates();
        intent = new Intent(this, MainMenu.class);
        b = new Bundle();
        b.putInt("Counter", j);
        intent.putExtras(b);
        startActivity(intent);
        Toast.makeText(getApplicationContext(), String.valueOf(j), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /*@Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }*/

    @Override
    public void onStop() {
        super.onStop();
        //mGoogleApiClient.disconnect();
        Log.i("Stop", "connection");

    }
}
