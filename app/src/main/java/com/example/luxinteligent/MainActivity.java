package com.example.luxinteligent;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // IMPORTANTE
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Switch swSala, swCocina, swDormitorio, swAutoMode;
    TextView statusText;
    LinearLayout btnRutinas;

    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private ConnectedThread mConnectedThread;
    Handler bluetoothIn;
    final int handlerState = 0;
    private StringBuilder recDataString = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. ACTIVAR TOOLBAR (Para ver los 3 puntos)
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        Intent newint = getIntent();
        address = newint.getStringExtra("EXTRA_ADDRESS");

        swSala = findViewById(R.id.sw_sala);
        swCocina = findViewById(R.id.sw_cocina);
        swDormitorio = findViewById(R.id.sw_dormitorio);
        swAutoMode = findViewById(R.id.sw_auto_mode);
        statusText = findViewById(R.id.txt_status_bt);
        btnRutinas = findViewById(R.id.card_rutinas);

        // ---------------------------------------------------------
        // LOGICA DE RECEPCIÓN MEJORADA (Bucle While)
        // ---------------------------------------------------------
        bluetoothIn = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message msg) {
                if (msg.what == handlerState) {
                    String readMessage = (String) msg.obj;
                    recDataString.append(readMessage);

                    // Procesar TODOS los mensajes acumulados, no solo el primero
                    int endOfLineIndex = recDataString.indexOf("\n");
                    while (endOfLineIndex > 0) {
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);
                        recDataString.delete(0, endOfLineIndex + 1);

                        // Limpiamos espacios extra por si acaso
                        dataInPrint = dataInPrint.trim();

                        if (dataInPrint.equals("SALA:ON")) swSala.setChecked(true);
                        else if (dataInPrint.equals("SALA:OFF")) swSala.setChecked(false);

                        else if (dataInPrint.equals("COCINA:ON")) swCocina.setChecked(true);
                        else if (dataInPrint.equals("COCINA:OFF")) swCocina.setChecked(false);

                        else if (dataInPrint.equals("DORM:ON")) swDormitorio.setChecked(true);
                        else if (dataInPrint.equals("DORM:OFF")) swDormitorio.setChecked(false);

                        // Buscamos si hay otro mensaje en la cola
                        endOfLineIndex = recDataString.indexOf("\n");
                    }
                }
            }
        };

        new ConnectBT().execute();

        swSala.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) sendSignal(isChecked ? "A" : "a");
        });
        swCocina.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) sendSignal(isChecked ? "B" : "b");
        });
        swDormitorio.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) sendSignal(isChecked ? "C" : "c");
        });
        swAutoMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) sendSignal(isChecked ? "S" : "s");
        });

        btnRutinas.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, RoutineActivity.class);
            startActivity(i);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Desconectar");
        menu.add(0, 2, 0, "Ayuda");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 1) {
            Disconnect();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendSignal(String s) {
        if (btSocket != null) {
            try { btSocket.getOutputStream().write(s.getBytes()); }
            catch (IOException e) { }
        }
    }

    private void Disconnect() {
        if (btSocket != null) {
            try { btSocket.close(); } catch (IOException e) { }
        }
        finish();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;
        @Override
        protected void onPreExecute() { progress = ProgressDialog.show(MainActivity.this, "Conectando HC-06...", "Espere..."); }
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
            } catch (IOException e) { ConnectSuccess = false; }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (!ConnectSuccess) {
                Toast.makeText(getApplicationContext(), "Error de Conexión", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                isBtConnected = true;
                statusText.setText("Conectado a HC-06");
                mConnectedThread = new ConnectedThread(btSocket);
                mConnectedThread.start();
            }
            progress.dismiss();
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            try { tmpIn = socket.getInputStream(); } catch (IOException e) { }
            mmInStream = tmpIn;
        }
        public void run() {
            byte[] buffer = new byte[256];
            int bytes;
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) { break; }
            }
        }
    }
}