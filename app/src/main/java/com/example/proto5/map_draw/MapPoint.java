package com.example.proto5.map_draw;

public class MapPoint {
    private String id;
    private float xRatio;
    private float yRatio;
    public boolean clickable;
    public MapPoint(String id, float xRatio, float yRatio,boolean clickable) {
        this.id = id;
        this.xRatio = xRatio;
        this.yRatio = yRatio;
        this.clickable=clickable;
    }

    public String getId() { return id; }
    public float getxRatio() { return xRatio; }
    public float getyRatio() { return yRatio; }
}
