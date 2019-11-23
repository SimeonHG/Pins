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

public class PersonProfileActivity extends AppCompatActivity {


    private TextView displayName, fullName, gender;
    private DatabaseReference userRef, personRef;
    private FirebaseAuth mAuth;
    private String senderUserID, recieverUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth = FirebaseAuth.getInstance();
        recieverUserID = getIntent().getExtras().get("visit_user_id").toString();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");


        InitFields(); // TODO: every activity should be refactored like <- this

        userRef.child(recieverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String MyUserName = dataSnapshot.child("display_name").getValue().toString();
                    String MyFullName = dataSnapshot.child("full_name").getValue().toString();
                    String MyGender = dataSnapshot.child("gender").getValue().toString();

                    displayName.setText("@" + MyUserName);
                    fullName.setText(MyFullName);
                    gender.setText(MyGender);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void InitFields() {
        displayName = findViewById(R.id.personDisplayName);
        fullName = findViewById(R.id.personFullname);
        gender = findViewById(R.id.personGender);
    }


}
