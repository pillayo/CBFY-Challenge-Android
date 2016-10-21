package com.uxerlabs.cabifychallenge.api;

import com.google.android.gms.maps.model.LatLng;
import com.uxerlabs.cabifychallenge.model.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the estimate request body. Serialize with GSON
 * @author Francisco Cuenca on 18/10/16.
 */

public class EstimateBodyRequest {

    // stops list
    List<Point> stops = new ArrayList<>();
    // Date to booking
    private String startAt;


    public EstimateBodyRequest(List<Point> stops, String startAt) {
        this.stops = stops;
        this.startAt = startAt;
    }

    /*
    Factory. Return the estimate request body
    @param startPoint Coordinate with start point
    @param endPoint Coordinate with end point
    @param startAt Date to booking
     */
    public static EstimateBodyRequest createEstimate(LatLng startPoint, LatLng endPoint, String startAt){
        List<Point> points = new ArrayList<>();
        points.add(new Point(startPoint));
        points.add(new Point(endPoint));
        return new EstimateBodyRequest(points, startAt);
    }
}
