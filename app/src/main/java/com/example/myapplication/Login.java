package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class Login extends AppCompatActivity {

    EditText edtUsername, edtPassword;
    AppCompatButton btnLogin;
    TextView txtForgot, txtSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Linking XML with Java
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtForgot = findViewById(R.id.txtForgot);
        txtSignup = findViewById(R.id.txtSignup);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = edtUsername.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(Login.this,
                            "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (username.equals("teacher123") && password.equals("teacher")) {

                    getSharedPreferences("CampusConnectPrefs", MODE_PRIVATE)
                            .edit()
                            .putBoolean("isLoggedIn", true)
                            .putString("role", "teacher")
                            .apply();

                    Intent i = new Intent(Login.this, Teacher_dashboard.class);
                    startActivity(i);
                }

                else if (username.equals("student123369") && password.equals("std123")) {

                    getSharedPreferences("CampusConnectPrefs", MODE_PRIVATE)
                            .edit()
                            .putBoolean("isLoggedIn", true)
                            .putString("role", "student")
                            .apply();

                    Intent i = new Intent(Login.this, StudentDashboardActivity.class);
                    startActivity(i);
                }

                // Invalid credentials
                else {
                    Toast.makeText(Login.this,
                            "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
