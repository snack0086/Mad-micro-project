package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class TeacherAssignmentActivity extends AppCompatActivity {

    TextView tvTitle, tvSelectedFile;
    EditText etAssignmentTitle, etAssignmentDesc;
    Spinner spinnerYear;
    Button btnAttachFile, btnPostAssignment;

    DBHelper dbHelper;

    private static final int PICK_FILE_REQUEST = 100;

    Uri selectedFileUri;
    String attachmentType = "none";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacher_assignment_activity);

        // UI binding
        tvTitle = findViewById(R.id.tvTitle);
        tvSelectedFile = findViewById(R.id.tvSelectedFile);
        etAssignmentTitle = findViewById(R.id.etAssignmentTitle);
        etAssignmentDesc = findViewById(R.id.etAssignmentDesc);
        spinnerYear = findViewById(R.id.spinnerYear);
        btnAttachFile = findViewById(R.id.btnAttachFile);
        btnPostAssignment = findViewById(R.id.btnPostAssignment);

        // DB init
        dbHelper = new DBHelper(this);

        // Spinner setup
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.year_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(adapter);

        btnAttachFile.setOnClickListener(v -> openFilePicker());
        btnPostAssignment.setOnClickListener(v -> postAssignment());
    }

    // Open file picker
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "application/pdf",
                "image/*"
        });
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();

            if (selectedFileUri != null) {
                String mimeType = getContentResolver().getType(selectedFileUri);

                if (mimeType != null) {
                    if (mimeType.equals("application/pdf")) {
                        attachmentType = "pdf";
                    } else if (mimeType.startsWith("image/")) {
                        attachmentType = "image";
                    }
                }
                tvSelectedFile.setText("File selected");
            }
        }
    }

    // Insert assignment into DB
    private void postAssignment() {

        String title = etAssignmentTitle.getText().toString().trim();
        String description = etAssignmentDesc.getText().toString().trim();
        String year = spinnerYear.getSelectedItem().toString();

        if (title.isEmpty()) {
            etAssignmentTitle.setError("Title required");
            return;
        }

        if (description.isEmpty()) {
            etAssignmentDesc.setError("Description required");
            return;
        }

        String attachmentUriString = "";
        if (selectedFileUri != null) {
            attachmentUriString = selectedFileUri.toString();
        } else {
            attachmentType = "none";
        }

        Log.d("DB_TEST", "Inserting assignment: " + title);

        boolean inserted = dbHelper.insertAssignment(
                title,
                description,
                year,
                attachmentType,
                attachmentUriString
        );

        if (inserted) {
            Toast.makeText(this, "Assignment posted successfully", Toast.LENGTH_SHORT).show();
            clearFields();
        } else {
            Toast.makeText(this, "Failed to post assignment", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFields() {
        etAssignmentTitle.setText("");
        etAssignmentDesc.setText("");
        spinnerYear.setSelection(0);
        tvSelectedFile.setText("No file selected");
        selectedFileUri = null;
        attachmentType = "none";
    }
}
