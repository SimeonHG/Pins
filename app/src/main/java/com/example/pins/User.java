package com.example.pins;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class User {
    public String uid;
    public String display_name;
    public String full_name;
    public String gender;
    public String profilePic;
    public Locale last_location;


    public User() {

    }

    public User(String uid, String username, String fullName, String gender) {
        this.uid = uid;
        this.full_name = fullName;
        this.display_name = username;
        this.gender = gender;

    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("display_name", display_name);
        result.put("full_name", full_name);
        result.put("gender", gender);

        return result;
    }
}