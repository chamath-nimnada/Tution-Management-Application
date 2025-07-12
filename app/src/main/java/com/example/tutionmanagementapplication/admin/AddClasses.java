package com.example.tutionmanagementapplication.admin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.tutionmanagementapplication.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddClasses extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String TAG = "AddClasses";

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;

    // UI Elements
    private EditText className, classDate, classTime, hallNo, studentCount, selectedLocationText;
    private Spinner assignTeacherCombo;
    private Button addClassBtn;

    // Location variables
    private LatLng selectedLocation;
    private String selectedAddress;

    // Teachers list
    private List<String> teachersList = new ArrayList<>();
    private List<String> teacherIds = new ArrayList<>();
    private ArrayAdapter<String> teachersAdapter;
    private String selectedTeacherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_classes);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize UI elements
        initializeViews();

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Load teachers
        loadTeachers();

        // Set up button click listener
        addClassBtn.setOnClickListener(v -> addClassToFirestore());

        // Back button
        findViewById(R.id.goBack).setOnClickListener(v -> finish());
    }

    private void initializeViews() {
        className = findViewById(R.id.className);
        classDate = findViewById(R.id.classDate);
        classTime = findViewById(R.id.classTime);
        hallNo = findViewById(R.id.hallNo);
        studentCount = findViewById(R.id.studentCount);
        assignTeacherCombo = findViewById(R.id.assignTeacherCombo);
        addClassBtn = findViewById(R.id.addClassBtn);
        selectedLocationText = findViewById(R.id.selectedLocationText);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Set map click listener
        mMap.setOnMapClickListener(latLng -> {
            selectedLocation = latLng;
            mMap.clear();
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Selected Location"));

            // Get address from coordinates
            getAddressFromLocation(latLng);
        });
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                    } else {
                        // Default to Colombo, Sri Lanka if location not available
                        LatLng colombo = new LatLng(6.9271, 79.8612);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(colombo, 12));
                    }
                });
    }

    private void getAddressFromLocation(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    latLng.latitude, latLng.longitude, 1);
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                selectedAddress = address.getAddressLine(0);
                selectedLocationText.setText(selectedAddress);
            } else {
                selectedAddress = "Lat: " + String.format("%.4f", latLng.latitude) + ", Lng: " + String.format("%.4f", latLng.longitude);
                selectedLocationText.setText(selectedAddress);
            }
        } catch (IOException e) {
            selectedAddress = "Lat: " + String.format("%.4f", latLng.latitude) + ", Lng: " + String.format("%.4f", latLng.longitude);
            selectedLocationText.setText(selectedAddress);
        }
    }

    private void loadTeachers() {
        Log.d(TAG, "Loading teachers from Firestore...");

        db.collection("teachers")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        teachersList.clear();
                        teacherIds.clear();

                        // Add a default option
                        teachersList.add("Select a Teacher");
                        teacherIds.add("");

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String teacherName = document.getString("fullName");
                            String teacherId = document.getId();

                            if (teacherName != null && !teacherName.isEmpty()) {
                                teachersList.add(teacherName);
                                teacherIds.add(teacherId);
                                Log.d(TAG, "Added teacher: " + teacherName + " (ID: " + teacherId + ")");
                            }
                        }

                        // Set up the adapter for Spinner
                        teachersAdapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_item, teachersList);
                        teachersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        assignTeacherCombo.setAdapter(teachersAdapter);

                        // Set up spinner item selection listener
                        assignTeacherCombo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                if (position > 0) { // Skip the default "Select a Teacher" option
                                    selectedTeacherId = teacherIds.get(position);
                                    Log.d(TAG, "Selected teacher: " + teachersList.get(position) + " (ID: " + selectedTeacherId + ")");
                                } else {
                                    selectedTeacherId = null;
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                selectedTeacherId = null;
                            }
                        });

                        Log.d(TAG, "Loaded " + (teachersList.size() - 1) + " teachers successfully");

                    } else {
                        Log.e(TAG, "Error loading teachers", task.getException());
                        Toast.makeText(this, "Failed to load teachers: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load teachers", e);
                    Toast.makeText(this, "Failed to load teachers: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addClassToFirestore() {
        // Validate inputs
        if (className.getText().toString().trim().isEmpty() ||
                classDate.getText().toString().trim().isEmpty() ||
                classTime.getText().toString().trim().isEmpty() ||
                hallNo.getText().toString().trim().isEmpty() ||
                studentCount.getText().toString().trim().isEmpty()) {

            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedTeacherId == null || selectedTeacherId.isEmpty()) {
            Toast.makeText(this, "Please select a teacher", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedLocation == null) {
            Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate student count is a number
        int maxStudents;
        try {
            maxStudents = Integer.parseInt(studentCount.getText().toString().trim());
            if (maxStudents <= 0) {
                Toast.makeText(this, "Student count must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number for student count", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate class ID
        String classId = db.collection("classes").document().getId();

        // Prepare class data
        Map<String, Object> classData = new HashMap<>();
        classData.put("classId", classId);
        classData.put("className", className.getText().toString().trim());
        classData.put("classDate", classDate.getText().toString().trim());
        classData.put("classTime", classTime.getText().toString().trim());
        classData.put("hallNo", hallNo.getText().toString().trim());
        classData.put("maxStudents", maxStudents);
        classData.put("assignedTeacherId", selectedTeacherId);
        classData.put("assignedTeacherName", teachersList.get(assignTeacherCombo.getSelectedItemPosition()));
        classData.put("status", "pending");
        classData.put("createdAt", System.currentTimeMillis());

        // Add location data
        Map<String, Object> location = new HashMap<>();
        location.put("latitude", selectedLocation.latitude);
        location.put("longitude", selectedLocation.longitude);
        location.put("address", selectedAddress);
        classData.put("location", location);

        // Add to Firestore
        db.collection("classes")
                .document(classId)
                .set(classData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Class added successfully with ID: " + classId);
                    Toast.makeText(this, "Class added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding class", e);
                    Toast.makeText(this, "Error adding class: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onMapReady(mMap);
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                // Default to Colombo, Sri Lanka
                LatLng colombo = new LatLng(6.9271, 79.8612);
                if (mMap != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(colombo, 12));
                }
            }
        }
    }
}