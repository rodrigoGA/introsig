package com.grupo8.sig.sig;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.esri.core.geometry.AreaUnit;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.Unit;
import com.esri.core.map.Feature;
import com.esri.core.map.FeatureResult;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.tasks.SpatialRelationship;
import com.esri.core.tasks.na.NAFeaturesAsFeature;
import com.esri.core.tasks.na.Route;
import com.esri.core.tasks.na.RouteParameters;
import com.esri.core.tasks.na.RouteResult;
import com.esri.core.tasks.na.RouteTask;
import com.esri.core.tasks.na.StopGraphic;
import com.esri.core.tasks.query.QueryParameters;
import com.esri.core.tasks.query.QueryTask;

import java.util.List;

/**
 * Created by Rodrigo on 11/11/2014.
 */
public class TaskCargarPuntosGuardados  extends AsyncTask<Void, Void, Boolean> {
    GeocodeActivity actividad;
    FeatureResult resultsQuery;

    public TaskCargarPuntosGuardados(GeocodeActivity actividad){
        this.actividad=actividad;
    }

    @Override
    protected void onPreExecute() {


    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {

            QueryParameters mParams = new QueryParameters();
            mParams.setReturnGeometry(true);
            mParams.setWhere("eventid=" + actividad.getIdToSave());
            String mFeatureServiceURL = "http://sampleserver5.arcgisonline.com/arcgis/rest/services/LocalGovernment/Events/FeatureServer/0";
            QueryTask queryTask = new QueryTask(mFeatureServiceURL);
            resultsQuery = queryTask.execute(mParams);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }


    @Override
    protected void onPostExecute(Boolean result) {
        if (result != false){


            for (Object element : resultsQuery) {
                if (element instanceof Feature) {
                    Feature f = (Feature) element;
                    Geometry p2 = GeometryEngine.project(f.getGeometry(),  actividad.wgs84, actividad.sistCoordenadas);

                    SimpleMarkerSymbol resultSymbol = new SimpleMarkerSymbol( Color.BLUE, 20, SimpleMarkerSymbol.STYLE.CIRCLE);
                    Graphic point= new Graphic(p2, resultSymbol);

                    actividad.locationLayer.addGraphic(point);

                    actividad.listaDePuntos.add(f.getGeometry());

                }
            }



        } else{
            actividad.setCreandoRuta(false);
            Toast toast = Toast.makeText(actividad, "No se pudo cargar los puntos en la nube", Toast.LENGTH_LONG);
            toast.show();
        }

    }

}