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

import com.example.tutionmanagementapplication.student.Pending;
import com.example.tutionmanagementapplication.student.StudentDashboard;
import com.google.firebase.auth.FirebaseAuth;
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

                    /* -------- 2️⃣  fetch profile doc -------- */
                    String uid = authResult.getUser().getUid();

                    db.collection("students").document(uid).get()
                            .addOnSuccessListener(snapshot -> {
                                progressBar.setVisibility(View.INVISIBLE);   // done loading

                                if (!snapshot.exists()) {
                                    Toast.makeText(Login.this,
                                            "Profile not found. Contact admin.", Toast.LENGTH_LONG).show();
                                    restoreButton();
                                    return;
                                }

                                String status = snapshot.getString("status");
                                if (status == null) status = "";       // avoid NPE

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
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(Login.this,
                                        "Couldn’t read account status: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                restoreButton();
                            });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Login.this,
                            "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    restoreButton();
                });
    }

    private void restoreButton() {
        progressBar.setVisibility(View.INVISIBLE);
        btnLogin.setVisibility(View.VISIBLE);
    }
}