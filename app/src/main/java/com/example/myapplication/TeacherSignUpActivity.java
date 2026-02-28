package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.*;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class TeacherSignUpActivity extends AppCompatActivity {

    EditText name, id, email, password;
    TextView txtLogin;
    Button signup;

    FirebaseAuth mAuth;
    DatabaseReference teacherRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_signup);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Database reference
        teacherRef = FirebaseDatabase.getInstance()
                .getReference("CampusConnect")
                .child("Teachers");

        // Initialize views
        name = findViewById(R.id.etTeacherName);
        id = findViewById(R.id.etTeacherId);
        email = findViewById(R.id.etTeacherEmail);
        password = findViewById(R.id.etTeacherPassword);
        signup = findViewById(R.id.btnTeacherSignup);
        txtLogin=findViewById(R.id.txtLogin);
        signup.setOnClickListener(v -> {

            String tName = name.getText().toString().trim();
            String tId = id.getText().toString().trim();
            String tEmail = email.getText().toString().trim();
            String tPass = password.getText().toString().trim();

            // Validation
            if (tName.isEmpty()) {
                Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (tId.isEmpty()) {
                Toast.makeText(this, "Teacher ID is required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (tEmail.isEmpty()) {
                Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (tPass.isEmpty()) {
                Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (tPass.length() < 6) {
                Toast.makeText(this,
                        "Password must be at least 6 characters",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Create account using Firebase Authentication
            mAuth.createUserWithEmailAndPassword(tEmail, tPass)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            String uid = mAuth.getCurrentUser().getUid();

                            // Create teacher data map
                            HashMap<String, Object> teacherMap = new HashMap<>();
                            teacherMap.put("name", tName);
                            teacherMap.put("teacherId", tId);
                            teacherMap.put("email", tEmail);
                            teacherMap.put("role", "teacher");

                            // Save teacher details in Realtime Database
                            teacherRef.child(uid).setValue(teacherMap)
                                    .addOnSuccessListener(unused -> {

                                        // Sign out so user must login manually
                                        FirebaseAuth.getInstance().signOut();

                                        Toast.makeText(this,
                                                "Registered successfully! Please login.",
                                                Toast.LENGTH_LONG).show();

                                        Intent intent = new Intent(
                                                TeacherSignUpActivity.this,
                                                Login.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this,
                                                    "Database Error: " + e.getMessage(),
                                                    Toast.LENGTH_LONG).show());

                        } else {
                            Toast.makeText(this,
                                    "Auth Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
        txtLogin.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherSignUpActivity.this, Login.class);
            startActivity(intent);
        });
    }
}