package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class TeacherSignUpActivity extends AppCompatActivity {

    EditText name, id, email, password;
    Button signup;
    DatabaseReference teacherRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_signup);

        name = findViewById(R.id.etTeacherName);
        id = findViewById(R.id.etTeacherId);
        email = findViewById(R.id.etTeacherEmail);
        password = findViewById(R.id.etTeacherPassword);
        signup = findViewById(R.id.btnTeacherSignup);

        teacherRef = FirebaseDatabase.getInstance()
                .getReference("CampusConnect")
                .child("Teachers");

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // ✅ CONFIRM BUTTON CLICK
                Toast.makeText(TeacherSignUpActivity.this,
                        "Sign Up clicked",
                        Toast.LENGTH_SHORT).show();

                String tName = name.getText().toString().trim();
                String tId = id.getText().toString().trim();
                String tEmail = email.getText().toString().trim();
                String tPass = password.getText().toString().trim();

                if (tName.isEmpty()) {
                    Toast.makeText(TeacherSignUpActivity.this,
                            "Name is required",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (tId.isEmpty()) {
                    Toast.makeText(TeacherSignUpActivity.this,
                            "Teacher ID is required",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (tEmail.isEmpty()) {
                    Toast.makeText(TeacherSignUpActivity.this,
                            "Email is required",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (tPass.isEmpty()) {
                    Toast.makeText(TeacherSignUpActivity.this,
                            "Password is required",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (tPass.length() < 6) {
                    Toast.makeText(TeacherSignUpActivity.this,
                            "Password must be at least 6 characters",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                HashMap<String, Object> teacherMap = new HashMap<>();
                teacherMap.put("name", tName);
                teacherMap.put("teacherId", tId);
                teacherMap.put("email", tEmail);
                teacherMap.put("password", tPass);

                teacherRef.child(tId).setValue(teacherMap)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(TeacherSignUpActivity.this,
                                    "Teacher successfully registered",
                                    Toast.LENGTH_LONG).show();

                            name.setText("");
                            id.setText("");
                            email.setText("");
                            password.setText("");
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(TeacherSignUpActivity.this,
                                        "Error: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show());
            }
        });
    }
}
