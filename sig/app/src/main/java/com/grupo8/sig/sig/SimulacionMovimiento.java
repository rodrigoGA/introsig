package com.grupo8.sig.sig;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.esri.core.geometry.AreaUnit;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Feature;
import com.esri.core.map.FeatureResult;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.tasks.SpatialRelationship;
import com.esri.core.tasks.query.QueryParameters;
import com.esri.core.tasks.query.QueryTask;

/**
 * Created by Rodrigo on 08/11/2014.
 */
public class SimulacionMovimiento extends Thread {

    private GeocodeActivity actividad;
    private PuntosRuta GPSs;

    public SimulacionMovimiento(GeocodeActivity actividad, PuntosRuta gpss) {
        this.actividad = actividad;
        this.GPSs = gpss;
    }

    public void excSimulation() {
        try {
            this.start();
        } catch (Exception e) {
            Toast toast = Toast.makeText(actividad, "Error al simular!", Toast.LENGTH_LONG);
            toast.show();
        }
    }


    public double  calcularVelocidad(Point origen, Point destino) {

        Polyline tramo = new Polyline();
        tramo.startPath(origen.getX(),origen.getY());
        tramo.lineTo(destino.getX(),destino.getY());

        double distanciaPaso = 5;
        double distancia = tramo.calculateLength2D();

        Log.d("--->en crear ruta","la distancia entre puntos da " + distancia);

        return  distancia * distanciaPaso;

    }




    Graphic graphicS;
    boolean fin = false;
    Point actual;
    Point anterior;
    double velocidad;

    public void run() {
        long time = 1;
        int size = 15;

        int cantPasos = GPSs.getCantidadPasos();


        Geometry geomS;
        int i = 1;
        anterior = actual = GPSs.obtenerPuntoGPS(0);

        AsyncTask actualizador =null;
        int actualizoBuffer =0;

        while ( i < cantPasos && !fin) {
            actual = GPSs.obtenerPuntoGPS(i);
            velocidad = calcularVelocidad(anterior,actual);
            anterior = actual;

            if (actualizador == null || (actualizador.getStatus() == AsyncTask.Status.FINISHED)){
                actualizador = new BufferCalc( actual).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                actualizoBuffer = i;
                Log.d("-->en simulacion", "finalizo pintar");
            } else if (i >= ( actualizoBuffer + actividad.getAceleracion()*15)){
                actualizador = new BufferCalc( actual).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                actualizoBuffer = i + actividad.getAceleracion()*5;
                Log.d("-->en simulacion", "creo otro pintar");
            }

                geomS = actual;
            SimpleMarkerSymbol resultSymbol = null;
            int colorGraphic = 0;
            if (velocidad <= 49) {
                colorGraphic = Color.GREEN;
            } else if (velocidad <= 99) {
                colorGraphic = Color.YELLOW;
            } else {
                colorGraphic = Color.RED;
            }
            resultSymbol = new SimpleMarkerSymbol(colorGraphic, size, SimpleMarkerSymbol.STYLE.CIRCLE);
            graphicS = new Graphic(geomS, resultSymbol);



            //GeocodeActivity.handler.post(new GeocodeActivity.MyRunnable());

            actividad.runOnUiThread(new Runnable() {
                public void run() {

                    if (graphicS != null){
                        actividad.movimientoAuto.removeAll();
                        actividad.movimientoAuto.addGraphic(graphicS);
                        actividad.actualizarVelocidad((int)velocidad);

                        if (actividad.getFocusInCar())
                            actividad.mMapView.zoomToResolution(actual, 2);
                    }


                }
            });


            try {
                TimeUnit.SECONDS.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            i= i + actividad.getAceleracion();
        }

        fin = true;


        actividad.runOnUiThread(new Runnable() {
            public void run() {
                Toast toast = Toast.makeText(actividad, "Simulación terminada", Toast.LENGTH_LONG);
                toast.show();
            }
        });

    }


    private class BufferCalc extends AsyncTask<Void, Void, Boolean> {
        Point actual;
        int cantPoblacion;

        Graphic buffer;
        LinkedList<Graphic> lista_counties = new LinkedList<Graphic>();


        public BufferCalc(Point actual) {
            this.actual=actual;
        }

        @Override
        protected Boolean  doInBackground(Void... params) {
            try {
                Polygon bufferPosition = GeometryEngine.buffer(actual, actividad.sistCoordenadas,  actividad.getTamBuffer(), GeocodeActivity.sistCoordenadas.getUnit());
                SimpleFillSymbol simpleFillSymbol = new SimpleFillSymbol(Color.CYAN);
                simpleFillSymbol.setAlpha(100);
                simpleFillSymbol.setOutline(new SimpleLineSymbol(Color.BLACK, 1));
                buffer = new Graphic(bufferPosition, simpleFillSymbol);


                //GeocodeActivity.query_campo_visible.setGeometry(GeocodeActivity.campo_visible);
                QueryParameters mParams = new QueryParameters();
                mParams.setReturnGeometry(true);
                mParams.setOutFields(new String[]{"TOTPOP_CY", "LANDAREA"});
                //mParams.setReturnIdsOnly(true);
                mParams.setGeometry(buffer.getGeometry());
                mParams.setSpatialRelationship(SpatialRelationship.INTERSECTS);
                mParams.setOutSpatialReference(actividad.sistCoordenadas);
                String mFeatureServiceURL = "http://services.arcgisonline.com/arcgis/rest/services/Demographics/USA_1990-2000_Population_Change/MapServer/3";
                QueryTask queryTask = new QueryTask(mFeatureServiceURL);

                FeatureResult resultsQuery = queryTask.execute(mParams);


///

                Geometry interseccion;
                for (Object element : resultsQuery) {
                    if (element instanceof Feature) {
                        SimpleFillSymbol simpleFillSymbol2 = new SimpleFillSymbol(Color.DKGRAY);
                        simpleFillSymbol2.setAlpha(100);
                        simpleFillSymbol2.setOutline(new SimpleLineSymbol(Color.BLACK, 1));

                        Feature f = (Feature) element;
                        Graphic counties = new Graphic(f.getGeometry(), simpleFillSymbol2, f.getAttributes());
                        lista_counties.add(counties);

                        interseccion = GeometryEngine.intersect(buffer.getGeometry(), counties.getGeometry(), actividad.sistCoordenadas);
                        Integer poblacion = (Integer) counties.getAttributeValue("TOTPOP_CY");
                        Double areaCounties = (Double) counties.getAttributeValue("LANDAREA");


                        Double areaInterseccion = GeometryEngine.geodesicArea(interseccion, actividad.sistCoordenadas, new AreaUnit(AreaUnit.Code.SQUARE_MILE_US));

                        cantPoblacion = (int) ((areaInterseccion / areaCounties) * poblacion);


                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean ok) {
            try {
                if (ok && !fin){
                    actividad.bufferLayer.removeAll();
                    actividad.bufferLayer.addGraphic(buffer);

                    actividad.countiesLayer.removeAll();
                    for (Graphic counties : lista_counties) {
                        actividad.countiesLayer.addGraphic(counties);
                    }

                    actividad.actualizarPoblacion(String.valueOf(cantPoblacion));

                }else{
                    Log.d("--> en simulacion movimiento", "no le dio ok para actualizar");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }






}
