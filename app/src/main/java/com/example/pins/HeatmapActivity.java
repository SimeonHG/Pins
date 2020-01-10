package com.example.pins;

import androidx.annotation.NonNull;
import androidx.constraintlayout.solver.widgets.Snapshot;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.List;

public class HeatmapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseReference dbRef;
    private ArrayList<LatLng> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heatmap);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        dbRef = FirebaseDatabase.getInstance().getReference();


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        data = new ArrayList<>();
        dbRef.child("Locations").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.hasChild("12")) {
                        double latitude = (double)snapshot.child("12").child("latitude").getValue();
                        double longitude = (double)snapshot.child("12").child("longitude").getValue();
                        data.add(new LatLng(latitude, longitude));
                        System.out.println(latitude + " / " + longitude  + " ->" + data.size());
                    }
                    if (snapshot.hasChild("18")) {
                        double latitude = (double)snapshot.child("18").child("latitude").getValue();
                        double longitude = (double)snapshot.child("18").child("longitude").getValue();
                        data.add(new LatLng(latitude, longitude));
                        System.out.println(latitude + " / " + longitude  + " ->" + data.size());
                    }
                }
                HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                    .data(data)
                    .build();
                mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
