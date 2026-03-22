package com.example.myapplication;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Intent;
import android.net.Uri;
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

        // open file when item is clicked
        holder.itemView.setOnClickListener(v -> {

            if (material.attachmentUri != null && !material.attachmentUri.isEmpty()) {

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(material.attachmentUri));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

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