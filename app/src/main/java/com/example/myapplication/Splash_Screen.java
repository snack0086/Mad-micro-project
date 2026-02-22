package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class Splash_Screen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {

            SharedPreferences prefs = getSharedPreferences("CampusConnectPrefs", MODE_PRIVATE);
            boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
            String role = prefs.getString("role", "");

            if (isLoggedIn) {
                if (role.equals("teacher")) {
                    startActivity(new Intent(Splash_Screen.this, Teacher_dashboard.class));
                } else if (role.equals("student")) {
                    startActivity(new Intent(Splash_Screen.this, StudentDashboardActivity.class));
                } else {
                    startActivity(new Intent(Splash_Screen.this, Login.class));
                }
            } else {
                startActivity(new Intent(Splash_Screen.this, Login.class));
            }

            finish();

        }, 2000);
    }
}