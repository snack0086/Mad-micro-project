package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * StudentDashboardActivity - Main dashboard screen for students.
 *
 * Extends BaseActivity → reuses the Navigation Drawer already in place.
 * Navigates to EXISTING activities via Intent:
 *   • AnnouncementActivity
 *   • LearningMaterialActivity
 *   • AssignmentListActivity
 *   • ProfileActivity
 */
public class StudentDashboardActivity extends BaseActivity {

    // ── Welcome Section ──────────────────────────────────────────────
    private TextView tvWelcomeName;
    private TextView tvWelcomeSubtitle;

    // ── Quick Stats ───────────────────────────────────────────────────
    private TextView tvAnnouncementCount;
    private TextView tvMaterialCount;
    private TextView tvPendingCount;
    private TextView tvSubmittedCount;

    // ── Recent Announcements (dynamic rows added in code) ─────────────
    private LinearLayout layoutRecentAnnouncements;
    private TextView tvNoAnnouncements;

    // ── Recent Materials (dynamic rows added in code) ─────────────────
    private LinearLayout layoutRecentMaterials;
    private TextView tvNoMaterials;

    // ── Quick Action Cards ────────────────────────────────────────────
    private CardView cardAnnouncements;
    private CardView cardMaterials;
    private CardView cardAssignments;
    private CardView cardProfile;

    // ── Profile Summary Card ──────────────────────────────────────────
    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private TextView tvProfileRole;

    // ── Firebase ──────────────────────────────────────────────────────
    private DatabaseReference dbRef;
    private String currentUserId;

    // ─────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        // Highlight "Dashboard" item in the Navigation Drawer
        setupDrawer(R.id.nav_dashboard);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Firebase root reference
        dbRef = FirebaseDatabase.getInstance().getReference("CampusConnect");

