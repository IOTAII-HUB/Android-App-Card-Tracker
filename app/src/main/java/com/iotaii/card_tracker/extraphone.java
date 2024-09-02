package com.iotaii.card_tracker;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;


import com.iotaii.card_tracker.R;

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

public class extraphone extends AppCompatActivity {

    private static final String DEVICE_URL_PREFIX = "http://3.109.34.34:8080/devices/";
    private static final String UPDATE_PHONE_URL_PREFIX = "http://3.109.34.34:8080/update-phone-number/";
    private static final String DEFAULT_SELECTION = "Select your device";
    private static final String PREF_NAME = "MyPrefs";
    private static final String PREF_PHONE_NUMBER = "phoneNumber";

    private Spinner spinner;
    private EditText editPhone;
    private List<String> deviceList;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setStatusBarColor(getResources().getColor(R.color.teal_700));

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // Apply system bar insets to the main layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        spinner = findViewById(R.id.spinner);
        editPhone = findViewById(R.id.editPhone);
        deviceList = new ArrayList<>();
        deviceList.add(DEFAULT_SELECTION);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, deviceList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Set click listener for the spinner to fetch and show device IDs
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (!spinner.getSelectedItem().toString().equals(DEFAULT_SELECTION)) {
                    // Toast.makeText(Phone.this, "Selected Device: " + spinner.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        // Fetch devices for the user
        userId = getIntent().getStringExtra("userId");
        fetchDevices(userId);

        // Set click listener for the submit button
        Button submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the selected device ID
                String selectedDevice = spinner.getSelectedItem().toString();
                if (selectedDevice.equals(DEFAULT_SELECTION)) {
                    Toast.makeText(extraphone.this, "Please select a device", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get the entered phone number
                String phoneNumber = editPhone.getText().toString().trim();
                if (phoneNumber.isEmpty()) {
                    Toast.makeText(extraphone.this, "Please enter a phone number", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if the entered phone number has more than 10 digits
                if (phoneNumber.length() != 10) {
                    Toast.makeText(extraphone.this, "Invalid number", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update the phone number
                updatePhoneNumber(userId, selectedDevice, phoneNumber);
            }
        });
    }

    private void fetchDevices(String userId) {
        String url = DEVICE_URL_PREFIX + userId;

        new Thread(() -> {
            StringBuilder response = new StringBuilder();
            try {
                URL apiEndpoint = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) apiEndpoint.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                } else {
                    // Handle error response
                    response.append("Error: ").append(responseCode);
                }
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                response.append("Error: ").append(e.getMessage());
            }

            runOnUiThread(() -> {
                // Process JSON response
                try {
                    JSONArray jsonArray = new JSONArray(response.toString());
                    deviceList.clear();
                    deviceList.add(DEFAULT_SELECTION);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String deviceId = jsonObject.getString("device_id");
                        deviceList.add(deviceId);
                    }
                    spinner.setSelection(0); // Reset spinner selection
                    ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
                    adapter.notifyDataSetChanged(); // Update spinner
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }).start();
    }

    private void updatePhoneNumber(String userId, String deviceId, String phoneNumber) {
        String url = UPDATE_PHONE_URL_PREFIX + userId + "/" + deviceId;
        Log.d("URL", "updatePhoneNumber: " + url); // Log the constructed URL

        new Thread(() -> {
            try {
                URL apiEndpoint = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) apiEndpoint.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                JSONObject requestBody = new JSONObject();
                requestBody.put("phone_number", phoneNumber);

                // Write JSON data to output stream
                connection.getOutputStream().write(requestBody.toString().getBytes("UTF-8"));

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> {
                        Toast.makeText(extraphone.this, "Phone number updated successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(extraphone.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(extraphone.this, "Failed to update phone number", Toast.LENGTH_SHORT).show();
                    });
                }
                connection.disconnect();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(extraphone.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}
