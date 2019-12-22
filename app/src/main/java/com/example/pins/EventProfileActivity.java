package com.example.pins;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.share.Share;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.Date;

public class EventProfileActivity extends AppCompatActivity {

    private TextView title, desc, time_start;
    private String eventID, currentUserID;
    private DatabaseReference eventsRef, usersRef, locationsRef, dbRef;
    private FirebaseAuth mAuth;
    private ImageView QRcode;

    private Button shareBtn;
    private CallbackManager callbackManager;
    private ShareDialog shareDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_profile);


        title = findViewById(R.id.eventProfileTitle);

        desc = findViewById(R.id.eventProfileDesc);
        mAuth = FirebaseAuth.getInstance();

        QRcode = findViewById(R.id.qr_code);
        shareBtn = findViewById(R.id.eventProfileShareBtn);
        FacebookSdk.sdkInitialize(this.getApplicationContext());

        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);



        time_start = findViewById(R.id.eventProfileStartTime);

        eventID = getIntent().getExtras().get("visit_event_id").toString();
        mAuth = FirebaseAuth.getInstance();


        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        currentUserID = mAuth.getCurrentUser().getUid();

        locationsRef = FirebaseDatabase.getInstance().getReference().child("Locations");
        dbRef  = FirebaseDatabase.getInstance().getReference();

        eventsRef = FirebaseDatabase.getInstance().getReference().child("Events");
        eventsRef.child(eventID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String db_title = dataSnapshot.child("title").getValue().toString();
                String db_desc = dataSnapshot.child("desc").getValue().toString();
                String db_start_time = (dataSnapshot.child("start_time_as_string").getValue().toString());
                Toast.makeText(EventProfileActivity.this,dataSnapshot.child("ownerID").getValue().toString(), Toast.LENGTH_LONG).show();
                if(mAuth.getCurrentUser().getUid().equals( dataSnapshot.child("ownerID").getValue().toString())){
                    MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

                    BitMatrix bitMatrix = null;
                    try {
                        bitMatrix = multiFormatWriter.encode(eventID, BarcodeFormat.QR_CODE, 500, 500);
                        Toast.makeText(EventProfileActivity.this, "Ne baca", Toast.LENGTH_LONG).show();

                    } catch (WriterException e) {
                        e.printStackTrace();
                    }

                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                    QRcode.setImageBitmap(bitmap);
                }
                else {
                   // Toast.makeText(EventProfileActivity.this, "Ne vliza v if-a", Toast.LENGTH_LONG).show();
                    //TODO: tuka pravim scanner-a
                }

                title.setText( db_title);
                desc.setText(db_desc);
                time_start.setText(db_start_time);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String text = " is attending " + title.getText() + " with ";


                dbRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String userName = dataSnapshot.child("Users").child(currentUserID).child("full_name").getValue().toString();
                        String latitude = dataSnapshot.child("Locations").child(currentUserID).child("last_location").child("latitude").getValue().toString();
                        String longitude = dataSnapshot.child("Locations").child(currentUserID).child("last_location").child("latitude").getValue().toString();


                        ShareLinkContent linkContent = new ShareLinkContent.Builder()
                                .setQuote(userName + text)
                                .setContentUrl(Uri.parse("https://www.google.com/maps/search/google+maps/@"+ latitude + "," + longitude +",15.89z"))
                                .setShareHashtag(new ShareHashtag.Builder()
                                    .setHashtag("#UsingPINS")
                                    .build())
                                .build();
                        if(ShareDialog.canShow(ShareLinkContent.class)){
                            shareDialog.show(linkContent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



            }
        });
    }
}
//TODO: kak se pravi attending post; koi oshte attend-va; possibly da ne e link? ;