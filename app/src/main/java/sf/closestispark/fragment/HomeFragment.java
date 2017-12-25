package sf.closestispark.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.util.DiffUtil;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.yalantis.filter.adapter.FilterAdapter;
import com.yalantis.filter.listener.FilterListener;
import com.yalantis.filter.widget.Filter;
import com.yalantis.filter.widget.FilterItem;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import sf.closestispark.R;
import sf.closestispark.helper.Tag;
import sf.closestispark.model.Park;
import sf.cuboidcirclebutton.CuboidButton;

/**
 * Created by mesutgenc on 22.07.2017.
 */

public class HomeFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMarkerClickListener,
        View.OnClickListener, FilterListener<Tag> {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private SupportMapFragment mapFragment;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Marker mCurrLocationMarker;
    private GoogleMap mMap;
    private LatLng myLocation;
    private String lati;
    private String longi;
    private CuboidButton btnSearchParking;
    private List<Park> parkMapList = new ArrayList<>();
    // For Search Filter
    private int[] mColors;
    private String[] mTitles;
    private Filter<Tag> mFilter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        mColors = getResources().getIntArray(R.array.colors);
        mTitles = getResources().getStringArray(R.array.cities);
        mFilter = (Filter<Tag>) rootView.findViewById(R.id.filter);
        mFilter.setAdapter(new Adapter(getTags()));
        mFilter.setListener(this);

        //the text to show when there's no selected items
        mFilter.setNoSelectedItemText(getString(R.string.city_all_selected));
        mFilter.build();

        /*btnSearchParking = rootView.findViewById(R.id.btn_search_parking);
        btnSearchParking.setOnClickListener(this);*/
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_home);
        mapFragment.getMapAsync(this);

        return rootView;
    }

    private List<Tag> getTags() {
        List<Tag> tags = new ArrayList<>();

        for (int i = 0; i < mTitles.length; ++i) {
            tags.add(new Tag(mTitles[i], mColors[i]));
        }

        return tags;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
        mMap.setOnMarkerClickListener(this);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getContext())
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getContext(),
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getContext(), "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            // my location xCoor and yCoor
            lati = String.valueOf(mLastLocation.getLatitude());
            longi = String.valueOf(mLastLocation.getLongitude());
            myLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

            mMap.addMarker(new MarkerOptions()
                    .position(myLocation)
                    .title("KONUMUM")
                    .snippet("Şuan ki Konumum")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .alpha(0.7f));

            CameraPosition cameraPosition = new CameraPosition.Builder().target(myLocation).zoom(11).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        // my location xCoor and yCoor
        lati = String.valueOf(mLastLocation.getLatitude());
        longi = String.valueOf(mLastLocation.getLongitude());
    }

    @Override
    public void onPause() {
        super.onPause();
        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onClick(View view) {
        /*if (view.getId() == R.id.btn_search_parking) {
            Toast.makeText(getContext(), "Uygun Park Yeri Getiriliyor...", Toast.LENGTH_SHORT).show();
            *//*addParkFromJson();
            addMyLocationMarkersToMap();*//*
        }*/
    }


    private String readJsonDataFromFile() throws IOException {

        InputStream inputStream = null;
        StringBuilder builder = new StringBuilder();

        try {
            String jsonDataString = null;
            inputStream = getResources().openRawResource(R.raw.park_item);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            while ((jsonDataString = bufferedReader.readLine()) != null) {
                builder.append(jsonDataString);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

        return new String(builder);
    }


    private void addParkFromJson() {
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Veriler Getiriliyor..");
        progressDialog.show();

        try {
            String jsonDataString = readJsonDataFromFile();
            JSONArray isparkItemsJsonArray = new JSONArray(jsonDataString);

            for (int i = 0; i < isparkItemsJsonArray.length(); ++i) {
                JSONObject isparkItemObject = isparkItemsJsonArray.getJSONObject(i);

                String code = isparkItemObject.getString("code");
                String name = isparkItemObject.getString("name");
                String location = isparkItemObject.getString("location");
                String address = isparkItemObject.getString("address");
                String city = isparkItemObject.getString("city");
                String type = isparkItemObject.getString("type");
                String xCoor = isparkItemObject.getString("xCoor");
                String yCoor = isparkItemObject.getString("yCoor");

                Park myPark = new Park(code, name, location, address, city, type, xCoor, yCoor);
                parkMapList.add(myPark);
            }

            addMarkersToMap();

            progressDialog.dismiss();

        } catch (IOException | JSONException exception) {
            progressDialog.dismiss();
            Log.e(HomeFragment.class.getName(), "Unable to parse JSON file.", exception);
        }
    }

    private void addMarkersToMap() {
        mMap.clear();

        for (Park p : parkMapList) {

            if (p.getType().equals("Yol Üstü Otopark")) {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.valueOf(p.getxCoor()), Double.valueOf(p.getyCoor())))
                        .title(p.getName())
                        .snippet(p.getType())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                        .alpha(0.7f));
            } else if (p.getType().equals("Açık Otopark")) {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.valueOf(p.getxCoor()), Double.valueOf(p.getyCoor())))
                        .title(p.getName())
                        .snippet(p.getType())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                        .alpha(0.7f));
            } else if (p.getType().equals("Kapalı Otopark")) {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.valueOf(p.getxCoor()), Double.valueOf(p.getyCoor())))
                        .title(p.getName())
                        .snippet(p.getType())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .alpha(0.7f));
            }


            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.valueOf(p.getxCoor()), Double.valueOf(p.getyCoor())), 11));
        }

    }

    private void addMyLocationMarkersToMap() {
        mMap.addMarker(new MarkerOptions()
                .position(myLocation)
                .title("KONUMUM")
                .snippet("Şuan ki Konumum")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .alpha(0.7f));

        CameraPosition cameraPosition = new CameraPosition.Builder().target(myLocation).zoom(11).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private List<Park> findByTags(List<Tag> filters) {
        List<Park> myparks = new ArrayList<>();

        for (Park park : parkMapList) {
            for (Tag tag : filters) {
                if (park.hasTag(tag.getText()) && !myparks.contains(park)) {
                    myparks.add(park);
                }
            }
        }
        return myparks;
    }


    private void addNewMarkersToMap(List<Park> parkList) {
        mMap.clear();

        for (Park p : parkList) {

            if (p.getType().equals("Yol Üstü Otopark")) {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.valueOf(p.getxCoor()), Double.valueOf(p.getyCoor())))
                        .title(p.getName())
                        .snippet(p.getType())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                        .alpha(0.7f));
            } else if (p.getType().equals("Açık Otopark")) {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.valueOf(p.getxCoor()), Double.valueOf(p.getyCoor())))
                        .title(p.getName())
                        .snippet(p.getType())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                        .alpha(0.7f));
            } else if (p.getType().equals("Kapalı Otopark")) {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.valueOf(p.getxCoor()), Double.valueOf(p.getyCoor())))
                        .title(p.getName())
                        .snippet(p.getType())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .alpha(0.7f));
            } else {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.valueOf(p.getxCoor()), Double.valueOf(p.getyCoor())))
                        .title(p.getName())
                        .snippet(p.getType())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                        .alpha(0.7f));
            }

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.valueOf(p.getxCoor()), Double.valueOf(p.getyCoor())), 12));
        }

    }



    @Override
    public void onFiltersSelected(ArrayList<Tag> filters) {
        // "parkMapList" listesine tüm park yerlerini ekle
        //addParkFromJson();
        List<Park> oldParks = parkMapList;
        if (oldParks.size() > 0) {
            List<Park> newParks = findByTags(filters);
            addNewMarkersToMap(newParks);
        }

        //addMyLocationMarkersToMap();
        //calculateDiff(oldParks, newParks);
    }

    @Override
    public void onNothingSelected() {
        if (parkMapList.size() > 0) {
            addMarkersToMap();
        } else addParkFromJson();
    }

    @Override
    public void onFilterSelected(Tag item) {
        if (item.getText().equals(mTitles[0])) {
            mFilter.deselectAll();
            mFilter.collapse();
        }
    }

    @Override
    public void onFilterDeselected(Tag item) {

    }


    class Adapter extends FilterAdapter<Tag> {

        Adapter(@NotNull List<? extends Tag> items) {
            super(items);
        }

        @NotNull
        @Override
        public FilterItem createView(int position, Tag item) {
            FilterItem filterItem = new FilterItem(getContext());

            filterItem.setStrokeColor(mColors[0]);
            filterItem.setTextColor(mColors[0]);
            filterItem.setCornerRadius(14);
            filterItem.setCheckedTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
            filterItem.setColor(ContextCompat.getColor(getContext(), android.R.color.white));
            filterItem.setCheckedColor(mColors[position]);
            filterItem.setText(item.getText());
            filterItem.deselect();

            return filterItem;
        }
    }

}
