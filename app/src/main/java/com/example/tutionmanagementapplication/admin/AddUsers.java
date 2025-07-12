package com.example.tutionmanagementapplication.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tutionmanagementapplication.R;
import com.example.tutionmanagementapplication.admin.UsersDashboard;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.core.view.Change;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class AddUsers extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_users);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase and ExecutorService
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        executorService = Executors.newFixedThreadPool(4);

        // Go back
        ImageView goBack = findViewById(R.id.goBack);
        goBack.setOnClickListener(view -> {
            Intent intent = new Intent(this, UsersDashboard.class);
            startActivity(intent);
        });

        // Register buttons
        Button registerTeacherBtn = findViewById(R.id.registerTeacherBtn);
        Button registerStudentBtn = findViewById(R.id.registerStudentBtn);
        Button registerAdminBtn = findViewById(R.id.registerAdminBtn);

        registerTeacherBtn.setOnClickListener(view -> registerUser("teacher"));
        registerStudentBtn.setOnClickListener(view -> registerUser("student"));
        registerAdminBtn.setOnClickListener(view -> registerUser("admin"));
    }

    private void registerUser(String userType) {
        EditText nameField, emailField, phoneField, additionalField;
        String name, email, phone, additionalInfo = "";

        switch (userType) {
            case "teacher":
                nameField = findViewById(R.id.teacherName);
                emailField = findViewById(R.id.teacherEmail);
                phoneField = findViewById(R.id.teacherPhone);
                additionalField = findViewById(R.id.teachingSubject);
                additionalInfo = additionalField.getText().toString().trim();
                break;
            case "student":
                nameField = findViewById(R.id.studentName);
                emailField = findViewById(R.id.studentEmail);
                phoneField = findViewById(R.id.studentPhone);
                additionalField = findViewById(R.id.studentBD);
                additionalInfo = additionalField.getText().toString().trim();
                break;
            case "admin":
                nameField = findViewById(R.id.adminName);
                emailField = findViewById(R.id.adminEmail);
                phoneField = findViewById(R.id.adminPhone);
                break;
            default:
                return;
        }

        name = nameField.getText().toString().trim();
        email = emailField.getText().toString().trim();
        phone = phoneField.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        String password = generateRandomPassword();
        sendPasswordToEmail(email, password);

        String finalAdditionalInfo = additionalInfo;

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    saveUserData(uid, name, email, phone, finalAdditionalInfo, userType);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private String generateRandomPassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            password.append(characters.charAt(random.nextInt(characters.length())));
        }
        return password.toString();
    }

    private void sendPasswordToEmail(String email, String password) {
        // Run email sending on background thread
        executorService.execute(() -> {
            try {
                // Gmail SMTP configuration
                String host = "smtp.gmail.com";
                String from = "calfa.studio@gmail.com";
                String pass = "znce zols iiqp wfnj"; // Use App Password, not regular password

                Properties props = new Properties();
                props.put("mail.smtp.host", host);
                props.put("mail.smtp.port", "587");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.starttls.required", "true");
                props.put("mail.smtp.ssl.protocols", "TLSv1.2");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.socketFactory.fallback", "false");

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(from, pass);
                    }
                });

                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(from));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
                message.setSubject("Your Account Password - Tuition Management System");

                String emailBody = "Hello,\n\n" +
                        "Your account has been created successfully.\n" +
                        "Your login credentials are:\n" +
                        "Email: " + email + "\n" +
                        "Password: " + password + "\n\n" +
                        "Please change your password after first login.\n\n" +
                        "Best regards,\n" +
                        "Tuition Management System";

                message.setText(emailBody);

                Transport.send(message);

                // Show success message on UI thread
                runOnUiThread(() ->
                        Toast.makeText(this, "Password sent to email: " + email, Toast.LENGTH_SHORT).show()
                );

            } catch (Exception e) {
                // Show error message on UI thread
                runOnUiThread(() ->
                        Toast.makeText(this, "Failed to send email: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
                e.printStackTrace();
            }
        });
    }

    private void saveUserData(String uid, String name, String email, String phone, String additionalInfo, String userType) {
        Map<String, Object> user = new HashMap<>();
        user.put("uid", uid);
        user.put("name", name);
        user.put("email", email);
        user.put("phone", phone);
        user.put("additionalInfo", additionalInfo);
        user.put("userType", userType);
        user.put("registeredAt", System.currentTimeMillis());
        user.put("status", "pending");

        // Change collection name from "admins" to "admin"
        db.collection(userType.equals("admin") ? "admin" : userType + "s")
                .document(uid)
                .set(user)
                .addOnSuccessListener(unused -> Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Firestore error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}