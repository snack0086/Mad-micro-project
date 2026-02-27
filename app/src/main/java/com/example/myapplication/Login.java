package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Login extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference rootRef;

    EditText edtUsername, edtPassword;
    AppCompatButton btnLogin;
    TextView txtSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtSignup = findViewById(R.id.txtSignup);

        mAuth = FirebaseAuth.getInstance();

        rootRef = FirebaseDatabase.getInstance()
                .getReference("CampusConnect");

        btnLogin.setOnClickListener(v -> {

            String email = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(Login.this,
                        "Please fill all fields",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // 🔥 Firebase Authentication Login
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            String uid = mAuth.getCurrentUser().getUid();

                            // 🔎 Check if Student
                            rootRef.child("Students").child(uid).get()
                                    .addOnCompleteListener(studentTask -> {

                                        if (studentTask.isSuccessful()
                                                && studentTask.getResult().exists()) {

                                            startActivity(new Intent(
                                                    Login.this,
                                                    StudentDashboardActivity.class));
                                            finish();

                                        } else {

                                            // 🔎 Check if Teacher
                                            rootRef.child("Teachers").child(uid).get()
                                                    .addOnCompleteListener(teacherTask -> {

                                                        if (teacherTask.isSuccessful()
                                                                && teacherTask.getResult().exists()) {

                                                            startActivity(new Intent(
                                                                    Login.this,
                                                                    Teacher_dashboard.class));
                                                            finish();

                                                        }
                                                    });
                                        }
                                    });

                        } else {

                            Toast.makeText(Login.this,
                                    "Login Failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

        txtSignup.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, Signup_main.class);
            startActivity(intent);
        });
    }
}