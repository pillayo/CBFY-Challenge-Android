package com.uxerlabs.cabifychallenge.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.uxerlabs.cabifychallenge.R;
import com.uxerlabs.cabifychallenge.utils.MapHelper;
import com.uxerlabs.cabifychallenge.utils.Utils;
import com.uxerlabs.cabifychallenge.api.CabifyAPI;
import com.uxerlabs.cabifychallenge.model.Vehicle;
import com.uxerlabs.cabifychallenge.view.adapters.VehicleAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnItemClick;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Launcher Activity with map and tools for to select start point and end point, calculate estimate cost.
 * @author Francisco Cuenca on 18/10/16.
 */

public class MapActivity extends BaseActivity implements MapHelper.MapHelperListener{

    private static final int MAKEROPTIONSA = R.mipmap.pin_a;
    private static final int MAKEROPTIONSB = R.mipmap.pin_b;
    private static final Long TIMEOUT = 30L;

    public enum SelectionPointType {
        SELECTING_START_POINT,
        SELECTING_END_POINT
    }

    Context context;
    SelectionPointType selectingPoint = SelectionPointType.SELECTING_START_POINT;

    private float viewOnTop;
    private float viewOnBelow;
    private int paddingTopEndPoint;

    private Marker markerStartPoint;
    private Marker markerEndPoint;
    private LatLng userLocation;

    private CabifyAPI api;

    @BindView(R.id.search_card_view) CardView searchCardView;

    @BindView(R.id.place_query_view) EditText queryView;

    @BindView(R.id.close_search_button) ImageButton closeSearchButton;

    @BindView(R.id.place_suggestions_list) ListView placeSuggestionsList;

    @BindView(R.id.start_point_card_view) CardView startPointCardView;

    @BindView(R.id.select_start_point) TextView selectStartText;

    @BindView(R.id.user_position_button) ImageButton userPostionButton;

    @BindView(R.id.end_point_card_view) CardView endPointCardView;

    @BindView(R.id.select_end_point) TextView selectEndPoint;

    @BindView(R.id.progress_bar) ProgressBar progressBar;

    @BindView(R.id.get_estimate_button) FloatingActionButton getEstimateButton;

    @BindView(R.id.progress_bar_floating) ProgressBar progressBarFloating;

    @BindView(R.id.bottom_sheet) LinearLayout bottomSheet;

    @BindView(R.id.vehicles_recycler_view) RecyclerView vehiclesRecyclerList;

    private VehicleAdapter vehicleAdapter;

    private List<Vehicle> vehiclesList = new ArrayList<>();

    private ReactiveLocationProvider reactiveLocationProvider;

    private MapHelper mapHelper;

    private BottomSheetBehavior bottomSheetBehavior;


