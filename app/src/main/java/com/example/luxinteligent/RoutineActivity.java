package com.example.luxinteligent;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RoutineActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine);

        ImageView btnBack = findViewById(R.id.btn_back);
        Button btnSave = findViewById(R.id.btn_save_routine);

        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            // Aquí iría la lógica para guardar en base de datos o enviar comando programado al Arduino
            Toast.makeText(this, "Rutina guardada exitosamente", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}