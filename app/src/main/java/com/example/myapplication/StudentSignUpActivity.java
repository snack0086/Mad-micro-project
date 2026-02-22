package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class StudentSignUpActivity extends AppCompatActivity {

    EditText name, id, email, password;
    Button signup;
    DatabaseReference studentRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_signup);

        // Initialize views
        name = findViewById(R.id.etStudentName);
        id = findViewById(R.id.etStudentId);
        email = findViewById(R.id.etStudentEmail);
        password = findViewById(R.id.etStudentPassword);
        signup = findViewById(R.id.btnStudentSignup);

        // Firebase reference
        studentRef = FirebaseDatabase.getInstance()
                .getReference("CampusConnect")
                .child("Students");

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String sName = name.getText().toString().trim();
                String sId = id.getText().toString().trim();
                String sEmail = email.getText().toString().trim();
                String sPass = password.getText().toString().trim();

                // Empty field checks
                if (sName.isEmpty()) {
                    Toast.makeText(StudentSignUpActivity.this,
                            "Name is required", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (sId.isEmpty()) {
                    Toast.makeText(StudentSignUpActivity.this,
                            "Student ID is required", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (sEmail.isEmpty()) {
                    Toast.makeText(StudentSignUpActivity.this,
                            "Email is required", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (sPass.isEmpty()) {
                    Toast.makeText(StudentSignUpActivity.this,
                            "Password is required", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Password length check
                if (sPass.length() < 6) {
                    Toast.makeText(StudentSignUpActivity.this,
                            "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create a map to store student data
                HashMap<String, Object> studentMap = new HashMap<>();
                studentMap.put("name", sName);
                studentMap.put("studentId", sId);
                studentMap.put("email", sEmail);
                studentMap.put("password", sPass);
                studentMap.put("attendance", "0%"); // Optional: initialize attendance

                // Save data to Firebase
                studentRef.child(sId).setValue(studentMap)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(StudentSignUpActivity.this,
                                    "Student successfully registered", Toast.LENGTH_LONG).show();

                            // Clear input fields
                            name.setText("");
                            id.setText("");
                            email.setText("");
                            password.setText("");

                            // Redirect to Student Dashboard
                            Intent intent = new Intent(StudentSignUpActivity.this, StudentDashboardActivity.class);
                            intent.putExtra("studentId", sId);
                            startActivity(intent);
                            finish();

                        })
                        .addOnFailureListener(e -> Toast.makeText(StudentSignUpActivity.this,
                                "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }
}
