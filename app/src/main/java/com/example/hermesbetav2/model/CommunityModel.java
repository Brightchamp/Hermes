package com.example.hermesbetav2.model;

public class CommunityModel {

    private String admin;
    private String name;

    public CommunityModel() { //mandatory empty constructor for Firebase
    }

    public CommunityModel(String admin, String name) {
        this.admin = admin;
        this.name = name;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
