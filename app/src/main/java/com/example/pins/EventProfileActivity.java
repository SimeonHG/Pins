package com.example.pins;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

public class EventProfileActivity extends AppCompatActivity {

    private TextView title, desc, time_start;
    private String eventID;
    private DatabaseReference eventsRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_profile);


        title = findViewById(R.id.eventProfileTitle);

        desc = findViewById(R.id.eventProfileDesc);


        time_start = findViewById(R.id.eventProfileStartTime);

        eventID = getIntent().getExtras().get("visit_event_id").toString();
        mAuth = FirebaseAuth.getInstance();
        eventsRef = FirebaseDatabase.getInstance().getReference().child("Events");
        eventsRef.child(eventID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String db_title = dataSnapshot.child("title").getValue().toString();
                String db_desc = dataSnapshot.child("desc").getValue().toString();
                String db_start_time = (dataSnapshot.child("start_time_as_string").getValue().toString());

                title.setText( db_title);
                desc.setText(db_desc);
                time_start.setText(db_start_time);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
