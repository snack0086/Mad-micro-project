package com.example.myapplication;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Login extends AppCompatActivity {

    public static final String PREFS_NAME    = "CampusConnectPrefs";
    public static final String KEY_ROLE      = "userRole";
    public static final String KEY_UID       = "userUid";
    public static final String KEY_IS_LOGGED = "isLoggedIn";

    FirebaseAuth mAuth;
    DatabaseReference rootRef;
    EditText etUsername, etPassword;
    AppCompatButton btnLogin;
    TextView tvSignUp;
    ImageView imgTogglePassword;
    boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Ask notification permission (Android 13+)
        requestNotificationPermission();

        // Auto-login
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (prefs.getBoolean(KEY_IS_LOGGED, false)) {
            String role = prefs.getString(KEY_ROLE, "");
            navigateToDashboard(role);
            return;
        }

        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        imgTogglePassword = findViewById(R.id.imgTogglePassword);
        tvSignUp = findViewById(R.id.tvSignUp);

        mAuth = FirebaseAuth.getInstance();

        rootRef = FirebaseDatabase.getInstance()
                .getReference("CampusConnect")
                .child("Users");

        // Toggle password visibility
        imgTogglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                etPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                imgTogglePassword.setImageResource(R.drawable.login2_eye_visible);
            } else {
                etPassword.setInputType(
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                imgTogglePassword.setImageResource(R.drawable.login2_eye_invisible);
            }
            isPasswordVisible = !isPasswordVisible;
            etPassword.setSelection(etPassword.getText().length());
        });

        // LOGIN BUTTON
        btnLogin.setOnClickListener(v -> {

            String email = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            String uid = mAuth.getCurrentUser().getUid();

                            rootRef.child(uid).get()
                                    .addOnCompleteListener(userTask -> {

                                        if (userTask.isSuccessful()
                                                && userTask.getResult().exists()) {

                                            String role = userTask.getResult()
                                                    .child("role")
                                                    .getValue(String.class);

                                            // ✅ SAVE SESSION
                                            saveSession(uid, role);

                                            // ✅ SHOW NOTIFICATION
                                            showLoginNotification(email);

                                            // ✅ NAVIGATE
                                            navigateToDashboard(role);

                                        } else {
                                            Toast.makeText(this,
                                                    "User record not found",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });

                        } else {
                            Toast.makeText(this,
                                    "Login Failed: "
                                            + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

        tvSignUp.setOnClickListener(v ->
                startActivity(new Intent(this, Signup_main.class)));
    }

    // ================= NOTIFICATION =================

    private void showLoginNotification(String email) {

        String channelId = "login_channel";

        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Create channel (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Login Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }

        String username = email.split("@")[0];

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("Login Successful 🎉")
                        .setContentText("Welcome back, " + username)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        manager.notify(1, builder.build());
    }

    // ================= PERMISSION =================

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    // ================= SESSION =================

    private void saveSession(String uid, String role) {
        SharedPreferences.Editor editor =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();

        editor.putBoolean(KEY_IS_LOGGED, true);
        editor.putString(KEY_UID, uid);
        editor.putString(KEY_ROLE, role);
        editor.apply();
    }

    // ================= NAVIGATION =================

    private void navigateToDashboard(String role) {

        Intent intent;

        if ("teacher".equals(role)) {
            intent = new Intent(this, Teacher_dashboard.class);
        } else {
            intent = new Intent(this, StudentDashboardActivity.class);
        }

        startActivity(intent);
        finish();
    }
}