package com.example.myapplication;

import com.google.firebase.auth.FirebaseAuth;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class StudentSignUpActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    EditText name, id, email, password;
    Button signup;
    DatabaseReference studentRef;
    TextView txtLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_signup);
        mAuth = FirebaseAuth.getInstance();
        // Initialize views
        name = findViewById(R.id.etStudentName);
        id = findViewById(R.id.etStudentId);
        email = findViewById(R.id.etStudentEmail);
        password = findViewById(R.id.etStudentPassword);
        signup = findViewById(R.id.btnStudentSignup);
        txtLogin=findViewById(R.id.txtLogin);
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


                // Save data to Firebase
                mAuth.createUserWithEmailAndPassword(sEmail, sPass)
                        .addOnCompleteListener(task -> {

                            if (task.isSuccessful()) {

                                String userId = mAuth.getCurrentUser().getUid();

                                HashMap<String, Object> studentMap = new HashMap<>();
                                studentMap.put("name", sName);
                                studentMap.put("studentId", sId);
                                studentMap.put("email", sEmail);
                                studentMap.put("attendance", "0%");
                                studentMap.put("role", "student");

                                studentRef.child(userId).setValue(studentMap)
                                        .addOnSuccessListener(unused -> {

                                            Toast.makeText(StudentSignUpActivity.this,
                                                    "Student successfully registered",
                                                    Toast.LENGTH_LONG).show();

                                            Intent intent = new Intent(
                                                    StudentSignUpActivity.this,
                                                    StudentDashboardActivity.class);

                                            intent.putExtra("studentId", sId);
                                            startActivity(intent);
                                            finish();
                                        });

                            } else {
                                Toast.makeText(StudentSignUpActivity.this,
                                        "Error: " + task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
        txtLogin.setOnClickListener(v -> {
            Intent intent = new Intent(StudentSignUpActivity.this, Login.class);
            startActivity(intent);
        });
    }
}
