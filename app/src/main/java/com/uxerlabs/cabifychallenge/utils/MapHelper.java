package com.uxerlabs.cabifychallenge.utils;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper with all methods and listener to handle a Google map, like, zoom, add marker.
 * @author Francisco Cuenca on 20/10/16.
 */

public class MapHelper implements OnMapReadyCallback,
        GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener, GoogleMap.OnMarkerClickListener {

    public interface MapHelperListener {
        // The user has started to move the map
        void onUserStartMoveMap();
        // User has finished to move the map
        void onUserEndMoveMap(LatLng position);
        // The user has selected a marker in Map
        void onMarkerSelected(Marker marker);

    }

    public final static int MIN_ZOOM_TO_SELECT = 14;

    private GoogleMap mMap;
    private ImageView pinSelect;

    private FragmentActivity activity;
    private MapHelperListener mapHelperListener;
    boolean userMoveMap = false;

    private List<Marker> markers;


    public MapHelper(FragmentActivity activity, MapHelperListener listener) {
        this.activity = activity;
        this.mapHelperListener = listener;
        markers = new ArrayList<>();
    }

    public void initMap(int mapId, int pinId){
        SupportMapFragment mapFragment = (SupportMapFragment) activity.getSupportFragmentManager()
                .findFragmentById(mapId);
        mapFragment.getMapAsync(this);
        pinSelect = (ImageView) activity.findViewById(pinId);
    }

    public Marker addMarkerType(LatLng position, String address, int icon){
        Marker marker = mMap.addMarker(new MarkerOptions().position(position).title(address).icon(BitmapDescriptorFactory.fromResource(icon)));
        markers.add(marker);
        return marker;
    }

    public Marker replaceMarkerForNewMarker(Marker marker, LatLng position, String address, int icon){
        if (marker != null) {
            markers.remove(marker);
            marker.remove();
        }
        return addMarkerType(position,address,icon);
    }


    public void zoomToPosition(LatLng position){
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, MIN_ZOOM_TO_SELECT));

    }

    public void centerMap() {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }

        LatLngBounds latLngBounds = builder.build();
        int padding = 20; // offset from edges of the map in pixels
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, 500, 500,padding);

        mMap.animateCamera(cameraUpdate);

    }

    @SuppressWarnings({"MissingPermission"})
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (userLocationGranted()) {
            mMap.setMyLocationEnabled(true);
        }
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveStartedListener(this);
        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public void onCameraMoveStarted(int reason) {

        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE
                && mMap.getCameraPosition().zoom >= MIN_ZOOM_TO_SELECT) {

            userMoveMap = true;
            pinSelect.setVisibility(View.VISIBLE);
            mapHelperListener.onUserStartMoveMap();

        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_API_ANIMATION) {

        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_DEVELOPER_ANIMATION) {
        }
    }


    @Override
    public void onCameraIdle() {
        if (userMoveMap
                && mMap.getCameraPosition().zoom >= MIN_ZOOM_TO_SELECT) {
            userMoveMap = false;
            LatLng center = mMap.getCameraPosition().target;
            mapHelperListener.onUserEndMoveMap(center);
        }
        pinSelect.setVisibility(View.GONE);
    }

    public boolean userLocationGranted(){
        return  ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                ||ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        Log.i("MAPHELPER", "Marker touch");
        mapHelperListener.onMarkerSelected(marker);
        return true;
    }

}
