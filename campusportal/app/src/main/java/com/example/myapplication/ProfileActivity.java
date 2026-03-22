package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends BaseActivity {

    // ── Views ─────────────────────────────────────────────────────────
    EditText etName, etId, etEmail, etDept, etYear, etPhone;
    Button btnEdit, btnSave;
    TextView txtLogout, txtChangePassword;
    ImageView ivProfilePhoto, btnEditPhoto;
    // NOTE: btnBack removed — drawer back arrow handles navigation

    // ── Firebase ──────────────────────────────────────────────────────
    FirebaseAuth mAuth;
    DatabaseReference userRef;

    // ── State ─────────────────────────────────────────────────────────
    String uid, role;
    boolean isEditing = false;
    Uri selectedImageUri;

    // ── Constants ─────────────────────────────────────────────────────
    static final int PICK_IMAGE_REQUEST  = 200;
    static final int CAMERA_REQUEST      = 201;
    static final int PERMISSION_REQUEST_CODE = 300;

    SharedPreferences prefs;

    // ─────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Setup toolbar & navigation drawer
        toolbar = findViewById(R.id.toolbar);
        setupDrawer(R.id.nav_profile);

        // Initialise Cloudinary (needed for profile photo upload)
        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", getString(R.string.cloud_name));
            config.put("api_key",    getString(R.string.api_key));
            config.put("api_secret", getString(R.string.api_secret));
            MediaManager.init(this, config);
        } catch (IllegalStateException e) {
            // Already initialised — safe to ignore
        }

        // ── Wire up views ─────────────────────────────────────────────
        etName    = findViewById(R.id.etProfileName);
        etId      = findViewById(R.id.etProfileStudentId);
        etEmail   = findViewById(R.id.etProfileEmail);
        etDept    = findViewById(R.id.etProfileDepartment);
        etYear    = findViewById(R.id.etProfileYear);
        etPhone   = findViewById(R.id.etProfilePhone);

        btnEdit   = findViewById(R.id.btnEditProfile);
        btnSave   = findViewById(R.id.btnSaveProfile);

        txtLogout         = findViewById(R.id.txtLogout);
        txtChangePassword = findViewById(R.id.txtChangePassword);

        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        btnEditPhoto   = findViewById(R.id.btnEditPhoto);

        // ── Firebase & SharedPreferences ──────────────────────────────
        mAuth = FirebaseAuth.getInstance();
        prefs = getSharedPreferences(Login.PREFS_NAME, MODE_PRIVATE);

        uid  = prefs.getString(Login.KEY_UID,  "");
        role = prefs.getString(Login.KEY_ROLE, "");

        userRef = FirebaseDatabase.getInstance()
                .getReference("CampusConnect")
                .child("Users")
                .child(uid);

        // ── Load data ─────────────────────────────────────────────────
        loadUserData();
        loadProfilePhoto();

        // ── Click listeners ───────────────────────────────────────────
        btnEdit.setOnClickListener(v -> enableEditing());
        btnSave.setOnClickListener(v -> saveProfile());
        txtLogout.setOnClickListener(v -> logout());
        btnEditPhoto.setOnClickListener(v -> showImageOptions());

        // Change password click
        txtChangePassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (!email.isEmpty()) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnSuccessListener(unused ->
                                Toast.makeText(this,
                                        "Password reset email sent to " + email,
                                        Toast.LENGTH_LONG).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(this,
                                        "Failed: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────
    //  Load user data from Firebase
    // ─────────────────────────────────────────────────────────────────
    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                etName.setText(snapshot.child("name").getValue(String.class));
                etEmail.setText(snapshot.child("email").getValue(String.class));

                // Show studentId or teacherId depending on role
                if ("student".equals(role)) {
                    etId.setText(snapshot.child("studentId").getValue(String.class));
                } else {
                    etId.setText(snapshot.child("teacherId").getValue(String.class));
                }

                etDept.setText(snapshot.child("department").getValue(String.class));
                etYear.setText(snapshot.child("year").getValue(String.class));
                etPhone.setText(snapshot.child("phone").getValue(String.class));

                // Load profile photo if saved in Firebase
                String firebasePhotoUrl =
                        snapshot.child("profilePhotoUrl").getValue(String.class);
                if (firebasePhotoUrl != null && !firebasePhotoUrl.isEmpty()) {
                    Glide.with(ProfileActivity.this)
                            .load(firebasePhotoUrl)
                            .into(ivProfilePhoto);
                    prefs.edit()
                            .putString("profile_photo_url", firebasePhotoUrl)
                            .apply();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this,
                        "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────
    //  Enable editing of editable fields
    // ─────────────────────────────────────────────────────────────────
    private void enableEditing() {
        isEditing = true;
        etDept.setEnabled(true);
        etYear.setEnabled(true);
        etPhone.setEnabled(true);
        btnEdit.setVisibility(Button.GONE);
        btnSave.setVisibility(Button.VISIBLE);
    }

    // ─────────────────────────────────────────────────────────────────
    //  Save edited fields to Firebase
    // ─────────────────────────────────────────────────────────────────
    private void saveProfile() {
        String dept  = etDept.getText().toString().trim();
        String year  = etYear.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        userRef.child("department").setValue(dept);
        userRef.child("year").setValue(year);
        userRef.child("phone").setValue(phone);

        Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();

        etDept.setEnabled(false);
        etYear.setEnabled(false);
        etPhone.setEnabled(false);

        btnEdit.setVisibility(Button.VISIBLE);
        btnSave.setVisibility(Button.GONE);
        isEditing = false;
    }

    // ─────────────────────────────────────────────────────────────────
    //  Logout
    // ─────────────────────────────────────────────────────────────────
    private void logout() {
        FirebaseAuth.getInstance().signOut();
        prefs.edit().clear().apply();
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ─────────────────────────────────────────────────────────────────
    //  Profile photo options dialog
    // ─────────────────────────────────────────────────────────────────
    private void showImageOptions() {
        String[] options = {"Take Photo", "Choose From Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Change Profile Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) checkCameraPermissions();
                    else            openGallery();
                })
                .show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void checkCameraPermissions() {
        boolean cameraGranted  = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean storageGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        if (cameraGranted && storageGranted) {
            openCameraIntent();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    PERMISSION_REQUEST_CODE);
        }
    }

    private void openCameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    // ─────────────────────────────────────────────────────────────────
    //  Permission result
    // ─────────────────────────────────────────────────────────────────
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) openCameraIntent();
            else Toast.makeText(this,
                    "Camera & Storage permission required",
                    Toast.LENGTH_LONG).show();
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Activity result — image picked or captured
    // ─────────────────────────────────────────────────────────────────
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) return;

        if (requestCode == PICK_IMAGE_REQUEST) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                getContentResolver().takePersistableUriPermission(
                        selectedImageUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
                uploadProfilePhoto();
            }
        } else if (requestCode == CAMERA_REQUEST) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap photo = (Bitmap) extras.get("data");
                if (photo != null) {
                    selectedImageUri = Uri.parse(
                            MediaStore.Images.Media.insertImage(
                                    getContentResolver(), photo, "ProfilePhoto", null));
                    uploadProfilePhoto();
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Upload profile photo to Cloudinary, save URL to Firebase
    // ─────────────────────────────────────────────────────────────────
    private void uploadProfilePhoto() {
        if (selectedImageUri == null) return;

        Toast.makeText(this, "Uploading photo...", Toast.LENGTH_SHORT).show();

        MediaManager.get().upload(selectedImageUri)
                .callback(new UploadCallback() {
                    @Override public void onStart(String requestId) { }
                    @Override public void onProgress(String requestId,
                                                     long bytes, long totalBytes) { }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = resultData.get("secure_url").toString();

                        // Save to Firebase
                        userRef.child("profilePhotoUrl").setValue(imageUrl);

                        // Save to SharedPreferences for quick load next time
                        prefs.edit()
                                .putString("profile_photo_url", imageUrl)
                                .apply();

                        // Display immediately
                        Glide.with(ProfileActivity.this)
                                .load(imageUrl)
                                .into(ivProfilePhoto);

                        Toast.makeText(ProfileActivity.this,
                                "Profile photo updated", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String requestId,
                                        com.cloudinary.android.callback.ErrorInfo error) {
                        Toast.makeText(ProfileActivity.this,
                                "Upload failed: " + error.getDescription(),
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onReschedule(String requestId,
                                             com.cloudinary.android.callback.ErrorInfo error) { }
                })
                .dispatch();
    }

    // ─────────────────────────────────────────────────────────────────
    //  Load profile photo from SharedPreferences cache on startup
    // ─────────────────────────────────────────────────────────────────
    private void loadProfilePhoto() {
        String url = prefs.getString("profile_photo_url", null);
        if (url != null && !url.isEmpty()) {
            Glide.with(this).load(url).into(ivProfilePhoto);
        }
    }
}