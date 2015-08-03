package hyjjr.cs160.com.safe_radius;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.wearable.Wearable;

public class RadarFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback {

    private static final String TAG = RadarFragment.class.getSimpleName();
    private static int UPDATE_INTERVAL_MS = 5000;
    private static int FASTEST_INTERVAL_MS = 2500;
    private static final int zoomLevel = 19;

    private static View view;
    SupportMapFragment mapFragment;
    GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    private LatLng currentLoc;
    private LatLng childLoc;

    private double lonChildVelocity = 0.000005; // in unit of degree per second
    private double latChildVelocity = 0.000005; // in unit of degree per second
    private RepeatAction childChangeLoc;
    private boolean hasAlerted; // Once alert once every time open the map.
    // Actually, we should detect the movement once the app is opened, not only in the map fragment only.
    // change later.


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_radar, container, false);
        } catch (InflateException e) {
        /* map is already there, just return view as it is */
        }
        mapFragment = ((SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map));
        mapFragment.getMapAsync(this);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addApi(Wearable.API)  // used for data layer API
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        mapFragment = ((SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map));
        mapFragment.getMapAsync(this);

        try {
            if (map == null) {
                mapFragment.getMapAsync(this);
            }
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.addMarker(new MarkerOptions()
                    .position(new LatLng(0, 0))
                    .title("Marker"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onPause() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        super.onPause();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Init child position
        if (currentLoc == null || currentLoc.equals(new LatLng(0, 0))) {
            Log.d(TAG, "child");
            childLoc = new LatLng(location.getLatitude(), location.getLongitude());
            if (childChangeLoc != null) {
                childChangeLoc.stopUpdates();
            }
            childChangeLoc = new RepeatAction(new Runnable() {
                @Override
                public void run() {
                    childLoc = new LatLng(childLoc.latitude- latChildVelocity, childLoc.longitude-lonChildVelocity);
                }
            });
            childChangeLoc.startUpdates();
        }

        // Alert if child is too far
        boolean isDialogOpened;
        float[] distance = new float[1];
        if (currentLoc != null && childLoc != null) {
            Location.distanceBetween(childLoc.latitude, childLoc.longitude,
                    currentLoc.latitude, currentLoc.longitude, distance);
            if (distance[0] > ((Global) getActivity().getApplication()).getSafeRadiusInMeter()) {
                if (!hasAlerted) {
                    hasAlerted = true;
                    String title = "Warning";
                    String text = "Your child went out of the safe radius.";
                    Intent alertIntent = new Intent(getActivity(), AlertActivity.class);
                    alertIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    alertIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    alertIntent.putExtra("title", title);
                    alertIntent.putExtra("text", text);
                    startActivity(alertIntent);

                    // start Vibration
                    Intent vibrateIntent = new Intent(getActivity(), VibrationService.class);
                    getActivity().startService(vibrateIntent);
                }
            }
        }

        Log.d(TAG, "parent lat: " + location.getLatitude() + " lon: " + location.getLongitude());
        currentLoc = new LatLng(location.getLatitude(), location.getLongitude());
        if (childLoc != null) {
            Log.d(TAG, "child lat: " + childLoc.latitude + " lon: " + childLoc.longitude);
        }
        setupMap();


    }

    @Override
    public void onStop() {
        super.onStop();
        hasAlerted = false;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    @Override
    public void onConnected(Bundle bundle) {
        // Build a request for continual location updates
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_INTERVAL_MS);

        // Send request for location updates
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient,
                        locationRequest, this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.getStatus().isSuccess()) {
                            Log.d(TAG, "Location Successfully requested");
                        } else {
                            Log.e(TAG, status.getStatusMessage());
                        }
                    }
                });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
    }

    public void setupMap() {
        if (currentLoc != null) {
            map.clear();
            float[] distance = new float[1];
            LatLng previousLocation = map.getCameraPosition().target;
            Location.distanceBetween(previousLocation.latitude, previousLocation.longitude,
                    currentLoc.latitude, currentLoc.longitude, distance);
            if (distance[0] > 200) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        currentLoc, zoomLevel));
            }
            double safeRadius = ((Global) getActivity().getApplication()).getSafeRadiusInMeter();
            map.addCircle(new CircleOptions()
                    .center(currentLoc)
                    .radius(safeRadius));

            // Fake child position
            if (childLoc != null) {
                Log.d(TAG, "child red dot");
                map.addCircle(new CircleOptions()
                        .center(childLoc)
                        .fillColor(Color.RED)
                        .strokeColor(Color.RED)
                        .radius(23 / map.getCameraPosition().zoom));
            }
            map.setMyLocationEnabled(true);
            map.setIndoorEnabled(true);
        }

    }

}
