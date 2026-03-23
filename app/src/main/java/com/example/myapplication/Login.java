package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

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
        tvSignUp   = findViewById(R.id.tvSignUp);
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
        btnLogin.setOnClickListener(v -> {

            String email    = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            String uid = mAuth.getCurrentUser().getUid();

                            // Get user data
                            rootRef.child(uid).get()
                                    .addOnCompleteListener(userTask -> {

                                        if (userTask.isSuccessful()
                                                && userTask.getResult().exists()) {

                                            String role = userTask.getResult()
                                                    .child("role")
                                                    .getValue(String.class);

                                            saveSession(uid, role);
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

    private void saveSession(String uid, String role) {

        SharedPreferences.Editor editor =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();

        editor.putBoolean(KEY_IS_LOGGED, true);
        editor.putString(KEY_UID, uid);
        editor.putString(KEY_ROLE, role);
        editor.apply();
    }

    private void navigateToDashboard(String role) {

        Intent intent;

        if ("teacher".equals(role)) {
            intent = new Intent(this, Teacher_dashboard.class);
        } else if ("student".equals(role)) {
            intent = new Intent(this, StudentDashboardActivity.class);
        } else {
            // Role is null or unrecognised — don't silently enter any dashboard
            Toast.makeText(this,
                    "Account role not found. Please contact support.",
                    Toast.LENGTH_LONG).show();
            FirebaseAuth.getInstance().signOut();
            return;
        }

        startActivity(intent);
        finish();
    }
}