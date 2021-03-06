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
import java.text.SimpleDateFormat;
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


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        locationsRef = FirebaseDatabase.getInstance().getReference("Locations");
        dbRef = FirebaseDatabase.getInstance().getReference();


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



    }

    @Override
    public  void onBackPressed(){
        finish();
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;



        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {


                    final double latitude = location.getLatitude();
                    final double longitude = location.getLongitude();

                    currentLatLng = new LatLng(latitude, longitude);
                    Date date = Calendar.getInstance().getTime();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String lastSeenOn = sdf.format(date);
                    locationsRef.child(currentUserID).child("last_seen_on").setValue(lastSeenOn);

                    locationsRef.child(currentUserID).child("last_location").child("latitude").setValue(latitude);
                    locationsRef.child(currentUserID).child("last_location").child("longitude").setValue(longitude);

                    currentLoc = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)));

                    Calendar rightNow = Calendar.getInstance();
                    int hours = rightNow.get(Calendar.HOUR_OF_DAY);
                    if(hours == 12 || hours == 18){
                        locationsRef.child(currentUserID).child(String.valueOf(hours)).child("latitude").setValue(latitude);
                        locationsRef.child(currentUserID).child(String.valueOf(hours)).child("longitude").setValue(longitude);

                    }

                    locationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                                    if (!currentUserID.equals(user.getKey())
                                            && isInRadius(new LatLng(user_latitude, user_longitude), currentLatLng)
                                            && (user.hasChild("visible") && (boolean)user.child("visible").getValue())) {
                                        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.child("Friendlists").child(currentUserID).hasChild(user.getKey())
                                                        && !dataSnapshot.child("Blocked").child(currentUserID).hasChild(user.getKey())
                                                )
                                                {
                                                    if(dataSnapshot.child("Locations").child(user.getKey()).hasChild("last_seen_on")){
                                                        mMap.addMarker(new MarkerOptions()
                                                            .position(new LatLng(user_latitude, user_longitude))
                                                                .title(dataSnapshot.child("Users").child(user.getKey()).child("full_name").getValue().toString())
                                                                .snippet("Last seen on: " + dataSnapshot.child("Locations").child(user.getKey()).child("last_seen_on").getValue().toString())

                                                        );
                                                    } else {
                                                        mMap.addMarker(new MarkerOptions()
                                                                .position(new LatLng(user_latitude, user_longitude))
                                                                .title(dataSnapshot.child("Users").child(user.getKey()).child("full_name").getValue().toString())
                                                        );
                                                    }

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


                    final double latitude = location.getLatitude();
                    final double longitude = location.getLongitude();

                    currentLatLng = new LatLng(latitude, longitude);
                    Date date = Calendar.getInstance().getTime();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String lastSeenOn = sdf.format(date);
                    locationsRef.child(currentUserID).child("last_seen_on").setValue(lastSeenOn);

                    locationsRef.child(currentUserID).child("last_location").child("latitude").setValue(latitude);
                    locationsRef.child(currentUserID).child("last_location").child("longitude").setValue(longitude);


                    Calendar rightNow = Calendar.getInstance();
                    int hours = rightNow.get(Calendar.HOUR_OF_DAY);
                    if(hours == 12 || hours == 18){
                        locationsRef.child(currentUserID).child(String.valueOf(hours)).child("latitude").setValue(latitude);
                        locationsRef.child(currentUserID).child(String.valueOf(hours)).child("longitude").setValue(longitude);
                    }

                    locationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                                        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.child("Friendlists").child(currentUserID).hasChild(user.getKey())
                                                && !dataSnapshot.child("Blocked").child(currentUserID).hasChild(user.getKey())
                                                )
                                                {
                                                    if(dataSnapshot.child("Locations").child(user.getKey()).hasChild("last_seen_on")){
                                                        mMap.addMarker(new MarkerOptions()
                                                                .position(new LatLng(user_latitude, user_longitude))
                                                                .title(dataSnapshot.child("Users").child(user.getKey()).child("full_name").getValue().toString())
                                                                .snippet("Last seen on: " + dataSnapshot.child("Locations").child(user.getKey()).child("last_seen_on").getValue().toString())

                                                        );
                                                    } else {
                                                        mMap.addMarker(new MarkerOptions()
                                                                .position(new LatLng(user_latitude, user_longitude))
                                                                .title(dataSnapshot.child("Users").child(user.getKey()).child("full_name").getValue().toString())
                                                        );
                                                    }
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

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12.0f));


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
