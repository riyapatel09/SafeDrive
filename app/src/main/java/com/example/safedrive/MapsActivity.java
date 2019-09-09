package com.example.safedrive;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.safedrive.Helper.SDHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    final String TAG = "GPS";
    private final static int ALL_PERMISSIONS_RESULT = 101;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 5 * 1;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;

    private GoogleMap mMap,gap;
    LocationManager locationManager;
    Location loc;
    boolean isGPS = false;
    boolean isNetwork = false;
    ArrayList<String> permissions = new ArrayList<>();
    ArrayList<String> permissionsToRequest;
    boolean canGetLocation = true;
    Double latitude, longitude, sendLatitude, sendLongitude;
    ArrayList<String> permissionsRejected = new ArrayList<>();
    List<LocationInfo> recievedData, dataToShow,dataToShow1;
    GoogleMap gMap;
    Bitmap mDotMarkerBitmap;
    List<Location> myCarLocation;
    MyLocation l1,l2,l3,l4,l5,l6,l7,l8,l9,l10;
    List<MyLocation> locationList;
    int m=0;
    float zoomLevel = 18.0f;
    BitmapDescriptor icon,blue_icon,icon_yellow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        l1 = new MyLocation(42.304873,-83.063427);
        l2 = new MyLocation(42.304948,-83.063306);
        l3 = new MyLocation(42.305028,-83.063065);
        l4 = new MyLocation(42.305091,-83.062867);
        l5 = new MyLocation(42.305174,-83.062684);
        l6 = new MyLocation(42.30521,-83.062464);
        l7 = new MyLocation(42.305285,-83.06225);
        l8 = new MyLocation(42.305377,-83.061987);
        l9 = new MyLocation(42.305444,-83.061698);
        l10 = new MyLocation(42.305563,-83.061371);
        icon = BitmapDescriptorFactory.fromResource(R.drawable.icons8_car_top_view_25);
        Matrix matrix = new Matrix();
        matrix.postRotate(30);

//        Bitmap rotated = (Bitmap) Bitmap.createBitmap(icon, 0, 0, 100, 100, matrix, true);


        blue_icon = BitmapDescriptorFactory.fromResource(R.drawable.icons8_car_top_view_25_blue);
        icon_yellow = BitmapDescriptorFactory.fromResource(R.drawable.car_yellow);
