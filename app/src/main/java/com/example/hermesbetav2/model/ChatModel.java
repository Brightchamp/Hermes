package com.example.hermesbetav2.model;

import com.google.firebase.Timestamp;

public class ChatModel {
    private String messageText;
    private String sender;
    private String sendId;
    private String community;
    private Timestamp timestamp;

    public ChatModel() { //mandatory empty constructor for Firebase
    }

    public ChatModel(String messageText, String sender, String sendId, String community, Timestamp timestamp) {
        this.messageText = messageText;
        this.sender = sender;
        this.sendId = sendId;
        this.community = community;
        this.timestamp = timestamp;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSendId() {
        return sendId;
    }

    public void setSendId(String sendId) {
        this.sendId = sendId;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
