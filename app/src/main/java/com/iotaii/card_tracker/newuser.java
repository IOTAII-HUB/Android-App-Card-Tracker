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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.iotaii.card_tracker.R;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class newuser extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_newuser);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            getWindow().setStatusBarColor(getResources().getColor(R.color.dark_grey));
            return insets;
        });

        // Find views
        EditText userIdEditText = findViewById(R.id.user_id);
        EditText userNameEditText = findViewById(R.id.user_name);
        EditText tenantIdEditText = findViewById(R.id.tenant_id);
        EditText passwordEditText = findViewById(R.id.password);
        EditText roleEditText = findViewById(R.id.role);
        Button submitButton = findViewById(R.id.update);

        // Set onClickListener for the submit button
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get user inputs
                String userId = userIdEditText.getText().toString().trim();
                String userName = userNameEditText.getText().toString().trim();
                String tenantId = tenantIdEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String role = roleEditText.getText().toString().trim();

                // Validate inputs
                if (userId.isEmpty() || userName.isEmpty() || tenantId.isEmpty() || password.isEmpty() || role.isEmpty()) {
                    Toast.makeText(newuser.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Register new user
                registerNewUser(userId, userName, tenantId, password, role);
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private void registerNewUser(String userId, String userName, String tenantId, String password, String role) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String response = "";

                try {
                    // Create JSON object with user data
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("user_id", userId);
                    jsonObject.put("user_name", userName);
                    jsonObject.put("tenant_id", tenantId);
                    jsonObject.put("password", password);
                    jsonObject.put("role", role);

                    // Create URL and connection
                    URL url = new URL("http://3.109.34.34:8080/register");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept", "application/json");

                    // Write JSON data to the server
                    OutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
                    outputStream.write(jsonObject.toString().getBytes());
                    outputStream.flush();

                    // Get response from the server
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        response = "User registered successfully!";
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
                Toast.makeText(newuser.this, response, Toast.LENGTH_SHORT).show();
                if (response.equals("User registered successfully!")) {
                    // Redirect to admin activity
                    Intent intent = new Intent(newuser.this, DisplayAdminActivity.class);
                    startActivity(intent);
                }
            }
        }.execute();
    }
}
