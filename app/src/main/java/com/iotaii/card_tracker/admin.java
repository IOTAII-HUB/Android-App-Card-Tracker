package com.iotaii.card_tracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.iotaii.card_tracker.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class admin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            getWindow().setStatusBarColor(getResources().getColor(R.color.dark_grey));
            return insets;
        });

        // Log the passed user ID
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("USER_ID")) {
            String userId = intent.getStringExtra("USER_ID");
            Log.d("admin", "Passed User ID: " + userId);

            // Fetch and display user data
            fetchAndDisplayUserData(userId);
        }

        // Find the delete button
        Button deleteButton = findViewById(R.id.delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the DialogUserDeleteActivityActivity
                Intent deleteIntent = new Intent(admin.this, DialogUserDeleteActivityActivity.class);
                String userId = ((TextView) findViewById(R.id.user_id_display)).getText().toString();
                deleteIntent.putExtra("USER_ID", userId);
                startActivity(deleteIntent);
            }
        });

        // Find the addnew button
        findViewById(R.id.addnew).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the newuser activity
                Intent intent = new Intent(admin.this, newuser.class);
                startActivity(intent);
            }
        });

        // Find the update button
        findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the updateuser activity and pass the user ID
                Intent upintent = new Intent(admin.this, updateuser.class);
                // Retrieve the user ID from the TextView
                String userId = ((TextView) findViewById(R.id.user_id_display)).getText().toString();
                // Pass the user ID to updateuser activity
                upintent.putExtra("USER_ID", userId);
                startActivity(upintent);
            }
        });

        // Find the show device button
//        findViewById(R.id.show_device).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Start the DeviceInfoActivity and pass the user_id
//                Intent intent = new Intent(admin.this, DeviceInfoActivity.class);
//                // Retrieve the user_id from the TextView
//                String userId = ((TextView) findViewById(R.id.user_id_display)).getText().toString();
//                // Pass the user_id to DeviceInfoActivity
//                intent.putExtra("USER_ID", userId);
//                startActivity(intent);
//            }
//        });

//        // Find the search field
//        // Find the search field
//        SearchView searchView = findViewById(R.id.search_field);
//        searchView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                searchView.requestFocus(); // Request focus when clicked
//            }
//        });
//
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                // Perform search when query submitted
//                performSearch(query);
//                return true;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                // Handle text changes if needed
//                return false;
//            }
//        });
    }

    @SuppressLint("StaticFieldLeak")
    private void fetchAndDisplayUserData(String userId) {
        // Make HTTP request to fetch user data by user_id
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String jsonData = ""; // Placeholder for JSON response

                try {
                    // Construct URL with user_id
                    String urlString = "http://3.109.34.34:8080/user-details/" + userId;

                    URL url = new URL(urlString);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    bufferedReader.close();
                    inputStream.close();
                    jsonData = stringBuilder.toString();
                    urlConnection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return jsonData;
            }

            @Override
            protected void onPostExecute(String jsonData) {
                super.onPostExecute(jsonData);
                try {
                    JSONObject user = new JSONObject(jsonData);
                    displayUserData(user);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void displayUserData(JSONObject user) {
        try {
            TextView userIdDisplay = findViewById(R.id.user_id_display);
            TextView userNameDisplay = findViewById(R.id.user_name_display);
            TextView tenantIdDisplay = findViewById(R.id.tenant_id_display);
            TextView passwordDisplay = findViewById(R.id.password_display);
            TextView roleDisplay = findViewById(R.id.role_display);

            // Extract user data from JSON object
            String userId = user.getString("user_id");
            String userName = user.getString("user_name");
            String tenantId = user.getString("tenant_id");
            String password = user.getString("password");
            String role = user.getString("role");

            // Display user data in EditText fields
            userIdDisplay.setText(userId);
            userNameDisplay.setText(userName);
            tenantIdDisplay.setText(tenantId);
            passwordDisplay.setText(password);
            roleDisplay.setText(role);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void performSearch(String query) {
        // Check if the query is a user_id or user_name
        if (query.matches("\\d+")) {
            // If the query is numeric, search by user_id
            searchByUserId(query);
        } else {
            // If the query is not numeric, search by user_name
            searchByUserName(query);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void searchByUserId(String userId) {
        // Make HTTP request to search for user data by user_id
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String jsonData = ""; // Placeholder for JSON response

                try {
                    // Construct URL with search query for user_id
                    String urlString = "http://3.109.34.34:8080/search-users?user_id=" + userId;

                    URL url = new URL(urlString);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    bufferedReader.close();
                    inputStream.close();
                    jsonData = stringBuilder.toString();
                    urlConnection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return jsonData;
            }

            @Override
            protected void onPostExecute(String jsonData) {
                super.onPostExecute(jsonData);
                try {
                    JSONObject user = new JSONObject(jsonData);
                    displayUserData(user);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private void searchByUserName(String userName) {
        // Make HTTP request to search for user data by user_name
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String jsonData = ""; // Placeholder for JSON response

                try {
                    // Construct URL with search query for user_name
                    String urlString = "http://3.109.34.34:8080/search-users?user_name=" + userName;

                    URL url = new URL(urlString);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    bufferedReader.close();
                    inputStream.close();
                    jsonData = stringBuilder.toString();
                    urlConnection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return jsonData;
            }

            @Override
            protected void onPostExecute(String jsonData) {
                super.onPostExecute(jsonData);
                try {
                    JSONObject user = new JSONObject(jsonData);
                    displayUserData(user);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent backintent = new Intent(admin.this, DisplayAdminActivity.class);
        startActivity(backintent);
    }
}
