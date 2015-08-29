package com.xpdire.easeyourshop;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AutoCompleteTextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends ActionBarActivity implements TouchableWrapper.UpdateMapAfterUserInteraction, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    GoogleMap map;
    Location lastlocation;
    Toolbar maptoolbar;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Places.GEO_DATA_API).addApi(Places.PLACE_DETECTION_API).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        map = ((MySupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        maptoolbar = (Toolbar) findViewById(R.id.map_toolbar);
        this.setSupportActionBar(maptoolbar);
        map.setMyLocationEnabled(true);
        map.getUiSettings().setAllGesturesEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(true);
//        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.setPadding(0, 120, 0, 0);
        map.getUiSettings().setZoomControlsEnabled(true);
        final AutoCompleteTextView suggestions = (AutoCompleteTextView) findViewById(R.id.suggestions);
        final LatLngBounds WHOLE_EARTH = new LatLngBounds(new LatLng(-84.9, -180), new LatLng(84.9, 180));
        suggestions.setAdapter(new PlaceAutocompleteAdapter(this, android.R.layout.simple_list_item_1, mGoogleApiClient, WHOLE_EARTH, null));
        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                lastlocation = location;
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                map.addMarker(new MarkerOptions().title("Its Me!").snippet("My current location.").position(latLng));
                map.animateCamera(cameraUpdate);
                map.setOnMyLocationChangeListener(null);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onMapActionUp() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_top);
        maptoolbar.startAnimation(anim);
        maptoolbar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMapActionDown() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.abc_slide_out_top);
        maptoolbar.startAnimation(anim);
        maptoolbar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
