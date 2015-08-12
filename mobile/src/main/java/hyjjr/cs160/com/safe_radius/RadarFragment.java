package hyjjr.cs160.com.safe_radius;

import android.graphics.Color;
import android.graphics.Typeface;
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

public class RadarFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = RadarFragment.class.getSimpleName();
    private static final int ZOOM_LEVEL = 19;

    private static View view;
    SupportMapFragment mapFragment;
    GoogleMap map;
    private LatLng currentLatLng;
    private Double childAltitude;
    private Double currentAltitude;
    private LatLng childLatLng;

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

        Typeface custom_font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/gotham.ttf");
        ((TextView)getView().findViewById(R.id.map_status)).setTypeface(custom_font);

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
            if (map.getCameraPosition().zoom < 5) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        currentLatLng, ZOOM_LEVEL));
            }
            map.clear();
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMapToolbarEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);
            map.getUiSettings().setCompassEnabled(true);
            float[] distance = new float[1];
            LatLng previousLocation = map.getCameraPosition().target;
            Location.distanceBetween(previousLocation.latitude, previousLocation.longitude,
                    currentLatLng.latitude, currentLatLng.longitude, distance);
            double safeRadius = ((Global) getActivity().getApplication()).getSafeRadiusInMeter();

            map.addCircle(new CircleOptions()
                    .center(currentLatLng)
                    .radius(safeRadius));


            // Fake child position
            if (childLatLng != null) {
                // TODO Replace circle by marker?

                map.addCircle(new CircleOptions()
                        .center(childLatLng)
                        .fillColor(Color.RED)
                        .strokeColor(Color.RED)
                        .radius(40 / map.getCameraPosition().zoom));
                //map.addMarker(new MarkerOptions().position(childLatLng)
                 //          .draggable(false).flat(true));
            }
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
            double distanceCenterToEdgeLong = SphericalUtil.computeDistanceBetween(center, new LatLng(northeast.latitude, center.longitude))*1.05;

            if(!bounds.contains(childPos)) {
                // out of screen
                final int TIMES = 20;
                LatLng arrowPosEstimate = null;
                for (int i = TIMES; i >=0; i--) {
                    arrowPosEstimate = SphericalUtil.interpolate(center, childPos, i/(float)TIMES);
                    if (bounds.contains(arrowPosEstimate)) {
                        break;
                    }
                }

                double heading = SphericalUtil.computeHeading(center, childPos);
                LatLng arrowPos = SphericalUtil.interpolate(center, arrowPosEstimate, 0.8);
                map.addMarker(new MarkerOptions()
                        .position(arrowPos)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_sign_outofrange)))
                        .setRotation((float) heading);
            }
        }
    }

    public void onLocationChanged(Location location) {
        currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        currentAltitude = location.getAltitude();
        childLatLng = ((Global)getActivity().getApplication()).getChildLatLng();
        childAltitude = ((Global)getActivity().getApplication()).getChildAltitude();
        float[] distance = new float[1];
        if (currentLatLng != null && childLatLng != null && childAltitude != null) {
            Location.distanceBetween(childLatLng.latitude, childLatLng.longitude,
                    currentLatLng.latitude, currentLatLng.longitude, distance);
            Double dist = distance[0] / 0.308; // meter to feet
            Double altitudeDiff = (childAltitude - currentAltitude) / 0.308; // meter to feet
            if (getView() != null)
                if (altitudeDiff < 0)
                    ((TextView) (getView().findViewById(R.id.map_status))).setText(" " + dist.intValue() + " ft away " +
                        -altitudeDiff.intValue() + " ft above child");
                else
                    ((TextView) (getView().findViewById(R.id.map_status))).setText(" " + dist.intValue() + " ft away " +
                            altitudeDiff.intValue() + " ft below child");
            setupMap();
            mapFragment.getMapAsync(this);
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

}
