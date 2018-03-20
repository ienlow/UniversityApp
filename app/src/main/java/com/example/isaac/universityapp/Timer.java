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
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
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

/**
 * Created by isaac on 3/20/2018.
 */


public class Timer extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private LocationCallback mLocationCallback;
    private LatLng lebanon = new LatLng(36.896034, -82.068117);
    private CircleOptions one;
    private LocationRequest mLocationRequest = new LocationRequest();
    private boolean mRequestingLocationUpdates = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private DynamoDBMapper dynamoDBMapper;
    private LocationsDO locationItem;
    private IdentityManager identityManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setContentView(R.layout.timer_layout);
        AWSMobileClient.getInstance().initialize(this).execute();

        Button button = (Button) findViewById(R.id.button4);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTracking();
            }
        });

        // Instantiate a AmazonDynamoDBMapperClient
        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
        this.dynamoDBMapper = DynamoDBMapper.builder()
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
                locationItem = dynamoDBMapper.load(LocationsDO.class, "ienlow", "1");
            }
        }).start();

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                    LatLng mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    //mMap.addCircle(new CircleOptions().center(lebanon).radius(100));
                    //one = new CircleOptions().center(lebanon).radius(100);
                    if (((locationItem.getLongitude() - mCurrentLocation.longitude) < .001)
                            && ((locationItem.getLongitude() - mCurrentLocation.longitude) > -.001)
                            && ((locationItem.getLatitude() - mCurrentLocation.latitude) < .001)
                            && ((locationItem.getLatitude() - mCurrentLocation.latitude) > -.001)) {
                        Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                    }
                }
            };
        };
    }

    public void startTracking () {
        createLocationRequest();
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

    @Override
    protected void onResume() {
        super.onResume();
        /*if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }*/
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //stopLocationUpdates();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
        Log.i("Stop", "connection");
    }
}
