package com.example.myapplication;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;
import java.util.ArrayList;
import android.widget.*;
import android.view.View;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AlertDialog;
import java.util.HashMap;


public class AssignmentListActivity extends BaseActivity {

    private ValueEventListener assignmentsListener;

    RecyclerView rvAssignments;
    ArrayList<Assignment> list;
    AssignmentAdapter adapter;
    Button btnPostAssignemnt;
    boolean isTeacher=false;
    FloatingActionButton fabAddAssignment;


    DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.assignment_list_activity);
        setupDrawer(R.id.nav_manage_assignments);
        String role = getSharedPreferences(Login.PREFS_NAME, MODE_PRIVATE)
                .getString(Login.KEY_ROLE, "");
        isTeacher = "teacher".equals(role);
        userRole = role;
        rvAssignments = findViewById(R.id.rvAssignments);
        rvAssignments.setLayoutManager(new LinearLayoutManager(this));
        fabAddAssignment = findViewById(R.id.fabAddAssignment);
        if (!isTeacher) {
            fabAddAssignment.setVisibility(View.GONE);
        }

        fabAddAssignment.setOnClickListener(v -> showPostDialog());
        list = new ArrayList<>();
        adapter = new AssignmentAdapter(this, list, userRole);
        rvAssignments.setAdapter(adapter);

        ref = FirebaseDatabase.getInstance()
                .getReference("CampusConnect")
                .child("Assignments");

        loadAssignments();
    }

    private void showPostDialog() {

        View view = getLayoutInflater()
                .inflate(R.layout.dialog_post_assignment, null);

        EditText etTitle = view.findViewById(R.id.etAssignmentTitle);
        EditText etDesc = view.findViewById(R.id.etAssignmentDesc);
        Button btnPost = view.findViewById(R.id.btnPostAssignment);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        btnPost.setOnClickListener(v -> {

            String title = etTitle.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();

            if (title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "Complete all fields",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            com.google.firebase.auth.FirebaseUser currentUser =
                    FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
                return;
            }
            String teacherUid = currentUser.getUid();

            String id = ref.push().getKey();

            HashMap<String, Object> map = new HashMap<>();
            map.put("title", title);
            map.put("description", desc);
            map.put("teacherUid", teacherUid);
            map.put("timestamp", System.currentTimeMillis());

            ref.child(id).setValue(map);

            Toast.makeText(this,
                    "Assignment Posted",
                    Toast.LENGTH_SHORT).show();

            dialog.dismiss();
        });

        dialog.show();
    }
    private void loadAssignments() {
        assignmentsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Assignment a = ds.getValue(Assignment.class);
                    if (a != null) {
                        a.id = ds.getKey();
                        list.add(a);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        ref.addValueEventListener(assignmentsListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ref != null && assignmentsListener != null) {
            ref.removeEventListener(assignmentsListener);
        }
    }
}