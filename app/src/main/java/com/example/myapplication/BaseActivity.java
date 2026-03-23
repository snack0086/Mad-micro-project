package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import android.widget.Toast;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public abstract class BaseActivity extends AppCompatActivity {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected Toolbar toolbar;
    protected String userRole;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get user role early using the correct key from Login class
        userRole = getSharedPreferences(Login.PREFS_NAME, MODE_PRIVATE)
                .getString(Login.KEY_ROLE, "");
    }

    protected void setupDrawer(int checkedItemId) {
        // Initialize views
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);

        // Check if views exist before proceeding
        if (drawerLayout == null || navigationView == null) {
            // Log error or handle gracefully
            return;
        }

        // Setup ActionBarDrawerToggle
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toggle = new ActionBarDrawerToggle(
                    this,
                    drawerLayout,
                    toolbar,
                    R.string.open_drawer,
                    R.string.close_drawer);
        } else {
            toggle = new ActionBarDrawerToggle(
                    this,
                    drawerLayout,
                    R.string.open_drawer,
                    R.string.close_drawer);
        }

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set checked item if valid
        if (checkedItemId != 0) {
            navigationView.setCheckedItem(checkedItemId);
        }

        // Set navigation item selection listener
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            handleNavigationItemClick(id);
            drawerLayout.closeDrawers();
            return true;
        });
    }

    private void handleNavigationItemClick(int itemId) {
        // Dashboard navigation
        if (itemId == R.id.nav_dashboard) {
            navigateToDashboard();
        }
        // Announcements
        else if (itemId == R.id.nav_announcements) {
            startActivity(new Intent(this, AnnouncementActivity.class));
        }
        // Upload Material
        else if (itemId == R.id.nav_upload_material) {
            startActivity(new Intent(this, LearningMaterialActivity.class));
        }
        // Manage Assignments
        else if (itemId == R.id.nav_manage_assignments) {
            startActivity(new Intent(this, AssignmentListActivity.class));
        }
        // Profile
        else if (itemId == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        }
        // Logout
        else if (itemId == R.id.nav_logout) {
            performLogout();
        }
    }

    private void navigateToDashboard() {
        Intent intent = null;

        if ("teacher".equalsIgnoreCase(userRole)) {
            intent = new Intent(this, Teacher_dashboard.class);
        } else if ("student".equalsIgnoreCase(userRole)) {
            intent = new Intent(this, StudentDashboardActivity.class);
        } else {
            // Default to login if role not recognized
            Toast.makeText(this, "Role not recognized. Please login again.", Toast.LENGTH_LONG).show();
            performLogout();
            return;
        }

        if (intent != null) {
            // Prevent creating multiple instances of dashboard
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
    }

    private void performLogout() {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut();

        // Clear shared preferences
        getSharedPreferences(Login.PREFS_NAME, MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        // Navigate to login screen
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Helper method to update user role if needed
    protected void updateUserRole(String newRole) {
        if (newRole != null && !newRole.isEmpty()) {
            this.userRole = newRole;
            // Optionally update in preferences
            getSharedPreferences(Login.PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putString("role", newRole)
                    .apply();
        }
    }

    // Override onOptionsItemSelected if you want to handle home button press
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (toggle != null && toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}