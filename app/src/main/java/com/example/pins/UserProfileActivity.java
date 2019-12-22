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

public class UserProfileActivity extends AppCompatActivity {

    private TextView userName, fullName, gender, pic;
    private DatabaseReference userProfileRef;
    private FirebaseAuth mAuth;

    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        userProfileRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);




        userName = findViewById(R.id.profile_display_name);
        fullName = findViewById(R.id.profile_fullname_name);
        gender = findViewById(R.id.profile_gender);
        pic = findViewById(R.id.profile_pic);



        userProfileRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String MyUserName = dataSnapshot.child("display_name").getValue().toString();
                    String MyFullName = dataSnapshot.child("full_name").getValue().toString();
                    String MyGender = dataSnapshot.child("gender").getValue().toString();

                    userName.setText("@" + MyUserName);
                    fullName.setText(MyFullName);
                    gender.setText(MyGender);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
