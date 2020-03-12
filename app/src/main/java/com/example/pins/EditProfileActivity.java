package com.example.pins;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText username, fullname, gender;
    private Button FinishBtn;
    private Button uploadPhotoBtn;
    private ImageView profileImage;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private DatabaseReference dbRef;
    private StorageReference userProfilePicRef;
    private FirebaseUser currentUser;

    private String currentUserID;
    final static int Gallery_Pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserID);
        dbRef = FirebaseDatabase.getInstance().getReference();
        userProfilePicRef = FirebaseStorage.getInstance().getReference().child("Profile pics");

        uploadPhotoBtn = findViewById(R.id.uploadPhotoBtn);
        profileImage = findViewById(R.id.all_users_pic);


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

        uploadPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });
        Picasso.get().load(R.drawable.genericpic).into(profileImage);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.hasChild("profilePic")){

                    String image = dataSnapshot.child("profilePic").getValue().toString();
                    Picasso.get().load(image).placeholder(R.drawable.genericpic).into(profileImage);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Gallery_Pick && resultCode==RESULT_OK && data!=null){
            Uri imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                Uri resultUri = result.getUri();
                StorageReference filePath = userProfilePicRef.child(currentUserID + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(EditProfileActivity.this, "Picture uploaded", Toast.LENGTH_LONG).show();
                            Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();
                            final String picUrl = task.getResult().getUploadSessionUri().toString();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUrl = uri.toString();
                                    userRef.child("profilePic").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
//                                                Intent selfIntent = new Intent(EditProfileActivity.this, EditProfileActivity.class);
//                                                startActivity(selfIntent);

                                            } else {
                                                Toast.makeText(EditProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }
                            });



//                            userRef.child("profilePic").setValue(picUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//                                    if(task.isSuccessful()){
//                                        Toast.makeText(EditProfileActivity.this, "Picture saved in FB database", Toast.LENGTH_LONG).show();
//                                    }
//                                    else {
//                                        Toast.makeText(EditProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
//                                    }
//                                }
//                            });
                        } else {
                            Toast.makeText(EditProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else {
                Toast.makeText(EditProfileActivity.this, "image can't be cropped", Toast.LENGTH_LONG).show();
            }
        }
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
