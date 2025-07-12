package com.example.tutionmanagementapplication.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tutionmanagementapplication.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ClassSubjectDashboad extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_class_subject_dashboad);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Navigations
        LinearLayout course = findViewById(R.id.navDashboard);
        course.setOnClickListener(view -> {
            Intent intent = new Intent(this, AdminDashboard.class);
            startActivity(intent);
        });

        LinearLayout dashboard = findViewById(R.id.navClassSub);
        dashboard.setOnClickListener(view -> {
            Intent intent = new Intent(this, ClassSubjectDashboad.class);
            startActivity(intent);
        });

        LinearLayout announcements = findViewById(R.id.navProfile);
        announcements.setOnClickListener(view -> {
            Intent intent = new Intent(this, ProfileDashboard.class);
            startActivity(intent);
        });

        // Floating action button to AddClasses page
        FloatingActionButton fabAddClass = findViewById(R.id.userAddTask);
        fabAddClass.setOnClickListener(view -> {
            Intent intent = new Intent(this, AddClasses.class);
            startActivity(intent);
        });
    }
}