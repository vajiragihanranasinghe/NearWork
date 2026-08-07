package com.example.nearwork;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    TextView locationText;
    Button locationButton;

    String[] radius = {
            "100 meters",
            "500 meters",
            "1 km",
            "10 km",
            "100 km",
            "1000 km",
            "Worldwide"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        locationText = findViewById(R.id.locationText);
        locationButton = findViewById(R.id.locationButton);

        Spinner spinner = findViewById(R.id.radiusSpinner);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        radius
                );

        spinner.setAdapter(adapter);


        locationButton.setOnClickListener(v -> {

            if(ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED){

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION
                        },
                        100
                );

            } else {

                locationText.setText(
                        "GPS Ready\nSearching your area..."
                );
            }

        });

    }
}
