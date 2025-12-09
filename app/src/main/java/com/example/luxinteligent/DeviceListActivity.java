package com.example.luxinteligent;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.util.ArrayList;
import java.util.Set;

public class DeviceListActivity extends AppCompatActivity {

    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;
    ListView deviceList;

    // Código para identificar la solicitud de permiso
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        deviceList = findViewById(R.id.device_list_view);
        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        if (myBluetooth == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth no disponible", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // VERIFICACIÓN DE PERMISOS PARA ANDROID 12+ (API 31)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // Si no tenemos permiso, lo pedimos y NO continuamos todavía
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN},
                        REQUEST_BLUETOOTH_PERMISSIONS);
                return;
            }
        }

        // Si tenemos permiso o es un Android antiguo, mostramos la lista
        checkBluetoothState();
    }

    // Este método se ejecuta cuando el usuario responde al mensaje de "Permitir"
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, procedemos
                checkBluetoothState();
            } else {
                Toast.makeText(this, "Se requiere permiso Bluetooth para funcionar", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void checkBluetoothState() {
        if (!myBluetooth.isEnabled()) {
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // Verificación de seguridad redundante necesaria por Android Studio
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                return;
            }
            startActivityForResult(turnBTon, 1);
        } else {
            mostrarDispositivos();
        }
    }

    private void mostrarDispositivos() {
        // Verificación de seguridad nuevamente antes de llamar a getBondedDevices
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return;
        }

        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList<String> list = new ArrayList<>();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice bt : pairedDevices) {
                list.add(bt.getName() + "\n" + bt.getAddress());
            }
        } else {
            Toast.makeText(getApplicationContext(), "No se encontraron dispositivos emparejados.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        deviceList.setAdapter(adapter);

        deviceList.setOnItemClickListener((parent, view, position, id) -> {
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);

            Intent i = new Intent(DeviceListActivity.this, MainActivity.class);
            i.putExtra("EXTRA_ADDRESS", address);
            startActivity(i);
        });
    }
}