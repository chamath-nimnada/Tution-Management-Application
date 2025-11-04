package com.example.teacherapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teacherapp.R;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText emailET, passwordET;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailET = findViewById(R.id.emailLogin);
        passwordET = findViewById(R.id.passwordLogin);

        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.loginBtn).setOnClickListener(v -> loginUser());

        findViewById(R.id.goToRegister).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        //code for loginBtn to navigate to teacher dashboard
        Button loginBtn = findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(v -> {
            String email = emailET.getText().toString().trim();
            String password = passwordET.getText().toString().trim();

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(LoginActivity.this, TeacherDashboardActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        //code for the register button to open Register Activity
        TextView goToRegister = findViewById(R.id.goToRegister);
        goToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

    }
    private void loginUser() {
        String email = emailET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, TeacherDashboardActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}