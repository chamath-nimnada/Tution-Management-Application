package com.example.teacherapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.teacherapp.Models.Teacher;
import com.example.teacherapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private EditText teacherIdET, nameET, subjectET, emailET, passwordET;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        teacherIdET = findViewById(R.id.teacherId);
        nameET = findViewById(R.id.name);
        subjectET = findViewById(R.id.subject);
        emailET = findViewById(R.id.emailRegister);
        passwordET = findViewById(R.id.passwordRegister);

        findViewById(R.id.registerBtn).setOnClickListener(v -> registerTeacher());

        findViewById(R.id.goToLogin).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        //To load the Login activity when Clicked
        TextView goToLogin = findViewById(R.id.goToLogin);

        goToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });


    }

    private void registerTeacher() {
        String teacherId = teacherIdET.getText().toString().trim();
        String name = nameET.getText().toString().trim();
        String subject = subjectET.getText().toString().trim();
        String email = emailET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email & password required", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    Teacher teacher = new Teacher(teacherId, name, email, subject);

                    db.collection("teachers").document(uid)
                            .set(teacher)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, TeacherDashboardActivity.class));
                                finish();
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
