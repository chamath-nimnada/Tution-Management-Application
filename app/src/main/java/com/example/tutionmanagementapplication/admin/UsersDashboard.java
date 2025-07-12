package com.example.tutionmanagementapplication.admin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tutionmanagementapplication.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class UsersDashboard extends AppCompatActivity {

    private FirebaseFirestore db;
    private EditText userSearchBar;
    private LinearLayout searchUserContainer;
    private LinearLayout allUsersContainer;
    private Spinner collectionSpinner;
    private FloatingActionButton userAddTask;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_users_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Navigations
        LinearLayout dashboard = findViewById(R.id.navDashboard);
        dashboard.setOnClickListener(view -> {
            Intent intent = new Intent(this, AdminDashboard.class);
            startActivity(intent);
        });

        LinearLayout classSub = findViewById(R.id.navCourses);
        classSub.setOnClickListener(view -> {
            Intent intent = new Intent(this, UsersDashboard.class);
            startActivity(intent);
        });

        LinearLayout announcements = findViewById(R.id.navProfile);
        announcements.setOnClickListener(view -> {
            Intent intent = new Intent(this, ProfileDashboard.class);
            startActivity(intent);
        });

        userAddTask = findViewById(R.id.userAddTask);
        userAddTask.setOnClickListener(view -> {
            Intent intent = new Intent(this, AddUsers.class);
            startActivity(intent);
        });

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize search user components
        userSearchBar = findViewById(R.id.userSearchBar);
        ImageView userSearchIcon = findViewById(R.id.userSearchIcon);
        searchUserContainer = findViewById(R.id.userContainer);

        userSearchIcon.setOnClickListener(v -> searchUsersByEmail(userSearchBar.getText().toString()));

        // Initialize all users components
        allUsersContainer = findViewById(R.id.allUsersContainer);
        collectionSpinner = findViewById(R.id.collectionSpinner);

        // Array of collection names
        String[] collections = {"students", "teachers", "admin"};

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, collections);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        collectionSpinner.setAdapter(adapter);

        // Set a listener for the spinner
        collectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedCollection = collections[position];
                Toast.makeText(UsersDashboard.this, "Selected: " + selectedCollection, Toast.LENGTH_SHORT).show();
                // Load data from the selected collection
                loadCollectionData(selectedCollection);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });
    }

    private void searchUsersByEmail(String email) {
        db.collection("students")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            displaySearchResults(querySnapshot);
                        } else {
                            Toast.makeText(this, "No registered accounts", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error fetching data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displaySearchResults(QuerySnapshot querySnapshot) {
        searchUserContainer.removeAllViews(); // Clear previous results
        for (QueryDocumentSnapshot document : querySnapshot) {
            View userView = getLayoutInflater().inflate(R.layout.searched_user, searchUserContainer, false);

            TextView fullName = userView.findViewById(R.id.pendingUser);
            TextView userPhone = userView.findViewById(R.id.searchUserPhone);

            fullName.setText(document.getString("fullName"));
            userPhone.setText(document.getString("phone"));

            searchUserContainer.addView(userView);
        }
    }

    private void loadCollectionData(String collectionName) {
        db.collection(collectionName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            displayAllUsers(querySnapshot);
                        } else {
                            Toast.makeText(this, "No data found in " + collectionName, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error fetching data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayAllUsers(QuerySnapshot querySnapshot) {
        allUsersContainer.removeAllViews(); // Clear previous results
        for (QueryDocumentSnapshot document : querySnapshot) {
            View userView = getLayoutInflater().inflate(R.layout.item_user, allUsersContainer, false);

            TextView userName = userView.findViewById(R.id.userName);
            TextView userEmail = userView.findViewById(R.id.userEmail);
            TextView userPhone = userView.findViewById(R.id.userPhone);

            userName.setText(document.getString("name"));
            userEmail.setText(document.getString("email"));
            userPhone.setText(document.getString("phone"));

            allUsersContainer.addView(userView);
        }
    }
}