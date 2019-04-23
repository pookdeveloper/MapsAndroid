package com.example.mapasmono;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {
    double longitudeBest, latitudeBest;
    double longitudeGPS, latitudeGPS;
    double longitudeNetwork, latitudeNetwork;
    private TextView mTextMessage;

    private Button button;
    private Button botonActividad;

    LocationManager locationManager;
    private Location lastLocation;

    private String addressOutput;

    private MapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private GoogleMap gmap;


    private AddressResultReceiver resultReceiver = new AddressResultReceiver(null);
    private GeocodeAddressIntentService geocodeAddressIntentService;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d("PERMISO requestCode", String.valueOf(requestCode));

        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("PERMISO ACEPTADO", "PERMISO ACEPTADO".concat(permissions.toString()));
        } else {
            // permission denied, boo! Disable the
            // functionality that depends on this permission.
            Log.d("PERMISO CANCELADO", "PERMISO CANCELADO");
        }
        return;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        mTextMessage = findViewById(R.id.message);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        //GPS
        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                Log.d("onLocationChanged", location.toString());
                mTextMessage.setText("onLocationChanged...");
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                if (addressOutput != null)
                                    mTextMessage.setText(addressOutput.toString());
                            }
                        },
                        1000);
            }


            public void onStatusChanged(String provider, int status, Bundle extras) {
                Toast.makeText(MainActivity.this, "onStatusChanged", Toast.LENGTH_SHORT).show();
                Log.d("onStatusChanged", provider);
                mTextMessage.setText("onStatusChanged...");
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                mTextMessage.setText(addressOutput.toString());
                            }
                        },
                        300);
            }

            public void onProviderEnabled(String provider) {
                Toast.makeText(MainActivity.this, "onProviderEnabled", Toast.LENGTH_SHORT).show();
                Log.d("onProviderEnabled", provider);
            }

            public void onProviderDisabled(String provider) {
                Toast.makeText(MainActivity.this, "onProviderDisabled", Toast.LENGTH_SHORT).show();
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

                //Minimo tiempo para updates en Milisegundos
                long MIN_TIEMPO_ENTRE_UPDATES = 1000 * 60 * 1; // 1 minuto
                //Minima distancia para updates en metros.
                double MIN_CAMBIO_DISTANCIA_PARA_UPDATES = 1.5; // 1.5 metros


                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }

            //showAlert("OKKKK");
        } else {
            //showAlert("Kooooo");
        }


        // Capture our button from layout
        button = (Button) findViewById(R.id.botonRefrescar);
        // button.setOnClickListener(clickBotonRefrescar());
        button.setOnClickListener(this);

        botonActividad = (Button) findViewById(R.id.botonActividad);
        botonActividad.setOnClickListener(this);

        //button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        //do something...
        Log.d("CLICK BOTON", "CLICK BOTON " + v.getId());

        if (v.getId() == R.id.botonRefrescar) {
            // Code here executes on main thread after user presses button
            if (MainActivity.this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                    ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                Log.d("PERMISOS GPS", "PEMIDOS PERMISOS");
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            } else {
                // do something when the button is clicked
                Location locationGPS = MainActivity.this.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Location locationNet = MainActivity.this.locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                long GPSLocationTime = 0;
                if (null != locationGPS) {
                    GPSLocationTime = locationGPS.getTime();
                }

                long NetLocationTime = 0;

                if (null != locationNet) {
                    NetLocationTime = locationNet.getTime();
                }
                // TENEMOS LA LOCALIZACION

                lastLocation = new Location("");

                if (locationGPS != null) {
                    lastLocation.setLatitude(Double.parseDouble(String.valueOf(locationGPS.getLatitude())));
                    lastLocation.setLongitude(Double.parseDouble(String.valueOf(locationGPS.getLongitude())));
                } else if (locationNet != null) {
                    lastLocation.setLatitude(Double.parseDouble(String.valueOf(locationNet.getLatitude())));
                    lastLocation.setLongitude(Double.parseDouble(String.valueOf(locationNet.getLongitude())));
                }


                Intent intent = new Intent(MainActivity.this, GeocodeAddressIntentService.class);
                //intent.putExtra(GeocodeAddressIntentService.Constants.RECEIVER, (Parcelable) geocodeAddressIntentService);
                intent.putExtra(GeocodeAddressIntentService.Constants.RECEIVER, (Parcelable) resultReceiver);
                intent.putExtra(GeocodeAddressIntentService.Constants.LOCATION_DATA_EXTRA, lastLocation);
                intent.putExtra(GeocodeAddressIntentService.Constants.FETCH_TYPE_EXTRA, 2);
                startService(intent);
            }
        } else {
            // OTRA ACTIVIDAD
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);

        }


    }

    private void showAlert(String texto) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location ".concat(texto))
                .setMessage("Su ubicación esta desactivada.\npor favor active su ubicación " +
                        "usa esta app")
                .setPositiveButton("Configuración de ubicación", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    private final LocationListener locationListenerGPS = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitudeGPS = location.getLongitude();
            latitudeGPS = location.getLatitude();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //longitudeValueGPS.setText(longitudeGPS + "");
                    //latitudeValueGPS.setText(latitudeGPS + "");
                    Toast.makeText(MainActivity.this, "GPS Provider update", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    };


            /*    private View.OnClickListener clickBotonRefrescar = new View.OnClickListener() {
                    public void onClick(View v) {

                        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            Log.d("PERMISOS GPS", "PEMIDOS PERMISOS");
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1);
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
                            {
                            }
                        }

                        // do something when the button is clicked
                        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        long GPSLocationTime = 0;
                        if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }

                        long NetLocationTime = 0;

                        if (null != locationNet) {
                            NetLocationTime = locationNet.getTime();
                        }


                    }
                };*/


    // PARA SACAR EL NOMBRE DE LA UBICACIOON
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultData == null) {
                return;
            }

            // Display the address string
            // or an error message sent from the intent service.
            addressOutput = resultData.getString(GeocodeAddressIntentService.Constants.RESULT_DATA_KEY);
            if (addressOutput == null) {
                addressOutput = "";
            } else {
                Log.d("addressOutput", addressOutput);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextMessage.setText("Cargando...");
                        Toast.makeText(MainActivity.this, "Cargando", Toast.LENGTH_SHORT).show();
                        new android.os.Handler().postDelayed(new Runnable() {
                            public void run() {
                                mTextMessage.setText(addressOutput.toString());
                            }
                        }, 300);
                    }
                });
            }

            if (resultCode == GeocodeAddressIntentService.Constants.SUCCESS_RESULT) {
                final Address address = resultData.getParcelable(GeocodeAddressIntentService.Constants.RESULT_ADDRESS);
                Log.d("address", address.toString());
            } else {
                Log.d("error address", "KO");
            }

        }
    }


   /* @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //mMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }*/


}
