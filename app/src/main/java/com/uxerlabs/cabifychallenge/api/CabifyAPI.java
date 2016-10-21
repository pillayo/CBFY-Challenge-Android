package com.uxerlabs.cabifychallenge.api;

import com.google.android.gms.maps.model.LatLng;
import com.uxerlabs.cabifychallenge.model.Vehicle;

import java.util.List;

//import javax.inject.Inject;

import rx.Observable;

/**
 * Manages methods to connect with API Cabify
 * @author Francisco Cuenca on 18/10/16.
 */

public class CabifyAPI {

    private CabifyRest client;

    //@Inject
    public CabifyAPI(String token) {
        this.client = CabifyRestService.createService(CabifyRest.class,token);
    }

    /*
        To get an estimate, start and end points need to be provided.
        @param startPoint Coordinate with start point
        @param endPoint Coordinate with end point
        @param startAt date to booking
        @return Reactive Observable for asynchronous call
     */
    public Observable<List<Vehicle>> getEstimate(LatLng startPoint, LatLng endPoint, String startAt) {
        EstimateBodyRequest estimateBodyRequest = EstimateBodyRequest.createEstimate(startPoint, endPoint, startAt);
        return this.client.getEstimate(estimateBodyRequest);
    }
}
