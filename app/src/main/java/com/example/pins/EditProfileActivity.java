package com.example.pins;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText username, fullName, gender;
    private Button FinishBtn;
    //private ImageView profileImage;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);


        username = findViewById(R.id.username);
        fullName = findViewById(R.id.fullName);
        gender = findViewById(R.id.gender);
        FinishBtn = findViewById(R.id.finishBtn);

        FinishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveAccountInfo();
            }
        });
    }

    private void SaveAccountInfo() {
        String uname = username.getText().toString();
        String fname = fullName.getText().toString();
        String gen = gender.getText().toString();



        String key = usersRef.push().getKey();

        User user = new User(uname, fname, gen);

        Map<String, Object> userVals = user.toMap();

        if(TextUtils.isEmpty(uname)){
            Toast.makeText(EditProfileActivity.this,"Provide username", Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(fname)){
            Toast.makeText(EditProfileActivity.this,"Provide full name", Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(gen)){
            Toast.makeText(EditProfileActivity.this,"Provide gender", Toast.LENGTH_LONG).show();
        }
        else {
            HashMap childUpdates = new HashMap();
            childUpdates.put("GfpweNEFL79RO77M0s7Q", userVals);

            usersRef.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(EditProfileActivity.this,"Success ", Toast.LENGTH_LONG).show();
                    } else if(task.isCanceled()){
                        Toast.makeText(EditProfileActivity.this,"Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
