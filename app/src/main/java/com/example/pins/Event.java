package com.example.pins;

import java.util.Date;

public class Event {

    private Date dateCreated;
    private Date dateStarted;
    private String title;
    private String decription;


    public Event(Date dateCreated, Date dateStarted, String title, String decription) {
        this.dateCreated = dateCreated;
        this.dateStarted = dateStarted;
        this.title = title;
        this.decription = decription;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateStarted() {
        return dateStarted;
    }

    public void setDateStarted(Date dateStarted) {
        this.dateStarted = dateStarted;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDecription() {
        return decription;
    }

    public void setDecription(String decription) {
        this.decription = decription;
    }




}
