package com.example.proto5.qr_scanner;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;

public class MapDownloadManager {
    private static final String SERVER_URL =
            "https://server2-3-afi9.onrender.com/api/getMapZip";

    public interface DownloadCallback {
        void onSuccess(String mapFolderPath);
    }

    public static void downloadMap(
            Context context,
            String qrValue,
            DownloadCallback callback) {

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                HttpURLConnection conn =
                        (HttpURLConnection)
                                new URL(SERVER_URL).openConnection();


                conn.setRequestMethod("POST");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);
                conn.setDoOutput(true);
                conn.setRequestProperty(
                        "Content-Type", "application/json");

                String body =
                        "{\"qrValue\":\"" + qrValue + "\"}";
                conn.getOutputStream().write(body.getBytes());

                if (conn.getResponseCode() != 200) return;

                File zipFile =
                        new File(context.getFilesDir(), "mapdata.zip");
                FileOutputStream fos =
                        new FileOutputStream(zipFile);

                InputStream in = conn.getInputStream();
                byte[] buffer = new byte[4096];
                int len;

                while ((len = in.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                in.close();

                File outDir =
                        new File(context.getFilesDir(), "mapdata");
                ZipUtils.unzip(zipFile, outDir);

                callback.onSuccess(outDir.getAbsolutePath());

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
