package com.example.myapplication;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnnouncementActivity extends BaseActivity {

    private RecyclerView rvAnnouncements;
    private FloatingActionButton fabCreate;
    private LinearLayout emptyState;
    private ProgressBar progressBar;
    private TabLayout tabLayout;
    private TextView tvEmptyMessage;

    private AnnouncementAdapter adapter;
    private boolean isTeacher = false;
    private String username = "User";
    private String currentCategory = "All";

    private DatabaseReference announcementsRef;

    private final List<Announcement> allAnnouncements = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement);

        String role = getSharedPreferences(Login.PREFS_NAME, MODE_PRIVATE)
                .getString(Login.KEY_ROLE, "");
        isTeacher = "teacher".equals(role);
        userRole = role;

        // Highlight "Announcements" in the drawer
        setupDrawer(R.id.nav_announcements);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                username = currentUser.getDisplayName();
            } else if (currentUser.getEmail() != null) {
                String name = currentUser.getEmail().split("@")[0];
                if (!name.isEmpty()) {
                    username = name.substring(0, 1).toUpperCase() + name.substring(1);
                }
            }
        }

        announcementsRef = FirebaseDatabase.getInstance()
                .getReference("CampusConnect")
                .child("Announcements");

        initViews();
        setupRecyclerView();
        setupTabs();
        observeAnnouncements();
    }

    private void initViews() {
        rvAnnouncements = findViewById(R.id.rvAnnouncements);
        fabCreate = findViewById(R.id.fabCreate);
        emptyState = findViewById(R.id.emptyState);
        progressBar = findViewById(R.id.progressBar);
        tabLayout = findViewById(R.id.tabLayout);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);

        if (isTeacher) {
            fabCreate.setVisibility(View.VISIBLE);
            fabCreate.setOnClickListener(v -> showAnnouncementDialog(null));
            tvEmptyMessage.setText("No announcements yet. Tap + to create one.");
        } else {
            tvEmptyMessage.setText("No announcements available.");
        }
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("All"));
        tabLayout.addTab(tabLayout.newTab().setText("Urgent"));
        tabLayout.addTab(tabLayout.newTab().setText("Event"));
        tabLayout.addTab(tabLayout.newTab().setText("Academic"));
        tabLayout.addTab(tabLayout.newTab().setText("General"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentCategory = tab.getText().toString();
                filterAnnouncements();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                currentCategory = tab.getText().toString();
                filterAnnouncements();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new AnnouncementAdapter();
        rvAnnouncements.setLayoutManager(new LinearLayoutManager(this));
        rvAnnouncements.setAdapter(adapter);
    }

    private void observeAnnouncements() {
        progressBar.setVisibility(View.VISIBLE);
        rvAnnouncements.setVisibility(View.GONE);

        announcementsRef.orderByChild("timestamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allAnnouncements.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Announcement a = child.getValue(Announcement.class);
                            if (a != null) {
                                if (a.getId() == null) {
                                    a.setId(child.getKey());
                                }
                                allAnnouncements.add(a);
                            }
                        }
                        sortAnnouncements();
                        filterAnnouncements();
                        progressBar.setVisibility(View.GONE);
                        rvAnnouncements.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AnnouncementActivity.this,
                                "Failed to load announcements",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sortAnnouncements() {
        Collections.sort(allAnnouncements, (a1, a2) -> {
            if (a1.isUrgent() && !a2.isUrgent()) return -1;
            if (!a1.isUrgent() && a2.isUrgent()) return 1;
            return Long.compare(a2.getTimestamp(), a1.getTimestamp());
        });
    }

    private void filterAnnouncements() {
        List<Announcement> display = new ArrayList<>();

        if ("All".equals(currentCategory)) {
            display.addAll(allAnnouncements);
        } else {
            for (Announcement a : allAnnouncements) {
                if ("Urgent".equals(currentCategory) && a.isUrgent()) {
                    display.add(a);
                } else if (a.getCategory() != null && currentCategory.equals(a.getCategory())) {
                    display.add(a);
                }
            }
        }

        adapter.setAnnouncements(display);
        updateEmptyState();
    }

    private void showAnnouncementDialog(Announcement announcement) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_announcement, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etContent = dialogView.findViewById(R.id.etContent);
        CheckBox cbUrgent = dialogView.findViewById(R.id.cbUrgent);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnDelete = dialogView.findViewById(R.id.btnDelete);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.announcement_categories, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);

        if (announcement != null) {
            etTitle.setText(announcement.getTitle());
            etContent.setText(announcement.getContent());
            cbUrgent.setChecked(announcement.isUrgent());

            String[] categories = getResources().getStringArray(R.array.announcement_categories);
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equals(announcement.getCategory())) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }

            btnDelete.setVisibility(View.VISIBLE);
        } else {
            btnDelete.setVisibility(View.GONE);
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        btnSave.setOnClickListener(v -> {
            if (validateInput(etTitle, etContent)) {
                String category = spinnerCategory.getSelectedItem().toString();
                String title = etTitle.getText().toString().trim();
                String content = etContent.getText().toString().trim();
                boolean urgent = cbUrgent.isChecked();

                if (announcement == null) {
                    createAnnouncement(title, content, urgent, category);
                } else {
                    updateAnnouncement(announcement, title, content, urgent, category);
                }
                dialog.dismiss();
            }
        });

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete")
                    .setMessage("Delete this announcement?")
                    .setPositiveButton("Yes", (d, w) -> {
                        deleteAnnouncement(announcement);
                        dialog.dismiss();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private boolean validateInput(EditText etTitle, EditText etContent) {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Title required");
            return false;
        }
        if (content.isEmpty()) {
            etContent.setError("Content required");
            return false;
        }
        return true;
    }

    private void createAnnouncement(String title, String content, boolean urgent, String category) {
        String key = announcementsRef.push().getKey();
        if (key == null) {
            Toast.makeText(this, "Failed to create announcement", Toast.LENGTH_SHORT).show();
            return;
        }
        Announcement newAnnouncement = new Announcement(title, content, urgent, category, username);
        newAnnouncement.setId(key);

        announcementsRef.child(key).setValue(newAnnouncement)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Announcement created", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateAnnouncement(Announcement announcement, String title, String content,
                                    boolean urgent, String category) {
        if (announcement.getId() == null) {
            return;
        }

        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setUrgent(urgent);
        announcement.setCategory(category);
        announcement.setTimestamp(System.currentTimeMillis());

        announcementsRef.child(announcement.getId()).setValue(announcement)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Announcement updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deleteAnnouncement(Announcement announcement) {
        if (announcement == null || announcement.getId() == null) return;

        announcementsRef.child(announcement.getId()).removeValue()
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Announcement deleted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            emptyState.setVisibility(View.VISIBLE);
            rvAnnouncements.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvAnnouncements.setVisibility(View.VISIBLE);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View cardView;
        TextView tvTitle, tvContent, tvDate, tvCategory;
        View urgentIndicator;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            urgentIndicator = itemView.findViewById(R.id.urgentIndicator);
        }

        void bind(Announcement announcement) {
            tvTitle.setText(announcement.getTitle());
            tvContent.setText(announcement.getContent());
            tvDate.setText(announcement.getDate());

            if (announcement.getCategory() != null) {
                tvCategory.setText(announcement.getCategory());
                tvCategory.setVisibility(View.VISIBLE);
            } else {
                tvCategory.setVisibility(View.GONE);
            }

            if (announcement.isUrgent()) {
                urgentIndicator.setVisibility(View.VISIBLE);
                tvCategory.setTextColor(getColor(R.color.urgent));
            } else {
                urgentIndicator.setVisibility(View.GONE);
                tvCategory.setTextColor(getColor(R.color.primary));
            }

            itemView.setOnClickListener(v -> {
                if (isTeacher) {
                    showAnnouncementDialog(announcement);
                } else {
                    new AlertDialog.Builder(AnnouncementActivity.this)
                            .setTitle(announcement.getTitle())
                            .setMessage(announcement.getContent() + "\n\nPosted: " +
                                    announcement.getDate() + " at " + announcement.getTime() +
                                    (announcement.getCategory() != null
                                            ? ("\nCategory: " + announcement.getCategory())
                                            : "") +
                                    (announcement.getAuthor() != null
                                            ? ("\nBy: " + announcement.getAuthor())
                                            : ""))
                            .setPositiveButton("OK", null)
                            .show();
                }
            });
        }
    }

    class AnnouncementAdapter extends RecyclerView.Adapter<ViewHolder> {
        private List<Announcement> displayList = new ArrayList<>();

        void setAnnouncements(List<Announcement> announcements) {
            this.displayList = announcements;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_announcement_list, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Announcement announcement = displayList.get(position);
            holder.bind(announcement);
        }

        @Override
        public int getItemCount() {
            return displayList.size();
        }
    }
}

