package com.example.lab3group4;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    public static final String SENSOR_NUMBER = "com.example.SENSOR_NUMBER";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



    }

    public void clickAccelerometer(View view) {

        // Accelerometer sensor # = 1
        int number = 1;
        Intent intent = new Intent(this, SensorActivity.class);
        intent.putExtra(SENSOR_NUMBER, number);
        startActivity(intent);

    }

    public void clickGyroscope(View view) {

        // Gyroscope sensor # = 2
        int number = 2;
        Intent intent = new Intent(this, SensorActivity.class);
        intent.putExtra(SENSOR_NUMBER, number);
        startActivity(intent);

    }

    public void clickProximity(View view) {

        // Proximity sensor # = 3
        int number = 3;
        Intent intent = new Intent(this, SensorActivity.class);
        intent.putExtra(SENSOR_NUMBER, number);
        startActivity(intent);

    }
}