package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Teacher_dashboard extends BaseActivity {

    private TextView teacherGreeting;
    private TextView tvTotalAssignments, tvAnnouncementsCount;
    private TextView tvMadCount, tvSftCount, tvEtiCount;
    private LinearLayout layoutAssignmentsList, layoutAnnouncementsList;
    private MaterialButton btnAddAssignment, btnGoToMaterials;
    private ImageView btnLogout;

    private DatabaseReference dbRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        userRole = "teacher";
        setupDrawer(R.id.nav_dashboard);

        teacherGreeting       = findViewById(R.id.teacherGreeting);
        tvTotalAssignments    = findViewById(R.id.tvTotalAssignments);
        tvAnnouncementsCount  = findViewById(R.id.tvAnnouncementsCount);
        tvMadCount            = findViewById(R.id.tvMadCount);
        tvSftCount            = findViewById(R.id.tvSftCount);
        tvEtiCount            = findViewById(R.id.tvEtiCount);
        layoutAssignmentsList   = findViewById(R.id.layoutAssignmentsList);
        layoutAnnouncementsList = findViewById(R.id.layoutAnnouncementsList);
        btnAddAssignment        = findViewById(R.id.btnAddAssignment);
        btnGoToMaterials        = findViewById(R.id.btnGoToMaterials);
        btnLogout               = findViewById(R.id.btnLogout);

        dbRef = FirebaseDatabase.getInstance().getReference("CampusConnect");

        loadTeacherName();
        loadAssignments();
        loadAnnouncements();

        btnAddAssignment.setOnClickListener(v ->
                startActivity(new Intent(this, TeacherAssignmentActivity.class)));

        btnGoToMaterials.setOnClickListener(v ->
                startActivity(new Intent(this, LearningMaterialActivity.class)));

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            getSharedPreferences(Login.PREFS_NAME, MODE_PRIVATE).edit().clear().apply();
            Intent intent = new Intent(this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadTeacherName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            teacherGreeting.setText("Welcome back, " + user.getDisplayName() + "!");
        } else if (user != null && user.getEmail() != null) {
            String name = user.getEmail().split("@")[0];
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            teacherGreeting.setText("Welcome back, " + name + "!");
        } else {
            teacherGreeting.setText("Welcome back, Teacher!");
        }
    }

    private void loadAssignments() {
        dbRef.child("Assignments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                layoutAssignmentsList.removeAllViews();

                int total = 0, mad = 0, sft = 0, eti = 0;

                if (!snapshot.exists()) {
                    addEmptyText(layoutAssignmentsList, "No assignments yet.");
                    tvTotalAssignments.setText("0");
                    tvMadCount.setText("0");
                    tvSftCount.setText("0");
                    tvEtiCount.setText("0");
                    return;
                }

                for (DataSnapshot child : snapshot.getChildren()) {
                    total++;
                    String title = child.child("title").getValue(String.class);
                    String subject = child.child("subject").getValue(String.class);
                    Long timestamp = child.child("timestamp").getValue(Long.class);

                    if (subject == null) subject = "GEN";

                    String subjectUpper = subject.toUpperCase();
                    if (subjectUpper.contains("MAD")) mad++;
                    else if (subjectUpper.contains("SFT")) sft++;
                    else if (subjectUpper.contains("ETI")) eti++;

                    String dateStr = "";
                    if (timestamp != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        dateStr = "Due: " + sdf.format(new Date(timestamp));
                    }

                    addAssignmentItem(title, subjectUpper, dateStr);
                }

                tvTotalAssignments.setText(String.valueOf(total));
                tvMadCount.setText(String.valueOf(mad));
                tvSftCount.setText(String.valueOf(sft));
                tvEtiCount.setText(String.valueOf(eti));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(Teacher_dashboard.this, "Failed to load assignments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAnnouncements() {
        dbRef.child("Announcements").orderByChild("timestamp").limitToLast(5)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        layoutAnnouncementsList.removeAllViews();

                        int count = 0;

                        if (!snapshot.exists()) {
                            addEmptyText(layoutAnnouncementsList, "No announcements yet.");
                            tvAnnouncementsCount.setText("0");
                            return;
                        }

                        for (DataSnapshot child : snapshot.getChildren()) {
                            count++;
                            String title   = child.child("title").getValue(String.class);
                            String message = child.child("message").getValue(String.class);
                            Long timestamp = child.child("timestamp").getValue(Long.class);

                            String meta = "By Admin";
                            if (timestamp != null) {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                meta = "By Admin • " + sdf.format(new Date(timestamp));
                            }

                            addAnnouncementItem(title, message, meta);
                        }

                        tvAnnouncementsCount.setText(String.valueOf(count));
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(Teacher_dashboard.this, "Failed to load announcements", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addAssignmentItem(String title, String subject, String dueDate) {
        View item = LayoutInflater.from(this).inflate(R.layout.item_assignment_dash, layoutAssignmentsList, false);

        TextView tvBadge = item.findViewById(R.id.tvSubjectBadge);
        TextView tvTitle = item.findViewById(R.id.tvAssignmentTitle);
        TextView tvDue   = item.findViewById(R.id.tvDueDate);

        tvBadge.setText(subject.length() > 4 ? subject.substring(0, 4) : subject);
        tvTitle.setText(title != null ? title : "Untitled");
        tvDue.setText(dueDate);

        layoutAssignmentsList.addView(item);
    }

    private void addAnnouncementItem(String title, String message, String meta) {
        View item = LayoutInflater.from(this).inflate(R.layout.item_announcement_dash, layoutAnnouncementsList, false);

        TextView tvTitle   = item.findViewById(R.id.tvAnnouncementTitle);
        TextView tvMessage = item.findViewById(R.id.tvAnnouncementMessage);
        TextView tvMeta    = item.findViewById(R.id.tvAnnouncementMeta);

        tvTitle.setText(title != null ? title : "Untitled");
        tvMessage.setText(message != null ? message : "");
        tvMeta.setText(meta);

        layoutAnnouncementsList.addView(item);
    }

    private void addEmptyText(LinearLayout parent, String text) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(android.view.Gravity.CENTER);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad, pad, pad);

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.drawer4_assignment);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                (int)(40 * getResources().getDisplayMetrics().density),
                (int)(40 * getResources().getDisplayMetrics().density));
        iconParams.gravity = android.view.Gravity.CENTER;
        iconParams.bottomMargin = (int)(8 * getResources().getDisplayMetrics().density);
        icon.setLayoutParams(iconParams);
        icon.setColorFilter(0xFFCBD5E1);
        container.addView(icon);

        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(0xFF94A3B8);
        tv.setTextSize(12f);
        tv.setGravity(android.view.Gravity.CENTER);
        container.addView(tv);

        parent.addView(container);
    }
}
