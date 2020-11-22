package com.example.osubuildingcapacitytracker;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.osubuildingcapacitytracker.dummy.DummyContent;

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.sql.DriverManager;
//import java.sql.ResultSet;
import java.sql.SQLException;
//import java.sql.Statement;
import android.os.StrictMode;
import android.content.pm.PackageManager;
import android.Manifest;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ItemListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private double latitude;
    private double longitude;
    private double accuracy;
    private Timer myTimer;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final int MY_PERMISSION_REQ_FINE_LOC = 101;
    private final int MY_PERMISSION_REQ_BACK_LOC = 201;
    private final int GEOFENCE_EXPIRE = 60000*3; //in ms, so 2 mins
    private final int THREAD_SLEEP = 60000*2; //in ms, so 2 mins
    private final int MY_REQ_CODE = 42;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean backgroundLocOn;
    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;
    public ArrayList<Geofence> geofenceList;
    private Map<String, Double[]> landmarks;
    private Map<String, Integer> capacity;
    private volatile Map<String, Double> distances;
    private Thread getCloseLandmarks;
    private boolean geofenceClientStarted;
    private DummyContent dummyContent = new DummyContent();
    private RecyclerView.LayoutManager layoutManager;
    private View recyclerView;
    private SimpleItemRecyclerViewAdapter simpleItemRecyclerViewAdapter;
    private HashMap<String, Integer> triggeredFences;
    private Boolean threadRun;

    /**
     * Variables for connecting to SQL Server
     */
    private TextView textView;

    private static String ip = "73.72.169.39";
    private static String port = "1433";
    private static String Classes = "net.sourceforge.jtds.jdbc.Driver";
    private static String database = "testDatabase";
    private static String username = "anderson2943";
    private static String password = "042190";
    private static String url = "jdbc:jtds:sqlserver://" + ip + ":" + port + "/" + database;

    private Connection connection = null;


    // Start map when map button clicked
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void map(View view) {
        Intent intent = new Intent(ItemListActivity.this, com.example.osubuildingcapacitytracker.MapBuildingsView.class);
        intent.putExtra("hashMap", (Serializable) capacity);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQ_FINE_LOC);
            //requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_REQ_FINE_LOC);
        }
        //Double lat = latitude;
       // Double lon = longitude;
        if(fusedLocationProviderClient!=null){
            //Location location = fusedLocationProviderClient.getLastLocation();

            Log.i("startmap: ", "got last loc");

        }
        Log.i("latitude: ", Double.toString(latitude));
        Log.i("longitude: ", Double.toString(longitude));
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        ItemListActivity.this.startActivity(intent);
    }


    //BR for catching intents thrown by GeofenceBroadcastReceiver
    BroadcastReceiver textBroadcastReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            //TextView textView = findViewById(R.id.geofenceCurr);
            //textView.setText(intent.getStringExtra("loc"));
            //Decide what to do with an intent here, like update UI or change a variable
            String loc = intent.getStringExtra("loc");
            Toast.makeText(context, loc+" fence triggered", Toast.LENGTH_LONG).show();
            if(intent.getAction().equals("UPDATE_DWELL")){
                Log.i("DWELL: ", loc );
                //intent from geofence
                String closestBuilding = "";
                Double closestDistance = distances.get(loc);
                if(!triggeredFences.containsKey(loc)) {
                    triggeredFences.put(loc, new Integer(0));
                }
                for (Map.Entry entry : triggeredFences.entrySet()) {
                    if(!entry.getKey().equals(loc) && closestDistance>distances.get(entry.getKey())){
                        closestDistance = distances.get(entry.getKey());
                        closestBuilding = (String)entry.getKey();
                    }
                }
                if(triggeredFences.size()>1){ // could end up in 2 fences at a time
                    //update values in map
                    for (Map.Entry entry : triggeredFences.entrySet()) {
                        if(entry.getKey().equals(closestBuilding)){ //update val to 1
                            if(entry.getValue().equals(0)){
                                //new closest building!
                                triggeredFences.replace((String)entry.getKey(), new Integer(1));
                                //TODO increment server cap for entry.getKey() or closestBuilding, they should be strings of the same thing
                                updateServerCapacity((String)entry.getKey(), 1);
                                //TODO then pull capacity data from server and do the following lines OR we do one big update at the end
                                simpleItemRecyclerViewAdapter.updateCapacity((String)entry.getKey(), getCurrentBuildingCapacity((String)entry.getKey()));
                                simpleItemRecyclerViewAdapter.notifyDataSetChanged();
                                //capacity.replace(closestBuilding,justPulledServerCap);
                                //simpleItemRecyclerViewAdapter.updateCapacity(closestBuilding, justPulledServerCap);
                            }else{
                                //already 1, no push needs to happen here
                                //although we could pull if we want.
                            }
                        }else{ //update num val to 0
                            if(!entry.getValue().equals(0)){
                                //fence already has 1 there, decrement the cap
                                triggeredFences.replace((String)entry.getKey(), new Integer(0));
                                //TODO decrement server cap for entry.getKey()
                                updateServerCapacity((String)entry.getKey(), -1);
                                //TODO pull capacity data from server and do the following lines OR we do one big update at the end
                                simpleItemRecyclerViewAdapter.updateCapacity((String)entry.getKey(), getCurrentBuildingCapacity((String)entry.getKey()));
                                simpleItemRecyclerViewAdapter.notifyDataSetChanged();
                                String temploc = (String)entry.getKey();
                                //capacity.replace(temploc,justPulledServerCap);
                                //update ui
                                //simpleItemRecyclerViewAdapter.updateCapacity(temploc, justPulledServerCap);
                            }else{
                                //already 0, nothing needs to be pushed
                            }

                        }
                    }
                }else{ // only one fence dwell, the current one
                    triggeredFences.replace(loc, new Integer(1));
                    //TODO increment server cap for entry.getKey() or closestBuilding, they should be strings of the same thing
                    updateServerCapacity(loc, 1);
                    //TODO then pull capacity data from server and do the following lines OR we do one big update at the end
                    simpleItemRecyclerViewAdapter.updateCapacity(loc, getCurrentBuildingCapacity(closestBuilding));
                    //capacity.replace(loc,justPulledServerCap);
                    //update ui
                    //simpleItemRecyclerViewAdapter.updateCapacity(loc, justPulledServerCap);
                }
                //increment function of loc triggeredFences.getValue(loc);
            }else if(intent.getAction().equals("UPDATE_EXIT")){
                if(triggeredFences.containsKey(loc)){
                    //the structure here is a little different than above since the following if statement
                    //is a combination of two conditions
                    if(triggeredFences.size()>1&&triggeredFences.get(loc).equals(new Integer(1))){
                        //more than 1 entry, value was 1 so we need to find the new closest building
                        //means we decrement the server for loc, and increment for the new one
                        triggeredFences.remove(loc);
                        //temp inits
                        String closestBuilding = loc;
                        Double closestDistance = distances.get(loc);
                        Boolean tempset = false;
                        for (Map.Entry entry : triggeredFences.entrySet()) {
                            if(!tempset){
                                //init these to some valid variables in the set. First pass thru only
                                tempset = true;
                                closestBuilding = (String)entry.getKey();
                                closestDistance = (Double)entry.getValue();
                            }
                            if(!entry.getKey().equals(loc) && closestDistance>distances.get(entry.getKey())){
                                closestDistance = distances.get(entry.getKey());
                                closestBuilding = (String)entry.getKey();
                            }
                        }
                        triggeredFences.replace(closestBuilding, new Integer(1));
                        //TODO decrement server for loc
                        updateServerCapacity(loc, -1);
                        //TODO increment server for closestBuilding
                        updateServerCapacity(closestBuilding, 1);
                        //TODO then pull capacity data from server for BOTH loc and closestBuilding and do the following lines OR we do one big update at the end
                        simpleItemRecyclerViewAdapter.updateCapacity(loc, getCurrentBuildingCapacity(loc));
                        simpleItemRecyclerViewAdapter.updateCapacity(closestBuilding, getCurrentBuildingCapacity(closestBuilding));
                        simpleItemRecyclerViewAdapter.notifyDataSetChanged();
                        //capacity.replace(loc,newServerCap_loc);
                        //simpleItemRecyclerViewAdapter.updateCapacity(loc, newServerCap_loc);
                        //capacity.replace(closestBuilding,newServerCap_closestBuilding);
                        //simpleItemRecyclerViewAdapter.updateCapacity(closestBuilding, newServerCap_closestBuilding);
                    }else{
                        if(triggeredFences.get(loc).equals(new Integer(1))){ //only one entry, decrement sever for loc
                            //the line below must go after this if check, otherwise we will get an error. But we could place it after the next else
                            triggeredFences.remove(loc);
                            //TODO decrement server for loc
                            updateServerCapacity(loc, -1);
                            //TODO then pull capacity data from server for loc and do the following lines OR we do one big update at the end
                            simpleItemRecyclerViewAdapter.updateCapacity(loc, getCurrentBuildingCapacity(loc));
                            //capacity.replace(loc,newServerCap_loc);
                            //simpleItemRecyclerViewAdapter.updateCapacity(loc, newServerCap_loc);
                        }else{
                            triggeredFences.remove(loc);
                            //the value was already zero, so we really don't care (nothing to do with server)
                        }
                    }
                }
            }
            //currently unused action for "UPDATE_CAPACITY"
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);
        //check initial permissions are granted
        //initPermissionsCheck(); was checking really often...
        initPermissionsCheck(Manifest.permission.ACCESS_FINE_LOCATION, MY_PERMISSION_REQ_FINE_LOC);

        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET}, PackageManager.PERMISSION_GRANTED);
        textView = findViewById(R.id.textView);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                accuracy = location.getAccuracy();
            }
        };

        locationRequest = new LocationRequest();
        locationRequest.setInterval(10); //in ms
        locationRequest.setFastestInterval(0);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //initialize location client
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        accuracy = location.getAccuracy();
                    }
                }
            });
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQ_FINE_LOC);
            }
        }


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            Class.forName(Classes);
            DriverManager.setLoginTimeout(2);
            connection = DriverManager.getConnection(url, username,password);
            textView.setText("SUCCESS");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            textView.setText("ERROR");
        } catch (SQLException e) {
            e.printStackTrace();
            textView.setText("FAILURE");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        /*fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }


        recyclerView = findViewById(R.id.item_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);
        backgroundLocOn = false;
        geofenceClientStarted = false;


        distances = new HashMap<>();
        triggeredFences = new HashMap<>();
        //populate landmarks list
        //TODO fill in remaining buildings
        //TODO find the rest of building capacities, using 200 as default rn
        landmarks = new HashMap<>();
        //format for landmarks is: lat, lang, radius, max capacity
        //TODO GET RID OF TEST VARIABLES
        //Double[] pool = {39.98839498, -83.02861959, 80.0, 200.0};
        //landmarks.put("pool", pool);

        //actual landmarks
        landmarks.put("Dreese Lab", new Double[]{40.00234558,-83.01599023, 33.02755, 200.0});
        landmarks.put("Baker Systems Engineering", new Double[]{40.00168193, -83.01597146,48.6623, 200.0});
        landmarks.put("Journalism Building", new Double[]{40.00200682,-83.01500711,37.3767, 200.0});
        landmarks.put("Caldwell Lab", new Double[]{40.00128333, -83.01490930,38.3341, 200.0});
        landmarks.put("Smith Lab", new Double[]{40.002110, -83.013190,52.4051, 200.0});
        landmarks.put("McPherson Chemical Lab", new Double[]{40.00228419,-83.01417563,58.603, 200.0});
        landmarks.put("Hitchcock Hall", new Double[]{40.00364845,-83.01521900,44.0233, 200.0});
        landmarks.put("Physics Research Building", new Double[]{40.00338546,-83.01418635,66.2825, 200.0});
        landmarks.put("Thompson Library", new Double[]{39.99930265993615,-83.01487305423194,55.5, 200.0});
        landmarks.put("18th Avenue Library", new Double[]{40.001653210743655,-83.01333614846641, 33.3, 200.0});
        landmarks.put("Stillman Hall", new Double[]{40.00186177473543,-83.01099075409095,36.11, 200.0});
        landmarks.put("OSU RPAC", new Double[]{39.99952150365391,-83.01845802398842,73.05, 200.0});
        landmarks.put("Bolz Hall", new Double[]{40.002650, -83.015520, 55.6, 200.0});
        landmarks.put("Knowlton Hall", new Double[]{40.00362508073249,-83.01683446552049,75.5, 200.0});
        //landmarks.put("", new Double[]{});
        //populate capacity variable
        //TODO update list from server data
        capacity = new HashMap<>();
        for(String entry: landmarks.keySet()){
            int cap = getCurrentBuildingCapacity(entry);
            capacity.put(entry, cap);
            simpleItemRecyclerViewAdapter.updateCapacity(entry, cap);
            //Log.d("refreshButton", "got value: " + getCurrentBuildingCapacity(entry));
        }

        //okay so here we need to populate the geofenceList. I want to do this in a new thread that we can put to sleep and wake up
        geofenceList = new ArrayList<>();
        //getCloseLandmarks = new locThread();

        //register receiver
        IntentFilter filter = new IntentFilter("UPDATE_DWELL");
        filter.addAction("UPDATE_EXIT");
        filter.addAction("UPDATE_ENTER");
        registerReceiver(textBroadcastReceiver, filter);

        //initialize geofencing client
        geofencingClient = LocationServices.getGeofencingClient(this);
        //startlocationupdates() moved to onStart() method
        initPermissionsCheck(Manifest.permission.ACCESS_BACKGROUND_LOCATION, MY_PERMISSION_REQ_BACK_LOC);

        //end of thread declaration
        //start the thread
        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {

            public double Distance(double lat1, double lat2, double lon1,
                                   double lon2, double el1, double el2) {

                final int R = 6371000; // Radius of the earth

                double latDistance = Math.toRadians(lat2 - lat1);
                double lonDistance = Math.toRadians(lon2 - lon1);
                double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                        + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
                double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                double distance = R * c; // convert to meters

                //double height = el1 - el2;

                //distance = Math.pow(distance, 2) + Math.pow(height, 2);

                return distance;
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                Log.i("Timer: ", "Starting loc thread, "+Double.toString(latitude)+" "+Double.toString(longitude));
                ArrayList<Geofence> tempList = new ArrayList<>();

                for (Map.Entry<String, Double[]> entry : landmarks.entrySet()) {
                    Double[] latlngrad = entry.getValue();
                    //check if building is within ~500 m of user
                    Double distance = Distance(latitude, latlngrad[0], longitude, latlngrad[1], 0.0, 0.0);
                    if(distances.containsKey(entry.getKey())){
                        distances.replace(entry.getKey(), distance);
                    }else{
                        distances.put(entry.getKey(), distance);
                    }
                    Log.i(entry.getKey(), Double.toString(distance));
                    if (distance < 300.00) {
                        Log.i(entry.getKey(), " added");
                        float rad = entry.getValue()[2].floatValue();
                        if(accuracy<=rad&&rad>50){
                            //Log.i("increasing radius", "from: "+Float.toString(rad)+"to "+Double.toString(rad+accuracy));
                            rad += 20*accuracy;
                        }else{
                            rad += 30*accuracy;
                        }
                        Geofence temp = new Geofence.Builder()// Set the request ID of the geofence. This is a string to identify this
                                // geofence.
                                .setRequestId(entry.getKey())

                                // Set the circular region of this geofence.
                                .setCircularRegion(
                                        latlngrad[0], latlngrad[1], rad
                                )
                                //.setNotificationResponsiveness()
                                // Set the expiration duration of the geofence
                                .setExpirationDuration(GEOFENCE_EXPIRE)

                                // Set the transition types of interest
                                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL |
                                        Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_ENTER)
                                //set loitering delay in ms
                                //TODO change this to a real value, set to 5s for testing
                                .setLoiteringDelay(5000)
                                // Create the geofence.
                                .build();
                        tempList.add(temp);
                    }
                }//end of for loop
                synchronized (geofenceList) {
                    Log.i("SyncGeofenceThread", tempList.toString());
                    if(!geofenceList.isEmpty()){
                        geofenceList.clear(); //clear the list and then populate it again. This could make a race condition so sync it
                    }
                    //TODO get rid of test place
                    Geofence g = new Geofence.Builder().setRequestId("Building 2").setCircularRegion(39.988411,-83.028641,80).setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_ENTER).setLoiteringDelay(5000).build();
                    geofenceList.add(g);
                    geofenceList.addAll(tempList);
                    updateGeofences();
                }//end of sync
            }
        }, 5000, 120000);
        //getCloseLandmarks.start();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void refresh(View view){
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET}, PackageManager.PERMISSION_GRANTED);
        textView = findViewById(R.id.textView);
        if(connection==null){
            Toast t = Toast.makeText(this, "Trying to reconnect...", Toast.LENGTH_SHORT);
            t.show();
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            try {
                Class.forName(Classes);
                DriverManager.setLoginTimeout(2);
                connection = DriverManager.getConnection(url, username,password);
                textView.setText("SUCCESS");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                textView.setText("ERROR");
            } catch (SQLException e) {
                e.printStackTrace();
                textView.setText("FAILURE");
            }
        }
        for(String entry: landmarks.keySet()) {
            int cap = getCurrentBuildingCapacity(entry);
            capacity.replace(entry, cap);
            simpleItemRecyclerViewAdapter.updateCapacity(entry, cap);
        }
        simpleItemRecyclerViewAdapter.notifyDataSetChanged();
    }

    public int getCurrentBuildingCapacity(String building){
        int cap;
        //added in a try to get connection again
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET}, PackageManager.PERMISSION_GRANTED);
        textView = findViewById(R.id.textView);

        if (connection!=null){
            Statement statement = null;
            try {
                statement = connection.createStatement();
                String query = String.format("SELECT Current_Capacity FROM buildingTable WHERE Building_Name = '%s'", building);
                ResultSet resultSet = statement.executeQuery(query);
                //Log.d("refreshButton", "got query: " + query);

                while (resultSet.next()){
                    cap = Integer.parseInt(resultSet.getString(1));
                    //Log.d("refreshButton", "got value: " + cap);
                    return cap;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else {
            textView.setText("Connection is null");
        }
        return 0;
    }

    // building: building name
    // value: 1 for inc, -1 for dec
    public void updateServerCapacity(String building, int value){
        if (connection == null){
            textView.setText("SQL Query Failed");
            return;
        }
        try {
            CallableStatement callableStatement = connection.prepareCall("{call UpdateBuildingCapacity(?,?)}");
            callableStatement.setString(1, building);
            callableStatement.setInt(2, value);
            callableStatement.execute();
            callableStatement.getMoreResults();
            callableStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    class locThread extends Thread {
        @Override
        public void run() {

                ArrayList<Geofence> tempList = new ArrayList<>();
                for (Map.Entry<String, Double[]> entry : landmarks.entrySet()) {
                    Double[] latlngrad = entry.getValue();
                    //check if building is within ~1000 ft of user
                    Double distance = Distance(latitude, latlngrad[0], longitude, latlngrad[1], 0.0, 0.0);
                    distances.put(entry.getKey(), distance);
                    if (distance < 1000) {
                        Geofence temp = new Geofence.Builder()// Set the request ID of the geofence. This is a string to identify this
                                // geofence.
                                .setRequestId(entry.getKey())

                                // Set the circular region of this geofence.
                                .setCircularRegion(
                                        latlngrad[0], latlngrad[1], latlngrad[2].floatValue()
                                )

                                // Set the expiration duration of the geofence
                                .setExpirationDuration(GEOFENCE_EXPIRE)

                                // Set the transition types of interest
                                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL |
                                        Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_ENTER)
                                //set loitering delay in ms
                                //TODO change this to a real value, set to 10s for testing
                                .setLoiteringDelay(10000)
                                // Create the geofence.
                                .build();
                        tempList.add(temp);
                    }
                }//end of for loop
                synchronized (geofenceList) {
                    Log.i("SyncGeofenceThread", "inside the sync");
                    geofenceList.clear(); //clear the list and then populate it again. This could make a race condition so sync it
                    geofenceList.addAll(tempList);
                    updateGeofences();
                }//end of sync
            /*
                try {
                    // thread to sleep for 5 minutes. reminder that geofences expire after 6 minutes
                    Log.i("SyncGeofenceThread", "putting thread to sleep");
                    Thread.sleep(THREAD_SLEEP);
                } catch (InterruptedException i) {
                    Log.i("InterruptedThread", i.toString());
                } catch (Exception e) {
                    Log.e("AddGeofenceThread", e.toString());
                }

             */

        }

        /**
         * TODO find credit
         * CREDIT to someone else
         * Calculate distance between two points in latitude and longitude taking
         * into account height difference. If you are not interested in height
         * difference pass 0.0. Uses Haversine method as its base.
         * <p>
         * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
         * el2 End altitude in meters
         *
         * @returns Distance in Meters
         */
        public double Distance(double lat1, double lat2, double lon1,
                               double lon2, double el1, double el2) {

            final int R = 6371; // Radius of the earth

            double latDistance = Math.toRadians(lat2 - lat1);
            double lonDistance = Math.toRadians(lon2 - lon1);
            double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                    + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                    * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double distance = R * c * 1000; // convert to meters

            double height = el1 - el2;

            distance = Math.pow(distance, 2) + Math.pow(height, 2);

            return Math.sqrt(distance);
        }
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL | GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    private PendingIntent getPendingBackgroundIntent(){
        Intent intent = new Intent(this, LocationBackgroundManager.class);
        intent.setAction(LocationBackgroundManager.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(this, MY_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSION_REQ_FINE_LOC:
            case MY_PERMISSION_REQ_BACK_LOC:
                if (grantResults.length>0&& grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    //we're all good
                } else {
                    if(grantResults.length>0){
                        Log.i("onPermissionsResults: ", grantResults.toString());
                        //Toast.makeText(getApplicationContext(), "This requires permissions to be granted", Toast.LENGTH_SHORT).show();
                        //finish();
                    }

                    //this line has been edited out, because we do'nt want the app to crash
                    //
                }
                break;
        }
    }

    public void updateGeofences(){

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            if(geofencingClient==null) {
                geofencingClient = LocationServices.getGeofencingClient(this);
                geofenceClientStarted = true;
                Log.i("inside weird if"," idk why we are here");
            }
            // I think because there's the FLAG_UPDATE_CURRENT on the pending intent, it will just update fences if they already exist
            //TODO get rid of the success listeners
            if(geofenceList.isEmpty()){
                //an error will occur if the list is empty
                return;
            }
            geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                    .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Geofences added, print Log message
                            Log.d("Geofence: ", "geofence added!");
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            // Failed to add geofences, print log message
                            Log.d("Geofence: ", "geofence failed");
                        }
                    });
        }else{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, MY_PERMISSION_REQ_BACK_LOC);
            }
        }


    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQ_FINE_LOC);
            }
        }
        //added in a check to see if client has been started already
        //updateGeofences();
        //print
        Log.d("StartUpdates: ", "Updates started");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(backgroundLocOn){ //if the app is pulled back into foreground
            stopLocationUpdatesInBackground();
            startLocationUpdates();
            backgroundLocOn=false;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            IntentFilter filter = new IntentFilter("UPDATE_DWELL");
            filter.addAction("UPDATE_EXIT");
            filter.addAction("UPDATE_ENTER");
            registerReceiver(textBroadcastReceiver, filter);
            Log.i("onresume: ", "textBR registered");
        }catch (Exception e){

        }
    }

    @Override
    protected void onPause() {
        unregisterReceiver(textBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();//

        if(!backgroundLocOn){ //if app is going sleepys
            stopLocationUpdates();
            startLocationUpdatesInBackground();
            backgroundLocOn=true;
        }
    }

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(textBroadcastReceiver);
        }catch (Exception e){

        }

        super.onDestroy();
    }

    private void startLocationUpdatesInBackground() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest newlocationRequest = new LocationRequest();
            newlocationRequest.setInterval(30000); //in ms
            newlocationRequest.setFastestInterval(10000);
            newlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            fusedLocationProviderClient.requestLocationUpdates(newlocationRequest, getPendingBackgroundIntent());
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQ_FINE_LOC);
            }
        }
        //print
        Log.d("StartUpdatesBackgrnd: ", "Updates started");
    }
    private void initPermissionsCheck(String permission, int reqCode){
        if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
           //good to go
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{permission}, reqCode);
            }
        }
    }

    private void stopLocationUpdatesInBackground(){
        fusedLocationProviderClient.removeLocationUpdates(getPendingBackgroundIntent());
        Log.d("StopUpdatesBackground: ", "Updates halted");
    }

    private void stopLocationUpdates(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        Log.d("StopUpdates: ", "Updates halted");
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        //layoutManager = new LinearLayoutManager(recyclerView.getContext());
        //recyclerView.setLayoutManager(layoutManager);
        simpleItemRecyclerViewAdapter = new SimpleItemRecyclerViewAdapter(this, dummyContent.ITEMS, mTwoPane);
        recyclerView.setAdapter(simpleItemRecyclerViewAdapter);
    }


    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final ItemListActivity mParentActivity;
        private final List<DummyContent.DummyItem> mValues;
        private final boolean mTwoPane;
        private final HashMap<String, Object[]> viewHolders = new HashMap<>();

        //final ViewHolder holder, int position
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DummyContent.DummyItem item = (DummyContent.DummyItem) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(ItemDetailFragment.ARG_ITEM_ID, item.id);
                    ItemDetailFragment fragment = new ItemDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, ItemDetailActivity.class);
                    intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, item.toString());

                    context.startActivity(intent);
                }
            }
        };

        SimpleItemRecyclerViewAdapter(ItemListActivity parent,
                                      List<DummyContent.DummyItem> items,
                                      boolean twoPane) {
            mValues = items;
            mParentActivity = parent;
            mTwoPane = twoPane;
        }

        public void updateCapacity(String name, int cap){
            try{
                for (DummyContent.DummyItem item :
                        mValues) {
                    if(item.toString().equals(name)){
                        item.setCapacity(cap);
                    }
                }
            }
            catch (Exception e){
                //error handling code
                Log.e("UpdateCapacity", "update content failed");
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            //holder.mIdView.setText(mValues.get(position).getInfo());
            //holder.mContentView.setText(mValues.get(position).getCapacity().toString());
            holder.mContentView.setText(mValues.get(position).getInfo());
            holder.mIdView.setText(mValues.get(position).getPercent());
            //Object[] holderPos = {holder, new Integer(position)};

            //Log.i("BindViewHolder", "Inside onbindviewholder");
           /* if(viewHolders!=null && viewHolders.size()<mValues.size()){
                viewHolders.put(mValues.get(position).toString(), holderPos);
                Log.i("BindViewHolder", "adding holder to viewholders");
            }
            else{
                if()
                //viewHolders = new HashMap<>();
                viewHolders.put(mValues.get(position).toString(), holderPos);
            }

             */

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);

        }
        //private Boolean holderAdded(final ViewHolder holder){        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;

            ViewHolder(View view) {
                super(view);
                mIdView = (TextView) view.findViewById(R.id.id_text);
                mContentView = (TextView) view.findViewById(R.id.content);
            }
        }


    }
}