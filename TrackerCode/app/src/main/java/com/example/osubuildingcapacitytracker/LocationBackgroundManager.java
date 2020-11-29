package com.example.osubuildingcapacitytracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

import java.util.List;
import java.util.Map;

public class LocationBackgroundManager extends BroadcastReceiver {

    static final String ACTION_PROCESS_UPDATES =
            "com.google.android.gms.location.sample.locationupdatespendingintent.action" +
                    ".PROCESS_UPDATES";


    private volatile Map<String, Double> distances;
    /*
    public LocationBackgroundManager(Map<String, Double> distancesArray){
        distances = distancesArray;
    }

     */

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    List<Location> locations = result.getLocations();

                    //Utils.setLocationUpdatesResult(context, locations);
                    //Utils.sendNotification(context, Utils.getLocationResultTitle(context, locations));
                    //Log.i(TAG, Utils.getLocationUpdatesResult(context));

                    //Do Server stuff!!

                    Log.d("Background: ", "Background update received!!");
                }
            }
        }
    }
}
