package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Signup_main extends AppCompatActivity {

    Button studentBtn, teacherBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        studentBtn = findViewById(R.id.btnStudent);
        teacherBtn = findViewById(R.id.btnTeacher);

        studentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Signup_main.this, StudentSignUpActivity.class);
                startActivity(intent);
            }
        });

        teacherBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Signup_main.this, TeacherSignUpActivity.class);
                startActivity(intent);
            }
        });
    }
}
