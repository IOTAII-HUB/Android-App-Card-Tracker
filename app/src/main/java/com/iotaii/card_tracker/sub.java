package com.iotaii.card_tracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;

import android.os.AsyncTask;
import android.os.Bundle;
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

public class sub extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private String userId;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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
                    intent = new Intent(sub.this, History.class);
                } else if (itemId == R.id.nav_tracking) {
                    intent = new Intent(sub.this, SecondActivity.class);
                } else if (itemId == R.id.nav_product) {
                    intent = new Intent(sub.this, product.class);
                } else if (itemId == R.id.nav_info) {
                    intent = new Intent(sub.this, info.class);
                } else if (itemId == R.id.nav_sub) {
                    //intent = new Intent(sub.this, sub.class);
                    Toast.makeText(sub.this, "Coming Soon!", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.nav_Contact) {
                    String url = "https://api.whatsapp.com/send/?phone=916291177352&text&type=phone_number&app_absent=0";
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                } else if (itemId == R.id.nav_logout) {
                    Intent logoutIntent = new Intent(sub.this, MainActivity.class);
                    logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
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
        View headerView = navigationView.getHeaderView(0);
        TextView userIdTextView = headerView.findViewById(R.id.userIdTextView);
        userIdTextView.setText("User ID:" + userId);
        fetchUserNameMenu(userId, userIdTextView);

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
            super.onBackPressed();
            Intent intent = new Intent(sub.this, SecondActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("deviceId", deviceId);
            startActivity(intent);
        }
    }
}
