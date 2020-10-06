package com.example.lab3group4;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "system:";
    private SensorManager sensorManager;
    Sensor accelerometer;
    Sensor gyroscope;
    Sensor proximity;
    public int sensorNumber = 0;
    private float oldSenseVal;
    private boolean rest;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<BluetoothDevice> devices;
    private ArrayList<String> devicenames;
    private Set<BluetoothDevice> pairedDevices;
    private ArrayAdapter adapter;
    ListView listView;
    UUID uuid = UUID.fromString("fcb71e62-125e-4910-94eb-52582e5105ef");
    dataThread dataThreadObject;
    AcceptThread serverThread;
    ConnectThread clientThread;
    String newData;
    Boolean server;
    //Switch sw;
    RadioButton button1;
    RadioButton button2;
    RadioButton button3;
    RadioButton button4;
    RadioButton button5;
    RadioButton button6;
    RadioButton button7;


    // Create a BroadcastReceiver for ACTION_STATE_CHANGED. ie turning bluetooth on/off. not totally necessary
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);
                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d("TAG", "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d("TAG", "onReceive: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d("TAG", "onReceive: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d("TAG", "onReceive: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    // Create a BroadcastReceiver for discovering other devices
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
           final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                switch(state){
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d("TAG", "onReceive: discoverability enabled");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d("TAG", "onReceive: discoverable, able to receive connections");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d("TAG", "onReceive: discoverability disabled");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d("TAG", "onReceive: connecting");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d("TAG", "onReceive: connected");
                        break;
                }
            }
        }
    };

    // Create a BroadcastReceiver for discovering other devices
    private final BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                Log.d("TAG", "ACTION FOUND");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                devices.add(device);
                Log.d("TAG", "Device name: "+device.getName()+"\nAddress: "+device.getAddress());
                devicenames.add(device.getName());
                adapter.notifyDataSetInvalidated();

                list(findViewById(R.id.discover_button));
            }
        }
    };

    public void clientStart(View view){
        for (BluetoothDevice device:mBluetoothAdapter.getBondedDevices()) {
            clientThread = new ConnectThread(device);
            clientThread.start();
        }
        server = false;
    }

    public void serverStart(View view){
        for (BluetoothDevice device:mBluetoothAdapter.getBondedDevices()){
            serverThread = new AcceptThread(device);
            serverThread.start();
        }
        server = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mBroadcastReceiver1);
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        }
        try {
            unregisterReceiver(mBroadcastReceiver2);
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        }
        try {
            unregisterReceiver(mBroadcastReceiver3);
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        }
        if(dataThreadObject!=null){
            dataThreadObject.cancel();
        }
        if(clientThread!=null){
            clientThread.cancel();
        }
        if(serverThread!=null){
            serverThread.cancel();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        Intent intent = getIntent();
        sensorNumber = intent.getIntExtra(MainActivity.SENSOR_NUMBER, 0);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        oldSenseVal = 0;
        rest = false;

        //initialize arraylist objects and listView adapter
        devices = new ArrayList<BluetoothDevice>();
        devicenames = new ArrayList<String>();
        listView = findViewById(R.id.deviceList);
        adapter  = new ArrayAdapter<String>(this, R.layout.activity_sensor, devicenames);
        listView.setAdapter(adapter);
       // sw = (Switch) findViewById(R.id.deviceSwitch);
        //initialize bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        server = false;

        button1 = (RadioButton) findViewById(R.id.radioButton1);
        button2 = (RadioButton) findViewById(R.id.radioButton2);
        button3 = (RadioButton) findViewById(R.id.radioButton3);
        button4 = (RadioButton) findViewById(R.id.radioButton4);
        button5 = (RadioButton) findViewById(R.id.radioButton5);
        button6 = (RadioButton) findViewById(R.id.radioButton6);
        button7 = (RadioButton) findViewById(R.id.radioButton7);

    }

    public void enableDisableBT(View view){
        if(mBluetoothAdapter == null){
            Log.d("TAG", "Bluetooth not supported");
        }
        if(!mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: enabling BT.");

            IntentFilter BTintent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTintent);

            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBT);
            //sw.setChecked(true);
            //start discover for 30 seconds
            //enableDisableDiscover(view);
        }else{
           // sw.setChecked(false);
            Log.d(TAG, "enableDisableBT: disabling BT.");

            IntentFilter BTintent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTintent);
            mBluetoothAdapter.disable();
        }

    }

    public void enableDisableDiscover(View view){
        Log.d("TAG", "Starting discovery for 300 seconds");
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2, filter);
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
       //doOrNotDiscover(view);
    }

    public void doOrNotDiscover(View view){

        if(mBluetoothAdapter.isDiscovering()){
            //turn off
            mBluetoothAdapter.cancelDiscovery();
            Log.d("TAG", "Stopping discovery...");
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, filter);

            mBluetoothAdapter.startDiscovery();

        }else{
            //turn on
            Log.d("TAG", "Looking for unpaired devices...");
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, filter);
            mBluetoothAdapter.startDiscovery();
        }
        //update list view
        list(view);
    }

    public void list(View v) {
        ListView deviceList = (ListView) findViewById(R.id.deviceList);
        deviceList.setVisibility(View.VISIBLE);
        pairedDevices = mBluetoothAdapter.getBondedDevices();

        ArrayList list = new ArrayList();

        for (BluetoothDevice bt : pairedDevices) list.add(bt.getName());
        Toast.makeText(getApplicationContext(), "Showing Paired Devices", Toast.LENGTH_SHORT).show();

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);

        listView.setAdapter(adapter);
    }



    @Override
    protected void onResume() {
        super.onResume();

        if (sensorNumber == 1) {  // Accelerometer
            sensorManager.registerListener(SensorActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        } else if (sensorNumber == 2) { // Gyroscope
            sensorManager.registerListener(SensorActivity.this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        } else if (sensorNumber == 3) { // Proximity
            sensorManager.registerListener(SensorActivity.this, proximity, SensorManager.SENSOR_DELAY_NORMAL);
        } else { // Default - do nothing
            ;
        }

        if(mBroadcastReceiver1!=null){
            IntentFilter BTintent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTintent);
        }
        if(mBroadcastReceiver2!=null){
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
            registerReceiver(mBroadcastReceiver2, filter);
        }
        if(mBroadcastReceiver3!=null){
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, filter);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister sensor listener after it is done being used
        // There is only one lister registered every time the activity is launched (See onResume())
        sensorManager.unregisterListener(SensorActivity.this);
        if(mBroadcastReceiver1!=null){
            unregisterReceiver(mBroadcastReceiver1);
        }
        if(mBroadcastReceiver2!=null){
            unregisterReceiver(mBroadcastReceiver2);
        }
        if(mBroadcastReceiver3!=null){
            unregisterReceiver(mBroadcastReceiver3);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(!server){
            Float inData = new Float(0);

            if (sensorNumber == 1) {  // Accelerometer

                float[] values = event.values;
                float x = values[0];
                float y = values[1];
                float z = values[2];
                float accelerationSquareRoot;

                accelerationSquareRoot = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
                if(dataThreadObject!=null) {
                    byte[] outData = Float.toString(accelerationSquareRoot).getBytes();
                    dataThreadObject.write(outData);
                }
                inData = accelerationSquareRoot;

                // This if-statement is added in conjunction with the private variables: rest and oldSenseVal
                // The purpose is to only keep the peak values when shaking phone
                // This is used order to reduce noise on radio buttons
                if (accelerationSquareRoot < 1.5) {
                    rest = true;
                    return;
                }

                if (rest && (oldSenseVal > inData)) {

                    // Acceleration ranges of these if-statements were calibrated using a Galaxy S8
                    if (inData <= 1) {
                        button1.setChecked(false);
                        button2.setChecked(false);
                        button3.setChecked(false);
                        button4.setChecked(false);
                        button5.setChecked(false);
                        button6.setChecked(false);
                        button7.setChecked(false);
                        rest = false;
                    }
                    if (inData > 1 && inData <= 2.5) {
                        button1.setChecked(true);
                        button2.setChecked(false);
                        button3.setChecked(false);
                        button4.setChecked(false);
                        button5.setChecked(false);
                        button6.setChecked(false);
                        button7.setChecked(false);
                        rest = false;
                    }
                    if (inData > 2.5 && inData <= 4) {
                        button1.setChecked(true);
                        button2.setChecked(true);
                        button3.setChecked(false);
                        button4.setChecked(false);
                        button5.setChecked(false);
                        button6.setChecked(false);
                        button7.setChecked(false);
                        rest = false;
                    }
                    if (inData > 4 && inData <= 5.5) {
                        button1.setChecked(true);
                        button2.setChecked(true);
                        button3.setChecked(true);
                        button4.setChecked(false);
                        button5.setChecked(false);
                        button6.setChecked(false);
                        button7.setChecked(false);
                        rest = false;
                    }
                    if (inData > 5.5 && inData <= 7) {
                        button1.setChecked(true);
                        button2.setChecked(true);
                        button3.setChecked(true);
                        button4.setChecked(true);
                        button5.setChecked(false);
                        button6.setChecked(false);
                        button7.setChecked(false);
                        rest = false;
                    }
                    if (inData > 7 && inData <= 8.5) {
                        button1.setChecked(true);
                        button2.setChecked(true);
                        button3.setChecked(true);
                        button4.setChecked(true);
                        button5.setChecked(true);
                        button6.setChecked(false);
                        button7.setChecked(false);
                        rest = false;
                    }
                    if (inData > 8.5 && inData <= 10) {
                        button1.setChecked(true);
                        button2.setChecked(true);
                        button3.setChecked(true);
                        button4.setChecked(true);
                        button5.setChecked(true);
                        button6.setChecked(true);
                        button7.setChecked(false);
                        rest = false;
                    }
                    if (inData > 10) {
                        button1.setChecked(true);
                        button2.setChecked(true);
                        button3.setChecked(true);
                        button4.setChecked(true);
                        button5.setChecked(true);
                        button6.setChecked(true);
                        button7.setChecked(true);
                        rest = false;
                    }

                }
                oldSenseVal = inData;
                //oldSenseVal = accelerationSquareRoot;

            } else if (sensorNumber == 2) { // Gyroscope
                float[] values = event.values;
                float x = event.values[2];
                if(dataThreadObject!=null){
                    byte[] outData = Float.toString(x).getBytes();
                    dataThreadObject.write(outData);
                }
                inData = x;

                button4.setChecked(true);
                if (inData*inData < 0.5) {  // Too small
                    return;
                }
                if (inData < -1) {
                    button5.setChecked(true);
                    button1.setChecked(false);
                    button2.setChecked(false);
                    button3.setChecked(false);
                }
                if (inData < -2) {
                    button6.setChecked(true);
                    button1.setChecked(false);
                    button2.setChecked(false);
                    button3.setChecked(false);
                }
                if (inData < -3) {
                    button7.setChecked(true);
                    button1.setChecked(false);
                    button2.setChecked(false);
                    button3.setChecked(false);
                }
                if (inData > 1) {
                    button3.setChecked(true);
                    button5.setChecked(false);
                    button6.setChecked(false);
                    button7.setChecked(false);
                }
                if (inData > 2) {
                    button2.setChecked(true);
                    button5.setChecked(false);
                    button6.setChecked(false);
                    button7.setChecked(false);
                }
                if (inData > 3) {
                    button1.setChecked(true);
                    button5.setChecked(false);
                    button6.setChecked(false);
                    button7.setChecked(false);
                }
            } else if (sensorNumber == 3) { // Proximity
                float[] values = event.values;
                float x = event.values[0];

                if (dataThreadObject != null) {
                    byte[] outData = Float.toString(x).getBytes();
                    dataThreadObject.write(outData);
                }
                inData = x;

                if (inData < 2) {
                    button1.setChecked(true);
                    button2.setChecked(true);
                    button3.setChecked(true);
                    button4.setChecked(true);
                    button5.setChecked(true);
                    button6.setChecked(true);
                    button7.setChecked(true);
                } else {
                    button1.setChecked(false);
                    button2.setChecked(false);
                    button3.setChecked(false);
                    button4.setChecked(false);
                    button5.setChecked(false);
                    button6.setChecked(false);
                    button7.setChecked(false);
                }
            } else { // Default - do nothing
                ;
            }
        }

    }

    private class buttonsThread implements Runnable {
        Float inData;

        public buttonsThread(float data) {
            inData = data;
        }
        @Override
        public void run() {
            if (sensorNumber == 1) {  // Accelerometer
                if (rest && (oldSenseVal > inData)) {
                    // Acceleration ranges of these if-statements were calibrated using a Galaxy S8
                    if (inData <= 1) {
                        button1.setChecked(false);
                        button1.setChecked(false);
                        button2.setChecked(false);
                        button3.setChecked(false);
                        button4.setChecked(false);
                        button5.setChecked(false);
                        button6.setChecked(false);
                        button7.setChecked(false);
                        rest = false;
                    }
                    if (inData > 1 && inData <= 2.5) {
                        button1.setChecked(true);
                        button2.setChecked(false);
                        button3.setChecked(false);
                        button4.setChecked(false);
                        button5.setChecked(false);
                        button6.setChecked(false);
                        button7.setChecked(false);
                        rest = false;
                    }
                    if (inData > 2.5 && inData <= 4) {
                        button1.setChecked(true);
                        button2.setChecked(true);
                        button3.setChecked(false);
                        button4.setChecked(false);
                        button5.setChecked(false);
                        button6.setChecked(false);
                        button7.setChecked(false);
                        rest = false;
                    }
                    if (inData > 4 && inData <= 5.5) {
                        button1.setChecked(true);
                        button2.setChecked(true);
                        button3.setChecked(true);
                        button4.setChecked(false);
                        button5.setChecked(false);
                        button6.setChecked(false);
                        button7.setChecked(false);
                        rest = false;
                    }
                    if (inData > 5.5 && inData <= 7) {
                        button1.setChecked(true);
                        button2.setChecked(true);
                        button3.setChecked(true);
                        button4.setChecked(true);
                        button5.setChecked(false);
                        button6.setChecked(false);
                        button7.setChecked(false);
                        rest = false;
                    }
                    if (inData > 7 && inData <= 8.5) {
                        button1.setChecked(true);
                        button2.setChecked(true);
                        button3.setChecked(true);
                        button4.setChecked(true);
                        button5.setChecked(true);
                        button6.setChecked(false);
                        button7.setChecked(false);
                        rest = false;
                    }
                    if (inData > 8.5 && inData <= 10) {
                        button1.setChecked(true);
                        button2.setChecked(true);
                        button3.setChecked(true);
                        button4.setChecked(true);
                        button5.setChecked(true);
                        button6.setChecked(true);
                        button7.setChecked(false);
                        rest = false;
                    }
                    if (inData > 10) {
                        button1.setChecked(true);
                        button2.setChecked(true);
                        button3.setChecked(true);
                        button4.setChecked(true);
                        button5.setChecked(true);
                        button6.setChecked(true);
                        button7.setChecked(true);
                        rest = false;
                    }

                }
                oldSenseVal = inData;
                //oldSenseVal = accelerationSquareRoot;

            } else if (sensorNumber == 2) { // Gyroscope

                button4.setChecked(true);
                if (inData*inData < 0.5) {  // Too small
                    return;
                }
                if (inData < -1) {
                    button5.setChecked(true);
                    button1.setChecked(false);
                    button2.setChecked(false);
                    button3.setChecked(false);
                }
                if (inData < -2) {
                    button6.setChecked(true);
                    button1.setChecked(false);
                    button2.setChecked(false);
                    button3.setChecked(false);
                }
                if (inData < -3) {
                    button7.setChecked(true);
                    button1.setChecked(false);
                    button2.setChecked(false);
                    button3.setChecked(false);
                }
                if (inData > 1) {
                    button3.setChecked(true);
                    button5.setChecked(false);
                    button6.setChecked(false);
                    button7.setChecked(false);
                }
                if (inData > 2) {
                    button2.setChecked(true);
                    button5.setChecked(false);
                    button6.setChecked(false);
                    button7.setChecked(false);
                }
                if (inData > 3) {
                    button1.setChecked(true);
                    button5.setChecked(false);
                    button6.setChecked(false);
                    button7.setChecked(false);
                }
            } else if (sensorNumber == 3) { // Proximity
                if (inData < 2) {
                    button1.setChecked(true);
                    button2.setChecked(true);
                    button3.setChecked(true);
                    button4.setChecked(true);
                    button5.setChecked(true);
                    button6.setChecked(true);
                    button7.setChecked(true);
                } else {
                    button1.setChecked(false);
                    button2.setChecked(false);
                    button3.setChecked(false);
                    button4.setChecked(false);
                    button5.setChecked(false);
                    button6.setChecked(false);
                    button7.setChecked(false);
                }
            } else { // Default - do nothing
                ;
            }
        }
    }



    // This function declaration is necessary to implement SensorEventListener
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
/////////////////////////////////////////////////////////////////////////////////
    ////////////// connect as server //////////////////////////////////
        private class AcceptThread extends Thread {
            private static final String TAG = "serverThread: ";
            private final BluetoothServerSocket mmServerSocket;
            private String name;

            public AcceptThread(BluetoothDevice device) {
                // Use a temporary object that is later assigned to mmServerSocket
                // because mmServerSocket is final.
                BluetoothServerSocket tmp = null;
                name = device.getName();

                try {
                    // MY_UUID is the app's UUID string, also used by the client code.
                    //REALLY NOT SURE THERE IS ANYTHING IN UUID[0]*****************
                    tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(name, uuid);
                    Log.d("TAG", "Server thread is created successfully");
                } catch (IOException e) {
                    Log.e(TAG, "Socket's listen() method failed", e);
                }
                mmServerSocket = tmp;
            }

            public void run() {
                BluetoothSocket socket = null;
                // Keep listening until exception occurs or a socket is returned.
                while (true) {
                    try {
                        socket = mmServerSocket.accept();
                    } catch (IOException e) {
                        Log.e(TAG, "Socket's accept() method failed", e);
                        break;
                    }

                    if (socket != null) {
                        // A connection was accepted. Perform work associated with
                        // the connection in a separate thread.
                        dataThreadObject = new dataThread(socket);
                        dataThreadObject.start();
                        try {
                            mmServerSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }

            // Closes the connect socket and causes the thread to finish.
            public void cancel() {
                try {
                    mmServerSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "Could not close the connect socket", e);
                }
            }
        }


/////////////////////////////////////////////////////////////////////////////////
    //thread for client
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private final String TAG = "clientThread";

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(uuid);
                Log.d("TAG", "Client thread was created successfully");
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            dataThreadObject = new dataThread(mmSocket);
            dataThreadObject.start();

        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

////////////////////////////////////////////////////////////////////////////////
    /////////////// thread for passing data ///////////////
    private class dataThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream
        private final String TAG = "dataThread: ";

        public dataThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
                Log.d(TAG, "Input Data stream created successfully");
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
                Log.d(TAG, "Output Data stream created successfully");
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;

        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    newData = new String(mmBuffer,0,numBytes);
                    //where we want to process the incoming data as a server
                    if(server){
                        Float inData = new Float(0);
                        if(newData!=null){
                            String newString = newData;

                            int index1 = newString.lastIndexOf(".");
                            String[] splitData = new String[3];
                            splitData[0] = newString.substring(index1-1,newString.length()-1);
                            newString = newString.substring(0, index1-2);
                            int index2 = newString.lastIndexOf(".");
                            splitData[1] = newString.substring(index2-1,index1-2);
                            splitData[2] = newString.substring(0,index2-1);

                            Float maxVal = Float.parseFloat(splitData[0]);
                            for (int i = 1; i < splitData.length; i++) {
                                if (Float.parseFloat(splitData[i]) > maxVal) {
                                    maxVal = Float.parseFloat(splitData[i]);
                                }
                            }

                            inData = maxVal;
                        }
                        Log.d("TAG", "inData is" + inData);
                        runOnUiThread(new buttonsThread(inData));
                    }

                    Log.d(TAG, "Incoming data!! "+ newData);
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                String text = new String(bytes, Charset.defaultCharset());
                mmOutStream.write(bytes);
                Log.d(TAG, "Outgoing data!! "+text);

            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}

