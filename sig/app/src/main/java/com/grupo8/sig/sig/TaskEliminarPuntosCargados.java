package com.grupo8.sig.sig;

import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.Toast;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.Feature;
import com.esri.core.map.FeatureEditResult;
import com.esri.core.map.FeatureResult;
import com.esri.core.map.FeatureTemplate;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.tasks.query.QueryParameters;
import com.esri.core.tasks.query.QueryTask;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Rodrigo on 11/11/2014.
 */
public class TaskEliminarPuntosCargados extends AsyncTask<Void, Void, Boolean> {
    GeocodeActivity actividad;
    FeatureResult resultsQuery;
    Graphic [] gA;

    public TaskEliminarPuntosCargados(GeocodeActivity actividad){
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
            mParams.setWhere("eventid=20148");
            String mFeatureServiceURL = "http://sampleserver5.arcgisonline.com/arcgis/rest/services/LocalGovernment/Events/FeatureServer/0";
            QueryTask queryTask = new QueryTask(mFeatureServiceURL);
            resultsQuery = queryTask.execute(mParams);


            ArrayList<Graphic> gRemove  = new ArrayList();


            for (Object element : resultsQuery) {
                if (element instanceof Feature) {
                    Feature f = (Feature) element;

                    Graphic g = new Graphic(f.getGeometry(),  f.getSymbol(), f.getAttributes());
                    gRemove.add(g);
                }
            }

            gA = new Graphic[gRemove.size()];
            gA=gRemove.toArray(gA);






        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }


    @Override
    protected void onPostExecute(Boolean result) {
        if (result != false){

            actividad.locationLayer.applyEdits(null,  gA , null, new CallbackListener<FeatureEditResult[][]>() {
                public void onError(Throwable e) {
                    //error = true; TODO
                }

                public void onCallback(FeatureEditResult[][] objs) {
                    // locationLayer.setVisible(true);
                }
            });



        } else{
            actividad.setCreandoRuta(false);
            Toast toast = Toast.makeText(actividad, "No se pudo eliminar los puntos de la nube", Toast.LENGTH_LONG);
            toast.show();
        }

    }

}