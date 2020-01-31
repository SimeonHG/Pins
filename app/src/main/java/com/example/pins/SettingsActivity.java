package com.example.pins;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends AppCompatActivity {
    private SeekBar seekBar;
    private TextView radiusText, visibilityText;
    private FirebaseAuth mAuth;
    private DatabaseReference dbref;
    private Button saveRadBtn;
    private Switch visibilitySwitch;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        seekBar = findViewById(R.id.settingsSeekBar);
        radiusText = findViewById(R.id.settingsRadTextView);
        saveRadBtn = findViewById(R.id.settingsSaveRadBtn);
        visibilitySwitch  = findViewById(R.id.visibilitySwitch);
        visibilityText = findViewById(R.id.settingsVisibilityText);
        visibilityText.setText("Not Visible");


        mAuth = FirebaseAuth.getInstance();
        dbref = FirebaseDatabase.getInstance().getReference();
        currentUserID = mAuth.getCurrentUser().getUid();

        maintainUI();



        visibilitySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                dbref.child("Locations").child(currentUserID).child("visible").setValue(b);

            }
        });

        saveRadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbref.child("Radiuses").child(currentUserID).child("radius").setValue(seekBar.getProgress()*250);
            }
        });
        manipulateSeekbar();
    }

    private void maintainUI() {
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //System.out.println(dataSnapshot.child("Locations").child(currentUserID).child("visible").getValue().toString());
                if(dataSnapshot.child("Locations").child(currentUserID).hasChild("visible")) {
                    boolean visibility = (boolean) dataSnapshot.child("Locations").child(currentUserID).child("visible").getValue();
                    if (visibility) {
                        visibilitySwitch.setChecked(true);
                        visibilityText.setText("Visible");
                    } else {
                        visibilityText.setText("Not Visible");
                    }
                }
                if(dataSnapshot.child("Radiuses").hasChild(currentUserID)){
                    long rad = (long)dataSnapshot.child("Radiuses").child(currentUserID).child("radius").getValue();
                    seekBar.setProgress(((int) rad)/250);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void manipulateSeekbar(){


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress_value;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progress_value = i;

                if(progress_value >= 16){
                    radiusText.setText(progress_value/4 + "." +(progress_value%4)*250 + "km");
                }else {
                    radiusText.setText(progress_value*250 + "m");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
               
            }
        });
    }
}
