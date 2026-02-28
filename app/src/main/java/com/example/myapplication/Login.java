package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
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

    public static final String PREFS_NAME    = "CampusConnectPrefs";
    public static final String KEY_ROLE      = "userRole";
    public static final String KEY_UID       = "userUid";
    public static final String KEY_IS_LOGGED = "isLoggedIn";

    FirebaseAuth mAuth;
    DatabaseReference rootRef;

    EditText edtUsername, edtPassword;
    AppCompatButton btnLogin;
    TextView txtSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Auto-login: if already logged in, go directly to dashboard
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (prefs.getBoolean(KEY_IS_LOGGED, false)) {
            String role = prefs.getString(KEY_ROLE, "");
            navigateToDashboard(role);
            return;
        }

        setContentView(R.layout.activity_login);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin    = findViewById(R.id.btnLogin);
        txtSignup   = findViewById(R.id.txtSignup);

        mAuth   = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference("CampusConnect");

        btnLogin.setOnClickListener(v -> {

            String email    = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            String uid = mAuth.getCurrentUser().getUid();

                            // Check if Student
                            rootRef.child("Students").child(uid).get()
                                    .addOnCompleteListener(studentTask -> {

                                        if (studentTask.isSuccessful()
                                                && studentTask.getResult().exists()) {

                                            saveSession(uid, "student");
                                            navigateToDashboard("student");

                                        } else {

                                            // Check if Teacher
                                            rootRef.child("Teachers").child(uid).get()
                                                    .addOnCompleteListener(teacherTask -> {

                                                        if (teacherTask.isSuccessful()
                                                                && teacherTask.getResult().exists()) {

                                                            saveSession(uid, "teacher");
                                                            navigateToDashboard("teacher");

                                                        } else {
                                                            Toast.makeText(this,
                                                                    "User record not found. Contact admin.",
                                                                    Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                        }
                                    });

                        } else {
                            Toast.makeText(this,
                                    "Login Failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

        txtSignup.setOnClickListener(v ->
                startActivity(new Intent(this, Signup_main.class)));
    }

    private void saveSession(String uid, String role) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(KEY_IS_LOGGED, true);
        editor.putString(KEY_UID, uid);
        editor.putString(KEY_ROLE, role);
        editor.apply();
    }

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
