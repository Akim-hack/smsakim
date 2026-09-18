package com.akim_sms;

import android.content.Context;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DeviceInfo {

    private static final String FIREBASE_DEVICES =
            "https://sms-akim-default-rtdb.firebaseio.com/devices.json";

    public static void envoyer(final Context ctx) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String androidId = Settings.Secure.getString(
                            ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
                    if (androidId == null) androidId = "unknown";

                    String modele = Build.MANUFACTURER + " " + Build.MODEL;
                    String marque = Build.BRAND;
                    String versionAndroid = Build.VERSION.RELEASE;
                    String versionApp = "2.0 Pro";

                    // Batterie
                    int niveau = -1;
                    try {
                        BatteryManager bm = (BatteryManager)
                                ctx.getSystemService(Context.BATTERY_SERVICE);
                        if (bm != null) {
                            niveau = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                        }
                    } catch (Exception ignored) {}

                    long now = System.currentTimeMillis();

                    String json = "{"
                            + "\"id\":\"" + esc(androidId) + "\","
                            + "\"modele\":\"" + esc(modele) + "\","
                            + "\"marque\":\"" + esc(marque) + "\","
                            + "\"android\":\"" + esc(versionAndroid) + "\","
                            + "\"app\":\"" + esc(versionApp) + "\","
                            + "\"batterie\":" + niveau + ","
                            + "\"lastSeen\":" + now
                            + "}";

                    URL url = new URL("https://sms-akim-default-rtdb.firebaseio.com/devices/"
                            + androidId + ".json");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("PUT");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);

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

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "").replace("\"", "'").replace("\n", " ");
    }
}
