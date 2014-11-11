package com.grupo8.sig.sig;


import android.util.Log;

import java.util.ArrayList;

import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.esri.core.symbol.SimpleMarkerSymbol;
/**
 * Created by Rodrigo on 08/11/2014.
 */
public class PuntosRuta  {

    private ArrayList<PointInfo> ruta;
    private int cantidadPasos;
    private int intermedios;
    private boolean zoomEnable;
    private SimpleMarkerSymbol.STYLE forma;

    public PuntosRuta(Polyline rutaGen){
        ruta = new ArrayList<PointInfo>();
        intermedios = 2;
        crearRuta(rutaGen);
    }

    private void crearRuta(Polyline rutaGen){
        Double pasos;

        Point anterior = rutaGen.getPoint(0);
        PointInfo sim = new PointInfo(anterior,0);
        ruta.add(sim);

        int pasosT=1;

        for (int j = 1; j < rutaGen.getPathSize(0); j++ )	{
            anterior = rutaGen.getPoint(j-1);
            Point puntoAct = rutaGen.getPoint(j);

            Polyline aux = new Polyline();
            aux.startPath(anterior);
            aux.lineTo(puntoAct);

            double distancia = aux.calculateLength2D();

            double x1 = anterior.getX();
            double y1 = anterior.getY();

            double x2 = puntoAct.getX();
            double y2 = puntoAct.getY();

            double dx = (x2 - x1);
            double dy = (y2 - y1);

            pasos = (distancia / intermedios);

            for (int i=1;i<pasos;i++){
                double variadorVel = Math.random()-0.5;//random entre -0.5y 0.5;
                Point nuevo = new Point(x1+(dx/pasos)*(i+variadorVel),y1+(dy/pasos)*(i+variadorVel));
                int vel = 0;//calcularVelocidad(anterior, nuevo);
                sim = new PointInfo(nuevo,vel);
                ruta.add(sim);
               // anterior = nuevo;
            }
            pasosT += pasos;
        }
        cantidadPasos = pasosT;
    }

/*
    public int calcularVelocidad(Point origen, Point destino) {

        Polyline tramo = new Polyline();
        tramo.startPath(origen.getX(),origen.getY());
        tramo.lineTo(destino.getX(),destino.getY());

        double distanciaPaso = 1;
        double distancia = tramo.calculateLength2D();

        Log.d("--->en crear ruta","la distancia entre puntos da " + distancia);

        double cantPasos = distancia / distanciaPaso;
        if (cantPasos < 3){
            return 25;
        }else if (cantPasos < 6){
            return 50;
        }else{
            return 150;
        }
    }*/

    public Point obtenerPuntoGPS(int paso){
        return ruta.get(paso).getPoint();
    }
    public int obtenerVelGPS(int paso){
        return ruta.get(paso).getVel();
    }
    public ArrayList<PointInfo> getRuta() {
        return ruta;
    }
    public void setRuta(ArrayList<PointInfo> ruta) {
        this.ruta = ruta;
    }
    public int getCantidadPasos() {
        return cantidadPasos;
    }
    public void setCantidadPasos(int cantidadPasos) {
        this.cantidadPasos = cantidadPasos;
    }
    public int getIntermedios() {
        return intermedios;
    }
    public void setIntermedios(int intermedios) {
        this.intermedios = intermedios;
    }
    public boolean isZoomEnable() {
        return zoomEnable;
    }
    public void setZoomEnable(boolean zoomEnable) {
        this.zoomEnable = zoomEnable;
    }
    public SimpleMarkerSymbol.STYLE getForma() {
        return forma;
    }
    public void setForma(SimpleMarkerSymbol.STYLE forma) {
        this.forma = forma;
    }
}