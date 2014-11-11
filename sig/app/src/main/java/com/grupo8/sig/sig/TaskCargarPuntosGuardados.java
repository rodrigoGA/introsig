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
 * Created by Rodrigo on 11/11/2014.
 */
public class TaskCargarPuntosGuardados  extends AsyncTask<Void, Void, Boolean> {
    GeocodeActivity actividad;

    public TaskCargarPuntosGuardados(GeocodeActivity actividad){
        this.actividad=actividad;
    }

    @Override
    protected void onPreExecute() {


    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {



        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }


    @Override
    protected void onPostExecute(Boolean result) {
        if (result != false){




        } else{
            actividad.setCreandoRuta(false);
            Toast toast = Toast.makeText(actividad, "No se pudo crear la ruta", Toast.LENGTH_LONG);
            toast.show();
        }

    }

}