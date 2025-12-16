package com.example.proto5.AssetLoader;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.example.proto5.map_draw.MapOverlayView;

import java.io.IOException;
import java.io.InputStream;

public class AssetFloorLoader implements FloorLoader{

    private final Context context;
    private final ImageView floorMap;
    private final MapOverlayView overlay;

    public AssetFloorLoader(Context context, ImageView floorMap, MapOverlayView overlay) {
        this.context = context;
        this.floorMap = floorMap;
        this.overlay = overlay;
    }

    @Override
    public void load(String folderName) {
        try {
            AssetManager am = context.getAssets();

            InputStream is = am.open(folderName + "/map.png");
            floorMap.setImageDrawable(Drawable.createFromStream(is, null));
            is.close();

            overlay.setPoints(folderName + "/datapoints.xml");
            overlay.setTextPoints(folderName + "/datapoints.xml");
            overlay.loadGraphFromXml(folderName + "/datapoints.xml");
            overlay.invalidate();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

