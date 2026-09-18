package com.akim_sms;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TextView txtStatus;
    private TextView txtStatusIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtStatus = findViewById(R.id.txt_status);
        txtStatusIcon = findViewById(R.id.txt_status_icon);

        // BOUTON GIGA BOOST — vérifie d'abord les notifications
        Button btnSim = findViewById(R.id.btn_simulateur);
        btnSim.setOnClickListener(v -> {
            if (estAutorise()) {
                Intent i = new Intent(MainActivity.this, SimulateurActivity.class);
                startActivity(i);
            } else {
                Toast.makeText(MainActivity.this,
                        "⚠️ Autorise d'abord les notifications !",
                        Toast.LENGTH_LONG).show();
                try {
                    Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Button btnActiver = findViewById(R.id.btn_activer);
        btnActiver.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Erreur", Toast.LENGTH_SHORT).show();
            }
        });

        majStatut();
    }

    @Override
    protected void onResume() {
        super.onResume();
        majStatut();
    }

    private void majStatut() {
        if (estAutorise()) {
            txtStatus.setText("Actif • En écoute");
            txtStatus.setTextColor(getResources().getColor(R.color.accent_green));
            txtStatusIcon.setText("✅");
        } else {
            txtStatus.setText("Inactif • Autorise les notifications");
            txtStatus.setTextColor(getResources().getColor(R.color.accent_orange));
            txtStatusIcon.setText("⚠️");
        }
    }

    private boolean estAutorise() {
        String flat = Settings.Secure.getString(getContentResolver(),
                "enabled_notification_listeners");
        return flat != null && flat.contains(getPackageName());
    }
}
