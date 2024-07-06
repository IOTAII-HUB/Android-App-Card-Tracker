package com.iotaii.card_tracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.iotaii.card_tracker.R;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NewDeviceActivity extends AppCompatActivity {

    private String userId;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.new_device);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setStatusBarColor(getResources().getColor(R.color.dark_grey));
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Retrieve the values from the intent extras
        Intent intent = getIntent();
        if (intent != null) {
            userId = intent.getStringExtra("userId");
            deviceId = intent.getStringExtra("deviceId");

            String emailId = intent.getStringExtra("emailId");
            String phone = intent.getStringExtra("phone");
            String address = intent.getStringExtra("address");

            // Display the values in the EditText fields
            EditText emailEditText = findViewById(R.id.email_id);
            EditText phoneEditText = findViewById(R.id.phone_number);
            EditText addressEditText = findViewById(R.id.address);

            emailEditText.setText(emailId);
            phoneEditText.setText(phone);
            addressEditText.setText(address);

            // Update button click listener
            Button updateButton = findViewById(R.id.update);
            updateButton.setOnClickListener(view -> {
                String updatedEmail = emailEditText.getText().toString();
                String updatedPhone = phoneEditText.getText().toString();
                String updatedAddress = addressEditText.getText().toString();

                // Check if the user is updating the phone number
                if (!phone.equals(updatedPhone)) {
                    // Make API call to update phone number
                    updatePhoneNumber(updatedPhone);
                } else {
                    // Make API call to update other details
                    updateUserDetails(updatedEmail, updatedAddress);
                }
            });
        }
    }

    private void updateUserDetails(String email, String address) {
        // Construct JSON request body
        String jsonBody = "{"
                + "\"email_id\": \"" + email + "\","
                + "\"address\": \"" + address + "\""
                + "}";

        // Make API call to update user details
        makePutRequest("http://3.109.34.34:8080/update-user/" + userId + "?device_id=" + deviceId, jsonBody);
    }

    private void updatePhoneNumber(String phone) {
        // Construct JSON request body
        String jsonBody = "{"
                + "\"phone_number\": \"" + phone + "\""
                + "}";

        // Make API call to update phone number
        makePutRequest("http://3.109.34.34:8080/update-phone-number/" + userId + "/" + deviceId, jsonBody);
    }

    @SuppressLint("StaticFieldLeak")
    private void makePutRequest(String url, String jsonBody) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String result = "";
                try {
                    URL urlObj = new URL(url);
                    HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
                    connection.setRequestMethod("PUT");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);

                    // Write the request body
                    connection.getOutputStream().write(jsonBody.getBytes());

                    // Get the response
                    InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    StringBuilder response = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    inputStream.close();

                    // Set the result
                    result = response.toString();

                    // Disconnect
                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                if (result.equals("User information updated successfully")) {
                    // Navigate back to DeviceInfoActivity
                    Intent intent = new Intent(NewDeviceActivity.this, admin.class);

                    startActivity(intent);
                    finish(); // Close this activity
                } else {
                    Toast.makeText(NewDeviceActivity.this, "Failed to update", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }
}
