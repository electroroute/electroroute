package com.tfg.evelyn.electroroute_v10;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Evelyn on 19/03/2016.
 */
public class PlaceAsObject {

    private String id;

    private String name;

    private double lat = -1, lng = -1;

    private String address;

    private String description;

    private String type;

    private String image;


    private int contador;

    private float rating;



    private final List<String> types = new ArrayList<>();


    public PlaceAsObject(String n, double latitud, double longitud, String dir, String desc, String type, float rat, int cont){

        this.name=n;
        this.lat=latitud;
        this.lng=longitud;
        this.address=dir;
        this.description=desc;
        this.type=type;
        this.rating=rat;
        this.contador=cont;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lng;
    }

    public void setLon(double lon) {
        this.lng = lon;
    }

    public String getDirection() {
        return address;
    }

    public void setDirection(String direction) {
        this.address = direction;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getContador() {
        return contador;
    }

    public void setContador(int contador) {
        this.contador = contador;
    }

}
