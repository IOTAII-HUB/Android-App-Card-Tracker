package com.iotaii.card_tracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.iotaii.card_tracker.R;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NewDeviceRegistrationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_device_registration);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setStatusBarColor(getResources().getColor(R.color.dark_grey));

        // Find EditText fields
        EditText userIdEditText = findViewById(R.id.user_id);
        EditText nameEditText = findViewById(R.id.name);
        EditText emailEditText = findViewById(R.id.email_id);
        EditText mobileEditText = findViewById(R.id.mob);
        EditText addressEditText = findViewById(R.id.address);
        EditText dateOfPurchaseEditText = findViewById(R.id.date_of_purchase);
        EditText invoiceNumberEditText = findViewById(R.id.invoice_no);
        EditText deviceIdEditText = findViewById(R.id.device_id);
        EditText servicesOfferedEditText = findViewById(R.id.services_offered);
        EditText phoneEditText = findViewById(R.id.phone_number);

        // Find Submit button
        Button submitButton = findViewById(R.id.submit_device);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get text from EditText fields
                String userId = userIdEditText.getText().toString();
                String name = nameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String mobile = mobileEditText.getText().toString();
                String address = addressEditText.getText().toString();
                String dateOfPurchase = dateOfPurchaseEditText.getText().toString();
                String invoiceNumber = invoiceNumberEditText.getText().toString();
                String deviceId = deviceIdEditText.getText().toString();
                String servicesOffered = servicesOfferedEditText.getText().toString();
                String phone = phoneEditText.getText().toString();

                // Validate inputs
                if (userId.isEmpty() || name.isEmpty() || email.isEmpty() || mobile.isEmpty() || address.isEmpty()
                        || dateOfPurchase.isEmpty() || invoiceNumber.isEmpty() || deviceId.isEmpty()
                        || servicesOffered.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(NewDeviceRegistrationActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Register new device
                registerNewDevice(userId, name, email, mobile, address, dateOfPurchase,
                        invoiceNumber, deviceId, servicesOffered, phone);
            }
        });
    }
    @SuppressLint("StaticFieldLeak")
    private void registerNewDevice(String userId, String name, String email, String mobile, String address,
                                   String dateOfPurchase, String invoiceNumber, String deviceId,
                                   String servicesOffered, String phone) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String response = "";

                try {
                    // Create JSON object with device data
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("user_id", userId);
                    jsonObject.put("name", name);
                    jsonObject.put("email_id", email);
                    jsonObject.put("mob", mobile);
                    jsonObject.put("address", address);
                    jsonObject.put("date_of_purchase", dateOfPurchase);
                    jsonObject.put("invoice_number", invoiceNumber);
                    jsonObject.put("device_id", deviceId);
                    jsonObject.put("services_offered", servicesOffered);
                    jsonObject.put("phone_number", phone);

                    // Create URL and connection
                    URL url = new URL("http://3.109.34.34:8080/register-device");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/json");

                    // Write JSON data to the server
                    OutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
                    outputStream.write(jsonObject.toString().getBytes());
                    outputStream.flush();

                    // Get response from the server
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        response = "Device registered successfully!";
                    } else {
                        response = "Error: " + connection.getResponseMessage();
                    }

                    // Close streams and connection
                    outputStream.close();
                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    response = "Error: " + e.getMessage();
                }

                return response;
            }

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                Toast.makeText(NewDeviceRegistrationActivity.this, response, Toast.LENGTH_SHORT).show();
                if (response.equals("Device registered successfully!")) {
                    // Redirect to DeviceInfoActivity
                    Intent intent = new Intent(NewDeviceRegistrationActivity.this, DisplayAdminActivity.class);
                    startActivity(intent);
                    finish(); // Finish this activity to prevent going back to it using the back button
                }
            }
        }.execute();
    }
}
