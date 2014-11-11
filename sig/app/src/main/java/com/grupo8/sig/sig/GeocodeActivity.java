package com.grupo8.sig.sig;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;

import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.FeatureEditResult;
import com.esri.core.map.FeatureTemplate;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.TextSymbol;
import com.esri.core.tasks.geocode.Locator;
import com.esri.core.tasks.geocode.LocatorFindParameters;

import java.util.GregorianCalendar;
import java.util.LinkedList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class GeocodeActivity extends Activity implements OnSingleTapListener{

    @InjectView(R.id.map)
    MapView mMapView;

    ArcGISTiledMapServiceLayer basemap;
    ArcGISFeatureLayer locationLayer;
    GraphicsLayer routeLayer;
    GraphicsLayer bufferLayer;
    GraphicsLayer movimientoAuto;
    GraphicsLayer countiesLayer;


   // ArcGISFeatureLayer rutaGuardada;

    @InjectView(R.id.editText)  EditText inputAddressText;
    @InjectView(R.id.informacionruta) View informacionRuta;
    @InjectView(R.id.rutear) View actionRutear;
    @InjectView(R.id.cargando)  FrameLayout cargando;
    @InjectView(R.id.bg_options)  View bgOpciones;
    @InjectView(R.id.poblacion) TextView poblacion;
    @InjectView(R.id.velocidad) TextView velocidad;
    @InjectView(R.id.gps) ImageButton gps;
    @InjectView(R.id.bg_options_GPS) View bg_bar_gps;
    @InjectView(R.id.aceleracionBar)  SeekBar selector_aceleracion;


    Locator locator;

    Polyline ruta = null;
    PuntosRuta puntosDeRuta;

    LinkedList<Geometry> listaDePuntos = new LinkedList<Geometry>();
    LinkedList <Polygon> listaCounties = new LinkedList<Polygon>();

    final static SpatialReference sistCoordenadas = SpatialReference.create(102100);
    final static SpatialReference wgs84 = SpatialReference.create(4326);

    SimulacionMovimiento simulador;


    int tamBuffer=500;
    int aceleracion = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        ButterKnife.inject(this);

        ///se a;ade el mapa base
        mMapView.addLayer(new ArcGISTiledMapServiceLayer("http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer"));
        //mMapView.addLayer(new ArcGISTiledMapServiceLayer("http://services.arcgisonline.com/arcgis/rest/services/Demographics/USA_1990-2000_Population_Change/MapServer/3"));

       // mMapView.addLayer(m);
        //locationLayer = new GraphicsLayer();
        locationLayer=new ArcGISFeatureLayer("http://sampleserver5.arcgisonline.com/arcgis/rest/services/LocalGovernment/Events/FeatureServer/0", ArcGISFeatureLayer.MODE.SELECTION);
        movimientoAuto =  new GraphicsLayer();
        routeLayer = new GraphicsLayer();
        bufferLayer = new GraphicsLayer();
        countiesLayer = new GraphicsLayer();

        mMapView.addLayer(locationLayer);
        mMapView.addLayer(countiesLayer);
        mMapView.addLayer(routeLayer);
        mMapView.addLayer(movimientoAuto);
        mMapView.addLayer(bufferLayer);

        locator =Locator.createOnlineLocator();

        setCreandoRuta(false);



        selector_aceleracion.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChanged = 1;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                progressChanged = progress;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                if (progressChanged>1)
                    aceleracion =progressChanged;
                else
                    aceleracion =1;
            }
        });


    }

    public synchronized void actualizarPoblacion(String poblacion){
        this.poblacion.setText("Poblacion:  " +poblacion);
    }

    public synchronized void actualizarVelocidad(int velocidad){
        this.velocidad.setText("Velocidad:  " +velocidad + " Km/h");
    }


    public void setBuscandoPunto(boolean buscando){
       if (!buscando){
           actionRutear.setVisibility(View.VISIBLE);
           cargando.setVisibility(View.GONE);
       }else {
           actionRutear.setVisibility(View.GONE);
           cargando.setVisibility(View.VISIBLE);
       }
    }

    public void setCreandoRuta(boolean creandoRuta){
        if (!creandoRuta){
            bgOpciones.setVisibility(View.VISIBLE);
            actionRutear.setVisibility(View.VISIBLE);
            cargando.setVisibility(View.GONE);
            mMapView.setOnSingleTapListener(this);

        }else {
            actionRutear.setVisibility(View.GONE);
            cargando.setVisibility(View.VISIBLE);
            bgOpciones.setVisibility(View.GONE);
            mMapView.setOnSingleTapListener(null);
        }
    }



    //se inicia el simulador del recorrido
    public void iniciarSimulacion(){
        actionRutear.setVisibility(View.GONE);
        bgOpciones.setVisibility(View.GONE);
        cargando.setVisibility(View.GONE);
        informacionRuta.setVisibility(View.VISIBLE);
        bg_bar_gps.setVisibility(View.VISIBLE);

        simulador = new SimulacionMovimiento(this, this.puntosDeRuta );
        simulador.excSimulation();
    }



    @OnClick(R.id.rutear)
    public void armarRuta() {
        new TaskCrearRuta(this).execute();
    }


    /*
     * Convert input address into geocoded point
     */
    @OnClick(R.id.button)
    public void address2Pnt() {
        try {
            String address = inputAddressText.getText().toString();
            // create Locator parameters from single line address string
            LocatorFindParameters findParams = new LocatorFindParameters(
                    address);
            // set the search country to USA
            findParams.setSourceCountry("USA");
            // limit the results to 2
            findParams.setMaxLocations(2);
            // set address spatial reference to match map
            findParams.setOutSR(wgs84);
            // execute async task to geocode address
            new TaskFindLocationToMap(this).execute(findParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void clearPoints() {
        // remove any previous graphics
        locationLayer.removeAll();
    }

/*
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.unpause();
    }
*/


    public void  guardarPunto(Geometry resultFormat1, String description, Geometry resultFormat2){
        //se crea el punto

        listaDePuntos.add(resultFormat2);

        SimpleMarkerSymbol resultSymbol = new SimpleMarkerSymbol( Color.BLUE, 20, SimpleMarkerSymbol.STYLE.CIRCLE);
        //Geometry p = resultLocGeom;
        Graphic resultLocation = new Graphic(resultFormat1, resultSymbol);

        //se guarda el punto
        FeatureTemplate template = locationLayer.getTypes()[0].getTemplates()[0];
        template.getPrototype().put("eventid", 20148);
        template.getPrototype().put("event_type", 5);
        template.getPrototype().put("description", description);
        template.getPrototype().put("eventdate", GregorianCalendar.getInstance().getTime());
    
        //Point p = (Point) GeometryEngine.project(result.getGeometry(), this.wgs84, this.sistCoordenadas);

        Graphic g = locationLayer.createFeatureWithTemplate(template, resultLocation.getGeometry());

        locationLayer.applyEdits(new Graphic[] {g}, null, null, new CallbackListener<FeatureEditResult[][]>() {
            public void onError(Throwable e) {
                //error = true; TODO
            }

            public void onCallback(FeatureEditResult[][] objs) {
                locationLayer.setVisible(true);
            }
        });



        //se a;ade el texto
        TextSymbol resultAddress = new TextSymbol(12, description, Color.BLACK);
        // create offset for text
        resultAddress.setOffsetX(10);
        resultAddress.setOffsetY(50);
        // create a graphic object for address text
        Graphic resultText = new Graphic(resultFormat1, resultAddress);
        // add address text graphic to location graphics layer
        locationLayer.addGraphic(resultText);
        // zoom to geocode result
        mMapView.zoomToResolution( ((Point)resultFormat1), 2);

        locationLayer.refresh();

    }


    //cuando tocan el el mapa
    @Override
    public void onSingleTap(final float x, final float y) {
        final Point loc = mMapView.toMapPoint(x, y);
        try {
            if (loc != null) {
                new TaskFindLocationByTouch(GeocodeActivity.this).execute(loc);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    //

    boolean focusInCar = true;

    @OnClick(R.id.gps)
    public void activarFoco() {
        focusInCar= !focusInCar;
        if (focusInCar){
          gps.setImageResource(R.drawable.ic_gps_active);
        }else{
            gps.setImageResource(R.drawable.ic_gps_disabled);
        }
    }



    public boolean getFocusInCar(){
        return focusInCar;
    }


    public int getAceleracion(){
        return aceleracion;
    }

    public int getTamBuffer(){
        return tamBuffer;
    }
}
