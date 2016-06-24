package com.tfg.evelyn.electroroute_v10;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsDirectionsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener {

    private GoogleMap mMap;
    Double lat, lon, lat_marker, lon_marker;
    String dir;
    static String dir_o, dir_e;
    LatLng start, end;
    static TextView  txtOrigen, txtDestination;
    TextView txtDistance, txtDuration;
    Button btnFindPath;
    static int cont = 0;

    private static final String tag = "CREATE_PLACE_ACTIVITY";


    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_directions);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Obteniendo la instancia del Intent
        Intent intent = getIntent();
        lat = intent.getDoubleExtra("EXTRA_ORIGEN_LAT", 0.1);
        lon = intent.getDoubleExtra("EXTRA_ORIGEN_LON", 0.1);
        lat_marker = intent.getDoubleExtra("EXTRA_DESTINATION_LAT", 0.1);
        lon_marker = intent.getDoubleExtra("EXTRA_DESTINATION_LON", 0.1);
        dir = intent.getStringExtra("EXTRA_DESTINATION_DIR");

        btnFindPath = (Button)findViewById(R.id.btnFindPath);
        txtOrigen = (TextView)findViewById(R.id.route_origin);
        txtDestination = (TextView) findViewById(R.id.route_destination);
        txtDistance = (TextView) findViewById(R.id.route_distance);
        txtDuration = (TextView) findViewById(R.id.route_duration);

        //txtDestination.setText(dir.substring(11));
        getAddressFromLocation(lat,lon, MapsDirectionsActivity.this, new GeocoderHandler());
        getAddressFromLocation(lat_marker,lon_marker, MapsDirectionsActivity.this, new GeocoderHandler());


        btnFindPath.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //txtDestination.setText(dir.substring(11));
                sendRequest();
            }
        });



        start = new LatLng(lat, lon);
        end = new LatLng(lat_marker, lon_marker);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start,18));

        //Satellite view with roads
        //mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

    }



    private void sendRequest(){
        try{
            new DirectionFinder(this,dir_o,dir_e).execute();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
    }


    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            txtDuration.setText(route.duration.text);
            txtDistance.setText(route.distance.text);

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_maps_pin_drop))
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_room))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.GREEN).
                    width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }

    //GEODECODER : Obtener la dirección a partir de las coordenadas.
    protected void getAddressFromLocation(
            final double lat, final double lng, final Context context, final Handler handler) {
        Thread thread = new Thread() {
            @Override public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                String result = null;
                try {
                    List<Address> list = geocoder.getFromLocation(lat,lng,1);
                    if (list != null && list.size() > 0) {
                        Address address = list.get(0);
                        // sending back first address line and locality
                        result = address.getAddressLine(0) + ", " + address.getLocality();
                    }
                } catch (IOException e) {
                    Log.e(tag, "Impossible to connect to Geocoder", e);
                } finally {
                    Message msg = Message.obtain();
                    msg.setTarget(handler);
                    if (result != null) {
                        msg.what = 1;
                        Bundle bundle = new Bundle();
                        bundle.putString("address", result);
                        msg.setData(bundle);
                    } else
                        msg.what = 0;
                    msg.sendToTarget();
                }
            }
        };
        thread.start();
    }


    //GEODECODER : Mostrar la dirección obtenida en pantalla

    public static class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String result;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    result = bundle.getString("address");
                    break;
                default:
                    result = null;
            }
            // replace by what you need to do
            if (cont == 0) {
                txtOrigen.setText(eliminarCaracteresEspeciales(result));
                dir_o = txtOrigen.getText().toString();

                cont = 1;
            }else {
                txtDestination.setText(eliminarCaracteresEspeciales(result));
                dir_e = txtDestination.getText().toString();
                cont =0;
            }
        }
    }

    private static String eliminarCaracteresEspeciales(String s){
        // Cadena de caracteres original a sustituir.
        String original = "áàäéèëíìïóòöúùuñÁÀÄÉÈËÍÌÏÓÒÖÚÙÜÑçÇ";
        // Cadena de caracteres ASCII que reemplazarán los originales.
        String ascii = "aaaeeeiiiooouuunAAAEEEIIIOOOUUUNcC";
        String output = s;

        for(int i=0; i<s.length(); i++) {
            for (int j = 0; i < original.length(); i++) {
                // Reemplazamos los caracteres especiales.
                if (s.charAt(i) == original.charAt(j)){
                    output = output.replace(s.charAt(i), ascii.charAt(j));
                }

            }//for i
        }
        return output;

    }


}
