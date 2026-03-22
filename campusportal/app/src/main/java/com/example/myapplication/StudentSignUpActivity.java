package com.example.myapplication;

import com.google.firebase.auth.FirebaseAuth;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class StudentSignUpActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    EditText name, id, email, password;
    Button signup;
    DatabaseReference userRef;
    TextView txtLogin;
    ImageView imgTogglePassword;
    boolean isPasswordVisible = false;
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
        txtLogin = findViewById(R.id.txtLogin);
        imgTogglePassword = findViewById(R.id.imgTogglePassword);
        // Toggle password visibility
        imgTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                password.setInputType(
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                imgTogglePassword.setImageResource(R.drawable.login2_eye_visible);
            } else {
                password.setInputType(
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                imgTogglePassword.setImageResource(R.drawable.login2_eye_invisible);
            }
            isPasswordVisible = !isPasswordVisible;
            password.setSelection(password.getText().length());
        });
        // Firebase reference (UPDATED → Users node)
        userRef = FirebaseDatabase.getInstance()
                .getReference("CampusConnect")
                .child("Users");

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

                if (sPass.length() < 6) {
                    Toast.makeText(StudentSignUpActivity.this,
                            "Password must be at least 6 characters",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Firebase Authentication
                mAuth.createUserWithEmailAndPassword(sEmail, sPass)
                        .addOnCompleteListener(task -> {

                            if (task.isSuccessful()) {

                                String userId = mAuth.getCurrentUser().getUid();

                                HashMap<String, Object> studentMap = new HashMap<>();
                                studentMap.put("name", sName);
                                studentMap.put("studentId", sId);
                                studentMap.put("email", sEmail);
                                studentMap.put("role", "student");

                                userRef.child(userId).setValue(studentMap)
                                        .addOnSuccessListener(unused -> {

                                            FirebaseAuth.getInstance().signOut();

                                            Toast.makeText(StudentSignUpActivity.this,
                                                    "Registered successfully! Please login.",
                                                    Toast.LENGTH_LONG).show();

                                            Intent intent = new Intent(
                                                    StudentSignUpActivity.this,
                                                    Login.class);

                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

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