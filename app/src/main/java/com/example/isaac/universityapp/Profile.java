package com.example.isaac.universityapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Profile extends AppCompatActivity {
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Button getPoints;
    private TextView displayPoints;
    private int points = 0;
    public static final String MY_PREFS = "MyPrefs";

   @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.profile);

       prefs = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
       editor = prefs.edit();

       if (prefs != null)
           points = prefs.getInt("points", 0);
       displayPoints = (TextView) findViewById(R.id.displayPoints);
       getPoints = (Button) findViewById(R.id.ShowPoints);
       displayPoints.setText(String.valueOf(points));
   }

   public void resetPoints(View view) {
       if (prefs != null) {
           editor.putInt("points", 0);
           editor.apply();
       }
       displayPoints.setText(String.valueOf(points));
   }

   public void getPoints(View view) {
       Toast.makeText(this, String.valueOf(points), Toast.LENGTH_SHORT).show();
   }
}
