package com.example.pins;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EventsActivity extends AppCompatActivity {

    private Button makeEventBtn;
    private Button searchEvemtsBtn, scanQRBtn;
    private EditText searchInputText;

    private RecyclerView Results;

    private DatabaseReference eventsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        makeEventBtn = findViewById(R.id.makeEventBtn);
        searchEvemtsBtn = findViewById(R.id.searchEventsBtn);
        searchInputText = findViewById(R.id.inputEventTitle);
        scanQRBtn = findViewById(R.id.eventScanQRBtn);
        Results = findViewById(R.id.eventSearchResults);
        Results.setHasFixedSize(true);
        Results.setLayoutManager(new LinearLayoutManager(this));

        eventsRef = FirebaseDatabase.getInstance().getReference().child("Events");


        searchEvemtsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = searchInputText.getText().toString();
                SearchEvents(input);
            }
        });


        makeEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent makeAnEvent = new Intent(EventsActivity.this, EventPickDateTimeActivity.class);
                startActivity(makeAnEvent);
            }
        });

        scanQRBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent QRscanner = new Intent(EventsActivity.this, QRscannerActivity.class);
                startActivity(QRscanner);
            }
        });
    }

    private void SearchEvents(String input) {
        java.text.SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Calendar date = Calendar.getInstance();


        Date newDate = new Date(date.getTime().getTime() - (2 * 24 * 60 * 60 * 1000));
        String formattedNewDate = sdf.format(newDate.getTime());

        Query test = eventsRef.orderByChild("date_start").startAt(formattedNewDate).endAt(sdf.format(date.getTime()));
        Query searchEventsQuery = (eventsRef.orderByChild("title").startAt(input).endAt(input + "\uf8ff"));

        FirebaseRecyclerAdapter<Event, EventsActivity.FindEventsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Event, EventsActivity.FindEventsViewHolder>
                (
                        Event.class, R.layout.all_events_display_layout, EventsActivity.FindEventsViewHolder.class, searchEventsQuery
                ) {
            @Override
            protected void populateViewHolder(EventsActivity.FindEventsViewHolder viewHolder, Event event, final int position) {
                if(event.time_start.after(new Date( Calendar.getInstance().getTime().getTime()- (2 * 24 * 60 * 60 * 1000)))) {
                    viewHolder.setTitle(event.title);
                    viewHolder.setDesc(event.desc);
                    viewHolder.setStartTime(String.valueOf(event.time_start));

                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String visit_event_id = getRef(position).getKey();

                            Intent eventIntent = new Intent(EventsActivity.this, EventProfileActivity.class);
                            eventIntent.putExtra("visit_event_id", visit_event_id);
                            startActivity(eventIntent);
                        }
                    });
                }
                else {
                    viewHolder.invisible();
                }
            }
        };
        Results.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FindEventsViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public FindEventsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setTitle(String title){
            TextView eTitle = (TextView) mView.findViewById(R.id.all_events_title);
            eTitle.setText(title);
        }
        public void setDesc(String desc){
            TextView eDesc = (TextView) mView.findViewById(R.id.all_events_desc);
            eDesc.setText(desc);
        }
        public void setStartTime(String startTime){
            TextView eStartTime = (TextView) mView.findViewById(R.id.all_events_startTime);
            eStartTime.setText(startTime);
        }

        public void invisible() {
            mView.findViewById(R.id.viewHolderLayout).setVisibility(View.GONE);
            mView.findViewById(R.id.all_events_title).setVisibility(View.GONE);
            mView.findViewById(R.id.all_events_desc).setVisibility(View.GONE);
            mView.findViewById(R.id.all_events_startTime).setVisibility(View.GONE);
        }
    }
}
