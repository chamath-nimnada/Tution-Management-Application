package com.example.teacherapp.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.teacherapp.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MarkAttendanceActivity extends AppCompatActivity {

    private final int QR_REQUEST_CODE = 101;
    private FirebaseFirestore db;
    private String teacherId = "T001"; // Replace with actual later

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_attendance);

        // Firebase instance
        db = FirebaseFirestore.getInstance();

        // Request Camera permission
        checkCameraPermission();

        // Scan button logic
        Button scanBtn = findViewById(R.id.scanBtn);
        scanBtn.setOnClickListener(v -> startQRScanner());
    }


    private void startQRScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Scan Student QR Code");
        integrator.setOrientationLocked(true);
        integrator.setCaptureActivity(CaptureActivity.class);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                markAttendance(result.getContents());
            } else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }


    private void markAttendance(String studentId) {
        String className = "Grade 10 - Science"; // 🔁 Replace with actual selected class
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String docId = teacherId + "_" + className + "_" + date + "_" + studentId;

        DocumentReference docRef = db.collection("attendance").document(docId);

        docRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Toast.makeText(this, "Attendance already marked for this student.", Toast.LENGTH_SHORT).show();
            } else {
                Map<String, Object> attendance = new HashMap<>();
                attendance.put("studentId", studentId);
                attendance.put("teacherId", teacherId);
                attendance.put("className", className);
                attendance.put("date", date);
                attendance.put("timestamp", FieldValue.serverTimestamp());

                docRef.set(attendance)
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(this, "Attendance marked!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Error marking attendance", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    100); // 100 = request code
        }
    }


}

