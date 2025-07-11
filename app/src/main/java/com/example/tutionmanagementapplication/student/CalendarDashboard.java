package com.example.tutionmanagementapplication.student;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tutionmanagementapplication.R;

public class CalendarDashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calendar_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setContentView(R.layout.activity_calendar_dashboard);

        ///
        ///
        /// Navigations
        LinearLayout course = findViewById(R.id.navCourses);
        course.setOnClickListener(view -> {
            Intent intent = new Intent(this, CourseDashboard.class);
            startActivity(intent);
        });


        // Navigations
        LinearLayout dashboard = findViewById(R.id.navDashboard);
        dashboard.setOnClickListener(view -> {
            Intent intent = new Intent(this, StudentDashboard.class);
            startActivity(intent);
        });

        LinearLayout profile = findViewById(R.id.navProfile);
        profile.setOnClickListener(view -> {
            Intent intent = new Intent(this, ProfileDashboard.class);
            startActivity(intent);
        });

        LinearLayout caurses = findViewById(R.id.navCourses);
        caurses.setOnClickListener(view -> {
            Intent intent = new Intent(this, CourseDashboard.class);
            startActivity(intent);
        });

        //Calendar View
        CalendarView calendarView = findViewById(R.id.calendarView);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Toast.makeText(this, "Selected: " + dayOfMonth + "/" + (month + 1) + "/" + year, Toast.LENGTH_SHORT).show();
        });

    }
}