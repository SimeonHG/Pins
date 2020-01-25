package com.example.pins;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EventCreateActivity extends AppCompatActivity {
    private Calendar start_date;
    private TextView test;
    private EditText title, desc;
    private Button createEventBtn;
    private FirebaseAuth mAuth;

    private DatabaseReference eventsRef;
    long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_create);

        start_date = (Calendar) getIntent().getExtras().get("start_time");
        test = findViewById(R.id.test);
        title = findViewById(R.id.createTitle);
        desc = findViewById(R.id.createDesc);

        createEventBtn = findViewById(R.id.createEventBtn);
        mAuth = FirebaseAuth.getInstance();
        test.setText(start_date.getTime().toString());
        eventsRef = FirebaseDatabase.getInstance().getReference("Events");

        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    id = dataSnapshot.getChildrenCount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        createEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventsRef.child(String.valueOf(id+1)).child("title").setValue(title.getText().toString());
                eventsRef.child(String.valueOf(id+1)).child("desc").setValue(desc.getText().toString());
                eventsRef.child(String.valueOf(id+1)).child("time_created").setValue(Calendar.getInstance().getTime());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String formattedStartTime = sdf.format(start_date.getTime());
                eventsRef.child(String.valueOf(id+1)).child("date_start").setValue(formattedStartTime);
                eventsRef.child(String.valueOf(id+1)).child("time_start").setValue(start_date.getTime());
                eventsRef.child(String.valueOf(id+1)).child("start_time_as_string").setValue(start_date.getTime().toString());
                eventsRef.child(String.valueOf(id+1)).child("ownerID").setValue(mAuth.getCurrentUser().getUid());
                Intent eventsPage = new Intent(EventCreateActivity.this, EventsActivity.class);
                startActivity(eventsPage);
            }
        });
    }
}
