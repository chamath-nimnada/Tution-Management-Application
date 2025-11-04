package com.example.teacherapp.Models;

import com.google.firebase.Timestamp;



public class Note {
    private String fileName, downloadUrl;
    private Timestamp timestamp;

    public Note(String fileName, String downloadUrl, Timestamp timestamp) {
        this.fileName = fileName;
        this.downloadUrl = downloadUrl;
        this.timestamp = timestamp;
    }

    public String getFileName() { return fileName; }
    public String getDownloadUrl() { return downloadUrl; }
    public Timestamp getTimestamp() { return timestamp; }
}
