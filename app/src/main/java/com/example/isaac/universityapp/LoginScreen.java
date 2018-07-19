package com.example.isaac.universityapp;

import com.amazonaws.mobile.client.AWSMobileClient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.util.Map;
import java.util.Set;

/**
 * Created by isaac on 2/21/2018.
 */



public class LoginScreen extends AppCompatActivity {

    Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editSharedPreferences;
    private EditText login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);
        context = getApplicationContext();
        sharedPreferences = context.getSharedPreferences(getString(R.string.preference_file), Context.MODE_PRIVATE);
        editSharedPreferences = sharedPreferences.edit();
        ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_COARSE_LOCATION}, 123);

        if (sharedPreferences != null) {
            //login.setText(sharedPreferences.getString("",""));
        }
    }

    /*
    Create Main Menu intent and save login info
     */
    public void mainMenu(View view) {
        login = findViewById(R.id.editText);
        editSharedPreferences.putString(getString(R.string.Username), login.getText().toString());
        editSharedPreferences.apply();
        Intent intent = new Intent(this, MainMenu.class);
        startActivity(intent);
        finish();
    }
}
