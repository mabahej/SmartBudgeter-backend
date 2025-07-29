package com.smartbudgeter.demo.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String sender; // "user" or "bot"

    @Column(columnDefinition = "TEXT")
    private String text;

    private String intent;

    private LocalDateTime timestamp;
    
    // Add this field to link to the actual user
    private Integer userId;

    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
    }

    public ChatMessage(String sender, String text, String intent) {
        this.sender = sender;
        this.text = text;
        this.intent = intent;
        this.timestamp = LocalDateTime.now();
    }
    
    // Constructor with userId
    public ChatMessage(String sender, String text, String intent, Integer userId) {
        this.sender = sender;
        this.text = text;
        this.intent = intent;
        this.userId = userId;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public String getIntent() {
        return intent;
    }
    public void setIntent(String intent) {
        this.intent = intent;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    // New getter and setter for userId
    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}