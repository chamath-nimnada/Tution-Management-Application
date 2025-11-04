package com.example.teacherapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teacherapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class TeacherDashboardActivity extends AppCompatActivity {

    private TextView welcomeText;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    String teacherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        teacherId = getIntent().getStringExtra("teacherId");


        welcomeText = findViewById(R.id.welcomeText);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Get current teacher UID
        String uid = mAuth.getCurrentUser().getUid();

        // Fetch teacher name from Firestore
        db.collection("teachers").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String name = doc.getString("name");
                welcomeText.setText("Welcome, " + name);
            } else {
                Toast.makeText(this, "Teacher data not found.", Toast.LENGTH_SHORT).show();
            }
        });

        //code for the navigation of view class activity through dashboard
        Button viewClassesBtn = findViewById(R.id.viewClassesBtn);
        viewClassesBtn.setOnClickListener(v -> {
            Intent i = new Intent(TeacherDashboardActivity.this, ViewAssignedClassesActivity.class);
            i.putExtra("teacherId", teacherId); // pass the logged-in teacher ID
            startActivity(i);
        });


        //code for the navigation of Upload notes activity through dashboard
        Button uploadNotesBtn = findViewById(R.id.uploadNotesBtn);
        uploadNotesBtn.setOnClickListener(v -> {
            Intent i = new Intent(TeacherDashboardActivity.this, UploadNotesActivity.class);
            i.putExtra("teacherId", teacherId);
            startActivity(i);
        });


        //code for the navigation of view notes activity through dashboard
        Button viewNotesBtn = findViewById(R.id.viewNotesBtn);
        viewNotesBtn.setOnClickListener(v -> {
            Intent i = new Intent(TeacherDashboardActivity.this, ViewNotesActivity.class);
            i.putExtra("teacherId", teacherId);
            startActivity(i);
        });

        //code for the navigation of mark attendance activity through dashboard
        Button scanQRBtn = findViewById(R.id.markAttendanceBtn);
        scanQRBtn.setOnClickListener(v -> {
            Intent i = new Intent(TeacherDashboardActivity.this, MarkAttendanceActivity.class);
            i.putExtra("teacherId", teacherId);
            startActivity(i);
        });


        //code for the navigation of logout  activity through dashboard
        Button logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });


        // Button listeners
        findViewById(R.id.viewClassesBtn).setOnClickListener(v ->
                startActivity(new Intent(this, ViewAssignedClassesActivity.class)));

        findViewById(R.id.uploadNotesBtn).setOnClickListener(v ->
                startActivity(new Intent(this, UploadNotesActivity.class)));

        findViewById(R.id.viewNotesBtn).setOnClickListener(v ->
                startActivity(new Intent(this, ViewNotesActivity.class)));

        findViewById(R.id.markAttendanceBtn).setOnClickListener(v ->
                startActivity(new Intent(this, MarkAttendanceActivity.class)));

        findViewById(R.id.logoutBtn).setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