        // Get current logged-in user UID (used for assignment submission check)
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        }

        initViews();
        loadUserInfo();
        loadAnnouncementData();
        loadMaterialData();
        loadAssignmentData();
        setupClickListeners();
    }

    // ─────────────────────────────────────────────────────────────────
    //  Wire all views from XML
    // ─────────────────────────────────────────────────────────────────
    private void initViews() {
        tvWelcomeName       = findViewById(R.id.tvWelcomeName);
        tvWelcomeSubtitle   = findViewById(R.id.tvWelcomeSubtitle);

        tvAnnouncementCount = findViewById(R.id.tvAnnouncementCount);
        tvMaterialCount     = findViewById(R.id.tvMaterialCount);
        tvPendingCount      = findViewById(R.id.tvPendingCount);
        tvSubmittedCount    = findViewById(R.id.tvSubmittedCount);

        layoutRecentAnnouncements = findViewById(R.id.layoutRecentAnnouncements);
        tvNoAnnouncements         = findViewById(R.id.tvNoAnnouncements);

        layoutRecentMaterials = findViewById(R.id.layoutRecentMaterials);
        tvNoMaterials         = findViewById(R.id.tvNoMaterials);

        cardAnnouncements = findViewById(R.id.cardQuickAnnouncements);
        cardMaterials     = findViewById(R.id.cardQuickMaterials);
        cardAssignments   = findViewById(R.id.cardQuickAssignments);
        cardProfile       = findViewById(R.id.cardQuickProfile);

        tvProfileName  = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfileRole  = findViewById(R.id.tvProfileRole);
    }

    // ─────────────────────────────────────────────────────────────────
    //  Fill Welcome & Profile cards from Firebase Auth user object
    // ─────────────────────────────────────────────────────────────────
    private void loadUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // Build a friendly name: prefer displayName, fallback to email prefix
        String displayName = user.getDisplayName();
        if (displayName == null || displayName.isEmpty()) {
            String email = user.getEmail();
            if (email != null && email.contains("@")) {
                String part = email.split("@")[0];
                displayName = part.substring(0, 1).toUpperCase() + part.substring(1);
            } else {
                displayName = "Student";
            }
        }

        // Welcome section
        tvWelcomeName.setText("Hello, " + displayName + "!");
        tvWelcomeSubtitle.setText("Here's what's happening today");

        // Profile summary card
        tvProfileName.setText(displayName);
        tvProfileEmail.setText(user.getEmail() != null ? user.getEmail() : "—");
        tvProfileRole.setText("Student");
    }

    // ─────────────────────────────────────────────────────────────────
    //  Announcements — count stat + 2 recent preview rows
    // ─────────────────────────────────────────────────────────────────
    private void loadAnnouncementData() {
        dbRef.child("Announcements")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long total = snapshot.getChildrenCount();
                        tvAnnouncementCount.setText(String.valueOf(total));

                        // Clear old dynamic rows before re-populating
                        layoutRecentAnnouncements.removeAllViews();

                        if (total == 0) {
                            tvNoAnnouncements.setVisibility(View.VISIBLE);
                            return;
                        }
                        tvNoAnnouncements.setVisibility(View.GONE);

                        // Show the 2 most recent announcements
                        int shown = 0;
                        for (DataSnapshot child : snapshot.getChildren()) {
                            if (shown >= 2) break;
                            Announcement a = child.getValue(Announcement.class);
                            if (a == null) continue;

                            // Inflate the small row layout (created below)
                            View row = getLayoutInflater().inflate(
                                    R.layout.item_dashboard_announcement,
                                    layoutRecentAnnouncements, false);

                            TextView tvTitle   = row.findViewById(R.id.tvDashAnnouncementTitle);
                            TextView tvPreview = row.findViewById(R.id.tvDashAnnouncementPreview);
                            TextView tvUrgent  = row.findViewById(R.id.tvDashAnnouncementUrgent);

                            tvTitle.setText(a.getTitle());

                            // Truncate content to 60 chars for a clean preview
                            String content = a.getContent();
                            if (content != null && content.length() > 60) {
                                content = content.substring(0, 60) + "…";
                            }
                            tvPreview.setText(content);
                            tvUrgent.setVisibility(a.isUrgent() ? View.VISIBLE : View.GONE);

                            layoutRecentAnnouncements.addView(row);
                            shown++;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvAnnouncementCount.setText("—");
                    }
                });
    }

    // ─────────────────────────────────────────────────────────────────
    //  Learning Materials — count stat + 2 recent titles
    // ─────────────────────────────────────────────────────────────────
    private void loadMaterialData() {
        dbRef.child("LearningMaterials")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long total = snapshot.getChildrenCount();
                        tvMaterialCount.setText(String.valueOf(total));

                        layoutRecentMaterials.removeAllViews();

                        if (total == 0) {
                            tvNoMaterials.setVisibility(View.VISIBLE);
                            return;
                        }
                        tvNoMaterials.setVisibility(View.GONE);

                        int shown = 0;
                        for (DataSnapshot child : snapshot.getChildren()) {
                            if (shown >= 2) break;
                            LearningMaterial m = child.getValue(LearningMaterial.class);
                            if (m == null) continue;

                            View row = getLayoutInflater().inflate(
                                    R.layout.item_dashboard_material,
                                    layoutRecentMaterials, false);

                            TextView tvTitle = row.findViewById(R.id.tvDashMaterialTitle);
                            tvTitle.setText(m.title);

                            layoutRecentMaterials.addView(row);
                            shown++;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvMaterialCount.setText("—");
                    }
                });
    }

    // ─────────────────────────────────────────────────────────────────
    //  Assignments — pending vs submitted counts for this student
    //  Logic: if student's UID exists under assignments/{id}/submissions
    //         → submitted; otherwise → pending
    // ─────────────────────────────────────────────────────────────────
    private void loadAssignmentData() {
        if (currentUserId == null) return;

        dbRef.child("Assignments")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int pending   = 0;
                        int submitted = 0;

                        for (DataSnapshot assignSnap : snapshot.getChildren()) {
                            boolean hasSubmitted =
                                    assignSnap.child("submissions")
                                            .child(currentUserId)
                                            .exists();
                            if (hasSubmitted) submitted++;
                            else             pending++;
                        }

                        tvPendingCount.setText(String.valueOf(pending));
                        tvSubmittedCount.setText(String.valueOf(submitted));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvPendingCount.setText("—");
                        tvSubmittedCount.setText("—");
                    }
                });
    }

    // ─────────────────────────────────────────────────────────────────
    //  Quick Action Cards → navigate to EXISTING activities
    // ─────────────────────────────────────────────────────────────────
    private void setupClickListeners() {
        // Uses the already-existing AnnouncementActivity
        cardAnnouncements.setOnClickListener(v ->
                startActivity(new Intent(this, AnnouncementActivity.class)));

        // Uses the already-existing LearningMaterialActivity
        cardMaterials.setOnClickListener(v ->
                startActivity(new Intent(this, LearningMaterialActivity.class)));

        // Uses the already-existing AssignmentListActivity
        cardAssignments.setOnClickListener(v ->
                startActivity(new Intent(this, AssignmentListActivity.class)));

        // Uses the already-existing ProfileActivity
        cardProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }
}