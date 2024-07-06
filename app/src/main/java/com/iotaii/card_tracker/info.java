package com.iotaii.card_tracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.iotaii.card_tracker.R;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class info extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private TextView servicesOfferedTextView;
    private TextView batteryPercentageTextView;
    private TextView deviceIdTextView;
    private TextView dateOfPurchaseTextView;

    private String userId;
    private String deviceId;

    private static final long UPDATE_INTERVAL = 60 * 1000; // 60 seconds
    private Handler handler;
    private Runnable updateBatteryRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setStatusBarColor(getResources().getColor(R.color.teal_700));

        // Set custom gradient background for the action bar
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient_background));

        // Set the title of the action bar
        setTitle("Device Information");
        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("userId");
            deviceId = intent.getStringExtra("deviceId");
        }

        // Initialize TextViews to display device information
        servicesOfferedTextView = findViewById(R.id.services_offered);
        batteryPercentageTextView = findViewById(R.id.battery_percentage);
        deviceIdTextView = findViewById(R.id.device_id);
        dateOfPurchaseTextView = findViewById(R.id.date_of_purchase);

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
                    intent = new Intent(info.this, History.class);

                } else if (itemId == R.id.nav_tracking) {
                    // Handle home item click
                    intent = new Intent(info.this, SecondActivity.class);

                } else if (itemId == R.id.nav_product) {
                    // Handle product item click
                    intent = new Intent(info.this, product.class);
                } else if (itemId == R.id.nav_info) {
                    // Handle settings item click
                    intent = new Intent(info.this, info.class);
                } else if (itemId == R.id.nav_sub) {
                    // Handle settings item click
                    intent = new Intent(info.this, sub.class);
                } else if (item.getItemId() == R.id.nav_Contact) {
                    // Handle Contact item click by opening the URL in a web browser
                    String url = "https://api.whatsapp.com/send/?phone=916291177352&text&type=phone_number&app_absent=0";
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                } else if (item.getItemId() == R.id.nav_logout) {
                    Intent logoutIntent = new Intent(info.this, MainActivity.class);
                    logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(logoutIntent);
                    finish();
                    return true;
                }

                if (intent != null) {
                    intent.putExtra("userId", userId);
                    intent.putExtra("deviceId", deviceId);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK );
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
        View headerView = navigationView.getHeaderView(0);
        TextView userIdTextView = headerView.findViewById(R.id.userIdTextView);
        userIdTextView.setText("User ID: " + userId);

        // Fetch and set user name asynchronously
        fetchUserNameMenu(userId, userIdTextView);

        // Fetch initial battery percentage and device info
        fetchBatteryPercentage(userId, deviceId);
        fetchDeviceInfo(userId, deviceId);

        // Start periodic updates for battery percentage
        handler = new Handler();
        updateBatteryRunnable = new Runnable() {
            @Override
            public void run() {
                fetchBatteryPercentage(userId, deviceId);
                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        };
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


    @Override
    protected void onResume() {
        super.onResume();
        handler.post(updateBatteryRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(updateBatteryRunnable);
    }

    // Method to fetch battery percentage from the server
    @SuppressLint("StaticFieldLeak")
    private void fetchBatteryPercentage(String userId, String deviceId) {
        // Construct the URL with the received userId and deviceId
        String batteryApiUrl = "http://3.109.34.34:8080/live-location-summary/" + userId + "/" + deviceId;

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
            protected void onPostExecute(String batteryInfoJson) {
                if (batteryInfoJson != null) {
                    try {
                        JSONObject batteryInfo = new JSONObject(batteryInfoJson);
                        // Display battery percentage in the TextView
                        String batteryPercentage = String.valueOf(batteryInfo.getInt("battery_percentage"));
                        batteryPercentageTextView.setText(batteryPercentage);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(info.this, "Error fetching battery percentage", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(batteryApiUrl);
    }

    // Method to fetch device information from the server
    @SuppressLint("StaticFieldLeak")
    private void fetchDeviceInfo(String userId, String deviceId) {
        // Construct the URL with the received userId and deviceId
        String deviceInfoApiUrl = "http://3.109.34.34:8080/device-info/" + userId + "/" + deviceId;

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
            protected void onPostExecute(String deviceInfoJson) {
                if (deviceInfoJson != null) {
                    try {
                        JSONObject deviceInfo = new JSONObject(deviceInfoJson);
                        // Display device information in the TextViews
                        servicesOfferedTextView.setText(deviceInfo.getString("services_offered"));
                        deviceIdTextView.setText(deviceInfo.getString("device_id"));
                        dateOfPurchaseTextView.setText(deviceInfo.getString("date_of_purchase"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(info.this, "Error fetching device information", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(deviceInfoApiUrl);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // Start SecondActivity when back button is pressed
            super.onBackPressed();
            Intent intent = new Intent(info.this, SecondActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("deviceId", deviceId);
            startActivity(intent);
        }
    }
}
