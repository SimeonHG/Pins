package com.example.pins;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private LocationManager locationManager;
    private DatabaseReference locationsRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        locationsRef = FirebaseDatabase.getInstance().getReference("Locations");


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        //TODO: refactor this plz \/\/\/\/
        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    //TODO update-ni lastKnownLoc vuv firebase?

                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    LatLng currentLatLng = new LatLng(latitude, longitude);

                    locationsRef.child(currentUserID).child("last_location").child("latitude").setValue(latitude);
                    locationsRef.child(currentUserID).child("last_location").child("longitude").setValue(longitude);

                   // final List<LatLng> allFriendsLocations = new ArrayList<>();
                    locationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mMap.clear();
                            for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                double db_latitude = (double) snapshot.child("latitude").getValue();
                                double db_longitude = (double) snapshot.child("longitude").getValue();
                                LatLng latLng = new LatLng(db_latitude, db_longitude);
                                mMap.addMarker(new MarkerOptions().position(latLng));
                                //allFriendsLocations.add(latLng);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                    mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            });
        } else if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    //TODO update-ni lastKnownLoc vuv firebase?

                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    LatLng currentLatLng = new LatLng(latitude, longitude);

                    locationsRef.child(currentUserID).child("last_location").child("latitude").setValue(latitude);
                    locationsRef.child(currentUserID).child("last_location").child("longitude").setValue(longitude);

                    //final List<LatLng> allFriendsLocations = new ArrayList<>();
                    locationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mMap.clear();
                            for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                double db_latitude = (double) snapshot.child("last_location").child("latitude").getValue();
                                double db_longitude = (double) snapshot.child("last_location").child("longitude").getValue();
                                LatLng latLng = new LatLng(db_latitude, db_longitude);
                                //allFriendsLocations.add(latLng);
                                mMap.addMarker(new MarkerOptions().position(latLng));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            });
        }

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

        // Add a marker in Sydney and move the camera
       // LatLng sydney = new LatLng(-34, 151);
       // LatLng currentLoc = new LatLng(LOCATION_SERVICE.)


    }
}
