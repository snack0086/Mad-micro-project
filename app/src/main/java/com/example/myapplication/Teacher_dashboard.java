package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class Teacher_dashboard extends BaseActivity {

    // UI Components
    private TextView teacherGreeting;
    private TextView teacherRole;
    private MaterialButton btnPostAnnouncement;
    private MaterialButton btnStartAttendance;
    private TextView className;
    private TextView classTime;
    private TextView classRoom;
    private TextView studentCount;
    private TextView tvPapersCount;
    private TextView tvMessagesCount;
    private TextView tvAvgGrade;
    private TextView tvViewSchedule;
    private ImageView notificationIcon;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_teacher_dashboard);
        userRole = "teacher";
        setupDrawer(R.id.nav_dashboard);
        // Initialize views
        initializeViews();

        // Set up click listeners
        setupClickListeners();

        // Load initial data
        loadDashboardData();
    }

    private void initializeViews() {
        // Header section
        teacherGreeting = findViewById(R.id.teacherGreeting);
        teacherRole = findViewById(R.id.teacherRole);
        notificationIcon = findViewById(R.id.notificationIcon);

        // Buttons
        btnPostAnnouncement = findViewById(R.id.btnPostAnnouncement);
        btnStartAttendance = findViewById(R.id.btnStartAttendance);

        // Class details
        className = findViewById(R.id.className);
        classTime = findViewById(R.id.classTime);
        classRoom = findViewById(R.id.classRoom);
        studentCount = findViewById(R.id.studentCount);

        // Quick stats
        tvPapersCount = findViewById(R.id.tvPapersCount);
        tvMessagesCount = findViewById(R.id.tvMessagesCount);
        tvAvgGrade = findViewById(R.id.tvAvgGrade);

        // Other elements
        tvViewSchedule = findViewById(R.id.tvViewSchedule);
    }

    private void setupClickListeners() {
        // Post Announcement button
        btnPostAnnouncement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePostAnnouncement();
            }
        });

        // Start Attendance button
        btnStartAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleStartAttendance();
            }
        });

        // View Schedule link
        tvViewSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleViewSchedule();
            }
        });

        // Notification icon
        notificationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleNotifications();
            }
        });
    }

    private void loadDashboardData() {
        // In a real app, this would fetch data from a database or API
        // Get user info from login intent
        String userName = getIntent().getStringExtra("USER_NAME");
        String userEmail = getIntent().getStringExtra("USER_EMAIL");
        
        // Set teacher greeting with user name
        if (userName != null && !userName.isEmpty()) {
            teacherGreeting.setText("Hello, " + userName);
        } else {
            teacherGreeting.setText("Hello, Teacher");
        }
        
        teacherRole.setText("Main Teacher");

        // Up Next class
        className.setText("Physics 101");
        classTime.setText("10:00 AM - 11:30 AM");
        classRoom.setText("Room 302");
        studentCount.setText("45 Students");

        // Quick stats
        tvPapersCount.setText("45");
        tvMessagesCount.setText("2");
        tvAvgGrade.setText("98%");
    }

    private void handlePostAnnouncement() {
        // Open announcement creation screen
        Toast.makeText(this, "Opening Post Announcement", Toast.LENGTH_SHORT).show();
        
        // In a real app, you would start a new activity or show a dialog:
        // Intent intent = new Intent(this, PostAnnouncementActivity.class);
        // startActivity(intent);
    }

    private void handleStartAttendance() {
        // Open attendance tracking screen
        Toast.makeText(this, "Starting Attendance for Physics 101", Toast.LENGTH_SHORT).show();
        
        // In a real app, you would start the attendance activity:
        // Intent intent = new Intent(this, AttendanceActivity.class);
        // intent.putExtra("CLASS_NAME", "Physics 101");
        // startActivity(intent);
    }

    private void handleViewSchedule() {
        // Open full schedule view
        Toast.makeText(this, "Opening Schedule", Toast.LENGTH_SHORT).show();
        
        // In a real app, you would start the schedule activity:
        // Intent intent = new Intent(this, ScheduleActivity.class);
        // startActivity(intent);
    }

    private void handleNotifications() {
        // Open notifications screen
        Toast.makeText(this, "Opening Notifications", Toast.LENGTH_SHORT).show();
        
        // In a real app, you would start the notifications activity:
        // Intent intent = new Intent(this, NotificationsActivity.class);
        // startActivity(intent);
    }

    // Helper method to format time
    private String formatTime(int hour, int minute) {
        String amPm = hour >= 12 ? "PM" : "AM";
        int displayHour = hour > 12 ? hour - 12 : hour;
        if (displayHour == 0) displayHour = 12;
        return String.format("%d:%02d %s", displayHour, minute, amPm);
    }

    // Data model classes (in a real app, these would be in separate files)
    public static class ClassInfo {
        String name;
        String time;
        String room;
        int studentCount;

        public ClassInfo(String name, String time, String room, int studentCount) {
            this.name = name;
            this.time = time;
            this.room = room;
            this.studentCount = studentCount;
        }
    }

    public static class DashboardStats {
        int papersToGrade;
        int newMessages;
        double avgGrade;

        public DashboardStats(int papersToGrade, int newMessages, double avgGrade) {
            this.papersToGrade = papersToGrade;
            this.newMessages = newMessages;
            this.avgGrade = avgGrade;
        }
    }
}
