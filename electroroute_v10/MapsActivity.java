package com.tfg.evelyn.electroroute_v10;

/*
 * Copyright (C) 2015 Google Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import android.text.Html;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import 	android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;


import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
{

    private GoogleMap mMap;

    private GoogleApiClient mGoogleApiClient;
    private static final String tag = "myloginfo";

    /* GPS Constant Permission */
    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 12;

    private double lat,lon;
    private String name,tipo,dir,des,imagen;
    private float rat;
    private int cont;

    private String action = "SEARCH";

    private AlertDialog alert = null;

    Marker lastOpened = null;

    ArrayList<PlaceAsObject> placesList = new ArrayList<>();

    private LoadPlaceActivity places;
    boolean nuevo_sitio_creado;
    LatLng actualLocation;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Get the the places from BBDD
        places = new LoadPlaceActivity(this);
        places.runLoadPlacesActivity();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        nuevo_sitio_creado = false;


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

        // Add a marker in Sydney and move the camera
        //mMap.addMarker(new MarkerOptions().position(location).title("Marker"));
        if ( ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions(MapsActivity.this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},
                   MY_PERMISSION_ACCESS_COARSE_LOCATION);
        }
        mMap.setMyLocationEnabled(true);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
        mMap.animateCamera(zoom);


        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                LocationService locationService = LocationService.getLocationManager(MapsActivity.this);
                if (locationService.canGetLocation()) {
                    lat = locationService.getLatitude();
                    lon = locationService.getLongitude();


                } else {
                    locationService.showSettingsAlert();
                }

                //if ((location.getLatitude() != lat) || (location.getLongitude()!=lon)){
                    CameraUpdate cam = CameraUpdateFactory.newLatLng(new LatLng(lat, lon));
                    mMap.moveCamera(cam);
               // }
                actualLocation= new LatLng(lat, lon);

                places.runLoadPlacesActivity();
                addNearPlaces(places.getPlacesList());


            }
        });
           // mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Log.i(tag, latLng.toString());
                crearSitio(latLng.latitude, latLng.longitude);
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            public boolean onMarkerClick(Marker marker) {
                // Check if there is an open info window
                if (lastOpened != null) {
                    // Close the info window
                    lastOpened.hideInfoWindow();

                    // Is the marker the same marker that was already open
                    if (lastOpened.equals(marker)) {
                        // Nullify the lastOpened object
                        lastOpened = null;
                        // Return so that the info window isn't opened again
                        return true;
                    }
                }

                getInfoContents(marker.getPosition().latitude, marker.getPosition().longitude);
                // Open the info window for the marker
                //marker.showInfoWindow();
                Intent i = new Intent(MapsActivity.this,ShowInfoMarker.class);
                i.putExtra("EXTRA_LATITUDE", marker.getPosition().latitude);
                i.putExtra("EXTRA_LONGITUDE", marker.getPosition().longitude);
                i.putExtra("EXTRA_TIPO", tipo);
                i.putExtra("EXTRA_NAME", name);
                i.putExtra("EXTRA_DIR", dir);
                i.putExtra("EXTRA_DES", des);
                i.putExtra("EXTRA_RTNG", rat);
                i.putExtra("EXTRA_CONT", cont);
                i.putExtra("EXTRA_LAT", lat);
                i.putExtra("EXTRA_LON", lon);

                startActivity(i);

                // Re-assign the last opened such that we can close it later
                lastOpened = marker;

                // Event was handled by our code do not launch default behaviour.
                return true;
            }
        });


    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode)
        {
            case MY_PERMISSION_ACCESS_COARSE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                } else {
                    // permission denied
                }
            }
            break;
            case MY_PERMISSION_ACCESS_FINE_LOCATION : {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // permission was granted
                    } else {
                        // permission denied
                    }
                    break;
                }

            }
    }


    private void addNearPlaces(ArrayList<PlaceAsObject> placesList){
        Log.i("****LUGARES 2 **", String.format("Place: " + placesList.size()));
        for (PlaceAsObject place : placesList){

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(place.getLat(), place.getLon()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_green_car))
                    .title(place.getName().toString())
                    .snippet("Direccion:" + place.getDirection()));
        }
        Log.i("****LUGARES **", "TERMINO EL ADDNEAR");
    }

    private void crearSitio(final double la, final double lo) {
        // Use the Builder class for convenient dialog construction
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.create_place)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent i = new Intent(MapsActivity.this, CreatePlaceActivity.class);
                        i.putExtra("EXTRA_LATITUDE", la);
                        i.putExtra("EXTRA_LONGITUDE", lo);
                        startActivity(i);
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.cancel();
                    }
                });
            // Create the AlertDialog object and return it
            nuevo_sitio_creado = true;
            alert= builder.create();
            alert.show();
    }

    public void getInfoContents(double lat, double lng) {
        // Getting the position from the marker
        Log.i("****MARKER****", String.format("MARKER: " + lat + " " + lng));
        Log.i("**LIST PLACE **", String.format("Place: " + placesList.size()));
        // Setting the info
        placesList = places.getPlacesList();
        for (PlaceAsObject place : placesList) {
            Log.i("****LIST PLACE****", String.format("PLACE: " + place));
            if ((place.getLat() == lat) && (place.getLon() == lng)) {
                tipo = "Tipo: " + place.getType();
                name = place.getName();
                dir = "Dirección: " + place.getDirection();
                des = "Descripción: " + place.getDescription();
                imagen = place.getImage();
                rat= place.getRating();
                cont = place.getContador();
            }
        }
    }





}
