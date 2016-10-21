package com.uxerlabs.cabifychallenge.api;

import com.uxerlabs.cabifychallenge.model.Vehicle;

import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Interface with methods to connect with API Cabify
 * @author Francisco Cuenca on 18/10/16.
 * @see retrofit2.Retrofit
 */

public interface CabifyRest {

    @POST("/api/v2/estimate")
    @Headers({
            "Content-Type:application/json",
            "Accept-Language:en"
    })
    Observable<List<Vehicle>> getEstimate(@Body EstimateBodyRequest body);
}
