package com.example.pins;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

public class EventPickDateTimeActivity extends AppCompatActivity {

    private Button setTimeBtn;
    private TimePicker timePicker;
    private DatePicker datePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_making);

        setTimeBtn = findViewById(R.id.setTimeBtn);
        timePicker = findViewById(R.id.time_picker);
        datePicker = findViewById(R.id.date_picker);

        setTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent makeAnEvent = new Intent(EventPickDateTimeActivity.this, EventCreateActivity.class);

               // Intent profileIntent = new Intent(FindFriendsActivity.this, PersonProfileActivity.class);

                Integer day = datePicker.getDayOfMonth();
                Integer month = datePicker.getMonth();
                Integer year = datePicker.getYear();

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    Integer hour = timePicker.getHour();
                    Integer minute = timePicker.getMinute();
                    Calendar start_date = Calendar.getInstance();
                    start_date.set(year,month,day, hour, minute);
                    makeAnEvent.putExtra("start_time", start_date);
                    startActivity(makeAnEvent);
                }





            }
        });
    }
}
