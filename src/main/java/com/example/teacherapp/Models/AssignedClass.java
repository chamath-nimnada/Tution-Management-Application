package com.example.teacherapp.Models;

public class AssignedClass {
    private String classId;
    private String teacherId;
    private String teacherName;
    private String className;
    private String subject;
    private String date;
    private String time;
    private String hall;
    private long studentCount;
    private String status;

    public AssignedClass() {} // Required for Firestore

    public AssignedClass(String classId, String teacherId, String teacherName, String className,
                         String subject, String date, String time, String hall,
                         long studentCount, String status) {
        this.classId = classId;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.className = className;
        this.subject = subject;
        this.date = date;
        this.time = time;
        this.hall = hall;
        this.studentCount = studentCount;
        this.status = status;
    }

    // Getters (you can add setters if needed)
    public String getClassId() { return classId; }
    public String getTeacherName() { return teacherName; }
    public String getClassName() { return className; }
    public String getSubject() { return subject; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getHall() { return hall; }
    public long getStudentCount() { return studentCount; }
    public String getStatus() { return status; }
}

