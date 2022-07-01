package com.matrix_maeny.onlinetodolist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

public class WorkAdapter extends RecyclerView.Adapter<WorkAdapter.viewHolder> {

    private final Context context;
    private final ArrayList<WorkModel> list;
    private final String currentUid;
    private final FirebaseDatabase database;
    private final WorkAdapterListener listener;

    public WorkAdapter(Context context, ArrayList<WorkModel> list) {
        this.context = context;
        this.list = list;

        listener = (WorkAdapterListener) context;

        currentUid = FirebaseAuth.getInstance().getUid();
        database = FirebaseDatabase.getInstance();
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.work_model, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {

        WorkModel model = list.get(position);

        holder.workNameTv.setText(model.getWorkName());

        holder.deleteIv.setOnClickListener(v -> {
            // delete it
            deleteTask(model.getWorkName());
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void deleteTask(String workName) {
        database.getReference().child("Tasks").child(currentUid).child(workName).removeValue()
                .addOnCompleteListener(task -> {

                    if(task.isSuccessful()){

                        Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show();
                        listener.refreshTasks();

                    }else Toast.makeText(context, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();

                }).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public interface WorkAdapterListener{
        void refreshTasks();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        TextView workNameTv;
        ImageView deleteIv;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            workNameTv = itemView.findViewById(R.id.workNameTv);
            deleteIv = itemView.findViewById(R.id.deleteIv);
        }
    }
}
