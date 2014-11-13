package com.grupo8.sig.sig;

import android.os.AsyncTask;
import android.widget.Toast;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.tasks.geocode.LocatorReverseGeocodeResult;

/**
 * Created by Rodrigo on 08/11/2014.
 */
public class TaskFindLocationByTouch  extends   AsyncTask<Point, Void, LocatorReverseGeocodeResult> {

    MainActivity actividad;

    public TaskFindLocationByTouch(MainActivity actividad){
        this.actividad=actividad;
    }

    @Override
         protected void onPreExecute() {
        actividad.setBuscandoPunto(true);
    }

    @Override
    protected void onCancelled() {
        actividad.setBuscandoPunto(false);
    }


    @Override
    protected LocatorReverseGeocodeResult doInBackground(Point... params) {

        Point location = params[0];
        SpatialReference sr = actividad.mMapView.getSpatialReference();
        double distance = 100.0;
        LocatorReverseGeocodeResult result = null;
        try {
            result = actividad.locator.reverseGeocode(location, distance, sr, sr);
        } catch (Exception e) {
            e.getMessage();
        }
        return result;
    }


    @Override
    protected void onPostExecute(LocatorReverseGeocodeResult result) {

        if (result != null ){
            Geometry resultLocGeom  =result.getLocation();
            Geometry p2 = GeometryEngine.project(resultLocGeom, actividad.sistCoordenadas, actividad.wgs84);
            actividad.guardarPunto(resultLocGeom, result.getAddressFields().get("Address"), p2);

        } else{
            Toast toast = Toast.makeText(actividad, "Ubicacion no encontrada.", Toast.LENGTH_LONG);
            toast.show();
        }

        actividad.setBuscandoPunto(false);



    }


}