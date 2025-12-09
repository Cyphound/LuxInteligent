package com.example.luxinteligent;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RoutineActivity extends AppCompatActivity {

    Spinner spinnerDisp;
    CheckBox chkRepetir;
    Button btnGuardar;
    TimePicker timePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine);

        spinnerDisp = findViewById(R.id.spinner_dispositivos);
        chkRepetir = findViewById(R.id.chk_repetir);
        btnGuardar = findViewById(R.id.btn_guardar);
        timePicker = findViewById(R.id.timePicker);

        // Configurar Spinner
        String[] dispositivos = {"Sala de Estar", "Cocina", "Dormitorio", "Todas las Luces"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dispositivos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDisp.setAdapter(adapter);

        // Botón Guardar
        btnGuardar.setOnClickListener(v -> {
            String seleccionado = spinnerDisp.getSelectedItem().toString();
            boolean esRepetitivo = chkRepetir.isChecked();

            // Obtener Hora y Minutos
            int hora = timePicker.getHour();
            int minuto = timePicker.getMinute();
            String tiempo = String.format("%02d:%02d", hora, minuto);

            String mensaje = "Rutina: " + seleccionado + " a las " + tiempo;
            if (esRepetitivo) mensaje += " (Diaria)";

            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
            finish();
        });
    }
}