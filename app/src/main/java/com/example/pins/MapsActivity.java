package com.example.pins;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private LocationManager locationManager;
    private DatabaseReference locationsRef, dbRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private  LatLng currentLatLng;
    private Marker currentLoc;
    private boolean friendsFlag;
    private long rad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        locationsRef = FirebaseDatabase.getInstance().getReference("Locations");
        dbRef = FirebaseDatabase.getInstance().getReference();


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



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
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
       // LatLng sydney = new LatLng(-34, 151);
       // LatLng currentLoc = new LatLng(LOCATION_SERVICE.)
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

                    final double latitude = location.getLatitude();
                    final double longitude = location.getLongitude();
                    Toast.makeText(MapsActivity.this, "GPS provider", Toast.LENGTH_LONG).show();
                    currentLatLng = new LatLng(latitude, longitude);

                    locationsRef.child(currentUserID).child("last_location").child("latitude").setValue(latitude);
                    locationsRef.child(currentUserID).child("last_location").child("longitude").setValue(longitude);
                    currentLoc = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)));

                    Calendar rightNow = Calendar.getInstance();
                    int hours = rightNow.get(Calendar.HOUR_OF_DAY);
                    if(hours == 12 || hours == 18){
                        locationsRef.child(currentUserID).child(String.valueOf(hours)).child("latitude").setValue(latitude);
                        locationsRef.child(currentUserID).child(String.valueOf(hours)).child("longitude").setValue(longitude);
                    }

                    locationsRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mMap.clear();
                            currentLoc = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(latitude,longitude))
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))

                            );

                            mMap.addCircle(new CircleOptions()
                                    .center(currentLatLng)
                                    .radius((float)rad)
                                    .fillColor(Color.argb(70, 150, 50, 50))
                            );
                            for(final DataSnapshot user : dataSnapshot.getChildren()){
                                if(user.hasChild("last_location")) {
                                    final double user_latitude = (double) user.child("last_location").child("latitude").getValue();
                                    final double user_longitude = (double) user.child("last_location").child("longitude").getValue();
                                    System.out.println("user: " + user.getKey());
                                    if (!currentUserID.equals(user.getKey())
                                            && isInRadius(new LatLng(user_latitude, user_longitude), currentLatLng)
                                            && (user.hasChild("visible") && (boolean)user.child("visible").getValue())
                                    )
                                    {
                                        dbRef.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.child("Friendlists").child(currentUserID).hasChild(user.getKey())
                                                        && !dataSnapshot.child("Blocked").child(currentUserID).hasChild(user.getKey())
                                                )
                                                {
                                                    mMap.addMarker(new MarkerOptions()
                                                            .position(new LatLng(user_latitude, user_longitude))
                                                            .title(dataSnapshot.child("Users").child(user.getKey()).child("full_name").getValue().toString())

                                                    );
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                                    }
                                }
                            }


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng));

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

                    final double latitude = location.getLatitude();
                    final double longitude = location.getLongitude();
                    Toast.makeText(MapsActivity.this, "GPS provider", Toast.LENGTH_LONG).show();
                    currentLatLng = new LatLng(latitude, longitude);

                    locationsRef.child(currentUserID).child("last_location").child("latitude").setValue(latitude);
                    locationsRef.child(currentUserID).child("last_location").child("longitude").setValue(longitude);
                    currentLoc = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)));

                    Calendar rightNow = Calendar.getInstance();
                    int hours = rightNow.get(Calendar.HOUR_OF_DAY);
                    if(hours == 12 || hours == 18){
                        locationsRef.child(currentUserID).child(String.valueOf(hours)).child("latitude").setValue(latitude);
                        locationsRef.child(currentUserID).child(String.valueOf(hours)).child("longitude").setValue(longitude);
                    }

                    locationsRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mMap.clear();
                            currentLoc = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(latitude,longitude))
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))

                            );

                            mMap.addCircle(new CircleOptions()
                                    .center(currentLatLng)
                                    .radius((float)rad)
                                    .fillColor(Color.argb(70, 150, 50, 50))
                            );
                            for(final DataSnapshot user : dataSnapshot.getChildren()){
                                if(user.hasChild("last_location")) {
                                    final double user_latitude = (double) user.child("last_location").child("latitude").getValue();
                                    final double user_longitude = (double) user.child("last_location").child("longitude").getValue();
                                    System.out.println("user: " + user.getKey());
                                    if (!currentUserID.equals(user.getKey())
                                            && isInRadius(new LatLng(user_latitude, user_longitude), currentLatLng)
                                            && (user.hasChild("visible") && (boolean)user.child("visible").getValue())
                                            )
                                    {
                                        dbRef.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.child("Friendlists").child(currentUserID).hasChild(user.getKey())
                                                && !dataSnapshot.child("Blocked").child(currentUserID).hasChild(user.getKey())
                                                )
                                                {
                                                    mMap.addMarker(new MarkerOptions()
                                                            .position(new LatLng(user_latitude, user_longitude))
                                                            .title(dataSnapshot.child("Users").child(user.getKey()).child("full_name").getValue().toString())

                                                    );
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                                    }
                                }
                            }


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng));


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



    private boolean isInRadius(LatLng latLng, LatLng currentLatLng) {
        float[] distance = new float[1];

        Location.distanceBetween(latLng.latitude, latLng.longitude, currentLatLng.latitude, currentLatLng.longitude, distance);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("Radiuses").hasChild(currentUserID)) {
                    rad = (long) dataSnapshot.child("Radiuses").child(currentUserID).child("radius").getValue();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        return distance[0] <= rad;

    }


}
