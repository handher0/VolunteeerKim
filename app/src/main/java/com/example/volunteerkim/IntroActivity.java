package com.example.volunteerkim;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_intro);

        findViewById(R.id.loginBtn).setOnClickListener(v -> navigateTo(LoginActivity.class));
        findViewById(R.id.joinBtn).setOnClickListener(v -> navigateTo(SignupActivity.class));
    }

    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        startActivity(intent);
    }
}