//        icons8_car_top_view_blue_20


        locationList = new ArrayList<MyLocation>();
        locationList.add(l1);locationList.add(l2);locationList.add(l3);locationList.add(l4);locationList.add(l5);locationList.add(l6);
        locationList.add(l7);locationList.add(l8);locationList.add(l9);locationList.add(l10);




        locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
        isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionsToRequest = findUnAskedPermissions(permissions);

        if (!isGPS && !isNetwork) {
            Log.d(TAG, "Connection off");
            showSettingsAlert();
            getLastLocation();
        } else {
            Log.d(TAG, "Connection on");
            // check permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (permissionsToRequest.size() > 0) {
                    requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]),
                            ALL_PERMISSIONS_RESULT);
                    Log.d(TAG, "Permission requests");
                    canGetLocation = false;
                }
            }

            // get location
            getLocation();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged");
        updateUI(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {}

    @Override
    public void onProviderEnabled(String s) {
        getLocation();
    }

    @Override
    public void onProviderDisabled(String s) {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    private ArrayList findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList result = new ArrayList();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canAskPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canAskPermission() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("GPS is not Enabled!");
        alertDialog.setMessage("Do you want to turn on GPS?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    private void getLastLocation() {
        try {
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, false);
            Location location = locationManager.getLastKnownLocation(provider);
            Log.d(TAG, provider);
            Log.d(TAG, location == null ? "NO LastLocation" : location.toString());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void getLocation() {
        try {
            if (canGetLocation) {
                Log.d(TAG, "Can get location");
                if (isGPS) {
                    // from GPS
                    Log.d(TAG, "GPS on");
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, (LocationListener) this);

                    if (locationManager != null) {
                        loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (loc != null)
                            updateUI(loc);
                    }
                } else if (isNetwork) {
                    // from Network Provider
                    Log.d(TAG, "NETWORK_PROVIDER on");
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, (LocationListener) this);

                    if (locationManager != null) {
                        loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (loc != null)
                            updateUI(loc);
                    }
                } else {
                    loc.setLatitude(0);
                    loc.setLongitude(0);
                    updateUI(loc);
                }
            } else {
                Log.d(TAG, "Can't get location");
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSIONS_RESULT:
                Log.d(TAG, "onRequestPermissionsResult");
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.toArray(
                                                        new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }
                } else {
                    Log.d(TAG, "No rejected permissions.");
                    canGetLocation = true;
                    getLocation();
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MapsActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void updateUI(Location loc) {
        Log.d(TAG, "updateUI");
//        tvLatitude.setText(Double.toString(loc.getLatitude()));
//        tvLongitude.setText(Double.toString(loc.getLongitude()));
//        tvTime.setText(DateFormat.getTimeInstance().format(loc.getTime()));
        latitude = loc.getLatitude();
        longitude = loc.getLongitude();
        String cityName = null;
//        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
//        List<Address> addresses;
//        String streetName = "";
//        String countryName = "";
//        String locality = "";
//        String stateName = "";
//        try {
//            addresses = gcd.getFromLocation(loc.getLatitude(), loc
//                    .getLongitude(), 1);
//            if (addresses.size() > 0) {
//                streetName = addresses.get(0).getThoroughfare();
//                countryName = addresses.get(0).getCountryName();
//                locality = addresses.get(0).getLocality();
//                stateName = addresses.get(0).getAddressLine(1);
//            }
//
//
//            String s = longitude + "\n" + latitude +
//                    "My Currrent Street is: " + streetName + "Country:" + countryName + "State:" + stateName + "Locality:" + locality;
//            Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
////            sendCoordinates(latitude, longitude, countryName, stateName, locality);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

        private List<LocationInfo> sendCoordinates(Double latitude, Double longitude, String countryName, String stateName, String locality, int locationID)
    {
        sendLatitude = latitude;
        sendLongitude = longitude;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("latitude", sendLatitude);
            jsonObject.put("longitude", sendLongitude);
            jsonObject.put("userID", 3);
            jsonObject.put("vehicleID", "Vehicle9");
            jsonObject.put("country", countryName);
            jsonObject.put("state", "Ontario");
            jsonObject.put("city", locality);
            jsonObject.put("locationID", locationID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://" + SDHelper.getNetworkIp() + "/LocationInfo";
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //System.out.println(response);
//                        Toast.makeText(getApplicationContext(), "Sent Successfully", Toast.LENGTH_SHORT).show();
                        try {
                                JSONArray jDataArray = response.getJSONArray("locationInfoList");
                                Type listType = new TypeToken<ArrayList<LocationInfo>>(){}.getType();
                                recievedData = (List<LocationInfo>) new Gson().fromJson(String.valueOf(jDataArray),listType);
                                dataToShow = recievedData;
                                ArrayList<LocationInfo> marArray = new ArrayList<LocationInfo>();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
//                            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.network_timeout_error), Toast.LENGTH_LONG).show();
                        } else if (error instanceof AuthFailureError) {
//                            Toast.makeText(getApplicationContext(), "AuthFailureError", Toast.LENGTH_LONG).show();
                        } else if (error instanceof ServerError) {
//                            Toast.makeText(getApplicationContext(), "ServerError", Toast.LENGTH_LONG).show();
                        } else if (error instanceof NetworkError) {
//                            Toast.makeText(getApplicationContext(), "NetworkError", Toast.LENGTH_LONG).show();
                        } else if (error instanceof ParseError) {
//                            Toast.makeText(getApplicationContext(), "ParseError", Toast.LENGTH_LONG).show();
                        }
                    }
                });
        queue.add(jsObjRequest);
        return recievedData;


    }

    private void showCarsOnMap(List<LocationInfo> locations)
    {
        int px = getResources().getDimensionPixelSize(R.dimen.map_dot_marker_size);
        mDotMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mDotMarkerBitmap);
        Drawable shape = getResources().getDrawable(R.drawable.map_dot_red);
        shape.setBounds(0, 0, mDotMarkerBitmap.getWidth(), mDotMarkerBitmap.getHeight());
        shape.draw(canvas);

        ArrayList<LocationInfo> markersArray = new ArrayList<LocationInfo>();
        markersArray = (ArrayList<LocationInfo>) locations;
        for(int i = 0 ; i < markersArray.size() ; i++) {

            createMarker(markersArray.get(i).getLatitude(), markersArray.get(i).getLongitude(),"car");
        }
    }

    protected Marker createMarker(double latitude, double longitude, String title) {

        Request googleMap;
        return gMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .anchor(0.5f, 0.5f)
                .title(title)
                .icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap))
                );
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        gap = googleMap;

