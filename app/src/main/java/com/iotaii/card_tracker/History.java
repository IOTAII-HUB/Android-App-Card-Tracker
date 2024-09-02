package com.iotaii.card_tracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.iotaii.card_tracker.R;
import com.google.android.material.navigation.NavigationView;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;

import android.graphics.Color;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class History extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Button optionsButton;
    private Button buttonZoomIn;
    private Button buttonZoomOut;
    // Mapbox variables
    private MapView mapView;
    private MapboxMap mapboxMap; // Make mapboxMap a class-level variable
    private final String mapId = "streets-v2"; // MapTiler map ID
    private final String apiKey = "O8hzf5l378NIwtGVcvEF"; // Replace with your MapTiler API key
    private LatLng currentLocation = new LatLng(22.5726, 88.3639); // Initial location
    private String userId;
    private String deviceId;
    private Polyline existingPolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // Assuming userId and deviceId are obtained from Intent or somewhere else in your code
        userId = getIntent().getStringExtra("userId");
        deviceId = getIntent().getStringExtra("deviceId");

        // Log the userId and deviceId
        Log.d("History", "User ID: " + userId);
        Log.d("History", "Device ID: " + deviceId);
        setTitle("History");

        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("userId");
            deviceId = intent.getStringExtra("deviceId");
        }

        // Setup the toolbar and navigation drawer toggle
        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBarDrawerToggle.syncState();

        // Setup navigation item click listener
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                drawerLayout.closeDrawers();

                Intent intent = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_history) {
                    // Handle history item click
                    intent = new Intent(History.this, History.class);
                } else if (itemId == R.id.nav_tracking) {
                    // Handle home item click
                    intent = new Intent(History.this, SecondActivity.class);
                } else if (itemId == R.id.nav_product) {
                    // Handle product item click
                    intent = new Intent(History.this, product.class);
                } else if (itemId == R.id.nav_info) {
                    // Handle settings item click
                    intent = new Intent(History.this, info.class);
                } else if (itemId == R.id.nav_sub) {
                    // Handle settings item click
                    //intent = new Intent(History.this, sub.class);
                    Toast.makeText(History.this, "Coming Soon!", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.nav_Contact) {
                    // Handle Contact item click by opening the URL in a web browser
                    String url = "https://api.whatsapp.com/send/?phone=916291177352&text&type=phone_number&app_absent=0";
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                } else if (itemId == R.id.nav_logout) {
                    Intent logoutIntent = new Intent(History.this, MainActivity.class);
                    logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(logoutIntent);
                    finish();
                    return true;
                }
                if (intent != null) {
                    intent.putExtra("userId", userId);
                    intent.putExtra("deviceId", deviceId);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });

        View headerView = navigationView.getHeaderView(0);
        TextView userIdTextView = headerView.findViewById(R.id.userIdTextView);
        userIdTextView.setText("User ID:" + userId);
        fetchUserNameMenu(userId, userIdTextView);

        // Button to display options
        optionsButton = findViewById(R.id.buttonOptions);
        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionsPopupMenu();
            }
        });

        // Zoom In Button
        buttonZoomIn = findViewById(R.id.buttonZoomIn);
        buttonZoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapboxMap.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });

        // Zoom Out Button
        buttonZoomOut = findViewById(R.id.buttonZoomOut);
        buttonZoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapboxMap.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });

        // Initialize Mapbox with your API key
        Mapbox.getInstance(this);

        // Init the MapView
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                History.this.mapboxMap = mapboxMap; // Assign mapboxMap to class-level variable
                mapboxMap.setStyle(new Style.Builder().fromUri("https://api.maptiler.com/maps/" + mapId + "/style.json?key=" + apiKey));
                mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(currentLocation).zoom(10.0).build()));

                // Fetch history route data and draw polyline with default filter as 6 months
                fetchHistoryAndDrawRoute("6months");
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

    private void fetchHistoryAndDrawRoute(String filter) {
        String url = "http://3.109.34.34:8080/fetch-location-history/" + userId + "/" + deviceId + "?filter=" + filter;
        new FetchDataAsyncTask().execute(url);
    }

    private class FetchDataAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String url = urls[0];
            return fetchDataFromUrl(url);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                // Parse JSON and draw polyline
                List<LatLng> coordinates = parseHistoryCoordinates(result);
                if (coordinates != null && !coordinates.isEmpty()) {
                    drawPolyline(coordinates);
                } else {
                    // Show message when no data is available
                    showToast("No data available, choose different option");
                }
            } else {
                Log.e("History", "Failed to fetch data from URL");
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu); // Assuming you have a menu resource file named menu_history.xml
        MenuItem userIdMenuItem = menu.findItem(R.id.action_userid);
        userIdMenuItem.setTitle("User ID: " + userId);
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

    private String fetchDataFromUrl(String url) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                String jsonResponse = response.body().string();
                Log.d("History", "Received data from URL: " + jsonResponse);
                return jsonResponse;
            } else {
                Log.e("History", "Error response from server: " + response.code());
                return null;
            }
        } catch (Exception e) {
            Log.e("History", "Error fetching data from URL: " + e.getMessage());
            e.printStackTrace(); // Log the stack trace for debugging
            return null;
        }
    }

    private List<LatLng> parseHistoryCoordinates(String historyJson) {
        try {
            // Implement parsing history coordinates from JSON and return as a list of LatLng objects
            // Parse the JSON string and extract latitude and longitude values for each location
            // Construct LatLng objects and add them to a list
            // Example:
            List<LatLng> coordinates = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(historyJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                double latitude = jsonObject.getDouble("latitude");
                double longitude = jsonObject.getDouble("longitude");
                coordinates.add(new LatLng(latitude, longitude));
            }
            return coordinates;

            // Placeholder implementation (replace with your actual implementation)
        } catch (JSONException e) {
            Log.e("History", "Error parsing JSON data: " + e.getMessage());
            return null;
        }
    }

    private void drawPolyline(List<LatLng> coordinates) {
        // Clear existing polylines
        mapboxMap.clear();

        List<LatLng> points = new ArrayList<>();
        points.addAll(coordinates);

        int tealBlueColor = Color.rgb(54, 117, 136);

        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(points)
                .color(tealBlueColor)
                .width(5f);

        // Add the polyline to the map
        Polyline polyline = mapboxMap.addPolyline(polylineOptions);

        // Calculate the bounding box of the polyline
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : points) {
            builder.include(point);
        }
        LatLngBounds bounds = builder.build();

        // Animate the camera to zoom into the bounding box
        int padding = 100; // Padding around the bounding box in pixels
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mapboxMap.animateCamera(cameraUpdate);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showOptionsPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(this, optionsButton);
        popupMenu.getMenuInflater().inflate(R.menu.options_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            String filter = "60minutes"; // Default filter

            if (itemId == R.id.option_1_hour) {
                filter = "60minutes";
            } else if (itemId == R.id.option_6_hours) {
                filter = "6hours";
            } else if (itemId == R.id.option_24_hours) {
                filter = "24hours";
            }

            // Fetch history route data and redraw polyline based on selected filter
            fetchHistoryAndDrawRoute(filter);
            return true;
        });
        popupMenu.show();
    }

    private void checkPolylineAvailability() {
        // Check if the polyline for the last 60 minutes is available on the map
        if (mapboxMap != null && mapboxMap.getPolylines() != null) {
            boolean polylineFound = false;
            for (Polyline polyline : mapboxMap.getPolylines()) {
                int polylineColor = polyline.getColor();
                if (polylineColor == Color.rgb(54, 117, 136)) { // Replace with the color used for the polyline
                    polylineFound = true;
                    break;
                }
            }

            if (!polylineFound) {
                // Show "No data available" message if the polyline for the last 60 minutes is not found
                showToast("Data Found");
            }
        } else {
            // Show "No data available" message if the map or polylines are not initialized
            showToast("No data available for the last 60 minutes");
        }
    }

    // Other lifecycle methods
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns true, then it has handled the app icon touch event
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        int id = item.getItemId();
        if (id == R.id.action_logout) {
            // Handle logout action
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        } else {
            // Start SecondActivity when back button is pressed
            super.onBackPressed();
            Intent intent = new Intent(History.this, SecondActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("deviceId", deviceId);
            startActivity(intent);
        }
    }
}
