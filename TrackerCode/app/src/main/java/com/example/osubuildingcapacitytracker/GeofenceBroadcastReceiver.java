package com.example.osubuildingcapacitytracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    //private final Handler handler;

    public GeofenceBroadcastReceiver(){
        Log.e("GeofenceBR: ", "Created GBR");
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes
                    .getStatusCodeString(geofencingEvent.getErrorCode());
            Log.e("GeofenceBR: ", errorMessage);
            return;
        }
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT || geofenceTransition==Geofence.GEOFENCE_TRANSITION_ENTER) {
            Toast.makeText(context, "geofence triggered", Toast.LENGTH_SHORT).show();
            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            for(Geofence geofence: triggeringGeofences){
                String name = geofence.getRequestId();
                if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL){
                    //dwelling inside geofence,
                    Intent textIntent = new Intent("UPDATE_DWELL");
                    textIntent.putExtra("loc", name);
                    textIntent.putExtra("increment", 1);
                    context.sendBroadcast(textIntent);
                    Log.d("GeofenceBR: ", name+" dwell transition");
                }else if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER){
                    // Log it, but don't do anything else
                    Log.e("GeofenceBR: ", "Entering geofence");
                    Intent textIntent = new Intent("UPDATE_ENTER");
                    textIntent.putExtra("loc", name);
                    textIntent.putExtra("increment", 0);
                    context.sendBroadcast(textIntent);
                }else{
                    //leaving geofence
                    Intent textIntent = new Intent("UPDATE_EXIT");
                    textIntent.putExtra("loc", name);
                    textIntent.putExtra("increment", -1);
                    context.sendBroadcast(textIntent);
                    Log.d("GeofenceBR: ", name+" exit transition");
                }
            }

            // Send notification and log the transition details.
            //sendNotification(geofenceTransitionDetails);
            //
        }

    }
}