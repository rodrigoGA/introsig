package com.grupo8.sig.sig;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.Unit;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.tasks.na.NAFeaturesAsFeature;
import com.esri.core.tasks.na.Route;
import com.esri.core.tasks.na.RouteParameters;
import com.esri.core.tasks.na.RouteResult;
import com.esri.core.tasks.na.RouteTask;
import com.esri.core.tasks.na.StopGraphic;

import java.util.List;

/**
 * Created by Rodrigo on 07/11/2014.
 */
public class TaskCrearRuta extends AsyncTask<Void, Void, Boolean> {
    RouteTask routeTask = null;
    RouteParameters routeParams = null;
    RouteResult mResults = null;
    GeocodeActivity actividad;
    Graphic symbolGraphicRuta;

    public TaskCrearRuta(GeocodeActivity actividad){
        this.actividad=actividad;
    }

    @Override
    protected void onCancelled() {
        actividad.setCreandoRuta(false);
    }

    @Override
    protected void onPreExecute() {
        actividad.setCreandoRuta(true);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            Log.d("Task crear Ruta-->", "entra a pedir");

            // create routing features class
            NAFeaturesAsFeature naFeatures = new NAFeaturesAsFeature();
            String routeTaskURL = "http://tasks.arcgisonline.com/ArcGIS/rest/services/NetworkAnalysis/ESRI_Route_NA/NAServer/Long_Route";
            routeTask = RouteTask.createOnlineRouteTask(routeTaskURL, null);

            routeParams= routeTask.retrieveDefaultRouteTaskParameters();


            Graphic listaStopGraphic[] = new Graphic [actividad.listaDePuntos.size()];
            int i=0;
            for(Geometry punto : actividad.listaDePuntos){
                listaStopGraphic[i] = new StopGraphic(punto);
                i++;
            }

            naFeatures.setFeatures(listaStopGraphic);
            naFeatures.setCompressedRequest(true);
            // Configuro la búsqueda del recorrido óptimo
            routeParams.setFindBestSequence(true);
            routeParams.setPreserveFirstStop(true);
            routeParams.setPreserveLastStop(true);
            // No preservo el último punto elegido. Por defecto se conserva el primero
            routeParams.setPreserveLastStop(false);
            // Seteo la precision de la salida para que la ruta calculada quede por encima de la ruta real
            routeParams.setOutputGeometryPrecisionUnits(Unit.EsriUnit.DECIMETERS);
            // Seteo las paradas que tendrá la ruta
            routeParams.setStops(naFeatures);
            routeParams.setOutSpatialReference(actividad.sistCoordenadas);



            /*******TODO *******/
            /*Cuando presione un boton hay que llamar a esto****guardar ruta
            QueryParameters mParams = new QueryParameters();
            mParams.
            mParams.setSpatialRelationship(SpatialRelationship.INTERSECTS);
            mParams.setOutSpatialReference(actividad.sistCoordenadas);
            String mFeatureServiceURL = "http://services.arcgisonline.com/arcgis/rest/services/Demographics/USA_1990-2000_Population_Change/MapServer/3";
            QueryTask queryTask = new QueryTask(mFeatureServiceURL);

            resultsQuery = queryTask.execute(mParams);
            */

            mResults = routeTask.solve(routeParams);

/*
        NAFeaturesAsLayer naLayer = new NAFeaturesAsLayer();
        // set layer name with the layer in the service
        // containing stops
        naLayer.setLayerName("camino mas corto");
        // set where clause for 'Overnight' delivery_type
        //naLayer.setWhere("delivery_type='Overnight'");
        // set stops on routing layer
        routeParams.setStops(naLayer);
*/


            //trae la ruta y la agrega
            Log.d("Task crear Ruta-->", "obtuvo resultado de la red");


            List<Route> routes = mResults.getRoutes();
            Route mRoute = routes.get(0);

            // Access the whole route geometry and add it as a graphic
            Geometry routeGeom = mRoute.getRouteGraphic().getGeometry();

            symbolGraphicRuta = new Graphic(routeGeom, new SimpleLineSymbol(Color.BLUE, 3));

            actividad.ruta = (Polyline) symbolGraphicRuta.getGeometry();


            Log.d("Task crear Ruta-->", "va a ccrear la ruta de puntos por los que se va a mover");
            actividad.puntosDeRuta = new PuntosRuta(actividad.ruta);



        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }


    @Override
    protected void onPostExecute(Boolean result) {
        if (result != false){
            try {
                actividad.routeLayer.addGraphic(symbolGraphicRuta);
                actividad.iniciarSimulacion();
                Log.d("Task crear Ruta-->", "termino con el mapa");

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else{
            actividad.setCreandoRuta(false);
            Toast toast = Toast.makeText(actividad, "No se pudo crear la ruta", Toast.LENGTH_LONG);
            toast.show();
        }

    }

}