package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LearningMaterialActivity extends BaseActivity {

    RecyclerView rvMaterials;
    FloatingActionButton fabAdd;
    Uri selectedFileUri;
    static final int PICK_FILE_REQUEST = 101;

    DatabaseReference ref;
    ArrayList<LearningMaterial> materialList = new ArrayList<>();
    LearningMaterialAdapter adapter;
    String userRole;
    private ValueEventListener materialsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.learning_material_activity);

        toolbar = findViewById(R.id.toolbar);
        setupDrawer(R.id.nav_upload_material);

        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", getString(R.string.cloud_name));
            config.put("api_key", getString(R.string.api_key));
            config.put("api_secret", getString(R.string.api_secret));
            MediaManager.init(this, config);
        } catch (IllegalStateException e) { }

        rvMaterials = findViewById(R.id.rvMaterials);
        adapter = new LearningMaterialAdapter(this, materialList);
        rvMaterials.setAdapter(adapter);
        rvMaterials.setLayoutManager(new LinearLayoutManager(this));

        fabAdd = findViewById(R.id.fabAdd);

        ref = FirebaseDatabase.getInstance()
                .getReference("CampusConnect")
                .child("LearningMaterials");

        com.google.firebase.auth.FirebaseUser currentUser =
                FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String uid = currentUser.getUid();
        DatabaseReference roleRef = FirebaseDatabase.getInstance()
                .getReference("CampusConnect")
                .child("Users")
                .child(uid)
                .child("role");

        roleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                userRole = snapshot.getValue(String.class);
                fabAdd.setVisibility("teacher".equals(userRole) ? View.VISIBLE : View.GONE);
            }
            @Override public void onCancelled(DatabaseError error) { }
        });

        fabAdd.setOnClickListener(v -> showUploadDialog());
        loadMaterials();
    }

    private void showUploadDialog() {
        selectedFileUri = null;
        View view = getLayoutInflater().inflate(R.layout.dialog_upload_material, null);
        EditText etTitle = view.findViewById(R.id.etMaterialTitle);
        Button btnAttach = view.findViewById(R.id.btnAttach);
        Button btnUpload = view.findViewById(R.id.btnUpload);
        TextView tvFile = view.findViewById(R.id.tvSelectedFile);

        AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();

        btnAttach.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/pdf", "image/*"});
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(intent, PICK_FILE_REQUEST);
        });

        btnUpload.setOnClickListener(v -> {
            if (!"teacher".equals(userRole)) {
                Toast.makeText(this, "Only teachers can upload materials", Toast.LENGTH_SHORT).show();
                return;
            }
            String title = etTitle.getText().toString().trim();
            if (title.isEmpty() || selectedFileUri == null) {
                Toast.makeText(this, "Complete all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String teacherUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String id = ref.push().getKey();

            MediaManager.get().upload(selectedFileUri)
                    .option("resource_type", "raw")
                    .callback(new UploadCallback() {
                        @Override public void onStart(String requestId) { }
                        @Override public void onProgress(String requestId, long bytes, long totalBytes) { }
                        @Override public void onSuccess(String requestId, Map resultData) {
                            String fileUrl = resultData.get("secure_url").toString();
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("title", title);
                            map.put("attachmentUri", fileUrl); // store Cloudinary URL
                            map.put("teacherUid", teacherUid);
                            map.put("timestamp", System.currentTimeMillis());
                            ref.child(id).setValue(map);
                            Toast.makeText(LearningMaterialActivity.this, "Material Uploaded", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                        @Override public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                            Toast.makeText(LearningMaterialActivity.this, "Upload Failed", Toast.LENGTH_LONG).show();
                        }
                        @Override public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) { }
                    })
                    .dispatch();
        });

        dialog.show();
    }

    private void loadMaterials() {
        materialsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                materialList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    LearningMaterial material = data.getValue(LearningMaterial.class);
                    if (material != null) materialList.add(material);
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(DatabaseError error) { }
        };
        ref.addValueEventListener(materialsListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ref != null && materialsListener != null) {
            ref.removeEventListener(materialsListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            getContentResolver().takePersistableUriPermission(selectedFileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Toast.makeText(this, "File Selected: " + selectedFileUri.getLastPathSegment(), Toast.LENGTH_SHORT).show();
        }
    }
}