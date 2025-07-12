package com.example.tutionmanagementapplication.student;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tutionmanagementapplication.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.QueryDocumentSnapshot;

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
            loadUserData();
        } else {
            Log.w(TAG, "No current user found");
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

        // Navigations
        LinearLayout course = findViewById(R.id.navCourses);
        course.setOnClickListener(view -> startActivity(new Intent(this, CourseDashboard.class)));

        LinearLayout calendar = findViewById(R.id.navCalendar);
        calendar.setOnClickListener(view -> startActivity(new Intent(this, CalendarDashboard.class)));

        LinearLayout profile = findViewById(R.id.navProfile);
        profile.setOnClickListener(view -> startActivity(new Intent(this, ProfileDashboard.class)));
    }

    private void loadUserData() {
        String userId = currentUser.getUid();
        DocumentReference userRef = db.collection("students").document(userId);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String fullName = document.getString("fullName");
                    String className = document.getString("className");

                    welcomeMessage.setText((fullName != null && !fullName.isEmpty())
                            ? "Welcome, " + fullName + "!" : "Welcome, User!");

                    if (className != null && !className.isEmpty()) {
                        loadUpcomingClasses(className);
                    } else {
                        Log.w(TAG, "No className found for user");
                    }
                } else {
                    handleUserDataError();
                }
            } else {
                Log.e(TAG, "Error getting user document", task.getException());
                handleUserDataError();
            }
        });
    }

    private void loadUpcomingClasses(String className) {
        LinearLayout upcomingClassContainer = findViewById(R.id.upcomingClassContainer);
        TextView noUpcomingClass = findViewById(R.id.noUpcomingClass);

        upcomingClassContainer.removeAllViews();

        db.collection("classes")
                .whereEqualTo("className", className)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        noUpcomingClass.setVisibility(View.GONE);

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            View classCard = getLayoutInflater().inflate(R.layout.item_up_class, null);

                            TextView classDate = classCard.findViewById(R.id.classDate);
                            TextView classNameText = classCard.findViewById(R.id.className);
                            TextView classTime = classCard.findViewById(R.id.classTime);

                            classDate.setText("Date: " + doc.getString("date"));
                            classNameText.setText(doc.getString("className"));
                            classTime.setText("Time: " + doc.getString("time"));

                            upcomingClassContainer.addView(classCard);
                        }
                    } else {
                        noUpcomingClass.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch upcoming classes", e);
                    Toast.makeText(this, "Failed to load upcoming classes", Toast.LENGTH_SHORT).show();
                    noUpcomingClass.setVisibility(View.VISIBLE);
                });
    }

    private void handleUserDataError() {
        welcomeMessage.setText("Welcome, User!");
        Toast.makeText(this, "Unable to load user data", Toast.LENGTH_SHORT).show();
    }

    private void redirectToLogin() {
        welcomeMessage.setText("Welcome, Please log in!");
        Toast.makeText(this, "Please log in to continue", Toast.LENGTH_LONG).show();
        // Optional: Redirect to login activity
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            redirectToLogin();
        }
    }

    // User model (optional but kept for reference)
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

        // Getters and setters...
    }
}
