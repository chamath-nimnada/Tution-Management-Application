package com.example.teacherapp.Models;
public class Student {
    private String id;
    private String name;

    public Student() {} // Needed for Firestore

    public Student(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }
}
