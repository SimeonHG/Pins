package com.example.pins;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class User {
    //public String uid;
    public String username;
    public String fullName;
    public String gender;
    public Locale last_location;


    public User() {

    }

    public User(String username, String fullName, String gender) {
        this.fullName = fullName;
        this.username = username;
        this.gender = gender;

    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();


        result.put("username", username);
        result.put("fullName", fullName);
        result.put("gender", gender);

        return result;
    }
}