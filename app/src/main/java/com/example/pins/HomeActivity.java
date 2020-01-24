package com.example.pins;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    private Button logoutBtn;
    private Button openProfile, findFriends, editProfile, checkMap, events, settings, heatmap;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        logoutBtn = findViewById(R.id.homePageLogoutBtn);
        openProfile = findViewById(R.id.homeProfileBtn);
        findFriends = findViewById(R.id.findFriendsBtn);
        editProfile = findViewById(R.id.editProfileBtn);
        checkMap = findViewById(R.id.checkMapBtn);
        events = findViewById(R.id.eventsBtn);
        settings = findViewById(R.id.settingsBtn);
        heatmap = findViewById(R.id.checkHeatmapBtn);

        mAuth = FirebaseAuth.getInstance();

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut(); //this will logout from Firebase
                LoginManager.getInstance().logOut(); //this will logout from Facebook
                updateUI();

            }
        });
        openProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profilePage = new Intent(HomeActivity.this, PersonProfileActivity.class);
                profilePage.putExtra("visit_user_id", mAuth.getCurrentUser().getUid());
                startActivity(profilePage);
            }
        });
        findFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent findFriendsPage = new Intent(HomeActivity.this, FindFriendsActivity.class);
                startActivity(findFriendsPage);
            }
        });
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editProfile = new Intent(HomeActivity.this, EditProfileActivity.class);
                startActivity(editProfile);
            }
        });
        checkMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent maps = new Intent(HomeActivity.this, MapsActivity.class);
                startActivity(maps);
            }
        });
        events.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent eventsPage = new Intent(HomeActivity.this, EventsActivity.class);
                startActivity(eventsPage);
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsPage = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(settingsPage);
            }
        });
        heatmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsPage = new Intent(HomeActivity.this, HeatmapActivity.class);
                startActivity(settingsPage);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null){
            updateUI();
        }
    }

    private void updateUI(){
        Toast.makeText(HomeActivity.this, "You are logged out.",  Toast.LENGTH_SHORT).show();

        Intent HomePage = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(HomePage);
        finish();
    }
}
