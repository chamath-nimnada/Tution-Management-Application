package com.example.tutionmanagementapplication.student;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.tutionmanagementapplication.MainActivity;
import com.example.tutionmanagementapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class QrView extends AppCompatActivity {

    private static final String TAG = "QrView";

    /* ---------- UI Components ---------- */
    private ImageView qrImageView;
    private TextView userNameText, userEmailText, userPhoneText, userGradeText;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    /* ---------- Firebase ---------- */
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_view);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Check if user is logged in
        if (currentUser == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI components
        initViews();
        setupToolbar();
        loadUserQRCode();

        //Go back to main page
        ImageView backButton = findViewById(R.id.goBack);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, StudentDashboard.class);
            startActivity(intent);
        });
    }

    private void initViews() {
        qrImageView = findViewById(R.id.qrImageView);
        userNameText = findViewById(R.id.userNameText);
        userEmailText = findViewById(R.id.userEmailText);
        userPhoneText = findViewById(R.id.userPhoneText);
        userGradeText = findViewById(R.id.userGradeText);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("My QR Code");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadUserQRCode() {
        showLoading(true);
        String userId = currentUser.getUid();
        Log.d(TAG, "Loading QR code for userId: " + userId);

        firestore.collection("students").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "User data found: " + documentSnapshot.getData());

                        String fullName = documentSnapshot.getString("fullName");
                        String email = documentSnapshot.getString("email");
                        String phone = documentSnapshot.getString("phone");
                        String grade = documentSnapshot.getString("grade");
                        String className = documentSnapshot.getString("class");
                        String qrImageUrl = documentSnapshot.getString("qrImageUrl");

                        updateUserInfo(fullName, email, phone, grade, className);

                        if (qrImageUrl != null && !qrImageUrl.isEmpty()) {
                            loadQRImage(qrImageUrl);
                        } else {
                            showError("QR code not found. Please contact administrator.");
                        }
                    } else {
                        showError("User data not found.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore error: ", e);
                    showError("Failed to load user data: " + e.getMessage());
                });
    }

    private void updateUserInfo(String fullName, String email, String phone, String grade, String className) {
        if (fullName != null) userNameText.setText(fullName);
        if (email != null) userEmailText.setText(email);
        if (phone != null) userPhoneText.setText(phone);
        if (grade != null && className != null) {
            userGradeText.setText(grade + " - " + className);
        }
    }

    private void loadQRImage(String imageUrl) {
        Log.d(TAG, "Loading QR image from URL: " + imageUrl);

        Glide.with(this)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.search_qr)
                .error(R.drawable.error_qr)
                .into(qrImageView);

        showLoading(false);
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            qrImageView.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            qrImageView.setVisibility(View.VISIBLE);
        }
    }

    private void showError(String message) {
        showLoading(false);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error: " + message);
    }


}
