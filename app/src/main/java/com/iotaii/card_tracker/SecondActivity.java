package com.iotaii.card_tracker;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.iotaii.card_tracker.R;
import com.google.android.material.navigation.NavigationView;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class SecondActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Button zoomToLocationButton;
    private NavigationView navigationView;
    private MapView mapView;
    private final String mapId = "streets-v2";
    private final String apiKey = "O8hzf5l378NIwtGVcvEF";
    private LatLng currentLocation = new LatLng(22.5726, 88.3639);
    private TextView showTextView;
    private String userId;
    private Button deviceButton;
    private Geocoder geocoder;
    private final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LocationManager locationManager;
    private LatLng deviceLocation;
    private static final long REFRESH_INTERVAL = 60 * 1000; // 60 seconds
    private Handler handler;
    private Runnable refreshRunnable;
    private String selectedDeviceId;
    private Button Zoomin;
    private Button Zoomout;
    private Button callButton;
    private String phoneNumber;
    private TextView timeTextView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this);
        setContentView(R.layout.activity_second);
        userId = getIntent().getStringExtra("userId");
        String username = getIntent().getStringExtra("username");
        //Navigation Drawer userid
        timeTextView = findViewById(R.id.time);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView userIdTextView = headerView.findViewById(R.id.userIdTextView);

        userIdTextView.setText("User ID: " + userId);

        // Fetch and set user name asynchronously
        fetchUserNameMenu(userId, userIdTextView);

        // end here
        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        handler = new Handler();
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                refrshDeviceButton();
                handler.postDelayed(this, REFRESH_INTERVAL);
            }
        };
        handler.postDelayed(refreshRunnable, REFRESH_INTERVAL);
        String selectedDeviceId = retrieveSelectedDeviceId();
        if (selectedDeviceId != null) {
            deviceButton = findViewById(R.id.Device);
            deviceButton.setText(selectedDeviceId);
            deviceButton.setTag(selectedDeviceId);
            fetchLocationForDevice(userId, selectedDeviceId);
            fetchPhoneNumber(selectedDeviceId);
            fetchDataAndDisplayOnConsole(selectedDeviceId);
        }

        //Zoomin = findViewById(R.id.zoominButton);
        Zoomout = findViewById(R.id.zoomoutButton);
        Zoomout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(MapboxMap mapboxMap) {
                        CameraPosition position = new CameraPosition.Builder()
                                .target(mapboxMap.getCameraPosition().target)
                                .zoom(mapboxMap.getCameraPosition().zoom - 1)
                                .build();
                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000);
                    }
                });
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            checkAndPromptLocationEnabled();
        }
        //Navigation Drawer
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                drawerLayout.closeDrawers();
                if (item.getItemId() == R.id.nav_history) {
                    // Handle history item click
                    Intent historyIntent = new Intent(SecondActivity.this, History.class);
                    historyIntent.putExtra("userId", userId);
                    Button deviceButton = findViewById(R.id.Device);
                    String deviceId = (String) deviceButton.getTag();
                    historyIntent.putExtra("deviceId", deviceId);
                    startActivity(historyIntent);
                    return true;
                } else if (item.getItemId() == R.id.nav_tracking) {
                    // Handle tracking item click
                    Intent trackingIntent = new Intent(SecondActivity.this, SecondActivity.class);
                    trackingIntent.putExtra("userId", userId);
                    Button deviceButton = findViewById(R.id.Device);
                    String deviceId = (String) deviceButton.getTag();
                    trackingIntent.putExtra("deviceId", deviceId);
                    startActivity(trackingIntent);
                    return true;
                } else if (item.getItemId() == R.id.nav_product) {
                    // Handle product item click
                    Intent productIntent = new Intent(SecondActivity.this, product.class);
                    productIntent.putExtra("userId", userId);
                    Button deviceButton = findViewById(R.id.Device);
                    String deviceId = (String) deviceButton.getTag();
                    productIntent.putExtra("deviceId", deviceId);
                    startActivity(productIntent);
                    return true;
                } else if (item.getItemId() == R.id.nav_info) {
                    // Handle info item click
                    Intent infoIntent = new Intent(SecondActivity.this, info.class);
                    // Pass user ID and device ID to the info class
                    infoIntent.putExtra("userId", userId);
                    Button deviceButton = findViewById(R.id.Device);
                    String deviceId = (String) deviceButton.getTag();
                    infoIntent.putExtra("deviceId", deviceId);
                    startActivity(infoIntent);
                    return true;
                } else if (item.getItemId() == R.id.nav_sub) {
                    // Handle settings item click
                    //Intent subscriptionIntent = new Intent(SecondActivity.this, sub.class);
                    //subscriptionIntent.putExtra("userId", userId);
                    //Button deviceButton = findViewById(R.id.Device);
                    //String deviceId = (String) deviceButton.getTag();
                    //subscriptionIntent.putExtra("deviceId", deviceId);
                    //startActivity(subscriptionIntent);
                    Toast.makeText(SecondActivity.this, "Coming Soon!", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (item.getItemId() == R.id.nav_Contact) {
                    // Handle settings item click
                    String url = "https://api.whatsapp.com/send/?phone=916291177352&text&type=phone_number&app_absent=0";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                } else if (item.getItemId() == R.id.privacy) {
                    String privacypolicy =  "https://iotaii.com/privacy-policy";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(privacypolicy));
                    startActivity(intent);

            } else if (item.getItemId() == R.id.nav_logout) {
                    goToMainActivity();
                    return true;
                }
                return false;
            }
        });
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        zoomToLocationButton = findViewById(R.id.zoomToLocationButton);
        Button directionsButton = findViewById(R.id.directionsButton);
        directionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deviceButton.getTag() == null) {
                    Toast.makeText(SecondActivity.this, "Please select a device first", Toast.LENGTH_SHORT).show();
                } else if (currentLocation != null && directionsButton != null) {
                    mapView.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(MapboxMap mapboxMap) {
                            if (deviceLocation != null) {
                                double userLat = mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude();
                                double userLng = mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude();
                                double deviceLat = deviceLocation.getLatitude();
                                double deviceLng = deviceLocation.getLongitude();

                                // Show a dialog to choose between Google Maps, Ola, and Uber
                                AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                                builder.setTitle("Choose an app for navigation")
                                        .setItems(new CharSequence[]{"Google Maps", "Ola", "Uber"}, (dialog, which) -> {
                                            switch (which) {
                                                case 0:
                                                    // Google Maps
                                                    String directionUrl = "https://www.google.com/maps/dir/?api=1" +
                                                            "&origin=" + userLat + "," + userLng +
                                                            "&destination=" + deviceLat + "," + deviceLng;
                                                    Intent googleMapsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(directionUrl));
                                                    googleMapsIntent.setPackage("com.google.android.apps.maps");
                                                    startActivity(googleMapsIntent);
                                                    break;
                                                case 1:
                                                    // Ola
                                                    String olaUrl = "https://book.olacabs.com/?lat=" + deviceLat + "&lng=" + deviceLng;
                                                    Intent olaIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(olaUrl));
                                                    startActivity(olaIntent);
                                                    break;
                                                case 2:
                                                    // Uber
                                                    String uberUrl = "https://m.uber.com/ul/?action=setPickup&pickup=my_location&dropoff[latitude]=" + deviceLat + "&dropoff[longitude]=" + deviceLng;
                                                    Intent uberIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uberUrl));
                                                    startActivity(uberIntent);
                                                    break;
                                            }
                                        });
                                builder.show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(SecondActivity.this, "Destination not found", Toast.LENGTH_SHORT).show();
                }
            }

        });

        deviceButton = findViewById(R.id.Device);
        Button hisbutton = findViewById(R.id.history);
        showTextView = findViewById(R.id.show);
        deviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchDevicesForUser(userId);
            }
        });
        hisbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SecondActivity.this, History.class);
                intent.putExtra("userId", userId);
                Button deviceButton = findViewById(R.id.Device);
                String deviceId = (String) deviceButton.getTag();
                intent.putExtra("deviceId", deviceId);
                startActivity(intent);
            }
        });
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                mapboxMap.setStyle(new Style.Builder().fromUri("https://api.maptiler.com/maps/" + mapId + "/style.json?key=" + apiKey));
                //updatePhoneLocationMarker(currentLocation);
                mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(currentLocation).zoom(15.0).build()));
            }
        });
        mapView.getMapAsync(mapboxMap -> {
            mapboxMap.setStyle(new Style.Builder().fromUri("https://api.maptiler.com/maps/" + mapId + "/style.json?key=" + apiKey), style -> {
                if (checkLocationPermission()) {
                    enableLocationComponent(style);
                } else {
                    requestLocationPermission();
                }
            });
        });
        zoomToLocationButton.setOnClickListener(view -> zoomToUsersLocation());

        callButton = findViewById(R.id.callButton);
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + phoneNumber));
                    startActivity(callIntent);
                }
            }
        });

    }

    @SuppressLint("StaticFieldLeak")
    private void fetchUserNameMenu(String userId, TextView userIdTextView) {
        String apiUrl = "http://3.109.34.34:8080/get-user-name/" + userId;

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    URL url = new URL(params[0]);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        bufferedReader.close();
                        return stringBuilder.toString();
                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String response) {
                if (response != null) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String userName = jsonResponse.getString("user_name");
                        userIdTextView.setText("User Name: " + userName); // Set user name
                    } catch (JSONException e) {
                        e.printStackTrace();
                        // Handle JSON parsing error
                    }
                } else {
                    // Handle API response error
                }
            }
        }.execute(apiUrl);
    }


    private void refrshDeviceButton() {
        selectedDeviceId = retrieveSelectedDeviceId();
        if (selectedDeviceId != null) {
            deviceButton.setText(selectedDeviceId);
            deviceButton.setTag(selectedDeviceId);
            fetchLocationForDevice(userId, selectedDeviceId);
            fetchPhoneNumber(selectedDeviceId);
            fetchDataAndDisplayOnConsole(selectedDeviceId);
        }
    }
    @SuppressLint("StaticFieldLeak")
    private void fetchDataAndDisplayOnConsole(String deviceId) {
        String apiUrl = "http://3.109.34.34:8080/latest-timestamp/" + deviceId;

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    URL url = new URL(params[0]);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        bufferedReader.close();
                        return stringBuilder.toString();
                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            protected void onPostExecute(String responseData) {
                if (responseData != null) {
                    // Log the retrieved data to the console
                    Log.d("API Response", responseData);
                    timeTextView.setText("Last TimeStamp:" + responseData);
                } else {
                    Log.e("API Error", "Error fetching data from API");
                }
            }
        }.execute(apiUrl);
    }
    private void checkAndPromptLocationEnabled() {
        if (!isLocationEnabled()) {
            // Location services are not enabled, prompt the user to enable them
            showLocationDisabledDialog();
        }
    }
    private boolean isLocationEnabled() {
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
    private void saveSelectedDeviceId(String deviceId) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("selectedDeviceId", deviceId);
        editor.apply();
    }
    private String retrieveSelectedDeviceId() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getString("selectedDeviceId", null);
    }
    private void showLocationDisabledDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Location Disabled");
        builder.setMessage("Please Turn On Location services to use this app.");
        builder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(SecondActivity.this, "Location services are required to use this app.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }
    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mapView.getMapAsync(mapboxMap -> {
                    mapboxMap.getStyle(style -> enableLocationComponent(style));
                });
            }
        }
    }
    //three dot
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Get the menu item for user ID
        MenuItem userIdMenuItem = menu.findItem(R.id.action_userid);
        userIdMenuItem.setTitle("User ID: " + userId); // Set user ID

        // Fetch and set user name asynchronously
        fetchUserName(userId, userIdMenuItem);

        return true;
    }

    @SuppressLint("StaticFieldLeak")
    private void fetchUserName(String userId, MenuItem userIdMenuItem) {
        String apiUrl = "http://3.109.34.34:8080/get-user-name/" + userId;

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    URL url = new URL(params[0]);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        bufferedReader.close();
                        return stringBuilder.toString();
                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String response) {
                if (response != null) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String userName = jsonResponse.getString("user_name");
                        userIdMenuItem.setTitle("User: " + userName); // Set user name
                    } catch (JSONException e) {
                        e.printStackTrace();
                        // Handle JSON parsing error
                        userIdMenuItem.setTitle("User ID: " + userId); // Set user ID if name not available
                    }
                } else {
                    // Handle API response error
                    userIdMenuItem.setTitle("User ID: " + userId); // Set user ID if name not available
                }
            }
        }.execute(apiUrl);
    }


    private void enableLocationComponent(@NonNull Style style) {
        LocationComponentActivationOptions activationOptions = LocationComponentActivationOptions.builder(this, style)
                .locationComponentOptions(LocationComponentOptions.builder(this)
                        .trackingGesturesManagement(true)
                        .accuracyColor(ContextCompat.getColor(this, R.color.mapboxBlue))
                        .build())
                .build();
        mapView.getMapAsync(mapboxMap -> {
            mapboxMap.getLocationComponent().activateLocationComponent(activationOptions);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestLocationPermission();
                return;
            }
            mapboxMap.getLocationComponent().setLocationComponentEnabled(true);
            mapboxMap.getLocationComponent().setRenderMode(RenderMode.COMPASS);

        });
    }
    private void zoomToUsersLocation() {
        mapView.getMapAsync(mapboxMap -> {
            if (mapboxMap.getLocationComponent().getLastKnownLocation() != null) {
                double userLat = mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude();
                double userLng = mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude();
                LatLng userLocation = new LatLng(userLat, userLng);
                mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 18));
            }
        });
    }
    @SuppressLint("StaticFieldLeak")
    private void fetchDevicesForUser(String userId) {
        String apiUrl = "http://3.109.34.34:8080/devices/" + userId;

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    URL url = new URL(params[0]);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        bufferedReader.close();
                        return stringBuilder.toString();
                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String devicesJson) {
                if (devicesJson != null) {
                    try {
                        JSONArray devicesArray = new JSONArray(devicesJson);
                        if (devicesArray.length() > 0) {
                            String[] deviceIds = new String[devicesArray.length()];
                            for (int i = 0; i < devicesArray.length(); i++) {
                                JSONObject device = devicesArray.getJSONObject(i);
                                String deviceId = device.getString("device_id");
                                deviceIds[i] = deviceId;
                            }
                            // Show the device list dialog
                            showDeviceSelectionDialog(deviceIds);
                        } else {
                            Toast.makeText(SecondActivity.this, "No devices found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(SecondActivity.this, "Error fetching devices", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(apiUrl);
    }
    private void showDeviceSelectionDialog(String[] deviceIds) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
        builder.setTitle("Select Device");
        builder.setItems(deviceIds, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Fetch location data for the selected device
                String selectedDeviceId = deviceIds[which];
                deviceButton.setText(selectedDeviceId);
                deviceButton.setTag(selectedDeviceId);
                fetchLocationForDevice(userId, selectedDeviceId);
                saveSelectedDeviceId(selectedDeviceId);
                fetchPhoneNumber(selectedDeviceId);
                fetchDataAndDisplayOnConsole(selectedDeviceId);
            }
        });
        builder.show();
    }
    @SuppressLint("StaticFieldLeak")
    private void fetchPhoneNumber(String deviceId) {
        Log.d("fetchPhoneNumber", "Fetching phone number for device ID: " + deviceId);
        String apiUrl = "http://3.109.34.34:8080/retrieve-phone-number/" + deviceId;

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    URL url = new URL(params[0]);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        bufferedReader.close();
                        return stringBuilder.toString();
                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String phoneNumberJson) {
                if (phoneNumberJson != null) {
                    try {
                        JSONObject phoneNumberObject = new JSONObject(phoneNumberJson);
                        String phoneNumber = phoneNumberObject.getString("phoneNumber");
                        // Now you have the phone number, you can use it as needed
                        // For example, you can display it in a TextView or use it for further processing
                        setPhoneNumber(phoneNumber);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Handle error condition, if any
                    Log.e("PhoneNumber", "Error fetching phone number");
                }
            }
        }.execute(apiUrl);
    }

    // Method to update phoneNumber variable in the activity class
    private void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @SuppressLint("StaticFieldLeak")
    private void fetchLocationForDevice(String userId, String deviceId) {
        String apiUrl = "http://3.109.34.34:8080/live-location-summary/" + userId + "/" + deviceId;
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    URL url = new URL(params[0]);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        bufferedReader.close();
                        return stringBuilder.toString();
                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String liveLocationJson) {
                if (liveLocationJson != null) {
                    try {
                        JSONObject locationObject = new JSONObject(liveLocationJson);
                        String latitudeString = locationObject.getString("latitude");
                        String longitudeString = locationObject.getString("longitude");
                        double latitude = parseLatitude(latitudeString);
                        double longitude = parseLongitude(longitudeString);
                        LatLng newLocation = new LatLng(latitude, longitude);
                        deviceLocation = new LatLng(latitude, longitude);
                        updateMarkerPosition(deviceLocation);

                        geocoder = new Geocoder(SecondActivity.this, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        if (!addresses.isEmpty()) {
                            Address address = addresses.get(0);
                            String addressLine = address.getAddressLine(0);
                            showTextView.setText("Current Location: " + addressLine);
                        } else {
                            showTextView.setText("Address not found");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    //Toast.makeText(SecondActivity.this, "Error fetching live location", Toast.LENGTH_SHORT).show();
                }
                mapView.getMapAsync(mapboxMap -> {
                    mapboxMap.getStyle(style -> enableLocationComponent(style));
                });
            }
        }.execute(apiUrl);
    }
    private double parseLatitude(String latitudeString) {
        return Double.parseDouble(latitudeString.substring(0, latitudeString.length() - 1));
    }
    private double parseLongitude(String longitudeString) {
        return Double.parseDouble(longitudeString.substring(0, longitudeString.length() - 1));
    }
    private void updateMarkerPosition(LatLng newLocation) {
        currentLocation = newLocation;
        if (mapView != null) {
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(MapboxMap mapboxMap) {
                    mapboxMap.clear(); // Clear any existing markers on the map
                    mapboxMap.addMarker(new MarkerOptions().position(currentLocation)); // Add a marker at the current location
                    mapboxMap.animateCamera(CameraUpdateFactory.newLatLng(currentLocation)); // Animate camera to the current location
                }
            });
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (mapView != null) {
            mapView.onStart();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (mapView != null) {
            mapView.onStop();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
        handler.removeCallbacks(refreshRunnable);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

    // Three dot portion
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        if (id == R.id.action_logout) {
            // Handle settings action
            goToMainActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_exit_confirmation, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialogView);

            AlertDialog dialog = builder.create();
            dialog.show();

            Button buttonNo = dialogView.findViewById(R.id.button_no);
            Button buttonYes = dialogView.findViewById(R.id.button_yes);

            // Apply pop animation for dialog entrance
            dialogView.setScaleX(0.8f);
            dialogView.setScaleY(0.8f);
            dialogView.setAlpha(0f);
            dialogView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(200)
                    .start();

            buttonYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Apply pop animation
                    dialogView.animate()
                            .scaleX(0.8f)
                            .scaleY(0.8f)
                            .alpha(0)
                            .setDuration(200)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.dismiss();
                                    goToMainActivity(); // Finish all activities in the stack
                                }
                            })
                            .start();
                }
            });

            buttonNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Apply pop animation
                    dialogView.animate()
                            .scaleX(0.8f)
                            .scaleY(0.8f)
                            .alpha(0)
                            .setDuration(200)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.dismiss(); // Dismiss the dialog when "No" is tapped
                                }
                            })
                            .start();
                }
            });
        }
    }
    private void goToMainActivity() {
        // Implement logout logic here, such as clearing user session, preferences, etc.

        // Start MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}