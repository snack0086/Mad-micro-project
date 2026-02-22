
package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

public class LearningMaterialActivity extends AppCompatActivity {

    EditText etMaterialTitle;
    Spinner spinnerYear;
    Button btnAttach, btnUpload;
    LinearLayout layoutUpload;
    RecyclerView rvMaterials;
    TextView tvSelectedFile;
    Uri selectedFileUri;
    DBHelper dbHelper;

    static final int PICK_FILE_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.learning_material_activity);
        dbHelper = new DBHelper(this);


        // Link UI elements
        etMaterialTitle = findViewById(R.id.etMaterialTitle);
        spinnerYear = findViewById(R.id.spinnerYear);
        btnAttach = findViewById(R.id.btnAttach);
        btnUpload = findViewById(R.id.btnUpload);
        layoutUpload = findViewById(R.id.layoutUpload);
        rvMaterials = findViewById(R.id.rvMaterials);

        // ---------- YEAR SPINNER ----------
        String[] years = {"Select Year", "1st Year", "2nd Year", "3rd Year"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                years
        );
        spinnerYear.setAdapter(adapter);

        // ---------- ROLE CHECK ----------
        // TEMPORARY (replace later with login data)
        String userRole = "teacher"; // change to "student" to test

        if (userRole.equals("student")) {
            layoutUpload.setVisibility(View.GONE);
        } else {
            layoutUpload.setVisibility(View.VISIBLE);
        }

        // ---------- ATTACH BUTTON ----------
        btnAttach.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES,
                    new String[]{"application/pdf", "image/*"});
            startActivityForResult(intent, PICK_FILE_REQUEST);
        });

        // ---------- UPLOAD BUTTON ----------
        btnUpload.setOnClickListener(v -> {
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

            // DATABASE INSERT WILL COME HERE LATER
            boolean inserted = dbHelper.insertMaterial(
                    title,
                    selectedFileUri.toString(),
                    year
            );

            if (inserted) {
                Toast.makeText(this, "Material uploaded successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Database error", Toast.LENGTH_SHORT).show();
            }

            // Clear fields
            etMaterialTitle.setText("");
            spinnerYear.setSelection(0);
            selectedFileUri = null;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            tvSelectedFile.setText("File selected");
            Toast.makeText(this, "File attached", Toast.LENGTH_SHORT).show();
        }
    }
}
