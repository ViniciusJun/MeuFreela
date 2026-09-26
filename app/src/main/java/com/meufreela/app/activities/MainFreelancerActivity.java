package com.meufreela.app.activities;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainFreelancerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("Painel do Freelancer — em construção 🚧");
        tv.setTextSize(18);
        tv.setPadding(40, 80, 40, 40);
        setContentView(tv);
    }
}