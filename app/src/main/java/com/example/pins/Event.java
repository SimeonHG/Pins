package com.example.pins;

import com.google.firebase.database.Exclude;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Event {

    public Date dateCreated;
    public Date time_start;
    public String title;
    public String desc;
    public String start_time_as_string;

    public Event() {

    }


    public Event(Date dateCreated, Date dateStarted, String title, String description) {
        this.dateCreated = dateCreated;
        this.time_start = dateStarted;
        this.title = title;
        this.desc = description;
    }



    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("title", title);
        result.put("desc", desc);
        result.put("time_started", time_start);
        result.put("start_time_as_string", time_start.toString());

        return result;
    }
}
