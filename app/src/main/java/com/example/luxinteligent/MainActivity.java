package com.example.luxinteligent;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    // Componentes UI
    Switch swSala, swCocina, swDormitorio;
    TextView statusText;
    LinearLayout btnRutinas;

    // Bluetooth
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // UUID estándar HC-05

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Recibir dirección MAC
        Intent newint = getIntent();
        address = newint.getStringExtra("EXTRA_ADDRESS");

        // UI Init
        swSala = findViewById(R.id.sw_sala);
        swCocina = findViewById(R.id.sw_cocina);
        swDormitorio = findViewById(R.id.sw_dormitorio);
        statusText = findViewById(R.id.txt_status_bt);
        btnRutinas = findViewById(R.id.card_rutinas);

        // Iniciar conexión
        new ConnectBT().execute();

        // Listeners para Interruptores
        // Protocolo: A=Sala On, a=Sala Off, B=Cocina On, etc.
        swSala.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) sendSignal("A"); else sendSignal("a");
        });

        swCocina.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) sendSignal("B"); else sendSignal("b");
        });

        swDormitorio.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) sendSignal("C"); else sendSignal("c");
        });

        // Ir a Rutinas
        btnRutinas.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, RoutineActivity.class);
            startActivity(i);
        });
    }

    // Método para enviar caracteres al Arduino
    private void sendSignal(String s) {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write(s.toString().getBytes());
            } catch (IOException e) {
                Toast.makeText(this, "Error enviando señal", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Desconectar al cerrar
    private void Disconnect() {
        if (btSocket != null) {
            try {
                btSocket.close();
            } catch (IOException e) {
                Toast.makeText(this, "Error al cerrar socket", Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    // Clase asíncrona para conexión Bluetooth (evita congelar la UI)
    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(MainActivity.this, "Conectando...", "Por favor espere");
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (!ConnectSuccess) {
                Toast.makeText(getApplicationContext(), "Conexión Fallida. Revisa el HC-05", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Conectado", Toast.LENGTH_LONG).show();
                isBtConnected = true;
                statusText.setText("Conectado a HC-05");
                statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            }
            progress.dismiss();
        }
    }
}