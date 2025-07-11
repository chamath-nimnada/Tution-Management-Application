package com.example.tutionmanagementapplication.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tutionmanagementapplication.MainActivity;
import com.example.tutionmanagementapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import com.example.tutionmanagementapplication.plainclasses.Student;

public class AdminDashboard extends AppCompatActivity {

    // Your existing variables
    TextView totalStudentTextView;
    TextView totalTeachers;
    private FirebaseFirestore db;

    // New variables for pending students functionality
    private static final String TAG = "AdminDashboard";
    private LinearLayout pendingStudentContainer;
    private TextView noPendingAcc;
    private List<Student> pendingStudents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Your existing navigations
        LinearLayout course = findViewById(R.id.navUsers);
        course.setOnClickListener(view -> {
            Intent intent = new Intent(this, UsersDashboard.class);
            startActivity(intent);
        });

        LinearLayout dashboard = findViewById(R.id.navClass);
        dashboard.setOnClickListener(view -> {
            Intent intent = new Intent(this, ClassSubjectDashboad.class);
            startActivity(intent);
        });

        LinearLayout announcements = findViewById(R.id.navProfile);
        announcements.setOnClickListener(view -> {
            Intent intent = new Intent(this, ProfileDashboard.class);
            startActivity(intent);
        });

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Bind the TextView
        totalStudentTextView = findViewById(R.id.totalStudent);

        // Your existing method calls
        countApprovedStudents();

        // New initialization for pending students
        initializePendingStudentsViews();
        loadPendingStudents();
    }

    // Your existing method - unchanged
    private void countApprovedStudents() {
        db.collection("students")
                .whereEqualTo("status", "approved")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int count = task.getResult().size();
                            totalStudentTextView.setText(String.valueOf(count));
                        } else {
                            Toast.makeText(AdminDashboard.this, "Error getting documents: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Your existing method - unchanged
    private void countAllStudents() {
        db.collection("students")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int count = task.getResult().size();
                            totalTeachers.setText(String.valueOf(count));
                        } else {
                            Toast.makeText(AdminDashboard.this, "Error getting documents: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // View pending student accounts
    private void initializePendingStudentsViews() {
        pendingStudentContainer = findViewById(R.id.pendingStudentContainer);
        noPendingAcc = findViewById(R.id.noPendingAcc);
        pendingStudents = new ArrayList<>();
    }

    private void loadPendingStudents() {
        db.collection("students")
                .whereEqualTo("status", "pending")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            pendingStudents.clear();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    Student student = document.toObject(Student.class);
                                    pendingStudents.add(student);

                                    Log.d(TAG, "Loaded student: " + student.getFullName());
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing student data", e);
                                }
                            }

                            // Update UI on main thread
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    displayPendingStudents();
                                }
                            });

                        } else {
                            Log.e(TAG, "Error getting documents: ", task.getException());
                            Toast.makeText(AdminDashboard.this, "Error loading pending students", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void displayPendingStudents() {
        // Clear existing views (except the "no pending" message)
        pendingStudentContainer.removeAllViews();

        if (pendingStudents.isEmpty()) {
            // Show "no pending accounts" message
            noPendingAcc.setVisibility(View.VISIBLE);
            pendingStudentContainer.addView(noPendingAcc);
        } else {
            // Hide "no pending accounts" message
            noPendingAcc.setVisibility(View.GONE);

            // Add each pending student
            for (Student student : pendingStudents) {
                addPendingStudentView(student);
            }
        }
    }

    private void addPendingStudentView(Student student) {
        // Inflate the item layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View itemView = inflater.inflate(R.layout.item_pending_user, null);

        // Find views in the inflated layout
        TextView pendingUserName = itemView.findViewById(R.id.pendingUser);
        TextView pendingUserEmail = itemView.findViewById(R.id.pendingUserEmail);

        // Set data to views
        pendingUserName.setText(student.getFullName());
        pendingUserEmail.setText(student.getEmail());

        // Add click listener for the item (optional - for handling clicks)
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePendingStudentClick(student);
            }
        });

        // Add the view to container
        pendingStudentContainer.addView(itemView);
    }

    private void handlePendingStudentClick(Student student) {
        // Handle click on pending student item
        // You can show a dialog, navigate to details, etc.
        Toast.makeText(this, "Clicked on: " + student.getFullName(), Toast.LENGTH_SHORT).show();

        // Example: Show student details
        showStudentDetails(student);
    }

    private void showStudentDetails(Student student) {
        // You can implement this to show more details or actions
        String details = "Name: " + student.getFullName() + "\n" +
                "Email: " + student.getEmail() + "\n" +
                "Phone: " + student.getPhone() + "\n" +
                "Parent Phone: " + student.getParentPhone() + "\n" +
                "Grade: " + student.getGrade() + "\n" +
                "Class: " + student.getClazz();

        Toast.makeText(this, details, Toast.LENGTH_LONG).show();
    }

    // Method to refresh the pending students list
    public void refreshPendingStudents() {
        loadPendingStudents();
    }
}