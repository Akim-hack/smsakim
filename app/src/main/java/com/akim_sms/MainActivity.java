package com.akim_sms;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int CODE_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        demanderPermissions();
    }

    private void demanderPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.RECEIVE_SMS,
                                Manifest.permission.READ_SMS
                        }, CODE_PERMISSION);
            } else {
                demarrerService();
                Toast.makeText(this, "Service SMS demarre", Toast.LENGTH_SHORT).show();
            }
        } else {
            demarrerService();
        }
    }

    private void demarrerService() {
        try {
            Intent intent = new Intent(this, SmsWatcherService.class);
            startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODE_PERMISSION) {
            boolean ok = true;
            for (int r : grantResults) {
                if (r != PackageManager.PERMISSION_GRANTED) { ok = false; break; }
            }
            if (ok) {
                demarrerService();
                Toast.makeText(this, "Permissions accordees", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Autorise les SMS !", Toast.LENGTH_LONG).show();
            }
        }
    }
}
