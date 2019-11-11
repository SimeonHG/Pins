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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText username, fullname, gender;
    private Button FinishBtn;
    //private ImageView profileImage;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
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


        username = findViewById(R.id.username);
        fullname = findViewById(R.id.fullName);
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
        String fname = fullname.getText().toString();
        String gen = gender.getText().toString();










        if(TextUtils.isEmpty(uname)){
            Toast.makeText(EditProfileActivity.this,"Provide username", Toast.LENGTH_LONG).show();
        }

        else {
            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                    .setDisplayName(uname)
                    .build();

            currentUser.updateProfile(profileUpdate)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(EditProfileActivity.this,"Done", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(EditProfileActivity.this,task.getException().toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("display_name", currentUser.getDisplayName());
            childUpdates.put("full_name", fname);
            childUpdates.put("gender", gen);

            userRef.updateChildren(childUpdates);

        }
    }
}
