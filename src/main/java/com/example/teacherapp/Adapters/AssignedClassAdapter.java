package com.example.teacherapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teacherapp.Activities.ViewEnrolledStudentsActivity;
import com.example.teacherapp.Models.AssignedClass;
import com.example.teacherapp.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AssignedClassAdapter extends RecyclerView.Adapter<AssignedClassAdapter.ViewHolder> {

    private List<AssignedClass> classList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Context context;

    public AssignedClassAdapter(List<AssignedClass> classList, Context context) {
        this.classList = classList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_assigned_class, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AssignedClass c = classList.get(position);

        holder.className.setText(c.getClassName());
        holder.subject.setText("Subject: " + c.getSubject());
        holder.date.setText("Date: " + c.getDate());
        holder.time.setText("Time: " + c.getTime());
        holder.hall.setText("Hall: " + c.getHall());
        holder.studentCount.setText("Students: " + c.getStudentCount());
        holder.status.setText("Status: " + c.getStatus());

        // Set checkbox state
        holder.completed.setChecked("completed".equalsIgnoreCase(c.getStatus()));
        holder.completed.setEnabled(!"completed".equalsIgnoreCase(c.getStatus())); // disable if already done

        // On check, update Firestore
        holder.completed.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isChecked) {
                db.collection("classes").document(c.getClassId())
                        .update("status", "completed")
                        .addOnSuccessListener(aVoid -> {
                            holder.status.setText("Status: completed");
                            Toast.makeText(context, "Class marked as completed", Toast.LENGTH_SHORT).show();
                            holder.completed.setEnabled(false);
                        });
            }
        });

        // View Enrolled Students
        holder.viewBtn.setOnClickListener(v -> {
            Intent i = new Intent(context, ViewEnrolledStudentsActivity.class);
            i.putExtra("classId", c.getClassId());
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return classList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView className, subject, date, time, hall, studentCount, status;
        CheckBox completed;
        Button viewBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            className = itemView.findViewById(R.id.className);
            subject = itemView.findViewById(R.id.subject);
            date = itemView.findViewById(R.id.date);
            time = itemView.findViewById(R.id.time);
            hall = itemView.findViewById(R.id.hall);
            studentCount = itemView.findViewById(R.id.studentCount);
            status = itemView.findViewById(R.id.status);
            completed = itemView.findViewById(R.id.markCompletedCheckbox);
            viewBtn = itemView.findViewById(R.id.viewEnrolledBtn);
        }
    }
}

