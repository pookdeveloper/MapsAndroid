package com.example.mapasmono;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnPolylineClickListener,
        GoogleMap.OnPolygonClickListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraMoveCanceledListener,
        GoogleMap.OnCameraIdleListener, View.OnClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    private Location lastLocation;

    private String addressOutput;

    private Polyline gpsTrack;

    Location mLastLocation;
    Marker mCurrLocationMarker;
    GoogleApiClient mGoogleApiClient;

    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    // Create a stroke pattern of a gap followed btextViewVelocidadMaximay a dot.
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);

    Polyline polyline1 = null;
    ArrayList<LatLng> arrayPosiciones = new ArrayList<LatLng>();

    TextView textView;
    TextView textView2;
    TextView textView3;
    TextView textViewVelocidadMaxima;


    Button button;
    boolean grabando = false;
    Double distance = 0.0;
    float distanceFloat = 0;
    float velocidadMaxima = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);
        button = findViewById(R.id.button);
        button.setOnClickListener(this);

        textView3 = findViewById(R.id.textView3);
        textViewVelocidadMaxima = findViewById(R.id.textViewVelocidadMaxima);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //GPS -- Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                Log.d("onLocationChanged", location.toString());
                mLastLocation = location;


                if (grabando) {
                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    if (mLastLocation != null)
                                        if (mLastLocation != null)
                                            Log.d(mLastLocation.toString(), mLastLocation.toString());

                                    LatLng calymayor = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(calymayor));
                                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(calymayor, 15));


                                    PolylineOptions polylineOptions = new PolylineOptions();
                                    Random rand = new Random();
                                    int r = rand.nextInt();
                                    int g = rand.nextInt();
                                    int b = rand.nextInt();

                                    polylineOptions.color(Color.rgb(r, g, b));
                                    polylineOptions.width(10);


                                    if (arrayPosiciones.size() > 1) {
                                        polyline1 = mMap.addPolyline(polylineOptions.clickable(true).add(arrayPosiciones.get(arrayPosiciones.size() - 1), calymayor));
                                        //polyline1 = mMap.addPolyline(polylineOptions.clickable(true).add(arrayPosiciones.get(arrayPosiciones.size()-1), new LatLng(39.432751, -0.431233)));
                                        textView.setText(arrayPosiciones.get(arrayPosiciones.size() - 1) + "-->");
                                        textView2.setText(calymayor + "-->");
                                    }


                                    distance = distance + SphericalUtil.computeDistanceBetween(arrayPosiciones.get(arrayPosiciones.size() - 1), calymayor);
                                    Log.d("asdasd", distance.toString());

                                    float[] results = new float[1];

                                    Location.distanceBetween(arrayPosiciones.get(arrayPosiciones.size() - 1).latitude,
                                            arrayPosiciones.get(arrayPosiciones.size() - 1).longitude,
                                            calymayor.latitude,calymayor.longitude, results);

                                    distanceFloat = distanceFloat+results[0];
                                    textView3.setText("Distancia: " + distanceFloat + "km --> " + mLastLocation.getSpeed() +"Km/h");

                                    velocidadMaxima = (mLastLocation.getSpeed() > velocidadMaxima) ? mLastLocation.getSpeed():velocidadMaxima;
                                    textViewVelocidadMaxima.setText("VM: "+velocidadMaxima+" Km/h");

                                    //polyline1 = mMap.addPolyline(polylineOptions.clickable(true).addAll(arrayPosiciones));

                                /*new android.os.Handler().postDelayed(
                                        new Runnable() {
                                            public void run() {
                                                polyline1.remove();
                                            }
                                        }, 3000);*/

                                    // Nos guardamos la posicion;
                                    arrayPosiciones.add(calymayor);
                                }
                            },
                            3000);
                }
            }


            public void onStatusChanged(String provider, int status, Bundle extras) {
                //Toast.makeText(MapsActivity.this, "onStatusChanged", Toast.LENGTH_SHORT).show();
                Log.d("onStatusChanged", provider);

                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                Log.d(addressOutput.toString(), addressOutput.toString());
                            }
                        }, 300);
            }

            public void onProviderEnabled(String provider) {
                //Toast.makeText(MapsActivity.this, "onProviderEnabled", Toast.LENGTH_SHORT).show();
                Log.d("onProviderEnabled", provider);
            }

            public void onProviderDisabled(String provider) {
                //Toast.makeText(MapsActivity.this, "onProviderDisabled", Toast.LENGTH_SHORT).show();
                Log.d("onProviderDisabled", provider);
            }
        };
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                Log.d("PERMISOS GPS", "PEMIDOS PERMISOS");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            } else {
                Log.d("PERMISOS GPS", "LOS TENEMOS");

                long MIN_TIEMPO_ENTRE_UPDATES = 0; // 1 minuto //Minimo tiempo para updates en Milisegundos
                float MIN_CAMBIO_DISTANCIA_PARA_UPDATES = 0; // 1.5 metros //Minima distancia para updates en metros.

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIEMPO_ENTRE_UPDATES, MIN_CAMBIO_DISTANCIA_PARA_UPDATES, locationListener);
                //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIEMPO_ENTRE_UPDATES, MIN_CAMBIO_DISTANCIA_PARA_UPDATES, locationListener);
            }

            //showAlert("OKKKK");
        } else {
            //showAlert("Kooooo");
        }


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

        // PEDIMOS PERMISOS
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        mMap = googleMap;

        // Set listeners for click events.
        mMap.setOnPolylineClickListener(this);
        mMap.setOnPolygonClickListener(this);
        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveStartedListener(this);
        mMap.setOnCameraMoveListener(this);
        mMap.setOnCameraMoveCanceledListener(this);

        mMap.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener);
        mMap.setOnMyLocationClickListener(onMyLocationClickListener);
        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        //mMap.setMinZoomPreference(11);

        // ACTIVAMOS PARA MOSTARR EL ICONO DE POSICION
        enableMyLocationIfPermitted();


        /*PolylineOptions polylineOptions = new PolylineOptions();
        Random rand = new Random();
        int r = rand.nextInt();
        int g = rand.nextInt();
        int b = rand.nextInt();

        polylineOptions.color(Color.rgb(r, g, b));
        polylineOptions.width(10);
        polyline1 = mMap.addPolyline(polylineOptions.clickable(true).add(calymayor));*/


        // EJEMPLO DE GOOGLE
        // Add polylines and polygons to the map. This section shows just
        // a single polyline. Read the rest of the tutorial to learn more.

     /*   PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.CYAN);
        polylineOptions.width(10
        );

        Polyline polyline1 = mMap.addPolyline(polylineOptions
                .clickable(true)
                .add(
                        new LatLng(39.43235818261798, -0.4307332634925843),
                        new LatLng(39.431940, -0.429485)));*/

        // Position the map's camera near Alice Springs in the center of Australia,
        // and set the zoom factor so most of Australia shows on the screen.
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-23.684, 133.903), 4));


    }


    private void enableMyLocationIfPermitted() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private GoogleMap.OnMyLocationButtonClickListener onMyLocationButtonClickListener =
            new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {

                    LatLng calymayor = new LatLng(mMap.getCameraPosition().target.latitude, mMap.getCameraPosition().target.longitude);
                    /*mMap.moveCamera(CameraUpdateFactory.newLatLng(calymayor));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(calymayor, 15));*/

                    //mMap.setMinZoomPreference(15);

                    return false;
                }
            };

    private GoogleMap.OnMyLocationClickListener onMyLocationClickListener =
            new GoogleMap.OnMyLocationClickListener() {
                @Override
                public void onMyLocationClick(@NonNull Location location) {

                    //dmMap.setMinZoomPreference(12);
                }
            };

    @Override
    public void onCameraMove() {
        //Toast.makeText(this, "The camera is moving.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCameraIdle() {
        //Toast.makeText(this, "The camera has stopped moving.",Toast.LENGTH_SHORT).show();

        LatLng calymayor = new LatLng(mMap.getCameraPosition().target.latitude, mMap.getCameraPosition().target.longitude);
                    /*mMap.moveCamera(CameraUpdateFactory.newLatLng(calymayor));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(calymayor, 15));*/


        CircleOptions circleoptions = new CircleOptions().strokeWidth(2).strokeColor(Color.BLUE).fillColor(Color.parseColor("#500084d3"));
        //Circle circle = mMap.addCircle(circleoptions.center(calymayor).radius(40));


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onCameraMoveCanceled() {

    }

    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            //Toast.makeText(this, "The user gestured on the map.",Toast.LENGTH_SHORT).show();
        } else if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_API_ANIMATION) {
            //Toast.makeText(this, "The user tapped something on the map.",Toast.LENGTH_SHORT).show();
        } else if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION) {
            //Toast.makeText(this, "The app moved the camera.",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPolygonClick(Polygon polygon) {

    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        // Flip from solid stroke to dotted stroke pattern.
        if ((polyline.getPattern() == null) || (!polyline.getPattern().contains(DOT))) {
            polyline.setPattern(PATTERN_POLYLINE_DOTTED);
        } else {
            // The default pattern is a solid stroke.
            polyline.setPattern(null);
        }

        //Toast.makeText(this, "Route type " + polyline.getTag().toString(),Toast.LENGTH_SHORT).show();
    }


    // ONCLICK LISTENER
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button) {
            if (!grabando) {
                grabando = true;
                button.setText("Grabando...");
                obtenerUltimaPosicion();
            } else {
                grabando = false;
                button.setText("Grabar");
                obtenerUltimaPosicion();
            }
        }
    }


    private LatLng obtenerUltimaPosicion() {

        // Obtenemos la ultima localizacion
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return null;
        }


        Location myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (myLocation == null) {
            myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        LatLng calymayor = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(calymayor));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(calymayor, 15));

        // Nos guardamos la posicion;
        arrayPosiciones.add(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));

        return calymayor;

    }
}

