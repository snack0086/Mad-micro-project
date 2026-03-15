package com.example.myapplication;

import android.content.Intent;

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

    protected void setupDrawer(int checkedItemId) {
        userRole = getSharedPreferences(Login.PREFS_NAME, MODE_PRIVATE)
                .getString("role", "");
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);


        ActionBarDrawerToggle toggle;
        Toast.makeText(this, "Role: " + userRole, Toast.LENGTH_SHORT).show();
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

        navigationView.setCheckedItem(checkedItemId);

        navigationView.setNavigationItemSelectedListener(item -> {

            int id = item.getItemId();
    
            if (id == R.id.nav_dashboard) {

                if ("teacher".equals(userRole)) {
                    startActivity(new Intent(this, Teacher_dashboard.class));
                } else if ("student".equals(userRole)) {
                    startActivity(new Intent(this, StudentDashboardActivity.class));
                }

            } else if (id == R.id.nav_announcements) {
                startActivity(new Intent(this, AnnouncementActivity.class));
            }

            else if (id == R.id.nav_upload_material) {
                startActivity(new Intent(this, LearningMaterialActivity.class));
            }

            else if (id == R.id.nav_manage_assignments) {
                startActivity(new Intent(this, AssignmentListActivity.class));
            }

            else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
            }

            else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                getSharedPreferences(Login.PREFS_NAME, MODE_PRIVATE).edit().clear().apply();
                Intent intent = new Intent(this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            drawerLayout.closeDrawers();
            return true;
        });
    }
}