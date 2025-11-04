package com.example.teacherapp.Adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teacherapp.Models.Note;
import com.example.teacherapp.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private List<Note> noteList;

    public NoteAdapter(List<Note> noteList) {
        this.noteList = noteList;
    }


    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);
        holder.noteFileName.setText(note.getFileName());

        if (note.getTimestamp() != null) {
            String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .format(note.getTimestamp().toDate());
            holder.noteUploadTime.setText("Uploaded on: " + dateStr);
        } else {
            holder.noteUploadTime.setText("No timestamp");
        }

        // Optional: open file on click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(note.getDownloadUrl()));
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView noteFileName, noteUploadTime;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteFileName = itemView.findViewById(R.id.noteFileName);
            noteUploadTime = itemView.findViewById(R.id.noteUploadTime);
        }
    }
}

