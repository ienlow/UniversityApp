package com.example.isaac.universityapp;

import com.amazonaws.mobile.client.AWSMobileClient;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

/**
 * Created by isaac on 2/21/2018.
 */



public class LoginScreen extends AppCompatActivity {
    private DynamoDBMapper dynamoDBMapper;
    private Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);
        ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_COARSE_LOCATION}, 123);

    }

    public void mainMenu(View view) {
        Intent intent = new Intent(this, MainMenu.class);
        startActivity(intent);
    }
}
