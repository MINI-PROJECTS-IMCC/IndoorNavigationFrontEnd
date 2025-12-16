package com.example.proto5.qr_scanner;

import static com.example.proto5.qr_scanner.ZipUtils.unzip;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.proto5.MainActivity;
import com.example.proto5.R;
import com.example.proto5.map_ui.MapActivity;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;

public class QrMain extends AppCompatActivity {
    private PreviewView previewView;
    private static final String SERVER_BASE =
            "https://server2-3-afi9.onrender.com/api/getMapZip";
    private static final int CAMERA_PERMISSION_CODE = 101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.qr_scanner);

        previewView = findViewById(R.id.previewView);


        findViewById(R.id.back2)
                .setOnClickListener(v ->
                        startActivity(
                                new Intent(this, MainActivity.class)));
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE
            );
        } else {
            startCameraNow(); // ✅ correct entry point
        }





        //scan line animation
        View scanLine = findViewById(R.id.scanLine);
        FrameLayout scanContainer = findViewById(R.id.scanContainer);

        scanContainer.post(() -> {
            int containerHeight = scanContainer.getHeight();
            int lineHeight = scanLine.getHeight();

            ValueAnimator animator =
                    ValueAnimator.ofFloat(0, containerHeight - lineHeight);
            animator.setDuration(1500);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setInterpolator(new LinearInterpolator());

            animator.addUpdateListener(a ->
                    scanLine.setTranslationY((float) a.getAnimatedValue())
            );

            animator.start();
        });
    }


    private void startCameraNow() {
        startCamera();
    }
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            startCameraNow(); // ✅ start camera AFTER permission
        }
    }
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider =
                        cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(
                                ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                analysis.setAnalyzer(
                        Executors.newSingleThreadExecutor(), image -> {
                            @SuppressLint("UnsafeOptInUsageError")
                            Image mediaImage = image.getImage();

                            if (mediaImage != null) {
                                InputImage inputImage =
                                        InputImage.fromMediaImage(
                                                mediaImage,
                                                image.getImageInfo()
                                                        .getRotationDegrees()
                                        );

                                BarcodeScanner scanner =
                                        BarcodeScanning.getClient();

                                scanner.process(inputImage)
                                        .addOnSuccessListener(barcodes -> {
                                            for (Barcode barcode : barcodes) {
                                                String value =
                                                        barcode.getRawValue();

                                                if (value != null) {
                                                    downloadMapForQr(value);
                                                    break;
                                                }
                                            }
                                        })
                                        .addOnCompleteListener(
                                                task -> image.close()
                                        );
                            }
                        });

                CameraSelector cameraSelector =
                        new CameraSelector.Builder()
                                .requireLensFacing(
                                        CameraSelector.LENS_FACING_BACK)
                                .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, analysis
                );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }
    private void downloadMapForQr(String qrValue) {

        Executors.newSingleThreadExecutor().execute(() -> {
            HttpURLConnection conn = null;

            try {
                URL url =
                        new URL(SERVER_BASE);

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);
                conn.setDoOutput(true);

                conn.setRequestProperty(
                        "Content-Type", "application/json");

                // ✅ Send JSON body
                String json =
                        "{\"qrValue\":\"" + qrValue + "\"}";
                conn.getOutputStream().write(json.getBytes());
                conn.getOutputStream().flush();

                if (conn.getResponseCode()
                        != HttpURLConnection.HTTP_OK) {
                    return;
                }

                InputStream in = conn.getInputStream();

                File zipFile =
                        new File(getFilesDir(),
                                "mapdata.zip");

                FileOutputStream fos =
                        new FileOutputStream(zipFile);

                byte[] buffer = new byte[4096];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                in.close();

                File outFolder =
                        new File(getFilesDir(), "mapdata");
                outFolder.mkdirs();

                unzip(zipFile, outFolder);

                Intent i =
                        new Intent(QrMain.this, MapActivity.class);
                i.putExtra(
                        "mapFolderPath",
                        outFolder.getAbsolutePath());
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

}