    @Override
    public int getLayoutId(){
        return R.layout.activity_map;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        initServicesAndHelper();
        settingView();
        initObservableSearchAddress();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private void settingView(){

        viewOnTop = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics());
        viewOnBelow = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
        paddingTopEndPoint = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, getResources().getDisplayMetrics());

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(0);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    showActionButton(false);
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED
                        || newState == BottomSheetBehavior.STATE_HIDDEN) {
                    showActionButton(markerEndPoint != null && markerStartPoint != null);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });

        vehicleAdapter = new VehicleAdapter(this, vehiclesList);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        vehiclesRecyclerList.setLayoutManager(layoutManager);
        vehiclesRecyclerList.setAdapter(vehicleAdapter);

        progressBarFloating.getIndeterminateDrawable().setColorFilter(Color.DKGRAY, android.graphics.PorterDuff.Mode.MULTIPLY);

        queryView.clearFocus();

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.footer_list, null);
        placeSuggestionsList.addFooterView(view);
    }

    private void initServicesAndHelper(){
        mapHelper = new MapHelper(this, this);
        mapHelper.initMap(R.id.map, R.id.pin_select);
        api = new CabifyAPI(getString(R.string.cabify_api_token));
        reactiveLocationProvider = new ReactiveLocationProvider(this);
    }

    private void initObservableSearchAddress(){
        Observable<String> queryObservable = RxTextView
                .textChanges(queryView)
                .map(new Func1<CharSequence, String>() {
                    @Override
                    public String call(CharSequence charSequence) {
                        return charSequence.toString();
                    }
                })
                .debounce(1, TimeUnit.SECONDS)
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return s.length() >3;
                    }
                });

        Observable<AutocompletePredictionBuffer> suggestionsObservable = queryObservable
                .flatMap(new Func1<String, Observable<AutocompletePredictionBuffer>>() {
                    @Override
                    public Observable<AutocompletePredictionBuffer> call(String s) {
                        return reactiveLocationProvider.
                                getPlaceAutocompletePredictions(s, null, null)
                                .timeout(TIMEOUT,TimeUnit.SECONDS);
                    }
                });
        suggestionsObservable.subscribe(new Action1<AutocompletePredictionBuffer>() {
            @Override
            public void call(AutocompletePredictionBuffer buffer) {
                List<AutocompleteInfo> infos = new ArrayList<>();
                for (AutocompletePrediction prediction : buffer) {
                    infos.add(new AutocompleteInfo(prediction.getFullText(null).toString(), prediction.getPlaceId()));
                }
                buffer.release();
                placeSuggestionsList.setAdapter(new ArrayAdapter<>(MapActivity.this, android.R.layout.simple_list_item_1, infos));
            }
        },new Action1<Throwable>() {
            public void call(Throwable t1) {
                t1.printStackTrace();
            }
        });
    }



    @SuppressWarnings({"MissingPermission"})
    @Override
    protected void onLocationPermissionGranted() {
        if (mapHelper.userLocationGranted()) {
            reactiveLocationProvider.getLastKnownLocation()
                    .subscribe(new Action1<Location>() {
                        @Override
                        public void call(Location location) {
                            userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mapHelper.zoomToPosition(userLocation);
                        }
                    }, new Action1<Throwable>() {
                        public void call(Throwable t1) {
                            t1.printStackTrace();
                        }
                    });
        }
    }


    //CallBack Methods of listener
    @OnItemClick(R.id.place_suggestions_list)
    void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        showLoading(true);
        placeSuggestionsList.setVisibility(View.GONE);
        reactiveLocationProvider
                .getPlaceById(((AutocompleteInfo)parent.getItemAtPosition(position)).getId())
                .timeout(TIMEOUT,TimeUnit.SECONDS)
                .subscribe(new Action1<PlaceBuffer>() {
                    @Override
                    public void call(PlaceBuffer buffer) {
                        Place place = buffer.get(0);
                        if (place != null) {
                            selectPlaceInList(place);
                            searchCardView.setVisibility(View.INVISIBLE);
                        }
                        buffer.release();
                        showLoading(false);
                    }
                },new Action1<Throwable>() {
                    public void call(Throwable t1) {
                        showLoading(false);
                        t1.printStackTrace();
                    }
                });
    }

    @OnClick(R.id.select_start_point)
    void onClickStartPoint() {
        viewOverView(startPointCardView, endPointCardView);
        if (selectingPoint == SelectionPointType.SELECTING_START_POINT){
            searchCardView.setVisibility(View.VISIBLE);
            queryView.requestFocus();
        }
        moveSearchView(true);
        selectingPoint = SelectionPointType.SELECTING_START_POINT;
        if (markerStartPoint != null) mapHelper.zoomToPosition(markerStartPoint.getPosition());
    }

    @OnClick(R.id.select_end_point)
    void onClickEndPoint() {
        viewOverView(endPointCardView, startPointCardView);
        if (selectingPoint == SelectionPointType.SELECTING_END_POINT){
            searchCardView.setVisibility(View.VISIBLE);
            queryView.requestFocus();
        }
        moveSearchView(false);
        selectingPoint = SelectionPointType.SELECTING_END_POINT;
        if (markerEndPoint != null) mapHelper.zoomToPosition(markerEndPoint.getPosition());
    }

    @OnClick(R.id.user_position_button)
    void onClickUserLocation() {
        if (userLocation != null) {
            mapHelper.zoomToPosition(userLocation);
            onUserEndMoveMap(userLocation);
        } else
            Toast.makeText(MapActivity.this, getString(R.string.error_no_gps), Toast.LENGTH_SHORT).show();
    }

    @OnFocusChange(R.id.place_query_view)
    void onSearchFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            placeSuggestionsList.setVisibility(View.VISIBLE);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
        } else {
            placeSuggestionsList.setVisibility(View.GONE);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(),0);
        }
    }
    @OnClick(R.id.close_search_button)
    void closeSearch(){
        searchCardView.setVisibility(View.INVISIBLE);
        queryView.setText("");
        placeSuggestionsList.setVisibility(View.GONE);
    }

    @OnClick(R.id.get_estimate_button)
    public void getEstimate(){

        progressBarFloating.setVisibility(View.VISIBLE);
        LatLng startPoint = markerStartPoint.getPosition();
        LatLng endPoint = markerEndPoint.getPosition();

        mapHelper.centerMap();

        //Invoca a la api de Cabify y se subscribe para obtener la respuesta
        api.getEstimate(startPoint, endPoint, "").subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Vehicle>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        progressBarFloating.setVisibility(View.INVISIBLE);
                        showAlert(MapActivity.this.getString(R.string.error_comunication), MapActivity.this.getString(R.string.error_comunication_info));
                    }

                    @Override
                    public void onNext(List<Vehicle> vehicles) {
                        showEstimateResults(vehicles);
                        progressBarFloating.setVisibility(View.INVISIBLE);
                    }
                });
    }
    //END CallBack Methods of listener

    /*
    Se selecciona una dirección en la lista de búqueda de lugar
    @param place lugar seleccionado
     */
    private void selectPlaceInList(Place place){
        LatLng locationPoint = place.getLatLng();
        placeSelected(locationPoint, place.getAddress().toString());
    }

    /*
    Se selecciona una dirección interactuando con el mapa
    @param dirección seleccionada
     */
    private void selectAddressInMap(Address address){
        LatLng locationPoint = new LatLng(address.getLatitude(), address.getLongitude());
        placeSelected(locationPoint, address.getAddressLine(0));
    }

    /*
    se ha seleccionado un punto ya sea en el mapa o a través de la búsqueda
    @param position posición seleccionada
    @param address dirección de la posición seleccionada
     */
    private void placeSelected(LatLng position, String address){
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        if (selectingPoint == SelectionPointType.SELECTING_START_POINT){
            markerStartPoint = mapHelper.replaceMarkerForNewMarker(markerStartPoint, position, address, MAKEROPTIONSA);
            selectStartText.setText(address);
            if (markerEndPoint == null) showEndPoint();

        } else {
            markerEndPoint = mapHelper.replaceMarkerForNewMarker(markerEndPoint, position, address, MAKEROPTIONSB);
            selectEndPoint.setText(address);
        }
        mapHelper.zoomToPosition(position);

        showActionButton(markerEndPoint != null && markerStartPoint != null);
    }

    /*
    Se muestra la búsqueda del punto de fin
     */
    private void showEndPoint(){
        endPointCardView.setVisibility(View.VISIBLE);
        ValueAnimator varl = ValueAnimator.ofInt(0, paddingTopEndPoint);
        varl.setDuration(500);
        varl.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) endPointCardView.getLayoutParams();
                lp.setMargins(0, (Integer) animation.getAnimatedValue(), 0, 0);
                endPointCardView.setLayoutParams(lp);
            }
        });

        varl.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                onClickEndPoint();
            }
        });

        varl.start();
    }

    /*
    Una vez que el usuario ha terminado de mover el mapa se busca el lugar/dirección de la cordenada que queda en el centro del mapa
    @param position la posición que queda en el centro del mapa
     */
    @Override
    public void onUserEndMoveMap(final LatLng position){
        showLoading(true);
        reactiveLocationProvider
                .getReverseGeocodeObservable(position.latitude, position.longitude, 1)
                .timeout(TIMEOUT, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())               // use I/O thread to query for addresses
                .observeOn(AndroidSchedulers.mainThread())  // return result in main android thread to manipulate UI
                .subscribe(new Action1<List<Address>>() {
                    @Override
                    public void call(List<Address> addresses) {
                        if(!addresses.isEmpty()){
                            final Address addressFinding = addresses.get(0);
                            selectAddressInMap(addressFinding);
                        }
                        showLoading(false);
                    }
                },new Action1<Throwable>() {
                    public void call(Throwable t1) {
                        showLoading(false);
                        t1.printStackTrace();
                    }
                });
    }

    @Override
    public void onUserStartMoveMap(){
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    public void onMarkerSelected(Marker marker){
        if (markerStartPoint != null && marker.equals(markerStartPoint))
            onClickStartPoint();
        else if (markerEndPoint != null && marker.equals(markerEndPoint))
            onClickEndPoint();
    }

    /*
    Coloca la barra de búsqueda correctamente por encima de la slección del punto de origen o de destino, según la que esté activa
     */
    private void moveSearchView(boolean top){
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) searchCardView.getLayoutParams();
        params.setMargins(0, top ? 0 : paddingTopEndPoint, 0, 0);
        searchCardView.setLayoutParams(params);
    }

    /*
    Muestra la lista de vehiculos desde abajo, se mete un retardo para que se pinte correctamente el recyclerview
    @param vehiclesList Vehicles list
     */
    private void showEstimateResults(final List<Vehicle> vehiclesList){
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        vehicleAdapter.updateVehicles(vehiclesList);
                        vehiclesRecyclerList.scrollToPosition(0);
                    }
                },500);
    }

    private void showLoading(boolean show){
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showActionButton(boolean show) {
        if (show)
            getEstimateButton.show();
        else
            getEstimateButton.hide();
    }

    /*
    Superpone una vista sobre otra cambiando la elevación
    @param top vista que queda arriba
    @param below vista que queda abajo
     */
    private void viewOverView(View top, View below){
        below.setAlpha(0.75f);
        top.setAlpha(1.0f);
        Utils.changeElevation(top, viewOnTop);
        Utils.changeElevation(below, viewOnBelow);
    }

    private void showAlert(String title, String message){
        new AlertDialog.Builder(MapActivity.this)
                .setMessage(message)
                .setTitle(title)
                .setPositiveButton("Ok", null)
                .create()
                .show();
    }

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    /*
    Class place info
     */
    private static class AutocompleteInfo {
        private final String description;
        private final String id;

        private AutocompleteInfo(String description, String id) {
            this.description = description;
            this.id = id;
        }

        public String getId() {
            return id;
        }

        @Override
        public String toString() {
            return description;
        }
    }

}
