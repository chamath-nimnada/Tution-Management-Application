package com.example.tutionmanagementapplication.student;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tutionmanagementapplication.R;

public class CourseDashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_course_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setContentView(R.layout.activity_course_dashboard);

        ///
        ///
        /// Navigations
        LinearLayout dashboard = findViewById(R.id.navDashboard);
        dashboard.setOnClickListener(view -> {
            Intent intent = new Intent(this, StudentDashboard.class);
            startActivity(intent);
        });

        LinearLayout calendar = findViewById(R.id.navCalendar);
        calendar.setOnClickListener(view -> {
            Intent intent = new Intent(this, CalendarDashboard.class);
            startActivity(intent);
        });

        LinearLayout announcements = findViewById(R.id.navProfile);
        announcements.setOnClickListener(view -> {
            Intent intent = new Intent(this, ProfileDashboard.class);
            startActivity(intent);
        });
    }
}