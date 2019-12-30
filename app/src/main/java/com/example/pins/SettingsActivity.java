package com.example.pins;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsActivity extends AppCompatActivity {
    private SeekBar seekBar;
    private TextView radiusText;
    private FirebaseAuth mAuth;
    private DatabaseReference dbref;
    private Button saveRadBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        seekBar = findViewById(R.id.settingsSeekBar);
        radiusText = findViewById(R.id.settingsRadTextView);
        saveRadBtn = findViewById(R.id.settingsSaveRadBtn);

        mAuth = FirebaseAuth.getInstance();
        dbref = FirebaseDatabase.getInstance().getReference();

        saveRadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbref.child("Radiuses").child(mAuth.getCurrentUser().getUid()).child("radius").setValue(seekBar.getProgress()*250);
            }
        });
        manipulateSeekbar();
    }
    public void manipulateSeekbar(){
        radiusText.setText(seekBar.getProgress() + "km");

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
                if(progress_value >= 16){
                    radiusText.setText(progress_value/4 + "." +(progress_value%4)*250 + "km");
                }else {
                    radiusText.setText(progress_value*250 + "m");
                }
            }
        });
    }
}
