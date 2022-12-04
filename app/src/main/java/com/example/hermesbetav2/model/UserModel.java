package com.example.hermesbetav2.model;

import java.util.List;

public class UserModel {
    private String userId;
    private String userName;
    private List<String> userCommunities;

    public UserModel() { //mandatory empty constructor for Firebase
    }

    public UserModel(String userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    public UserModel(String userId, String userName, List<String> userCommunities) {
        this.userId = userId;
        this.userName = userName;
        this.userCommunities = userCommunities;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<String> getUserCommunities() {
        return userCommunities;
    }

    public void setUserCommunities(List<String> userCommunities) {
        this.userCommunities = userCommunities;
    }
}


