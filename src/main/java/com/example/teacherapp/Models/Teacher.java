package com.example.teacherapp.Models;

public class Teacher {
    private String teacherId;
    private String name;
    private String email;
    private String subject;

    public Teacher() {} // Firestore requires empty constructor

    public Teacher(String teacherId, String name, String email, String subject) {
        this.teacherId = teacherId;
        this.name = name;
        this.email = email;
        this.subject = subject;
    }

    public String getTeacherId() { return teacherId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getSubject() { return subject; }
}
