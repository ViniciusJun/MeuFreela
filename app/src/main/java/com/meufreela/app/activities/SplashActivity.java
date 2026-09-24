package com.meufreela.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.meufreela.app.storage.TokenManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            TokenManager tm = new TokenManager(this);
            Intent intent;

            if (tm.isLogado()) {
                intent = "FREELANCER".equals(tm.getUserType())
                        ? new Intent(this, MainFreelancerActivity.class)
                        : new Intent(this, MainClienteActivity.class);
            } else {
                intent = new Intent(this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, 1500); // 1.5s de splash
    }
}