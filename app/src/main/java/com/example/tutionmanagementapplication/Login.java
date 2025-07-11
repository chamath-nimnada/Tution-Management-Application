package com.example.tutionmanagementapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tutionmanagementapplication.admin.AdminDashboard;
import com.example.tutionmanagementapplication.student.Pending;
import com.example.tutionmanagementapplication.student.StudentDashboard;
import com.example.tutionmanagementapplication.teacher.TeacherDashboard;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    /* ---------- UI ---------- */
    EditText edtEmail, edtPassword;
    Button   btnLogin;
    ProgressBar progressBar;

    /* ---------- Firebase ---------- */
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    /* ---------- Helpers ---------- */
    String txtEmail, txtPassword;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Go back to main page
        ImageView backButton = findViewById(R.id.goBack);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        /* Bind views */
        edtEmail    = findViewById(R.id.username);     // <— your XML ID
        edtPassword = findViewById(R.id.password);
        btnLogin    = findViewById(R.id.loginBtn);
        progressBar = findViewById(R.id.loginProgressBar); // add a ProgressBar in XML (style like registration)

        /* Firebase */
        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance(); // kept in case you need profile reads later

        btnLogin.setOnClickListener(view -> {
            txtEmail    = edtEmail.getText().toString().trim();
            txtPassword = edtPassword.getText().toString().trim();

            /* --------- simple validation --------- */
            if (!TextUtils.isEmpty(txtEmail) && txtEmail.matches(emailPattern)
                    && Patterns.EMAIL_ADDRESS.matcher(txtEmail).matches()) {

                if (!TextUtils.isEmpty(txtPassword)) {
                    loginUser();
                } else {
                    edtPassword.setError("Password can't be empty");
                }

            } else {
                edtEmail.setError("Enter a valid email");
            }
        });
    }

    /* ---------------- LOGIN ---------------- */
    private void loginUser() {
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setVisibility(View.INVISIBLE);

        mAuth.signInWithEmailAndPassword(txtEmail, txtPassword)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();

                    // Check in students collection
                    db.collection("students").document(uid).get()
                            .addOnSuccessListener(snapshot -> {
                                if (snapshot.exists()) {
                                    handleStudentLogin(snapshot);
                                } else {
                                    // Check in teachers collection
                                    db.collection("teachers").document(uid).get()
                                            .addOnSuccessListener(teacherSnapshot -> {
                                                if (teacherSnapshot.exists()) {
                                                    startActivity(new Intent(Login.this, TeacherDashboard.class));
                                                    finish();
                                                } else {
                                                    // Check in admin collection
                                                    db.collection("admin").document(uid).get()
                                                            .addOnSuccessListener(adminSnapshot -> {
                                                                if (adminSnapshot.exists()) {
                                                                    startActivity(new Intent(Login.this, AdminDashboard.class));
                                                                    finish();
                                                                } else {
                                                                    Toast.makeText(Login.this,
                                                                            "Profile not found. Contact admin.", Toast.LENGTH_LONG).show();
                                                                    restoreButton();
                                                                }
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                showError(e.getMessage());
                                                            });
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                showError(e.getMessage());
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                showError(e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    showError(e.getMessage());
                });
    }

    private void handleStudentLogin(DocumentSnapshot snapshot) {
        progressBar.setVisibility(View.INVISIBLE);   // done loading

        String status = snapshot.getString("status");
        if (status == null) status = "";

        switch (status.toLowerCase()) {
            case "approved":
                startActivity(new Intent(Login.this, StudentDashboard.class));
                finish();
                break;

            case "pending":
                startActivity(new Intent(Login.this, Pending.class));
                finish();
                break;

            default:
                Toast.makeText(Login.this,
                        "Account status: " + status, Toast.LENGTH_LONG).show();
                restoreButton();
                break;
        }
    }

    private void showError(String message) {
        Toast.makeText(Login.this,
                "Couldn't read account status: " + message, Toast.LENGTH_LONG).show();
        restoreButton();
    }


    private void restoreButton() {
        progressBar.setVisibility(View.INVISIBLE);
        btnLogin.setVisibility(View.VISIBLE);
    }
}