package com.example.teacherapp.Activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teacherapp.Adapters.AssignedClassAdapter;
import com.example.teacherapp.Models.AssignedClass;
import com.example.teacherapp.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ViewAssignedClassesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<AssignedClass> classList;
    private AssignedClassAdapter adapter;
    private FirebaseFirestore db;
    String teacherId = getIntent().getStringExtra("teacherId");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_assigned_classes);

        recyclerView = findViewById(R.id.recyclerViewClasses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        classList = new ArrayList<>();
        adapter = new AssignedClassAdapter(classList, this);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadClasses();
    }

    private void loadClasses() {
        db.collection("classes")
                .whereEqualTo("teacherId", teacherId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    classList.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        AssignedClass ac = doc.toObject(AssignedClass.class);
                        ac = new AssignedClass(
                                doc.getId(),
                                doc.getString("teacherId"),
                                doc.getString("teacherName"),
                                doc.getString("className"),
                                doc.getString("subject"),
                                doc.getString("date"),
                                doc.getString("time"),
                                doc.getString("hall"),
                                doc.getLong("studentCount"),
                                doc.getString("status")
                        );
                        classList.add(ac);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
