package com.example.osubuildingcapacitytracker;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_YELLOW;


public class MapBuildingsView extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private LatLngBounds.Builder builder;
    private MapView mapView;

    //@Override
    //public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    //    return inflater.inflate(R.layout.map_layout, container, false);
    //}

    //@Override
    //public void onViewCreated(View view, Bundle savedInstanceState) {
    ///    mapView = (MapView) mapView.findViewById(R.id.map);
    //    mapView.onCreate(savedInstanceState);
    //    mapView.onResume();
    //    mapView.getMapAsync(this);
    //}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mMapFragment = SupportMapFragment.newInstance();
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        builder = new LatLngBounds.Builder();

        LatLng userPoint = new LatLng(40.004840, -83.012440);
        MarkerOptions user = new MarkerOptions().position(userPoint).title("You are Here").icon(BitmapDescriptorFactory.defaultMarker(HUE_YELLOW));
        mMap.addMarker(user);
        // builder.include(dreeseLab.getPosition());
        builder.include(userPoint);


        // Add a markers at OSU building locations

        LatLng dreeseLabPoint = new LatLng(40.002300, -83.015877);
        MarkerOptions dreeseLab = new MarkerOptions().position(dreeseLabPoint).title("Dreese Lab").snippet("100 out of 200");
        mMap.addMarker(dreeseLab);
        // builder.include(dreeseLab.getPosition());
        builder.include(dreeseLabPoint);

        LatLng bakerSysPoint = new LatLng(40.001572, -83.015877);
        MarkerOptions bakerSys = new MarkerOptions().position(bakerSysPoint).title("Baker Systems Engr Bldg").snippet("100 out of 200");
        mMap.addMarker(bakerSys);
        builder.include(bakerSysPoint);

        LatLng journalismPoint = new LatLng(40.002000, -83.015160);
        MarkerOptions journalism = new MarkerOptions().position(journalismPoint).title("Journalism Bldg").snippet("100 out of 200");
        mMap.addMarker(journalism);
        builder.include(journalismPoint);

        LatLng caldwellLabPoint = new LatLng(40.0024, -83.0150);
        MarkerOptions caldwellLab = new MarkerOptions().position(caldwellLabPoint).title("Caldwell Lab").snippet("100 out of 200");
        mMap.addMarker(caldwellLab);
        builder.include(caldwellLabPoint);

        LatLng smithLabPoint = new LatLng(40.002110, -83.013190);
        MarkerOptions smithLab = new MarkerOptions().position(smithLabPoint).title("Smith Lab").snippet("100 out of 200");
        mMap.addMarker(smithLab);
        builder.include(smithLabPoint);

        LatLng mcphersonChemPoint = new LatLng(40.00242, -83.012320);
        MarkerOptions mcphersonChem = new MarkerOptions().position(mcphersonChemPoint).title("McPherson Chem Lab").snippet("100 out of 200");
        mMap.addMarker(mcphersonChem);
        builder.include(mcphersonChemPoint);

        LatLng hitchcockHallPoint = new LatLng(40.003799, -83.016029);
        MarkerOptions hitchcockHall = new MarkerOptions().position(hitchcockHallPoint).title("Hitchcock Hall").snippet("100 out of 200");
        mMap.addMarker(hitchcockHall);
        builder.include(hitchcockHallPoint);

        LatLng physicsResearchPoint = new LatLng(40.003930, -83.013330);
        MarkerOptions physicsResearch = new MarkerOptions().position(physicsResearchPoint).title("Physics Research Bldg").snippet("100 out of 200");
        mMap.addMarker(physicsResearch);
        builder.include(physicsResearchPoint);

        LatLng thompLibraryPoint = new LatLng(39.999241, -83.015060);
        MarkerOptions thompLibrary = new MarkerOptions().position(thompLibraryPoint).title("Thompson Library").snippet("100 out of 200");
        mMap.addMarker(thompLibrary);
        builder.include(thompLibraryPoint);

        LatLng avenue18Point = new LatLng(40.001919, -83.013588);
        MarkerOptions avenue18 = new MarkerOptions().position(avenue18Point).title("18th Ave Library").snippet("100 out of 200");
        mMap.addMarker(avenue18);
        builder.include(avenue18Point);

        LatLng stillmanHallPoint = new LatLng(40.002159, -83.010582);
        MarkerOptions stillmanHall = new MarkerOptions().position(stillmanHallPoint).title("Stillman Hall").snippet("100 out of 200");
        mMap.addMarker(stillmanHall);
        builder.include(stillmanHallPoint);

        LatLng rpacPoint = new LatLng(39.9994, -83.0183);
        MarkerOptions rpac = new MarkerOptions().position(rpacPoint).title("OSU RPAC").snippet("100 out of 200");
        mMap.addMarker(rpac);
        builder.include(rpacPoint);

        LatLng bolzHallPoint = new LatLng(40.002650, -83.015520);
        MarkerOptions bolzHall = new MarkerOptions().position(bolzHallPoint).title("Bolz Hall").snippet("100 out of 200");
        mMap.addMarker(bolzHall);
        builder.include(bolzHallPoint);

        LatLng knowltonHallPoint = new LatLng(40.004010, -83.016080);
        MarkerOptions knowltonHall = new MarkerOptions().position(knowltonHallPoint).title("Knowlton Hall").snippet("100 out of 200");
        mMap.addMarker(knowltonHall);
        builder.include(knowltonHallPoint);


        //mMap.moveCamera(CameraUpdateFactory.newLatLng(dreeseLab));
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.30);
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), width, height, padding));

    }
}
