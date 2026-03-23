package com.example.myapplication;

public class LearningMaterial {

    public String title;
    public String attachmentUri;
    public String teacherUid;
    public long timestamp;

    // Required empty constructor for Firebase
    public LearningMaterial() {
    }

    public LearningMaterial(String title,
                            String attachmentUri,
                            String teacherUid,
                            long timestamp) {
        this.title = title;
        this.attachmentUri = attachmentUri;
        this.teacherUid = teacherUid;
        this.timestamp = timestamp;
    }
}