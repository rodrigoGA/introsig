package com.grupo8.sig.sig;

import android.os.AsyncTask;
import android.widget.Toast;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.tasks.geocode.LocatorFindParameters;
import com.esri.core.tasks.geocode.LocatorGeocodeResult;

import java.util.List;

/**
 * Created by Rodrigo on 07/11/2014.
 */
 /*
     * AsyncTask to geocode an address to a point location Draw resulting point
     * location on the map with matching address
     */
public class TaskFindLocationToMap extends AsyncTask<LocatorFindParameters, Void, List<LocatorGeocodeResult>> {

    GeocodeActivity actividad;

    public TaskFindLocationToMap(GeocodeActivity actividad){
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
    protected List<LocatorGeocodeResult> doInBackground(LocatorFindParameters... params) {
        List<LocatorGeocodeResult> results = null;

        try {
            results = actividad.locator.find(params[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }



    protected void onPostExecute(List<LocatorGeocodeResult> result) {
        if (result == null || result.size() == 0) {
            Toast toast = Toast.makeText(actividad, "Ubicacion no encontrada.", Toast.LENGTH_LONG);
            toast.show();
        } else {
            // get return geometry from geocode result
            Geometry resultLocGeom = result.get(0).getLocation();
            Geometry p = GeometryEngine.project(resultLocGeom, actividad.wgs84, actividad.sistCoordenadas);

            actividad.guardarPunto(p, result.get(0).getAddress(), resultLocGeom);
        }

        actividad.setBuscandoPunto(false);

    }


}