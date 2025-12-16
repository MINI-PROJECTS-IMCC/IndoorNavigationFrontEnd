package com.example.proto5.load_xml;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.View;

import com.example.proto5.map_draw.MapPoint;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class LoadPoints extends View {
    public LoadPoints(Context context, AttributeSet attrs){
        super(context,attrs);
    }

    private ArrayList<MapPoint> points = new ArrayList<>();
    private InputStream is;

    /**
     * assetPath: either "folder/datapoints.xml" (asset) OR "/data/data/.../folder/datapoints.xml" (absolute)
     */
    public ArrayList<MapPoint> loadPointsFromXml(String assetPath) {
        points.clear();
        try {
            if (assetPath == null) return points;

            // If file exists on file system -> open as file
            if (new File(assetPath).exists()) {
                is = new FileInputStream(new File(assetPath));
            } else {
                // else treat as asset path
                is = getContext().getAssets().open(assetPath);
            }

            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(is, null);
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && "point".equals(parser.getName())) {
                    String id = parser.getAttributeValue(null, "id");
                    float x = Float.parseFloat(parser.getAttributeValue(null, "x"));
                    float y = Float.parseFloat(parser.getAttributeValue(null, "y"));
                    boolean clickable = false;
                    String clickAttr = parser.getAttributeValue(null, "clickable");
                    if (clickAttr != null) clickable = Boolean.parseBoolean(clickAttr);
                    points.add(new MapPoint(id, x, y, clickable));
                }
                eventType = parser.next();
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("LoadPoints", "Error loading points xml: " + e.getMessage());
            try { if (is != null) is.close(); } catch (Exception ignored) {}
        }
        return points;
    }
}
