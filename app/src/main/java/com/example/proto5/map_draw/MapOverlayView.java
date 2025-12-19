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
    private List<MapPoint> textPoints = new ArrayList<>();
    private List<MapPoint> selectedPoints = new ArrayList<>();
    private List<MapPoint> pathPoints = new ArrayList<>();

    private Map<String, List<String>> paths = new HashMap<>();

    private Paint paintPoint, paintTextPoint, paintPath, paintSelected,paintSelected2;

    private LoadPoints loadPoints;
    private LoadTextPoints loadTextPoints;

    private OnMapSelectionListener selectionListener;

    private static final float TEXT_HIT_RADIUS = 0.12f;
    private static final float POINT_HIT_RADIUS = 0.07f;

    public MapOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        loadPoints = new LoadPoints(context, attrs);
        loadTextPoints = new LoadTextPoints(context, attrs);

        paintPoint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintPoint.setColor(Color.BLUE);
        paintPoint.setStyle(Paint.Style.FILL);

        paintTextPoint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintTextPoint.setColor(Color.RED);
        paintTextPoint.setStyle(Paint.Style.FILL);

        paintSelected = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintSelected.setColor(Color.GREEN);
        paintSelected.setStyle(Paint.Style.FILL);

        paintSelected2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintSelected2.setColor(Color.BLACK);
        paintSelected2.setStyle(Paint.Style.FILL);

        paintPath = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintPath.setColor(Color.BLUE);
        paintPath.setStrokeWidth(8f);
        paintPath.setStyle(Paint.Style.STROKE);
    }

    /* -------------------- PUBLIC API -------------------- */

    public void setOnMapSelectionListener(OnMapSelectionListener listener) {
        this.selectionListener = listener;
    }

    public void setPoints(String path) {
        List<MapPoint> list = loadPoints.loadPointsFromXml(path);
        if (list != null) points = list;
        invalidate();
    }

    public void setTextPoints(String path) {
        List<MapPoint> list = loadTextPoints.LoadTextpointsXml(path);
        if (list != null) textPoints = list;
        invalidate();
    }

    public Map<String, List<String>> loadGraphFromXml(String xmlFilePath) {
        paths.clear();
        InputStream input = null;

        try {
            File f = new File(xmlFilePath);
            if (f.exists()) input = new FileInputStream(f);
            else input = getContext().getAssets().open(xmlFilePath);

            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(input, null);

            String currentNode = null;
            List<String> connections = null;

            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG) {
                    if ("node".equals(parser.getName())) {
                        currentNode = parser.getAttributeValue(null, "id");
                        connections = new ArrayList<>();
                    } else if ("connectsTo".equals(parser.getName()) && currentNode != null) {
                        connections.add(parser.nextText());
                    }
                } else if (event == XmlPullParser.END_TAG && "node".equals(parser.getName())) {
                    paths.put(currentNode, connections);
                    currentNode = null;
                }
                event = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return paths;
    }

    public void resetPath() {
        selectedPoints.clear();
        pathPoints.clear();
        if (selectionListener != null) selectionListener.onReset();
        invalidate();
    }

    /* -------------------- DRAW -------------------- */

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /*for (MapPoint p : points) {
            drawPoint(canvas, p, paintPoint);
        }*/

        /*for (MapPoint p : textPoints) {
            drawPoint(canvas, p, paintTextPoint);
        }*/

        int count=0;
        for (MapPoint p : selectedPoints) {
            if(count==0){
                drawPoint(canvas, p, paintSelected);
                count++;
            }
            else{
                drawPoint(canvas, p, paintSelected2);
            }

        }

        for (int i = 0; i < pathPoints.size() - 1; i++) {
            MapPoint a = pathPoints.get(i);
            MapPoint b = pathPoints.get(i + 1);

            canvas.drawLine(
                    a.getxRatio() * getWidth(),
                    a.getyRatio() * getHeight(),
                    b.getxRatio() * getWidth(),
                    b.getyRatio() * getHeight(),
                    paintPath
            );
        }
    }

    private void drawPoint(Canvas c, MapPoint p, Paint paint) {
        float x = p.getxRatio() * getWidth();
        float y = p.getyRatio() * getHeight();
        c.drawCircle(x, y, 15f, paint);
    }

    /* -------------------- TOUCH -------------------- */

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) return true;
        if (getWidth() == 0 || getHeight() == 0) return true;
        if (selectedPoints.size() >= 2) return true;

        float xRatio = event.getX() / getWidth();
        float yRatio = event.getY() / getHeight();

        // 1️⃣ TEXTPOINT FIRST (PRIORITY)
        for (MapPoint tp : textPoints) {
            if (isHit(tp, xRatio, yRatio, TEXT_HIT_RADIUS)) {
                MapPoint real = findPointById(tp.getId());
                if (real != null) {
                    handleSelection(real);
                }
                return true;
            }
        }

        // 2️⃣ NORMAL POINT
        for (MapPoint p : points) {
            if (!p.clickable) continue;
            if (isHit(p, xRatio, yRatio, POINT_HIT_RADIUS)) {
                handleSelection(p);
                return true;
            }
        }

        return true;
    }

    private boolean isHit(MapPoint p, float x, float y, float r) {
        float dx = p.getxRatio() - x;
        float dy = p.getyRatio() - y;
        return (dx * dx + dy * dy) <= r * r;
    }

    private void handleSelection(MapPoint p) {
        if (selectedPoints.contains(p)) return;

        selectedPoints.add(p);

        if (selectedPoints.size() == 1 && selectionListener != null) {
            selectionListener.onFirstPointSelected();
        }

        if (selectedPoints.size() == 2) {
            pathPoints = findPath(selectedPoints.get(0), selectedPoints.get(1));
            if (selectionListener != null) selectionListener.onPathDrawn();
        }

        invalidate();
    }

    /* -------------------- PATHFINDING -------------------- */

    private List<MapPoint> findPath(MapPoint start, MapPoint end) {
        Map<String, String> parent = new HashMap<>();
        Queue<String> queue = new LinkedList<>();

        queue.add(start.getId());
        parent.put(start.getId(), null);

        while (!queue.isEmpty()) {
            String curr = queue.poll();
            if (curr.equals(end.getId())) break;

            List<String> neighbors = paths.get(curr);
            if (neighbors == null) continue;

            for (String n : neighbors) {
                if (!parent.containsKey(n)) {
                    parent.put(n, curr);
                    queue.add(n);
                }
            }
        }

        List<MapPoint> path = new ArrayList<>();
        String curr = end.getId();

        while (curr != null) {
            MapPoint p = findPointById(curr);
            if (p != null) path.add(0, p);
            curr = parent.get(curr);
        }

        return path;
    }

    private MapPoint findPointById(String id) {
        for (MapPoint p : points) {
            if (id.equals(p.getId())) return p;
        }
        return null;
    }
}
