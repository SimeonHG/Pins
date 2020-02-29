package com.example.pins;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText username, fullname, gender;
    private Button FinishBtn;
    //private ImageView profileImage;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private DatabaseReference dbRef;
    private FirebaseUser currentUser;

    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserID);
        dbRef = FirebaseDatabase.getInstance().getReference();


        username = findViewById(R.id.username);
        fullname = findViewById(R.id.fullName);
        gender = findViewById(R.id.gender);

        dbRef.child("Users").child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    username.setText(dataSnapshot.child("display_name").getValue().toString());
                    fullname.setText(dataSnapshot.child("full_name").getValue().toString());
                    gender.setText(dataSnapshot.child("gender").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        FinishBtn = findViewById(R.id.finishBtn);

        FinishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveAccountInfo();
            }
        });
    }

    private void SaveAccountInfo() {
        final String uname = username.getText().toString();
        final String fname = fullname.getText().toString();
        final String gen = gender.getText().toString();

        dbRef.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean exists = false;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if(snapshot.child("display_name").getValue().equals(uname) && !snapshot.getKey().equals(currentUserID)) {
                        exists = true;
                    }
                }

                if(exists){
                    Toast.makeText(EditProfileActivity.this,"Username taken, choose a different one", Toast.LENGTH_LONG).show();
                } else if(TextUtils.isEmpty(uname)){
                    Toast.makeText(EditProfileActivity.this,"Provide username", Toast.LENGTH_LONG).show();
                } else {


                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("display_name", uname);
                    childUpdates.put("full_name", fname);
                    childUpdates.put("gender", gen);
                    childUpdates.put("uid", currentUserID);

                    userRef.updateChildren(childUpdates);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
