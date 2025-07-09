package com.example.tutionmanagementapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Add this import for QR code generation
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

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
    private FirebaseStorage storage;

    /* ---------- Form values ---------- */
    String txtFullName, txtEmail, txtBirthday, txtPhone, txtParentPhone, txtAddress,
            txtGrade, txtClass, txtPassword, txtConfirmPassword;


    /* ---------- Helpers ---------- */
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    /* Drop‑down data */
    private final HashMap<String, String[]> classMap = new HashMap<String, String[]>() {{
        put("OL", new String[]{"Mathematics", "Science", "English", "History", "ICT", "Commerce"});
        put("AL", new String[]{"Mathematics", "Science", "Technology", "Commerce"});
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

        //Go back to main page
        ImageView backButton = findViewById(R.id.goBack);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
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
        storage = FirebaseStorage.getInstance();

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

                    // Generate QR code with student information
                    generateAndUploadQRCode(uid);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Register.this,
                            "Auth error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    restoreButton();
                });
    }

    /* ---------------------------------- */
    /*         QR Code Generation         */
    /* ---------------------------------- */
    private void generateAndUploadQRCode(String uid) {
        try {
            // Create QR code content with student information
            String qrContent = "STUDENT_ID:" + uid +
                    "\nNAME:" + txtFullName +
                    "\nEMAIL:" + txtEmail +
                    "\nPHONE:" + txtPhone;

            // Generate QR code bitmap
            Bitmap qrBitmap = generateQRCode(qrContent);

            if (qrBitmap != null) {
                // Convert bitmap to byte array
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] qrData = baos.toByteArray();

                // Upload QR code to Firebase Storage
                StorageReference qrRef = storage.getReference()
                        .child("qr_codes")
                        .child(uid + ".png");

                UploadTask uploadTask = qrRef.putBytes(qrData);
                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    // Get download URL
                    qrRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String qrDownloadUrl = uri.toString();
                        // Save student data with QR code URL
                        saveStudentDataWithQR(uid, qrDownloadUrl, qrContent);
                    }).addOnFailureListener(e -> {
                        Toast.makeText(Register.this,
                                "Failed to get QR code URL: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        restoreButton();
                    });
                }).addOnFailureListener(e -> {
                    Toast.makeText(Register.this,
                            "Failed to upload QR code: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    restoreButton();
                });
            } else {
                Toast.makeText(Register.this,
                        "Failed to generate QR code", Toast.LENGTH_SHORT).show();
                restoreButton();
            }
        } catch (Exception e) {
            Toast.makeText(Register.this,
                    "QR generation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            restoreButton();
        }
    }

    private Bitmap generateQRCode(String content) {
        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveStudentDataWithQR(String uid, String qrImageUrl, String qrContent) {
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

        // Add QR code related fields
        student.put("qrImageUrl",    qrImageUrl);
        student.put("qrContent",     qrContent);
        student.put("qrGeneratedAt", System.currentTimeMillis());

        db.collection("students")
                .document(uid)
                .set(student)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(Register.this,
                            "Registered successfully with QR code!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Register.this, Login.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Register.this,
                            "Firestore error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        // First dropdown options
        String[] grades = {"OL", "AL"};
        ArrayAdapter<String> gradeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, grades);
        gradeCombo.setAdapter(gradeAdapter);
        gradeCombo.setThreshold(1000); // Won't show dropdown on typing, only on click

        // Show dropdown when clicked
        gradeCombo.setOnClickListener(v -> gradeCombo.showDropDown());

        gradeCombo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedGrade = (String) parent.getItemAtPosition(position);
                loadClassSpinner(selectedGrade);
            }
        });
    }

    private void loadClassSpinner(String grade) {
        String[] subjects = classMap.getOrDefault(grade, new String[]{});
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, subjects);
        classCombo.setAdapter(classAdapter);
        classCombo.setEnabled(true);
        classCombo.setText(""); // Clear previous selection
        classCombo.setThreshold(1000); // Won't show dropdown on typing, only on click

        // Show dropdown when clicked
        classCombo.setOnClickListener(v -> classCombo.showDropDown());

        // Handle second dropdown selection if needed
        classCombo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedSubject = (String) parent.getItemAtPosition(position);
                // Handle subject selection here
            }
        });
    }
}