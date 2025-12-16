package com.example.proto5.AssetLoader;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.example.proto5.map_draw.MapOverlayView;

import java.io.File;

public class InternalFloorLoader implements FloorLoader{
    private final Context context;
    private final ImageView floorMap;
    private final MapOverlayView overlay;

    public InternalFloorLoader(Context context, ImageView floorMap, MapOverlayView overlay) {
        this.context = context;
        this.floorMap = floorMap;
        this.overlay = overlay;
    }

    @Override
    public void load(String folderPath) {
        File mapFile = new File(folderPath, "map.png");
        if (mapFile.exists()) {
            floorMap.setImageBitmap(
                    BitmapFactory.decodeFile(mapFile.getAbsolutePath()));
        }

        File datapoints = new File(folderPath, "datapoints.xml");
        File textpoints = new File(folderPath, "datapoints.xml");
        File graph = new File(folderPath, "datapoints.xml");

        if (datapoints.exists())
            overlay.setPoints(datapoints.getAbsolutePath());

        if (textpoints.exists())
            overlay.setTextPoints(textpoints.getAbsolutePath());

        if (graph.exists())
            overlay.loadGraphFromXml(graph.getAbsolutePath());

        overlay.invalidate();
    }
}
