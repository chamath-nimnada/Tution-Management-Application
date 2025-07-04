package com.example.tutionmanagementapplication.plainclasses;

public class Student {

    private String uid;
    private String fullName;
    private String email;
    private String birthday;
    private String phone;
    private String parentPhone;
    private String address;
    private String grade;
    private String clazz;          // "class" is a Java keyword
    private long   registeredAt;

    /* NEW fixed‑value fields */
    private String position;       // always "student"
    private String status;         // always "pending"

    /** Public no‑arg constructor required by Firestore. */
    public Student() {}

    public Student(String uid, String fullName, String email, String birthday,
                   String phone, String parentPhone, String address,
                   String grade, String clazz, long registeredAt,
                   String position, String status) {

        this.uid          = uid;
        this.fullName     = fullName;
        this.email        = email;
        this.birthday     = birthday;
        this.phone        = phone;
        this.parentPhone  = parentPhone;
        this.address      = address;
        this.grade        = grade;
        this.clazz        = clazz;
        this.registeredAt = registeredAt;
        this.position     = position;
        this.status       = status;
    }

    /* ---------- getters (generate setters too if you need them) ---------- */

    public String getUid()         { return uid; }
    public String getFullName()    { return fullName; }
    public String getEmail()       { return email; }
    public String getBirthday()    { return birthday; }
    public String getPhone()       { return phone; }
    public String getParentPhone() { return parentPhone; }
    public String getAddress()     { return address; }
    public String getGrade()       { return grade; }
    public String getClazz()       { return clazz; }
    public long   getRegisteredAt(){ return registeredAt; }
    public String getPosition()    { return position; }
    public String getStatus()      { return status; }
}
