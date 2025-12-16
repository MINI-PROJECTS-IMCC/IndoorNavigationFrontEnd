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

public class LoadTextPoints extends View {
    public LoadTextPoints(Context context, AttributeSet attrs){
        super(context,attrs);
    }

    private ArrayList<MapPoint> points = new ArrayList<>();
    private InputStream is;

    public ArrayList<MapPoint> LoadTextpointsXml(String assetPath) {
        points.clear();
        try {
            if (assetPath == null) return points;

            if (new File(assetPath).exists()) {
                is = new FileInputStream(new File(assetPath));
            } else {
                is = getContext().getAssets().open(assetPath);
            }

            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(is, null);
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && "textpoint".equals(parser.getName())) {
                    String id = parser.getAttributeValue(null, "id");
                    float x = Float.parseFloat(parser.getAttributeValue(null, "x"));
                    float y = Float.parseFloat(parser.getAttributeValue(null, "y"));
                    boolean clickable = false;
                    String clickAttr = parser.getAttributeValue(null, "clickable");
                    if (clickAttr != null) clickable = Boolean.parseBoolean(clickAttr);
                    points.add(new MapPoint(id, x, y, clickable));
                }
                // Some files might use same <point> tag for textpoints - support that too

                eventType = parser.next();
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("LoadTextPoints", "Error loading textpoints xml: " + e.getMessage());
            try { if (is != null) is.close(); } catch (Exception ignored) {}
        }
        return points;
    }
}
