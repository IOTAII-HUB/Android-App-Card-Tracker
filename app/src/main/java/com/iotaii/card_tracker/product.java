package com.iotaii.card_tracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

public class product extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private String userId;
    private String deviceId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setStatusBarColor(getResources().getColor(R.color.teal_700));
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient_background));


        // Set the title of the action bar
        setTitle("Buy Product");

        // Retrieve userId and deviceId from the intent
        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("userId");
            deviceId = intent.getStringExtra("deviceId");
        }

        // Find the "Buy" button and "Car Tracker" button by their IDs
        Button tracker = findViewById(R.id.button);
        Button cartracker = findViewById(R.id.carbutton);

        // Setup the toolbar and navigation drawer toggle
        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        actionBarDrawerToggle.syncState();

        // Set a click listener on the "Buy" button
        tracker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to navigate to the BuyActivity
                String url = "https://iotaii.com/id-card-tracking";
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(webIntent); // Start the BuyActivity
            }
        });

        // Set a click listener on the "Car Tracker" button to open the URL
        cartracker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the URL in a web browser
                String url = "https://iotaii.com/vehicle-telematics";
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(webIntent);
            }
        });
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView userIdTextView = headerView.findViewById(R.id.userIdTextView);
        userIdTextView.setText("User ID: " + userId);
        // Fetch and set user name asynchronously
        fetchUserNameMenu(userId, userIdTextView);

        // Setup navigation item click listener
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                drawerLayout.closeDrawers();

                Intent intent = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_history) {
                    intent = new Intent(product.this, History.class);
                } else if (itemId == R.id.nav_tracking) {
                    intent = new Intent(product.this, SecondActivity.class);
                } else if (itemId == R.id.nav_product) {
                    intent = new Intent(product.this, product.class);
                } else if (itemId == R.id.nav_info) {
                    intent = new Intent(product.this, info.class);
                } else if (itemId == R.id.nav_sub) {
                    //intent = new Intent(product.this, sub.class);
                    Toast.makeText(product.this, "Coming Soon!", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.nav_Contact) {
                    // Handle Contact item click by opening the URL in a web browser
                    String url = "https://api.whatsapp.com/send/?phone=916291177352&text&type=phone_number&app_absent=0";
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(webIntent);
                    return true;
                } else if (itemId == R.id.nav_logout) {
                    Intent logoutIntent = new Intent(product.this, MainActivity.class);
                    logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(logoutIntent);
                    finish();
                    return true;
                }
                if (intent != null) {
                    intent.putExtra("userId", userId);
                    intent.putExtra("deviceId", deviceId);
                    startActivity(intent);
                    return true;
                }
                return false;
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
            Intent intent = new Intent(product.this, SecondActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("deviceId", deviceId);
            startActivity(intent);
        }
    }
}
