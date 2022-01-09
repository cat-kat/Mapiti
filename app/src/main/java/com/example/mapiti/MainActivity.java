package com.example.mapiti;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    static String ANDROID_ID;
    static ArrayList<MarkerOptions> markers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ANDROID_ID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        markers = new ArrayList<>();
        new myMap();

    }


    public void onClickSimpleUser(View v) {
        startActivity(new Intent(MainActivity.this, MapsActivity.class));

    }

    public void onClickProfile(View v) {
        startActivity(new Intent(MainActivity.this, ProfileActivity.class));

    }
}
