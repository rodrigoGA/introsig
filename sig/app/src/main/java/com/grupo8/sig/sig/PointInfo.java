package com.grupo8.sig.sig;

import com.esri.core.geometry.Point;

/**
 * Created by Rodrigo on 08/11/2014.
 */
public class PointInfo {
    private Point point;
    private int vel;

    public PointInfo(Point p, int v){
        point = p;
        vel = v;
    }
    public Point getPoint() {
        return point;
    }
    public void setPoint(Point point) {
        this.point = point;
    }
    public int getVel() {
        return vel;
    }
    public void setVel(int vel) {
        this.vel = vel;
    }
}