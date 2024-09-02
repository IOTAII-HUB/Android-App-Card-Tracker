package com.iotaii.card_tracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.iotaii.card_tracker.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DeviceInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.device_info);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setStatusBarColor(getResources().getColor(R.color.dark_grey));
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Log the passed user ID
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("DEVICE_ID")) {
            String deviceID = intent.getStringExtra("DEVICE_ID");
            //System.out.println("#################################");
            //System.out.println(deviceID);

            // Call fetchAndDisplayDeviceInfo() with the retrieved deviceId
            fetchAndDisplayDeviceInfo1(deviceID);

        }

//        // Set up SearchView
//        SearchView searchView = findViewById(R.id.search_device);
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                // Perform search when the user submits the query
//                searchDevice(query);
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                // You can implement live search functionality here if needed
//                return false;
//            }
//        });

        Button deleteButton = findViewById(R.id.delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the DialogUserDeleteActivityActivity
                Intent deleteIntent = new Intent(DeviceInfoActivity.this, DialogDeviceDeleteActivity.class);
                String userId = ((TextView) findViewById(R.id.user_id_device_display)).getText().toString();
                String deviceId = ((TextView) findViewById(R.id.device_id_device_display)).getText().toString();
                deleteIntent.putExtra("USER_ID", userId);
                deleteIntent.putExtra("DEVICE_ID", deviceId);
                startActivity(deleteIntent);
            }
        });

        // Set up OnClickListener for the update button
        Button updateButton = findViewById(R.id.update_device);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the values to pass
                String userId = ((TextView) findViewById(R.id.user_id_device_display)).getText().toString();
                String name = ((TextView) findViewById(R.id.user_name_device_display)).getText().toString();
                String emailId = ((TextView) findViewById(R.id.email_id_device_display)).getText().toString();
                String phone = ((TextView) findViewById(R.id.phone_device_display)).getText().toString();
                String address = ((TextView) findViewById(R.id.address_device_Display)).getText().toString();
                String deviceId = ((TextView) findViewById(R.id.device_id_device_display)).getText().toString();

                // Create an intent to start NewDeviceActivity
                Intent intent = new Intent(DeviceInfoActivity.this, NewDeviceActivity.class);

                // Pass the values via intent extras
                intent.putExtra("userId", userId);
                intent.putExtra("name", name);
                intent.putExtra("emailId", emailId);
                intent.putExtra("phone", phone);
                intent.putExtra("address", address);
                intent.putExtra("deviceId", deviceId);

                // Start the activity
                startActivity(intent);
            }
        });

        // Retrieve the user_id from the intent
        String userId = getIntent().getStringExtra("USER_ID");

        // Fetch and display device information
        fetchAndDisplayDeviceInfo(userId);

        // Set onClickListener for the "Add New" button
        findViewById(R.id.addnew).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the NewDeviceRegistrationActivity and pass the user_id only
                Intent intent = new Intent(DeviceInfoActivity.this, NewDeviceRegistrationActivity.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private void fetchAndDisplayDeviceInfo1(String deviceId) {
        // Make HTTP request to fetch device information for the given deviceId
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String jsonData = ""; // Placeholder for JSON response

                try {
                    // Construct URL with deviceId
                    URL url = new URL("http://3.109.34.34:8080/search-device?device_id=" + deviceId);
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
                    JSONArray jsonArray = new JSONArray(jsonData);
                    if (jsonArray.length() > 0) {
                        // Display data of the first device only
                        JSONObject device = jsonArray.getJSONObject(0);
                        displayDeviceInfo(device);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }


    @SuppressLint("StaticFieldLeak")
    private void searchDevice(String deviceId) {
        // Make HTTP request to fetch device details based on the device ID
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String jsonData = ""; // Placeholder for JSON response

                try {
                    // Construct URL with the device ID
                    URL url = new URL("http://3.109.34.34:8080/search-device?device_id=" + deviceId);
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
                    JSONArray jsonArray = new JSONArray(jsonData);
                    if (jsonArray.length() > 0) {
                        // Display data of the first device only
                        JSONObject device = jsonArray.getJSONObject(0);
                        displayDeviceInfo(device);
                    } else {
                        // No device found with the given ID
                        Toast.makeText(DeviceInfoActivity.this, "No device found with ID: " + deviceId, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private void fetchAndDisplayDeviceInfo(String userId) {
        // Make HTTP request to fetch device information for the given user_id
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String jsonData = ""; // Placeholder for JSON response

                try {
                    // Construct URL with user_id
                    URL url = new URL("http://3.109.34.34:8080/devices/" + userId);
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
                    JSONArray jsonArray = new JSONArray(jsonData);
                    if (jsonArray.length() > 0) {
                        // Display data of the first device only
                        JSONObject device = jsonArray.getJSONObject(0);
                        displayDeviceInfo(device);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void displayDeviceInfo(JSONObject device) {
        try {
            TextView userIdDisplay = findViewById(R.id.user_id_device_display);
            TextView userNameDisplay = findViewById(R.id.user_name_device_display);
            TextView emailIdDisplay = findViewById(R.id.email_id_device_display);
            TextView mobileDisplay = findViewById(R.id.mobile_device_display);
            TextView dateOfPurchaseDisplay = findViewById(R.id.date_of_purchase_device_display);
            TextView invoiceNumberDisplay = findViewById(R.id.invoice_no_device_display);
            TextView deviceIdDisplay = findViewById(R.id.device_id_device_display);
            TextView servicesOfferedDisplay = findViewById(R.id.services_offered_device_display);
            TextView phoneDisplay = findViewById(R.id.phone_device_display);
            TextView addressDisplay = findViewById(R.id.address_device_Display);

            // Extract device data from JSON object
            String userId = device.getString("user_id");
            String name = device.getString("name");
            String emailId = device.getString("email_id");
            String mobile = device.getString("mob");
            String dateOfPurchase = device.getString("date_of_purchase");
            String invoiceNumber = device.getString("invoice_number");
            String deviceId = device.getString("device_id");
            String servicesOffered = device.getString("services_offered");
            String phone = device.getString("phone_number");
            String address = device.getString("address");

            // Display device data in TextViews
            userIdDisplay.setText(userId);
            userNameDisplay.setText(name);
            emailIdDisplay.setText(emailId);
            mobileDisplay.setText(mobile);
            dateOfPurchaseDisplay.setText(dateOfPurchase);
            invoiceNumberDisplay.setText(invoiceNumber);
            deviceIdDisplay.setText(deviceId);
            servicesOfferedDisplay.setText(servicesOffered);
            phoneDisplay.setText(phone);
            addressDisplay.setText(address);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
