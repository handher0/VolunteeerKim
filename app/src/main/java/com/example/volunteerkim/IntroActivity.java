package com.example.volunteerkim;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_intro);

        // 버튼 연결
        Button loginButton = findViewById(R.id.loginBtn);
        Button joinButton = findViewById(R.id.joinBtn);

        // 로그인 버튼 클릭 이벤트
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 로그인 페이지로 이동
                Intent intent = new Intent(IntroActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        // 회원가입 버튼 클릭 이벤트
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 회원가입 페이지로 이동
                Intent intent = new Intent(IntroActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }
}
