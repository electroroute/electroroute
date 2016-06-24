package com.tfg.evelyn.electroroute_v10;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;


/**
 * Created by Evelyn on 05/06/2016.
 */
public class Route {

    public Distance distance;
    public Duration duration;
    public String endAddress;
    public LatLng endLocation;
    public String startAddress;
    public LatLng startLocation;

    public List<LatLng> points;
}
