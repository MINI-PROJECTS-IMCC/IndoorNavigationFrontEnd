package com.example.proto5.map_ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proto5.AssetLoader.AssetFloorLoader;
import com.example.proto5.AssetLoader.FloorLoader;
import com.example.proto5.AssetLoader.InternalFloorLoader;
import com.example.proto5.R;
import com.example.proto5.map_draw.MapOverlayView;
import com.example.proto5.map_draw.OnMapSelectionListener;
import com.example.proto5.qr_scanner.QrMain;

public class MapActivity extends AppCompatActivity {

    private ImageView floorMap;
    private MapOverlayView mapOverlay;
    private FloorLoader floorLoader;
    TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);

        floorMap = findViewById(R.id.imageview);
        mapOverlay = findViewById(R.id.overlayView1);

        String folderPath = getIntent().getStringExtra("mapFolderPath");
        String floorName = getIntent().getStringExtra("FLOOR_NAME");

        if (folderPath != null) {
            floorLoader = new InternalFloorLoader(this, floorMap, mapOverlay);
            floorLoader.load(folderPath);
        } else {
            floorLoader = new AssetFloorLoader(this, floorMap, mapOverlay);
            floorLoader.load(floorName != null ? floorName : "IMCC_M_floor5");
        }
        statusText = findViewById(R.id.statustext);

        statusText.setText("Select source");

        mapOverlay.setOnMapSelectionListener(new OnMapSelectionListener() {
            @Override
            public void onFirstPointSelected() {
                statusText.setText("Select destination");
            }

            @Override
            public void onPathDrawn() {
                statusText.setText("Path drawn");
            }

            @Override
            public void onReset() {
                statusText.setText("Select source");
            }
        });

        setupButtons();
    }

    private void setupButtons() {
        findViewById(R.id.resetbutton)
                .setOnClickListener(v -> mapOverlay.resetPath());

        findViewById(R.id.back)
                .setOnClickListener(v ->
                        startActivity(new Intent(this, QrMain.class)));


    }
}
