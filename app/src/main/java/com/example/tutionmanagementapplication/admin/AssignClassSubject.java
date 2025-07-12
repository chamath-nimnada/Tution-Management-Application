package com.example.tutionmanagementapplication.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.tutionmanagementapplication.R;
import com.google.firebase.firestore.*;
import java.util.*;

public class AssignClassSubject extends AppCompatActivity {

    private static final String TAG = "AssignClassSubject";
    private String studentId, studentName, studentEmail, studentPhone, studentAddress, studentCourse, studentGrade;
    private TextView tvStudentName, tvStudentEmail, tvStudentPhone, tvStudentAddress, tvStudentCourse, tvStudentGrade;
    private AutoCompleteTextView gradeCombo;
    private Button btnApprove, btnReject;
    private FirebaseFirestore db;
    private List<ClassData> classList;
    private ArrayAdapter<String> adapter;

    // Inner class to hold class data
    private static class ClassData {
        String id;
        String name;

        ClassData(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_assign_class_subject);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.backButton).setOnClickListener(v -> {
            finish();
        });

        db = FirebaseFirestore.getInstance();
        classList = new ArrayList<>();
        initializeViews();
        getStudentDataFromIntent();
        displayStudentData();
        loadClassesToDropdown();
        setupClickListeners();
    }

    private void initializeViews() {
        tvStudentName = findViewById(R.id.studentName);
        tvStudentEmail = findViewById(R.id.studentEmail);
        tvStudentPhone = findViewById(R.id.studentPhone);
        tvStudentAddress = findViewById(R.id.studentAddress);
        tvStudentCourse = findViewById(R.id.studentCourse);
        tvStudentGrade = findViewById(R.id.studentGrade);
        gradeCombo = findViewById(R.id.gradeCombo);
        btnApprove = findViewById(R.id.btnApprove);
        btnReject = findViewById(R.id.btnReject);
    }

    private void getStudentDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            studentId = intent.getStringExtra("student_id");
            studentName = intent.getStringExtra("student_name");
            studentEmail = intent.getStringExtra("student_email");
            studentPhone = intent.getStringExtra("student_phone");
            studentAddress = intent.getStringExtra("student_address");
            studentCourse = intent.getStringExtra("student_course");
            studentGrade = intent.getStringExtra("student_grade");

            Log.d(TAG, "Received student data:");
            Log.d(TAG, "ID: " + studentId);
            Log.d(TAG, "Name: " + studentName);
            Log.d(TAG, "Grade: " + studentGrade);
            Log.d(TAG, "Course: " + studentCourse);
        }
    }

    private void displayStudentData() {
        // Personal Information
        tvStudentName.setText(studentName != null ? studentName : "N/A");
        tvStudentEmail.setText(studentEmail != null ? studentEmail : "N/A");
        tvStudentPhone.setText(studentPhone != null ? studentPhone : "N/A");
        tvStudentAddress.setText(studentAddress != null ? studentAddress : "N/A");

        // Academic Information
        // Display grade in the studentGrade TextView
        if (studentGrade != null && !studentGrade.isEmpty()) {
            tvStudentGrade.setText(studentGrade);
        } else {
            tvStudentGrade.setText("Not specified");
        }

        // Display course/subject in the studentCourse TextView
        if (studentCourse != null && !studentCourse.isEmpty()) {
            tvStudentCourse.setText(studentCourse);
        } else {
            tvStudentCourse.setText("Not specified");
        }

        Log.d(TAG, "Displayed student data - Grade: " + studentGrade + ", Course: " + studentCourse);
    }

    private void loadClassesToDropdown() {
        // Show loading state
        gradeCombo.setHint("Loading classes...");

        db.collection("classes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    classList.clear();
                    List<String> classNames = new ArrayList<>();

                    Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " classes");

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String className = doc.getString("className");
                        String classId = doc.getId();

                        Log.d(TAG, "Class ID: " + classId + ", Name: " + className);

                        if (className != null && !className.trim().isEmpty()) {
                            classList.add(new ClassData(classId, className));
                            classNames.add(className);
                        }
                    }

                    if (classNames.isEmpty()) {
                        Toast.makeText(this, "No classes found", Toast.LENGTH_SHORT).show();
                        gradeCombo.setHint("No classes available");
                    } else {
                        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, classNames);
                        gradeCombo.setAdapter(adapter);
                        gradeCombo.setHint("Select a Class");
                        gradeCombo.setThreshold(1); // Show dropdown after 1 character
                        Log.d(TAG, "Loaded " + classNames.size() + " classes to dropdown");

                        // Force show dropdown if focused
                        if (gradeCombo.hasFocus()) {
                            gradeCombo.showDropDown();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load classes", e);
                    Toast.makeText(this, "Failed to load classes: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    gradeCombo.setHint("Failed to load classes");
                });
    }

    private void setupClickListeners() {
        btnApprove.setOnClickListener(v -> approveStudent());
        btnReject.setOnClickListener(v -> rejectStudent());

        // Add click listener to show dropdown when clicked
        gradeCombo.setOnClickListener(v -> {
            gradeCombo.showDropDown();
        });

        // Add focus listener to show dropdown when focused
        gradeCombo.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                gradeCombo.showDropDown();
            }
        });
    }

    private void approveStudent() {
        String selectedClassName = gradeCombo.getText().toString().trim();
        if (selectedClassName.isEmpty()) {
            Toast.makeText(this, "Please select a class", Toast.LENGTH_SHORT).show();
            return;
        }

        // Find the selected class ID
        String selectedClassId = null;
        for (ClassData classData : classList) {
            if (classData.name.equals(selectedClassName)) {
                selectedClassId = classData.id;
                break;
            }
        }

        if (selectedClassId == null) {
            Toast.makeText(this, "Invalid class selection", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update student status to approved and add enrollment info
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("status", "approved");
        updateData.put("enroll", true);
        updateData.put("classId", selectedClassId);
        updateData.put("className", selectedClassName);

        final String finalClassId = selectedClassId;

        db.collection("students").document(studentId)
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    // Also add to enrolls collection for additional tracking
                    Map<String, Object> enrollData = new HashMap<>();
                    enrollData.put("studentId", studentId);
                    enrollData.put("studentEmail", studentEmail);
                    enrollData.put("studentName", studentName);
                    enrollData.put("classId", finalClassId);
                    enrollData.put("className", selectedClassName);
                    enrollData.put("enrollmentDate", new Date());

                    db.collection("enrolls").add(enrollData)
                            .addOnSuccessListener(documentReference -> {
                                Log.d(TAG, "Student approved and enrolled successfully");
                                Toast.makeText(this, "Student approved and enrolled successfully", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to add enrollment record", e);
                                Toast.makeText(this, "Student approved but failed to create enrollment record", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error approving student", e);
                    Toast.makeText(this, "Error approving student", Toast.LENGTH_SHORT).show();
                });
    }

    private void rejectStudent() {
        db.collection("students").document(studentId)
                .update("status", "rejected")
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Student rejected successfully");
                    Toast.makeText(this, "Student rejected", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error rejecting student", e);
                    Toast.makeText(this, "Error rejecting student", Toast.LENGTH_SHORT).show();
                });
    }
}