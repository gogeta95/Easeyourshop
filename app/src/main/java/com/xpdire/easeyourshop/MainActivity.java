package com.xpdire.easeyourshop;

import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends ActionBarActivity implements TouchableWrapper.UpdateMapAfterUserInteraction, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    GoogleMap map;
    Location lastlocation;
    Toolbar maptoolbar;
    private GoogleApiClient mGoogleApiClient;
    private PlaceAutocompleteAdapter placeAutocompleteAdapter;
    private Marker lastmarker;
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.e("ass", "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);

            //Remove Old Marker.
            if (lastmarker!=null){
                lastmarker.remove();
            }

            // Format details of the place for display and show it in  the Map.
//            Check For Null bounds.
            if (place.getViewport()!=null){
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(place.getViewport(),400,400,0/*Set desired Padding*/);
                lastmarker=map.addMarker(new MarkerOptions().title(place.getName().toString()).snippet(place.getAddress().toString()).position(place.getLatLng()));
                map.animateCamera(cameraUpdate);
            }
            else if (place.getLatLng()!=null){
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(place.getLatLng(),15);
                lastmarker=map.addMarker(new MarkerOptions().title(place.getName().toString()).snippet(place.getAddress().toString()).position(place.getLatLng()));
                map.animateCamera(cameraUpdate);
            }



            // Display the third party attributions if set.


            Log.i("ass", "Place details received: " + place.getName());

            places.release();
        }
    };

    private static Spanned formatPlaceDetails(Resources res, CharSequence name, String id,
                                              CharSequence address, CharSequence phoneNumber, Uri websiteUri) {
        Log.e("ass", res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));
        return Html.fromHtml(res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));

    }

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
        suggestions.setAdapter(placeAutocompleteAdapter=new PlaceAutocompleteAdapter(this, android.R.layout.simple_list_item_1, mGoogleApiClient, WHOLE_EARTH, null));
        suggestions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               final PlaceAutocompleteAdapter.PlaceAutocomplete item = placeAutocompleteAdapter.getItem(position);
                /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
              details about the place.
              */
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(mGoogleApiClient, item.placeId.toString());
                placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            }
        });
        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                lastlocation = location;
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                lastmarker=map.addMarker(new MarkerOptions().title("Its Me!").snippet("My current location.").position(latLng));
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
