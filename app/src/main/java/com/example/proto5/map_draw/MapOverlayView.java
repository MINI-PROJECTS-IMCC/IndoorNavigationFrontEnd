package com.example.proto5.map_draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.MotionEvent;
import android.view.View;

import com.example.proto5.load_xml.LoadPoints;
import com.example.proto5.load_xml.LoadTextPoints;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class MapOverlayView extends View {
    private List<MapPoint> points = new ArrayList<>();
    private Map<String, List<String>> paths = new HashMap<>();
    private List<MapPoint> selectedPoints = new ArrayList<>();
    private List<MapPoint> selectedPoints2 = new ArrayList<>();
    private List<MapPoint> tempPoints = new ArrayList<>();
    private List<MapPoint> textPoints = new ArrayList<>();
    private List<MapPoint> pathPoints = new ArrayList<>();
    private Paint paintPoint, paintPoint2;
    private Paint paintPath;
    private InputStream is;
    private LoadPoints ld;
    private LoadTextPoints ld2;

    public MapOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ld = new LoadPoints(context, attrs);
        ld2 = new LoadTextPoints(context, attrs);

        paintPoint = new Paint();
        paintPoint.setColor(Color.BLUE);
        paintPoint.setStyle(Paint.Style.FILL);

        paintPoint2 = new Paint();
        paintPoint2.setColor(Color.RED);
        paintPoint2.setStyle(Paint.Style.FILL);

        paintPath = new Paint();
        paintPath.setColor(Color.BLUE);
        paintPath.setStrokeWidth(8f);
        paintPath.setStyle(Paint.Style.STROKE);
    }

    /**
     * xmlFilePath can be either an asset path like "folder/datapoints.xml"
     * or an absolute filesystem path like "/data/data/.../folder/datapoints.xml"
     */
    public Map<String, List<String>> loadGraphFromXml(String xmlFilePath) {
        paths.clear();
        InputStream input = null;
        try {
            File f = new File(xmlFilePath);
            if (f.exists()) {
                input = new FileInputStream(f);
            } else {
                input = getContext().getAssets().open(xmlFilePath);
            }
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(input, null);

            String currentNodeId = null;
            List<String> connections = null;

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String name = parser.getName();
                    if (name.equals("node")) {
                        currentNodeId = parser.getAttributeValue(null, "id");
                        connections = new ArrayList<>();
                    } else if (name.equals("connectsTo") && currentNodeId != null) {
                        connections.add(parser.nextText());
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if ("node".equals(parser.getName()) && currentNodeId != null) {
                        paths.put(currentNodeId, connections);
                        currentNodeId = null;
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (is != null) is.close(); } catch (Exception ignored) {}
        }
        return paths;
    }

    public void setPoints(String path){
        // path may be asset path or absolute
        List<MapPoint> loaded = ld.loadPointsFromXml(path);
        if (loaded != null) points = loaded;
        invalidate();
    }

    public void setTextPoints(String path){
        List<MapPoint> loaded = ld2.LoadTextpointsXml(path);
        if (loaded != null) textPoints = loaded;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Draw all points
        for (MapPoint p : points) {
            float x = p.getxRatio() * getWidth();
            float y = p.getyRatio() * getHeight();
            canvas.drawCircle(x, y, 15f, paintPoint);
        }
        for (MapPoint p : textPoints) {
            float x = p.getxRatio() * getWidth();
            float y = p.getyRatio() * getHeight();
            canvas.drawCircle(x, y, 15f, paintPoint2);
        }
        // Draw current path
        for (int i = 0; i < pathPoints.size() - 1; i++) {
            MapPoint p1 = pathPoints.get(i);
            MapPoint p2 = pathPoints.get(i + 1);
            canvas.drawLine(
                    p1.getxRatio() * getWidth(),
                    p1.getyRatio() * getHeight(),
                    p2.getxRatio() * getWidth(),
                    p2.getyRatio() * getHeight(),
                    paintPath
            );
        }
    }

    @Override
    public boolean onTouchEvent(android.view.MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float xRatio = event.getX() / getWidth();
            float yRatio = event.getY() / getHeight();

            MapPoint nearest = null;
            MapPoint nearest2 = null;
            double minDist = Double.MAX_VALUE;
            double minDist2 = Double.MAX_VALUE;

            for (MapPoint p : points) {
                if (p.clickable) {
                    double dx = p.getxRatio() - xRatio;
                    double dy = p.getyRatio() - yRatio;
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist < minDist) {
                        minDist = dist;
                        nearest = p;
                    }
                }
            }

            for (MapPoint p : textPoints) {
                double dx2 = p.getxRatio() - xRatio;
                double dy2 = p.getyRatio() - yRatio;
                double dist = Math.sqrt(dx2 * dx2 + dy2 * dy2);
                if (dist < minDist2) {
                    minDist2 = dist;
                    nearest2 = p;
                }
            }

            // select based on ranges (tweak thresholds if needed)
            if (nearest2 != null && minDist2 < 0.12) {
                // nearest2 refers to a textpoint. But your older code added nearest (from points).
                // We'll map nearest2.id -> points list entry (if exists).
                MapPoint mapped = findPointById(nearest2.getId());
                if (mapped != null) selectedPoints2.add(mapped);
                invalidate();
            } else if (nearest != null && minDist < 0.07) {
                selectedPoints.add(nearest);
                invalidate();
            }

            // Handle selectedPoints2 (converted to actual point objects in tempPoints)
            if (selectedPoints2.size() == 2) {
                tempPoints.clear();
                for (MapPoint sp2 : selectedPoints2) {
                    MapPoint p = findPointById(sp2.getId());
                    if (p != null) tempPoints.add(p);
                }
                if (tempPoints.size() >= 2) {
                    pathPoints = findPath(tempPoints.get(0), tempPoints.get(1));
                }
            }
            if (selectedPoints2.size() == 3) {
                selectedPoints2.clear();
                tempPoints.clear();
            }

            if (selectedPoints.size() == 2) {
                pathPoints = findPath(selectedPoints.get(0), selectedPoints.get(1));
            }
            if (selectedPoints.size() == 3) {
                selectedPoints.clear();
            }
            if (selectedPoints.size() == 1 && selectedPoints2.size() == 1) {
                tempPoints.clear();
                MapPoint p = findPointById(selectedPoints2.get(0).getId());
                if (p != null) tempPoints.add(p);
                if (!tempPoints.isEmpty()) {
                    pathPoints = findPath(selectedPoints.get(0), tempPoints.get(0));
                }
            }
        }
        return true;
    }

    private List<MapPoint> findPath(MapPoint start, MapPoint end) {
        Map<String, String> parent = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(start.getId());
        parent.put(start.getId(), null);
        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(end.getId())) break;
            List<String> neighbors = paths.get(current);
            if (neighbors == null) continue;
            for (String n : neighbors) {
                if (!parent.containsKey(n)) {
                    parent.put(n, current);
                    queue.add(n);
                }
            }
        }
        List<MapPoint> result = new ArrayList<>();
        String curr = end.getId();
        while (curr != null) {
            MapPoint p = findPointById(curr);
            if (p != null) result.add(0, p);
            curr = parent.get(curr);
        }
        return result;
    }

    private MapPoint findPointById(String id) {
        if (id == null) return null;
        for (MapPoint p : points) {
            if (id.equals(p.getId())) return p;
        }
        return null;
    }

    public void resetPath() {
        if (pathPoints != null) pathPoints.clear();
        selectedPoints.clear();
        selectedPoints2.clear();
        tempPoints.clear();
        invalidate();
    }
}
