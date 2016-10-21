package com.uxerlabs.cabifychallenge.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Map Coordinates, latitude and longitude
 * @author Francisco Cuenca on 19/10/16.
 */

public class Point {

    double[] loc;

    public Point(LatLng latLon) {
        loc = new double[] {latLon.latitude, latLon.longitude};
    }

    /*
    Returns coordinate like array [latitude,longitue]
     */
    @Override
    public String toString() {
        return loc.toString();
    }
}
