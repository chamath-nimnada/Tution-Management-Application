package com.example.teacherapp.Activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.example.teacherapp.R;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;



public class UploadNotesActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;

    private Button chooseFileBtn, uploadBtn;
    private TextView fileNameText;
    private Uri fileUri;

    private FirebaseStorage storage;
    private StorageReference storageRef;
    private FirebaseFirestore db;

    // Hardcoded teacher ID (change to dynamic later)
    private final String currentTeacherId = "T001";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_notes);

        //to back button
        ImageButton backBtn = findViewById(R.id.backBtn); // This links the button in XML
        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(UploadNotesActivity.this, ViewNotesActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        chooseFileBtn = findViewById(R.id.chooseFileBtn);
        uploadBtn = findViewById(R.id.uploadBtn);
        fileNameText = findViewById(R.id.fileNameText);

        storage = FirebaseStorage.getInstance();
        //storing DB
        storageRef = storage.getReference("Notes");
        db = FirebaseFirestore.getInstance();

        chooseFileBtn.setOnClickListener(v -> openFileChooser());
        uploadBtn.setOnClickListener(v -> {
            if (fileUri != null) {
                uploadFile();
            } else {
                Toast.makeText(this, "Please choose a file", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // accept all types (pdf, docx, etc.)
        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            fileUri = data.getData();
            String fileName = getFileName(fileUri);
            fileNameText.setText(fileName);
        }
    }

    private void uploadFile() {
        String fileName = getFileName(fileUri);
        StorageReference fileRef = storageRef.child(currentTeacherId + "/" + fileName);

        fileRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        saveMetadata(fileName, uri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveMetadata(String fileName, String downloadUrl) {
        Map<String, Object> data = new HashMap<>();
        data.put("teacherId", currentTeacherId);
        data.put("fileName", fileName);
        data.put("downloadUrl", downloadUrl);
        data.put("timestamp", FieldValue.serverTimestamp());

        db.collection("Notes")
                .add(data)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "File uploaded successfully", Toast.LENGTH_SHORT).show();
                    fileNameText.setText("No file selected");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving metadata", Toast.LENGTH_SHORT).show();
                });
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));

                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }
}

