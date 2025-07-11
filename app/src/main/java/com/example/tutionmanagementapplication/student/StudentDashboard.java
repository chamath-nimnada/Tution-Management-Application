package com.example.tutionmanagementapplication.student;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tutionmanagementapplication.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class StudentDashboard extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";

    // UI Components
    private TextView welcomeMessage;
    private FloatingActionButton fabQRCode;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Initialize UI components
        initializeViews();

        // Load user data
        if (currentUser != null) {
            Log.d(TAG, "Current user found: " + currentUser.getEmail());
            Log.d(TAG, "Current user UID: " + currentUser.getUid());
            loadUserData();
        } else {
            Log.w(TAG, "No current user found");
            // User is not logged in, redirect to login
            redirectToLogin();
        }

        // FloatingActionButton for QR Code
        fabQRCode = findViewById(R.id.fabAddTask);
        fabQRCode.setOnClickListener(view -> {
            if (currentUser != null) {
                Intent intent = new Intent(StudentDashboard.this, QrView.class);
                startActivity(intent);
            } else {
                Toast.makeText(StudentDashboard.this, "Please log in first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeViews() {
        welcomeMessage = findViewById(R.id.welcomeMessage);
    }

    private void loadUserData() {
        // Show loading state
        welcomeMessage.setText("Welcome, Loading...");

        // Get current user ID
        String userId = currentUser.getUid();
        Log.d(TAG, "Loading user data for userId: " + userId);

        // Reference to user document in Firestore
        DocumentReference userRef = db.collection("students").document(userId);

        // Fetch user data
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                Log.d(TAG, "Document exists: " + document.exists());

                if (document.exists()) {
                    // Document exists, get user data
                    String fullName = document.getString("fullName");
                    Log.d(TAG, "Full name retrieved: " + fullName);

                    // Log all available fields for debugging
                    Log.d(TAG, "All document data: " + document.getData());

                    // Update UI with user name
                    if (fullName != null && !fullName.isEmpty()) {
                        welcomeMessage.setText("Welcome, " + fullName + "!");
                    } else {
                        welcomeMessage.setText("Welcome, User!");
                    }

                    Log.d(TAG, "User data loaded successfully");
                } else {
                    Log.w(TAG, "User document doesn't exist for userId: " + userId);
                    handleUserDataError();
                }
            } else {
                Log.e(TAG, "Error getting user document", task.getException());
                if (task.getException() != null) {
                    Log.e(TAG, "Error details: " + task.getException().getMessage());
                }
                handleUserDataError();
            }
        });

        // Navigations
        LinearLayout course = findViewById(R.id.navCourses);
        course.setOnClickListener(view -> {
            Intent intent = new Intent(this, CourseDashboard.class);
            startActivity(intent);
        });

        LinearLayout dashboard = findViewById(R.id.navCalendar);
        dashboard.setOnClickListener(view -> {
            Intent intent = new Intent(this, CalendarDashboard.class);
            startActivity(intent);
        });

        LinearLayout announcements = findViewById(R.id.navProfile);
        announcements.setOnClickListener(view -> {
            Intent intent = new Intent(this, ProfileDashboard.class);
            startActivity(intent);
        });
    }

    private void handleUserDataError() {
        welcomeMessage.setText("Welcome, User!");
        Toast.makeText(this, "Unable to load user data", Toast.LENGTH_SHORT).show();
    }

    private void redirectToLogin() {
        welcomeMessage.setText("Welcome, Please log in!");
        Toast.makeText(this, "Please log in to continue", Toast.LENGTH_LONG).show();
        // Redirect logic can be added here if needed
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
        }
    }

    // User data model class
    public static class User {
        private String fullName;
        private String email;
        private String address;
        private String birthday;
        private String className;
        private String grade;
        private String parentPhone;
        private String phone;
        private String position;
        private String status;

        public User() {}

        public User(String fullName, String email, String address, String birthday,
                    String className, String grade, String parentPhone, String phone,
                    String position, String status) {
            this.fullName = fullName;
            this.email = email;
            this.address = address;
            this.birthday = birthday;
            this.className = className;
            this.grade = grade;
            this.parentPhone = parentPhone;
            this.phone = phone;
            this.position = position;
            this.status = status;
        }

        // Getters and setters
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getBirthday() { return birthday; }
        public void setBirthday(String birthday) { this.birthday = birthday; }

        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }

        public String getGrade() { return grade; }
        public void setGrade(String grade) { this.grade = grade; }

        public String getParentPhone() { return parentPhone; }
        public void setParentPhone(String parentPhone) { this.parentPhone = parentPhone; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
