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
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class RadarFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = RadarFragment.class.getSimpleName();
    private static int UPDATE_INTERVAL_MS = 5000;
    private static int FASTEST_INTERVAL_MS = 2500;
    private static final int zoomLevel = 19;

    private static View view;
    SupportMapFragment mapFragment;
    GoogleMap map;
    private LatLng currentLatLng;
    private Double childAltitude;
    private Double currentAltitude;
    private LatLng childLatLng;
    private Location currentLoc;


    private double lonChildVelocity = 0.000015; // in unit of degree per second
    private double latChildVelocity = 0.000015; // in unit of degree per second
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
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
    }

    public void setupMap() {
        if (currentLatLng != null && map != null) {
            map.clear();
            float[] distance = new float[1];
            LatLng previousLocation = map.getCameraPosition().target;
            Location.distanceBetween(previousLocation.latitude, previousLocation.longitude,
                    currentLatLng.latitude, currentLatLng.longitude, distance);
            if (distance[0] > 200) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        currentLatLng, zoomLevel));
            }
            double safeRadius = ((Global) getActivity().getApplication()).getSafeRadiusInMeter();
            map.addCircle(new CircleOptions()
                    .center(currentLatLng)
                    .radius(safeRadius));

            // Fake child position
            if (childLatLng != null) {
                Log.d(TAG, "child red dot");
                map.addCircle(new CircleOptions()
                        .center(childLatLng)
                        .fillColor(Color.RED)
                        .strokeColor(Color.RED)
                        .radius(23 / map.getCameraPosition().zoom));
            }
            //map.addPolyline(new PolylineOptions().add(currentLatLng).add(childLatLng).geodesic(true));
            map.setMyLocationEnabled(false);
            drawMarker(currentLoc);
            map.setIndoorEnabled(true);
            addArrowToChildren(childLatLng);
        }

    }

    private void addArrowToChildren(LatLng childPos)
    {
        if(map != null)
        {
            //This is the current user-viewable region of the map
            LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
            LatLng center = bounds.getCenter();
            LatLng northeast = bounds.northeast;
            LatLng southwest = bounds.southwest;
            double distanceToEdge = SphericalUtil.computeDistanceBetween(center, new LatLng(center.latitude, northeast.longitude))*0.9;
            LatLng pos;
            // TODO
            if(!bounds.contains(childPos)) {
                // out of screen
                double heading = SphericalUtil.computeHeading(center, childPos);
                LatLng show = SphericalUtil.computeOffset(center, distanceToEdge, heading);
                map.addMarker(new MarkerOptions()
                        .position(show))
                        .setRotation((float)heading);
            }
        }
    }

    public void onLocationChanged(Location location) {
        Log.d(TAG, "on location changed");
        currentLoc = location;
        currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        currentAltitude = location.getAltitude();
        // Init child position
        /*
        if (currentLatLng == null || currentLatLng.equals(new LatLng(0, 0))) {
            Log.d(TAG, "child");
            childLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            if (childChangeLoc != null) {
                childChangeLoc.stopUpdates();
            }
            childChangeLoc = new RepeatAction(new Runnable() {
                @Override
                public void run() {
                    childLatLng = new LatLng(childLatLng.latitude- latChildVelocity, childLatLng.longitude-lonChildVelocity);
                }
            });
            childChangeLoc.startUpdates();
        }*/
        childLatLng = ((Global)getActivity().getApplication()).getChildLatLng();
        childAltitude = ((Global)getActivity().getApplication()).getChildAltitude();
        float[] distance = new float[1];
        if (currentLatLng != null && childLatLng != null && childAltitude != null) {
            Location.distanceBetween(childLatLng.latitude, childLatLng.longitude,
                    currentLatLng.latitude, currentLatLng.longitude, distance);
            Double dist = distance[0] / 0.308; // meter to feet
            Double altitudeDiff = (childAltitude - currentAltitude) / 0.308; // meter to feet
            if (getView() != null)
                ((TextView) (getView().findViewById(R.id.map_status))).setText("distance: " + dist.intValue() + "ft altitude: " +
                        altitudeDiff.intValue() + "ft");

            setupMap();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        MainActivity.radar = this;
    }

    @Override
    public void onPause() {
        MainActivity.radar = null;
        super.onPause();
    }

    private void drawMarker(Location location) {

        LatLng currentPosition = new LatLng(location.getLatitude(),
                location.getLongitude());
        map.addMarker(new MarkerOptions()
                .position(currentPosition)
                .snippet(
                        "Lat:" + location.getLatitude() + "Lng:"
                                + location.getLongitude())
                        // TODO change picture
                .icon(BitmapDescriptorFactory.defaultMarker())
                .rotation(location.getBearing())
                .title("position"));

    }



}
