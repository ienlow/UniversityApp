package com.example.isaac.universityapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.example.isaac.universityapp.MapsActivity;
import com.example.isaac.universityapp.R;

/**
 * Created by isaac on 2/24/2018.
 */

public class MainMenu extends AppCompatActivity {
    Intent intent;
    private int i;
    private DynamoDBMapper dynamoDBMapper;
    private Button button6;

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
    }

    public void mapsStart(View view) {
        intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    public void onDestroy() {
        super.onDestroy();
        //Intent intent = new Intent(this, Tracker.class);
       // stopService(intent);
    }
}
