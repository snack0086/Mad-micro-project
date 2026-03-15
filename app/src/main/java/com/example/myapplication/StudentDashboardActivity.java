package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class StudentDashboardActivity extends BaseActivity {

    TextView tvWelcome;
    String userRole;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // VERY IMPORTANT – set layout first
        setContentView(R.layout.activity_student_dashboard);
        userRole="student";
        setupDrawer(R.id.nav_dashboard);
        Log.d("StudentDashboard", "onCreate started");

        tvWelcome = findViewById(R.id.tvWelcome);

        // Get data from intent
        Intent intent = getIntent();
        String studentId = intent.getStringExtra("studentId");

        if (studentId != null && !studentId.isEmpty()) {
            tvWelcome.setText("Welcome Student\nID: " + studentId);
            Log.d("StudentDashboard", "Student ID received: " + studentId);
        } else {
            tvWelcome.setText("Welcome Student");
            Log.e("StudentDashboard", "Student ID is NULL");

            Toast.makeText(
                    this,
                    "Student ID not found!",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}
