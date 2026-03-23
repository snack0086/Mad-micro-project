package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;

public class LearningMaterialAdapter
        extends RecyclerView.Adapter<LearningMaterialAdapter.MyViewHolder> {

    Context context;
    ArrayList<LearningMaterial> list;

    public LearningMaterialAdapter(Context context,
                                   ArrayList<LearningMaterial> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                           int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_material, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder,
                                 int position) {

        LearningMaterial material = list.get(position);

        holder.tvTitle.setText(material.title);
        holder.tvTeacher.setText("Uploaded by Teacher");

        String date = DateFormat.format("dd/MM/yyyy hh:mm a",
                new Date(material.timestamp)).toString();

        holder.tvTime.setText(date);

        // 🔥 FIXED CLICK HANDLING
        holder.itemView.setOnClickListener(v -> {

            if (material.attachmentUri != null && !material.attachmentUri.isEmpty()) {

                String url = material.attachmentUri;

                // Debug log (for testing)
                Log.d("FILE_URL", url);

                // Show small feedback (prevents "lag feeling")
                Toast.makeText(context, "Opening file...", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(Intent.ACTION_VIEW);

                // 📄 Handle PDF specifically
                if (url.endsWith(".pdf")) {
                    intent.setDataAndType(Uri.parse(url), "application/pdf");
                } else {
                    intent.setDataAndType(Uri.parse(url), "*/*");
                }

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                try {
                    context.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(context,
                            "No app found to open this file",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context,
                        "File not available",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvTeacher, tvTime;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTeacher = itemView.findViewById(R.id.tvTeacher);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}