//        LatLng myLocation = new LatLng(latitude, longitude);
        LatLng myLocation = new LatLng(42.304873,-83.063427);
        mMap.addMarker(new MarkerOptions().position(myLocation).title("Marker at my location").icon(blue_icon));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation,zoomLevel));
        final Handler h = new Handler();
        final int delay = 2 * 1000;
        m=0;

        h.postDelayed(new Runnable(){
            public void run(){
                //for(int m=0; m<locationList.size();m++) {
                    // This portion of code runs each 10s.
//                    Toast.makeText(getApplicationContext(), "5 Sec", Toast.LENGTH_SHORT).show();
                    List<LocationInfo> diaplayData = null;

                    // Add a marker in my location and move the camera
                if(m<10) {
                    LatLng l = new LatLng(locationList.get(m).getLatitude(), locationList.get(m).getLongitude());
//                    Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
//                    List<Address> addresses;
//                    String streetName = "";
//                    String countryName = "";
//                    String locality = "";
//                    String stateName = "";
//                    try {
//                        addresses = gcd.getFromLocation(loc.getLatitude(), loc
//                                .getLongitude(), 1);
//                        if (addresses.size() > 0) {
//                            streetName = addresses.get(0).getThoroughfare();
//                            countryName = addresses.get(0).getCountryName();
//                            locality = addresses.get(0).getLocality();
//                            stateName = addresses.get(0).getAddressLine(1);
//                        }
//
//                        String s = longitude + "\n" + latitude +
//                                "My Currrent Street is: " + streetName + "Country:" + countryName + "State:" + stateName + "Locality:" + locality;
////                        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
////            sendCoordinates(latitude, longitude, countryName, stateName, locality);
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

//                    dataToShow1 = sendCoordinates(l.latitude, l.longitude,"canada","Ontario","Windsor",m);
//                    ArrayList<LocationInfo> markersArray = new ArrayList<LocationInfo>();
//                    markersArray = (ArrayList<LocationInfo>) dataToShow;

                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(l).icon(blue_icon));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(l, zoomLevel));
//                    if (dataToShow != null) {
//                        for (int k = 0; k < locationList.size(); k++) {
//                            if(m % 2 == 0)
//                            {
                                //gap.clear();
                  //  for()
//                    l1 = new MyLocation(42.304873,-83.063427);
//                    l2 = new MyLocation(42.304948,-83.063306);
//                    l3 = new MyLocation(42.305028,-83.063065);
//                    l4 = new MyLocation(42.305091,-83.062867);
//                    l5 = new MyLocation(42.305174,-83.062684);
//                    l6 = new MyLocation(42.30521,-83.062464);
//                    l7 = new MyLocation(42.305285,-83.06225);
//                    l8 = new MyLocation(42.305377,-83.061987);
//                    l9 = new MyLocation(42.305444,-83.061698);
//                    l10 = new MyLocation(42.305563,-83.061371);
                    switch (m)
                    {
                        case 0:
                            LatLng addLocation1 = new LatLng(42.304858,-83.063627);
                            gap.addMarker(new MarkerOptions().position(addLocation1).icon(icon));
                            addLocation1 = null;
                            break;

                        case 1:
                            LatLng addLocation = new LatLng(42.305028, -83.063065);
                            gap.addMarker(new MarkerOptions().position(addLocation).icon(icon));
                            addLocation = null;
                            break;

                        case 2:
                            LatLng addLocation2 = new LatLng(42.304948,-83.063306);
                            gap.addMarker(new MarkerOptions().position(addLocation2).icon(icon));
                            addLocation2 = null;
                            break;
                        case 4:
                            LatLng addLocation3 = new LatLng(42.305091,-83.062867);
                            gap.addMarker(new MarkerOptions().position(addLocation3).icon(icon));
                            addLocation3 = null;

                            LatLng addLocation4 = new LatLng(42.30521,-83.062464);
                            gap.addMarker(new MarkerOptions().position(addLocation4).icon(icon));
                            addLocation4 = null;
                            break;

                        case 5:
                            LatLng addLocation5 = new LatLng(42.305174,-83.062684);
                            gap.addMarker(new MarkerOptions().position(addLocation5).icon(icon));
                            addLocation5 = null;

                            LatLng addLocation6 = new LatLng(42.305285,-83.06225);
                            gap.addMarker(new MarkerOptions().position(addLocation6).icon(icon));
                            addLocation6 = null;
                            break;


                        case 7:
                            LatLng addLocation9 = new LatLng(42.305444,-83.061698);
                            gap.addMarker(new MarkerOptions().position(addLocation9).icon(icon));
                            addLocation9 = null;
                        case 8:
                            LatLng addLocation8 = new LatLng(42.305563,-83.061371);
                            gap.addMarker(new MarkerOptions().position(addLocation8).icon(icon));
                            addLocation8 = null;
                            break;
//                        case 9:
//                            LatLng addLocation9 = new LatLng(42.305444,-83.061698);
//                            gap.addMarker(new MarkerOptions().position(addLocation9).icon(icon));
//                            addLocation9 = null;
//
//                            break;
                    }

//                            }
//                            else
//                            {
                                //gap.clear();

//                            }

//                            Toast.makeText(getApplicationContext(), i + "times", Toast.LENGTH_SHORT).show();
//                        }
//                    }
                    l = null;
                    m=m+1;
//                }
//                    Toast.makeText(getApplicationContext(), m + "times", Toast.LENGTH_SHORT).show();

//TO PRINT RECEIVED DATA RESPONSE
//                    sendCoordinates(latitude, longitude);
//                    ArrayList<LocationInfo> markersArray = new ArrayList<LocationInfo>();
//                    markersArray = (ArrayList<LocationInfo>) recievedData;
//                    BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.icons8_car_top_view_25);
//
//                    if (recievedData != null) {
//                        for (int i = 0; i < recievedData.size(); i++) {
//                            //                        LatLng addLocation = new LatLng(40.30115915182987, -80.07192234322429);
//                            LatLng addLocation = new LatLng(recievedData.get(i).getLatitude(), recievedData.get(i).getLongitude());
//
//                            gap.addMarker(new MarkerOptions().position(addLocation).icon(icon));
//                            addLocation = null;
//                            Toast.makeText(getApplicationContext(), i + "times", Toast.LENGTH_SHORT).show();
//                        }
//                    }
                }

                h.postDelayed(this, delay);
            }
        }, delay);


    }

    protected void setUpMap()
    {
//        Toast.makeText(getApplicationContext(), "5 Sec", Toast.LENGTH_SHORT).show();
//        List<LocationInfo> diaplayData = null;
//        // Add a marker in my location and move the camera
//
//
//       // diaplayData = sendCoordinates(latitude,longitude);
//
//        int px = getResources().getDimensionPixelSize(R.dimen.map_dot_marker_size);
//            mDotMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
//            Canvas canvas = new Canvas(mDotMarkerBitmap);
//            Drawable shape = getResources().getDrawable(R.drawable.map_dot_red);
//            shape.setBounds(0, 0, mDotMarkerBitmap.getWidth(), mDotMarkerBitmap.getHeight());
//            shape.draw(canvas);
//
//            ArrayList<LocationInfo> markersArray = new ArrayList<LocationInfo>();
//            markersArray = (ArrayList<LocationInfo>) diaplayData;
////        markersArray.size()
//            for(int i = 0 ; i <=1  ; i++) {
//                LatLng addLocation = new LatLng(34.30115915182987, -80.07192234322429);
////                createMarker(markersArray.get(i).getLatitude(), markersArray.get(i).getLongitude(),"car");
////                mMap.addMarker(new MarkerOptions()
////                        .position(new LatLng(34.30115915182987, -80.07192234322429))
////                        .title("car")
////                        .icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap)));
//                mMap.addMarker(new MarkerOptions().position(addLocation).title("Marker at my location"));
//                Toast.makeText(getApplicationContext(), i + "times", Toast.LENGTH_SHORT).show();
//            }
    }
}

//                createMarker(markersArray.get(i).getLatitude(), markersArray.get(i).getLongitude(),"car");
//                mMap.addMarker(new MarkerOptions()
//                        .position(new LatLng(34.30115915182987, -80.07192234322429))
//                        .title("car")
//                        .icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap)));.icon(R.drawable.icons8_car_top_view_25)

//                int px = getResources().getDimensionPixelSize(R.dimen.map_dot_marker_size);
//                mDotMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
//                Canvas canvas = new Canvas(mDotMarkerBitmap);
//                Drawable shape = getResources().getDrawable(R.drawable.map_dot_red);
//                shape.setBounds(0, 0, mDotMarkerBitmap.getWidth(), mDotMarkerBitmap.getHeight());
//                shape.draw(canvas);
