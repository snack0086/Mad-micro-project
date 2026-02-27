package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LearningMaterialActivity extends BaseActivity {

    EditText etMaterialTitle;
    Spinner spinnerYear;
    Button btnAttach, btnUpload;
    LinearLayout layoutUpload;
    RecyclerView rvMaterials;
    TextView tvSelectedFile;

    Uri selectedFileUri;
    String attachmentType = "none";

    static final int PICK_FILE_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.learning_material_activity);

        userRole = "teacher";
        setupDrawer(R.id.nav_upload_material);

        // Bind UI
        etMaterialTitle = findViewById(R.id.etMaterialTitle);
        btnAttach = findViewById(R.id.btnAttach);
        btnUpload = findViewById(R.id.btnUpload);
        layoutUpload = findViewById(R.id.layoutUpload);
        rvMaterials = findViewById(R.id.rvMaterials);
        tvSelectedFile = findViewById(R.id.tvSelectedFile);

        // Spinner Setup

        // Hide upload section if student
        if ("student".equals(userRole)) {
            layoutUpload.setVisibility(View.GONE);
        } else {
            layoutUpload.setVisibility(View.VISIBLE);
        }

        // Attach Button
        btnAttach.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES,
                    new String[]{"application/pdf", "image/*"});
            startActivityForResult(intent, PICK_FILE_REQUEST);
        });

        // Upload Button
        btnUpload.setOnClickListener(v -> uploadMaterial());
    }

    private void uploadMaterial() {

        String title = etMaterialTitle.getText().toString().trim();
        String year = spinnerYear.getSelectedItem().toString();

        if (title.isEmpty()) {
            Toast.makeText(this, "Enter material title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (year.equals("Select Year")) {
            Toast.makeText(this, "Select year", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedFileUri == null) {
            Toast.makeText(this, "Attach a file", Toast.LENGTH_SHORT).show();
            return;
        }

        String teacherUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("CampusConnect")
                .child("LearningMaterials");

        String materialId = ref.push().getKey();

        HashMap<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("year", year);
        map.put("attachmentUri", selectedFileUri.toString());
        map.put("teacherUid", teacherUid);
        map.put("timestamp", System.currentTimeMillis());

        ref.child(materialId).setValue(map)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Material uploaded", Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    private void clearFields() {
        etMaterialTitle.setText("");
        spinnerYear.setSelection(0);
        tvSelectedFile.setText("No file selected");
        selectedFileUri = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            tvSelectedFile.setText("File selected");
        }
    }
}