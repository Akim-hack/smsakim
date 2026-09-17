package com.akim_sms;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        demanderAccesNotifications();
    }

    private void demanderAccesNotifications() {
        if (!estAutorise()) {
            Toast.makeText(this, "Autorise l'acces aux notifications", Toast.LENGTH_LONG).show();
            try {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "SMS Akim est actif", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean estAutorise() {
        String flat = Settings.Secure.getString(getContentResolver(),
                "enabled_notification_listeners");
        return flat != null && flat.contains(getPackageName());
    }
}
