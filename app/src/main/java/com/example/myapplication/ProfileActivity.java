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

import java.util.Map;

public class ProfileActivity extends BaseActivity {

    EditText etName, etId, etEmail, etDept, etYear, etPhone;
    Button btnEdit, btnSave;
    TextView txtLogout, txtChangePassword;
    ImageView btnBack, ivProfilePhoto, btnEditPhoto;

    FirebaseAuth mAuth;
    DatabaseReference userRef;

    String uid, role;
    boolean isEditing = false;
    Uri selectedImageUri;

    static final int PICK_IMAGE_REQUEST = 200;
    static final int CAMERA_REQUEST = 201;
    static final int PERMISSION_REQUEST_CODE = 300;

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        toolbar = findViewById(R.id.toolbar);
        setupDrawer(R.id.nav_profile);

        etName = findViewById(R.id.etProfileName);
        etId = findViewById(R.id.etProfileStudentId);
        etEmail = findViewById(R.id.etProfileEmail);
        etDept = findViewById(R.id.etProfileDepartment);
        etYear = findViewById(R.id.etProfileYear);
        etPhone = findViewById(R.id.etProfilePhone);

        btnEdit = findViewById(R.id.btnEditProfile);
        btnSave = findViewById(R.id.btnSaveProfile);

        txtLogout = findViewById(R.id.txtLogout);
        txtChangePassword = findViewById(R.id.txtChangePassword);

        btnBack = findViewById(R.id.btnBack);
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        btnEditPhoto = findViewById(R.id.btnEditPhoto);

        mAuth = FirebaseAuth.getInstance();
        prefs = getSharedPreferences(Login.PREFS_NAME, MODE_PRIVATE);

        uid = prefs.getString(Login.KEY_UID, "");
        role = prefs.getString(Login.KEY_ROLE, "");

        userRef = FirebaseDatabase.getInstance()
                .getReference("CampusConnect")
                .child("Users")
                .child(uid);

        loadUserData();
        loadProfilePhoto();

        btnEdit.setOnClickListener(v -> enableEditing());
        btnSave.setOnClickListener(v -> saveProfile());
        txtLogout.setOnClickListener(v -> logout());
        btnBack.setOnClickListener(v -> finish());
        btnEditPhoto.setOnClickListener(v -> showImageOptions());
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    etName.setText(snapshot.child("name").getValue(String.class));
                    etEmail.setText(snapshot.child("email").getValue(String.class));
                    if ("student".equals(role)) etId.setText(snapshot.child("studentId").getValue(String.class));
                    else etId.setText(snapshot.child("teacherId").getValue(String.class));

                    etDept.setText(snapshot.child("department").getValue(String.class));
                    etYear.setText(snapshot.child("year").getValue(String.class));
                    etPhone.setText(snapshot.child("phone").getValue(String.class));

                    // Load photo from Firebase if exists
                    String firebasePhotoUrl = snapshot.child("profilePhotoUrl").getValue(String.class);
                    if (firebasePhotoUrl != null) {
                        Glide.with(ProfileActivity.this).load(firebasePhotoUrl).into(ivProfilePhoto);
                        prefs.edit().putString("profile_photo_url", firebasePhotoUrl).apply();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enableEditing() {
        isEditing = true;
        etDept.setEnabled(true);
        etYear.setEnabled(true);
        etPhone.setEnabled(true);
        btnEdit.setVisibility(Button.GONE);
        btnSave.setVisibility(Button.VISIBLE);
    }

    private void saveProfile() {
        userRef.child("department").setValue(etDept.getText().toString().trim());
        userRef.child("year").setValue(etYear.getText().toString().trim());
        userRef.child("phone").setValue(etPhone.getText().toString().trim());

        Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();

        etDept.setEnabled(false);
        etYear.setEnabled(false);
        etPhone.setEnabled(false);

        btnEdit.setVisibility(Button.VISIBLE);
        btnSave.setVisibility(Button.GONE);
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        prefs.edit().clear().apply();
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showImageOptions() {
        String[] options = {"Take Photo", "Choose From Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Profile Photo");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) checkCameraPermissions();
            else openGallery();
        });
        builder.show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void checkCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        } else openCameraIntent();
    }

    private void openCameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean granted = true;
            for (int res : grantResults) {
                if (res != PackageManager.PERMISSION_GRANTED) granted = false;
            }
            if (granted) openCameraIntent();
            else Toast.makeText(this, "Camera & Storage permission required", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                selectedImageUri = data.getData();
                getContentResolver().takePersistableUriPermission(selectedImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                uploadProfilePhoto();
            } else if (requestCode == CAMERA_REQUEST && data.getExtras() != null) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                selectedImageUri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), photo, "ProfilePhoto", null));
                uploadProfilePhoto();
            }
        }
    }

    private void uploadProfilePhoto() {
        if (selectedImageUri == null) return;

        MediaManager.get().upload(selectedImageUri)
                .callback(new UploadCallback() {
                    @Override public void onStart(String requestId) { }
                    @Override public void onProgress(String requestId, long bytes, long totalBytes) { }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = resultData.get("secure_url").toString();
                        // Save in Firebase & SharedPreferences
                        userRef.child("profilePhotoUrl").setValue(imageUrl);
                        prefs.edit().putString("profile_photo_url", imageUrl).apply();
                        Glide.with(ProfileActivity.this).load(imageUrl).into(ivProfilePhoto);
                        Toast.makeText(ProfileActivity.this, "Profile photo updated", Toast.LENGTH_SHORT).show();
                    }

                    @Override public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                        Toast.makeText(ProfileActivity.this, "Upload failed", Toast.LENGTH_LONG).show();
                    }

                    @Override public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) { }
                })
                .dispatch();
    }

    private void loadProfilePhoto() {
        String url = prefs.getString("profile_photo_url", null);
        if (url != null) Glide.with(this).load(url).into(ivProfilePhoto);
    }
}