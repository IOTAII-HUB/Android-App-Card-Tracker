package com.iotaii.card_tracker;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.iotaii.card_tracker.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DisplayAdminActivity extends AppCompatActivity {

    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_admin);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setStatusBarColor(getResources().getColor(R.color.dark_grey));
        container = findViewById(R.id.container_layout);
        retrieveUsers();

        // Find the addnew button
        findViewById(R.id.add_new_user).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the newuser activity
                Intent intent = new Intent(DisplayAdminActivity.this, newuser.class);
                startActivity(intent);
            }
        });

        // Find the addnew button
        findViewById(R.id.add_new_device).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the newuser activity
                Intent intent = new Intent(DisplayAdminActivity.this, NewDeviceRegistrationActivity.class);
                startActivity(intent);
            }
        });

    }

    private int serialNumber = 1; // Initialize serial number counter

    private void createTextViewAndButtons(String deviceId, String name, String userId) {
        // Create TextView for Serial Number
        TextView serialNumberTextView = new TextView(this);
        serialNumberTextView.setText(String.valueOf(serialNumber++)); // Increment serial number
        LinearLayout.LayoutParams serialNumberParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        serialNumberParams.setMargins(8, 8, 8, 8); // Adjust margins as needed
        serialNumberTextView.setLayoutParams(serialNumberParams);

        // Create TextView for device ID
        TextView deviceIdTextView = new TextView(this);
        deviceIdTextView.setText(deviceId);
        LinearLayout.LayoutParams deviceIdParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        deviceIdParams.setMargins(8, 8, 8, 8); // Adjust margins as needed
        deviceIdTextView.setLayoutParams(deviceIdParams);

        // Create TextView for name
        TextView nameTextView = new TextView(this);
        nameTextView.setText(name);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        nameParams.setMargins(50, 8, 30, 8); // Adjust margins as needed
        nameTextView.setLayoutParams(nameParams);

        // Create Buttons
        Button userInfoButton = new Button(this);
        userInfoButton.setText("User Info");
        userInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passUserIdToAdmin(userId);
            }
        });

        Button deviceInfoButton = new Button(this);
        deviceInfoButton.setText("Device Info");
        deviceInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passDeviceIdToAdmin(deviceId);
            }
        });

        // Create spacers
        TextView spacer1 = new TextView(this);
        spacer1.setLayoutParams(new LinearLayout.LayoutParams(40, LinearLayout.LayoutParams.WRAP_CONTENT)); // Adjust width as needed

        TextView spacer2 = new TextView(this);
        spacer2.setLayoutParams(new LinearLayout.LayoutParams(30, LinearLayout.LayoutParams.WRAP_CONTENT)); // Adjust width as needed

        // Create a horizontal LinearLayout to hold Serial Number
        LinearLayout serialNumberLayout = new LinearLayout(this);
        serialNumberLayout.setOrientation(LinearLayout.VERTICAL);
        serialNumberLayout.addView(serialNumberTextView);

        // Create a horizontal LinearLayout to hold device ID
        LinearLayout deviceIdLayout = new LinearLayout(this);
        deviceIdLayout.setOrientation(LinearLayout.VERTICAL);
        deviceIdLayout.addView(deviceIdTextView);

        // Create a horizontal LinearLayout to hold name with spacer
        LinearLayout nameLayout = new LinearLayout(this);
        nameLayout.setOrientation(LinearLayout.HORIZONTAL);
        nameLayout.addView(nameTextView);
        nameLayout.addView(spacer1);

        // Create a vertical LinearLayout to hold user info and device info buttons with spacer
        LinearLayout buttonsLayout = new LinearLayout(this);
        buttonsLayout.setOrientation(LinearLayout.VERTICAL);
        buttonsLayout.addView(userInfoButton);
        buttonsLayout.addView(deviceInfoButton);

        // Add serialNumberLayout, deviceIdLayout, nameLayout, and buttonsLayout to a horizontal LinearLayout
        LinearLayout entryLayout = new LinearLayout(this);
        entryLayout.setOrientation(LinearLayout.HORIZONTAL);
        entryLayout.addView(serialNumberLayout);
        entryLayout.addView(deviceIdLayout);
        entryLayout.addView(nameLayout);
        entryLayout.addView(spacer2);
        entryLayout.addView(buttonsLayout);

        // Create a View for the line
        View lineView = new View(this);
        LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                1 // Adjust the height of the line as needed
        );
        lineParams.setMargins(8, 0, 8, 0); // Adjust margins as needed
        lineView.setLayoutParams(lineParams);
        lineView.setBackgroundColor(Color.parseColor("#80000000")); // Black transparent color

        // Add the lineView below the entryLayout
        container.addView(lineView);


        // Add entryLayout to the container
        container.addView(entryLayout);
    }
    private void retrieveUsers() {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://3.109.34.34:8080/retrieve-users";

        // Request a string response from the provided URL.
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            // Loop through the response array
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject user = response.getJSONObject(i);
                                String name = user.getString("name");
                                String deviceId = user.getString("device_id");
                                String userId = user.getString("user_id");

                                // Create TextView and Buttons dynamically
                                createTextViewAndButtons(deviceId, name, userId);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        // Add the request to the RequestQueue.
        queue.add(jsonArrayRequest);
    }

    private void passDeviceIdToAdmin(String deviceID) {
        Intent intent = new Intent(DisplayAdminActivity.this, DeviceInfoActivity.class);
        intent.putExtra("DEVICE_ID", deviceID);
        startActivity(intent);
    }

    private void passUserIdToAdmin(String userId) {
        Intent intent = new Intent(DisplayAdminActivity.this, admin.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Navigate back to MainActivity
        Intent intent = new Intent(DisplayAdminActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Optional: This will close the current activity and prevent it from staying in the back stack
    }

}