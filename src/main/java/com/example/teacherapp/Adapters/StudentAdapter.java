package com.example.teacherapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teacherapp.Models.Student;
import com.example.teacherapp.R;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {

    private List<Student> studentList;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, idText;

        public ViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.nameText);
            idText = view.findViewById(R.id.idText);
        }
    }

    public StudentAdapter(List<Student> studentList) {
        this.studentList = studentList;
    }

    @NonNull
    @Override
    public StudentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.student_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StudentAdapter.ViewHolder holder, int position) {
        Student s = studentList.get(position);
        holder.nameText.setText("Name: " + s.getName());
        holder.idText.setText("ID: " + s.getId());
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }
}
