package com.example.tutionmanagementapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tutionmanagementapplication.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    /* ---------- UI ---------- */
    TextView txtLogin;
    EditText edtFullName, edtEmail, edtBirthday, edtPhone, edtParentPhone, edtAddress,
            edtPassword, edtConfirmPassword;
    AutoCompleteTextView gradeCombo, classCombo;
    ProgressBar progressBar;
    Button btnRegister;

    /* ---------- Firebase ---------- */
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    /* ---------- Form values ---------- */
    String txtFullName, txtEmail, txtBirthday, txtPhone, txtParentPhone, txtAddress,
            txtGrade, txtClass, txtPassword, txtConfirmPassword;

    /* ---------- Helpers ---------- */
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    /* Drop‑down data */
    private final HashMap<String, String[]> classMap = new HashMap<String, String[]>() {{
        put("Grade 6",  new String[]{"Mathematics","Science","English"});
        put("Grade 7",  new String[]{"Mathematics","Science","English","History"});
        put("Grade 8",  new String[]{"Mathematics","Science","English","ICT"});
        put("Grade 9",  new String[]{"Mathematics","Science","English","Commerce"});
        put("O/L 2026", new String[]{"Maths (OL)","Science (OL)","English (OL)","ICT (OL)"});
        put("A/L Arts", new String[]{"Economics","Political Science","ICT"});
        put("A/L Science", new String[]{"Physics","Chemistry","Biology","Combined Maths"});
    }};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /* ---------- bind views ---------- */
        txtLogin        = findViewById(R.id.alreadyHaveAccount);
        edtFullName     = findViewById(R.id.fullName);
        edtEmail        = findViewById(R.id.userMail);
        edtBirthday     = findViewById(R.id.userBirthday);
        edtPhone        = findViewById(R.id.userPhone);
        edtParentPhone  = findViewById(R.id.perentPhone);
        edtAddress      = findViewById(R.id.userAddress);
        edtPassword     = findViewById(R.id.userPassword);
        edtConfirmPassword = findViewById(R.id.userPassword);   // If you have a separate confirm field, change the ID
        gradeCombo      = findViewById(R.id.gradeCombo);
        classCombo      = findViewById(R.id.classCombo);
        progressBar     = findViewById(R.id.registrationProgressBar); // add in XML
        btnRegister     = findViewById(R.id.registerBtn);

        /* ---------- Firebase ---------- */
        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        /* ---------- drop‑downs ---------- */
        initGradeSpinner();

        /* ---------- listeners ---------- */
        txtLogin.setOnClickListener(view -> {
            startActivity(new Intent(Register.this, Login.class));
            finish();
        });

        btnRegister.setOnClickListener(view -> {
            /* grab & trim */
            txtFullName        = edtFullName.getText().toString().trim();
            txtEmail           = edtEmail.getText().toString().trim();
            txtBirthday        = edtBirthday.getText().toString().trim();
            txtPhone           = edtPhone.getText().toString().trim();
            txtParentPhone     = edtParentPhone.getText().toString().trim();
            txtAddress         = edtAddress.getText().toString().trim();
            txtGrade           = gradeCombo.getText().toString().trim();
            txtClass           = classCombo.getText().toString().trim();
            txtPassword        = edtPassword.getText().toString().trim();
            txtConfirmPassword = edtConfirmPassword.getText().toString().trim();

            /* nested validation (same style as your sample) */
            if (!TextUtils.isEmpty(txtFullName)) {
                if (!TextUtils.isEmpty(txtEmail)) {
                    if (txtEmail.matches(emailPattern) && Patterns.EMAIL_ADDRESS.matcher(txtEmail).matches()) {
                        if (!TextUtils.isEmpty(txtPhone) && txtPhone.length() == 10) {
                            if (!TextUtils.isEmpty(txtPassword)) {
                                if (!TextUtils.isEmpty(txtConfirmPassword)) {
                                    if (txtPassword.equals(txtConfirmPassword)) {
                                        /* passed validation -> sign up */
                                        signUpUser();
                                    } else {
                                        edtConfirmPassword.setError("Password and Confirm Password must match");
                                    }
                                } else { edtConfirmPassword.setError("Confirm Password can't be empty"); }
                            } else { edtPassword.setError("Password can't be empty"); }
                        } else { edtPhone.setError("Enter a valid 10‑digit phone"); }
                    } else { edtEmail.setError("Enter a valid email"); }
                } else { edtEmail.setError("Email can't be empty"); }
            } else { edtFullName.setError("Full Name can't be empty"); }
        });
    }

    /* ---------------------------------- */
    /*            Firebase sign‑up        */
    /* ---------------------------------- */
    private void signUpUser() {
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setVisibility(View.INVISIBLE);

        mAuth.createUserWithEmailAndPassword(txtEmail, txtPassword)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();

                    Map<String, Object> student = new HashMap<>();
                    student.put("uid", uid);
                    student.put("fullName",      txtFullName);
                    student.put("email",         txtEmail);
                    student.put("birthday",      txtBirthday);
                    student.put("phone",         txtPhone);
                    student.put("parentPhone",   txtParentPhone);
                    student.put("address",       txtAddress);
                    student.put("grade",         txtGrade);
                    student.put("class",         txtClass);
                    student.put("registeredAt",  System.currentTimeMillis());
                    student.put("position",      "student");
                    student.put("status",        "approved");

                    db.collection("students")
                            .document(uid)          // doc id = uid
                            .set(student)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(Register.this,
                                        "Registered successfully!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(Register.this, Login.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(Register.this,
                                        "Firestore error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                restoreButton();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Register.this,
                            "Auth error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    restoreButton();
                });
    }

    private void restoreButton() {
        progressBar.setVisibility(View.INVISIBLE);
        btnRegister.setVisibility(View.VISIBLE);
    }

    /* ---------------------------------- */
    /*       grade/subject drop‑downs     */
    /* ---------------------------------- */
    private void initGradeSpinner() {
        String[] grades = classMap.keySet().toArray(new String[0]);
        ArrayAdapter<String> gradeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, grades);
        gradeCombo.setAdapter(gradeAdapter);

        gradeCombo.setOnItemClickListener((parent, view, position, id) -> {
            String selectedGrade = (String) parent.getItemAtPosition(position);
            loadClassSpinner(selectedGrade);
        });
    }

    private void loadClassSpinner(String grade) {
        String[] subjects = classMap.getOrDefault(grade, new String[]{});
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, subjects);
        classCombo.setAdapter(classAdapter);
        classCombo.setEnabled(true);
        classCombo.setText("");  // clear previous selection
    }
}