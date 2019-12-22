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

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateStarted() {
        return time_start;
    }

    public void setDateStarted(Date dateStarted) {
        this.time_start = dateStarted;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDecription() {
        return desc;
    }

    public void setDecription(String decription) {
        this.desc = decription;
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
