package com.example.myapplication;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class Announcement implements Serializable {
    private String id;
    private String title;
    private String message;
    private String content;
    private String date;
    private String time;
    private boolean urgent;
    private String category;
    private String author;
    private long timestamp;

    public Announcement() {
        // Required empty constructor for Firebase
    }

    public Announcement(String title, String content, boolean urgent, String category, String author) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.message = content;
        this.content = content;
        this.urgent = urgent;
        this.category = category;
        this.author = author;
        this.timestamp = System.currentTimeMillis();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        Date now = new Date();

        long diff = System.currentTimeMillis() - timestamp;
        long days = diff / (24 * 60 * 60 * 1000);

        if (days < 1) {
            this.date = "Today";
        } else if (days < 2) {
            this.date = "Yesterday";
        } else {
            this.date = dateFormat.format(now);
        }

        this.time = timeFormat.format(now);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) {
        this.message = message;
        this.content = message;
    }

    public String getContent() { return content != null ? content : message; }
    public void setContent(String content) {
        this.content = content;
        this.message = content;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public boolean isUrgent() { return urgent; }
    public void setUrgent(boolean urgent) { this.urgent = urgent; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}

