package com.example.teacherapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.teacherapp.Adapters.NoteAdapter;
import com.example.teacherapp.Models.Note;
import com.example.teacherapp.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class ViewNotesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<Note> notesList;
    private NoteAdapter adapter;


    private FirebaseFirestore db;
    private final String currentTeacherId = "T001"; // Replace with dynamic ID later

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_note);

        recyclerView = findViewById(R.id.notesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        notesList = new ArrayList<>();
        adapter = new NoteAdapter(notesList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadNotes();

        ImageButton backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> finish());

        Button uploadNoteBtn = findViewById(R.id.uploadNoteBtn);
        uploadNoteBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ViewNotesActivity.this, UploadNotesActivity.class);
            startActivity(intent);
        });
    }

    private void loadNotes() {
        db.collection("Notes")
                .whereEqualTo("teacherId", "T001") // replace with dynamic ID later
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    notesList.clear();
                    Log.d("FIREBASE_DEBUG", "Total notes: " + query.size());


                    for (DocumentSnapshot doc : query) {
                        String fileName = doc.getString("fileName");
                        String url = doc.getString("downloadUrl");
                        Timestamp ts = doc.getTimestamp("timestamp");
                        Log.d("NOTE_DEBUG", "Doc ID: " + doc.getId());
                        Log.d("NOTE_DEBUG", "fileName: " + fileName);
                        Log.d("NOTE_DEBUG", "URL: " + url);
                        Log.d("NOTE_DEBUG", "Timestamp: " + ts);

                        if (fileName != null && url != null && ts != null) {
                            notesList.add(new Note(fileName, url, ts));
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load notes", Toast.LENGTH_SHORT).show();
                    Log.e("FIREBASE_DEBUG", "Error loading notes: " + e.getMessage());
                });

    }

}

