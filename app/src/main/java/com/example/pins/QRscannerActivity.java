package com.example.pins;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

public class QRscannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private static final int REQUEST_CAMERA =1;
    private ZXingScannerView scannerView;
    private DatabaseReference eventsRef, dbRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);
        mAuth = FirebaseAuth.getInstance();
        eventsRef = FirebaseDatabase.getInstance().getReference("Events");
        dbRef = FirebaseDatabase.getInstance().getReference();
        currentUserId = mAuth.getCurrentUser().getUid();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(CheckPermission()){
                Toast.makeText(QRscannerActivity.this, "Permission granted", Toast.LENGTH_LONG).show();
            }
            else {
                RequestPermission();
            }
        }
    }

    private void RequestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    private boolean CheckPermission() {
        return (ContextCompat.checkSelfPermission(QRscannerActivity.this, CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    public void onRequestPermissionResult(int requestCode, String permission[], int grantResults[]){
        switch (requestCode){
            case REQUEST_CAMERA :
                if(grantResults.length > 0){
                    boolean cameraAccepted =  grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted){
                        Toast.makeText(QRscannerActivity.this, "Permission granted --", Toast.LENGTH_LONG).show();
                    }else {

                        Toast.makeText(QRscannerActivity.this, "Permission not granted --", Toast.LENGTH_LONG).show();
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                            if(shouldShowRequestPermissionRationale(CAMERA)){
                                displayAlertMessage("Allow access please",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                requestPermissions(new String[]{CAMERA}, REQUEST_CAMERA);
                                            }
                                        });
                                return;
                            }
                        }
                    }


                }
                break;
        }
    }

    private void displayAlertMessage(String message, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(QRscannerActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", listener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void handleResult(Result rawResult) {
        final String scanResult = rawResult.getText();

        eventsRef.child(scanResult).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                if(!dataSnapshot.child("guests").hasChild(currentUserId)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(QRscannerActivity.this);
                    builder.setTitle("scan result");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Calendar date = Calendar.getInstance();
                            SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd");
                            String date_attended = currentDate.format(date.getTime());
                            final String ownerID = dataSnapshot.child("ownerID").getValue().toString();
                            eventsRef.child(scanResult).child("guests").child(currentUserId).child("date")
                                    .setValue(date_attended);
                            eventsRef.child(scanResult).child("guests").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                        if(!currentUserId.equals(snapshot.getKey())) {
                                            increaseScore(snapshot.getKey());
                                        }
                                    }
                                    if(!currentUserId.equals(ownerID)) {
                                        increaseScore(ownerID);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                            Intent eventPage = new Intent(QRscannerActivity.this, EventProfileActivity.class);
                            eventPage.putExtra("visit_event_id", scanResult);
                            startActivity(eventPage);
                        }
                    });
                    builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            scannerView.resumeCameraPreview(QRscannerActivity.this);
                        }
                   });
                    builder.setMessage("Do you want to join " + dataSnapshot.child("title").getValue().toString() + "?");
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(QRscannerActivity.this);
                    builder.setTitle("scan result");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent eventPage = new Intent(QRscannerActivity.this, EventProfileActivity.class);
                            eventPage.putExtra("visit_event_id", scanResult);
                            startActivity(eventPage);
                        }
                    });
                    builder.setMessage("You have already joined " + dataSnapshot.child("title").getValue().toString());
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("scan result");
//        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                scannerView.resumeCameraPreview(QRscannerActivity.this);
//
//            }
//        });
//        builder.setNeutralButton("Visit", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(scanResult));
//                startActivity(intent);
//            }
//        });
//        builder.setMessage(scanResult);
//        AlertDialog alertDialog = builder.create();
//        alertDialog.show();
    }

    private void increaseScore(final String user) {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long score;
                if(dataSnapshot.child("Scores").child(user).hasChild(currentUserId)) {
                    score = (long) dataSnapshot.child("Scores").child(user).child(currentUserId).getValue();
                    score += 1;
                } else {
                    score = 1;
                }
                dbRef.child("Scores").child(user).child(currentUserId).setValue(score);
                dbRef.child("Scores").child(currentUserId).child(user).setValue(score);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        scannerView.stopCamera();
    }

    @Override
    public void onResume() {

        super.onResume();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(CheckPermission()){
                if(scannerView == null){
                    scannerView = new ZXingScannerView(this);
                    setContentView(scannerView);
                }
                scannerView.setResultHandler(this);
                scannerView.startCamera();
            }
            else {
                RequestPermission();
            }
        }
    }
}
