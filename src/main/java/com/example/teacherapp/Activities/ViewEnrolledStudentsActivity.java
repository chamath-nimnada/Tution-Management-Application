package com.example.teacherapp.Activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teacherapp.Adapters.StudentAdapter;
import com.example.teacherapp.Models.Student;
import com.example.teacherapp.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ViewEnrolledStudentsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private StudentAdapter adapter;
    private ArrayList<Student> studentList = new ArrayList<>();
    private TextView classNameText;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_enrolled_students);

        recyclerView = findViewById(R.id.studentsRecyclerView);
        classNameText = findViewById(R.id.classNameText);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new StudentAdapter(studentList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        String classId = getIntent().getStringExtra("classId");
        String className = getIntent().getStringExtra("className");

        if (classId == null || classId.isEmpty()) {
            Toast.makeText(this, "Missing class ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        classNameText.setText("Class: " + className);

        db.collection("students")
                .whereEqualTo("classId", classId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    studentList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String id = doc.getId();
                        String name = doc.getString("name");
                        studentList.add(new Student(id, name));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE", "Error loading students", e);
                    Toast.makeText(this, "Failed to load students", Toast.LENGTH_SHORT).show();
                });
    }
}

