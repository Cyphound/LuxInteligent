package com.example.luxinteligent;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Simular carga de recursos (2 segundos)
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, DeviceListActivity.class);
            startActivity(intent);
            finish();
        }, 2000);
    }
}