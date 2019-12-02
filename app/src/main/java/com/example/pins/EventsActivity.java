package com.example.pins;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class EventsActivity extends AppCompatActivity {

    private Button makeEventBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        makeEventBtn = findViewById(R.id.makeEventBtn);


        makeEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent makeAnEvent = new Intent(EventsActivity.this, EventPickDateTimeActivity.class);
                startActivity(makeAnEvent);
            }
        });
    }
}
