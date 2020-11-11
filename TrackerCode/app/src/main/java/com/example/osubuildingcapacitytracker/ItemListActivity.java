package com.example.osubuildingcapacitytracker;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final int MY_PERMISSION_REQ_FINE_LOC = 101;
    private final int GEOFENCE_EXPIRE = 60000 * 6; //in ms, so 5 mins
    private final int THREAD_SLEEP = 60000 * 5; //in ms, so 6 mins
    private final int MY_REQ_CODE = 42;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean backgroundLocOn;
    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;
    ArrayList<Geofence> geofenceList;
    private Map<String, Double[]> landmarks;
    private Thread getCloseLandmarks;
    private boolean geofenceClientStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        View recyclerView = findViewById(R.id.item_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);
        backgroundLocOn = false;
        geofenceClientStarted = false;
        locationRequest = new LocationRequest();
        locationRequest.setInterval(7500); //in ms
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

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

        //populate landmarks list
        //TODO replace this with the real landmarks
        landmarks = new HashMap<>();
        Double[] pool = {39.98839498, -83.02861959};
        landmarks.put("pool", pool);
        //okay so here we need to populate the geofenceList. I want to do this in a new thread that we can put to sleep and wake up
        geofenceList = new ArrayList<>();
        getCloseLandmarks = new Thread() {
            @Override
            public void run() {
                ArrayList<Geofence> tempList = new ArrayList<>();
                for (Map.Entry<String, Double[]> entry : landmarks.entrySet()) {
                    Double[] latlng = entry.getValue();
                    //check if building is within 1000 ft of user
                    if (distance(latitude, latlng[0], longitude, latlng[1], 0.0, 0.0) < 300) {
                        Geofence temp = new Geofence.Builder()// Set the request ID of the geofence. This is a string to identify this
                                // geofence.
                                .setRequestId(entry.getKey())

                                // Set the circular region of this geofence.
                                .setCircularRegion(
                                        latlng[0], latlng[1], 80
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
                synchronized (this) {
                    Log.i("SyncGeofenceThread", "inside the sync");
                    geofenceList.clear(); //clear the list and then populate it again. This could make a race condition so sync it
                    geofenceList.addAll(tempList);
                    updateGeofences();
                }//end of sync
                try {
                    // thread to sleep for 5 minutes. reminder that geofences expire after 6 minutes
                    Log.i("SyncGeofenceThread", "putting thread to sleep");
                    Thread.sleep(THREAD_SLEEP);
                } catch (Exception e) {
                    Log.e("AddGeofenceThread", e.toString());
                }
            }
            /**
             * Calculate distance between two points in latitude and longitude taking
             * into account height difference. If you are not interested in height
             * difference pass 0.0. Uses Haversine method as its base.
             *
             * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
             * el2 End altitude in meters
             * @returns Distance in Meters
             */
            public double distance(double lat1, double lat2, double lon1,
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
        }; //end of thread declaration
        //start the thread
        getCloseLandmarks.start();

        //initialize geofencing client
        geofencingClient = LocationServices.getGeofencingClient(this);
        //startlocationupdates() moved to onStart() method
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
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //we're all good
                } else {
                    Toast.makeText(getApplicationContext(), "This requires permissions to be granted", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    public void updateGeofences(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            if(!geofenceClientStarted) {
                geofencingClient = LocationServices.getGeofencingClient(this);
                geofenceClientStarted = true;
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
                requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, MY_PERMISSION_REQ_FINE_LOC);
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
        updateGeofences();
        //print
        Log.d("StartUpdates: ", "Updates started");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(backgroundLocOn){ //if the app is pulled back into foreground
            stopLocationUpdatesInBackground();
            startLocationUpdates();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(!backgroundLocOn){ //if app is going sleepys
            stopLocationUpdates();
            startLocationUpdatesInBackground();
        }
    }

    private void startLocationUpdatesInBackground() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest newlocationRequest = new LocationRequest();
            newlocationRequest.setInterval(60000); //in ms
            newlocationRequest.setFastestInterval(30000);
            newlocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            fusedLocationProviderClient.requestLocationUpdates(newlocationRequest, getPendingBackgroundIntent());
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQ_FINE_LOC);
            }
        }
        //print
        Log.d("StartUpdatesBackgrnd: ", "Updates started");
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
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, DummyContent.ITEMS, mTwoPane));
    }

    public static class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final ItemListActivity mParentActivity;
        private final List<DummyContent.DummyItem> mValues;
        private final boolean mTwoPane;
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
                    intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, item.id);

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

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mIdView.setText(mValues.get(position).percent);
            holder.mContentView.setText(mValues.get(position).capacity);

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

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