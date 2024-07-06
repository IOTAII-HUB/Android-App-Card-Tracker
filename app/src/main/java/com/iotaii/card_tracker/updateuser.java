package com.iotaii.card_tracker;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class updateuser extends AppCompatActivity {

    private EditText userIdEditText;
    private EditText userNameEditText;
    private EditText tenantIdEditText;
    private EditText passwordEditText;
    private EditText roleEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updateuser);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setStatusBarColor(getResources().getColor(R.color.dark_grey));

        userIdEditText = findViewById(R.id.user_id);
        userNameEditText = findViewById(R.id.user_name);
        tenantIdEditText = findViewById(R.id.tenant_id);
        passwordEditText = findViewById(R.id.password);
        roleEditText = findViewById(R.id.role);

        // Retrieve user ID from intent extras
        String userId = getIntent().getStringExtra("USER_ID");

        // Make HTTP request to fetch user details
        new FetchUserDetailsTask().execute(userId);

        // Find the submit button
        Button submitButton = findViewById(R.id.userupdate);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update user details when submit button clicked
                updateUserDetails();
            }
        });
    }

    private void updateUserDetails() {
        // Retrieve user details from EditText fields
        String userId = userIdEditText.getText().toString();
        String userName = userNameEditText.getText().toString();
        String tenantId = tenantIdEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String role = roleEditText.getText().toString();

        // Make PUT request API call to update user details
        new UpdateUserDetailsTask().execute(userId, userName, tenantId, password, role);
    }

    private class UpdateUserDetailsTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            String userId = params[0];
            String userName = params[1];
            String tenantId = params[2];
            String password = params[3];
            String apiUrl = "http://3.109.34.34:8080/update-tenant/" + userId;

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("PUT");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setDoOutput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("user_name", userName);
                jsonParam.put("tenant_id", tenantId);
                jsonParam.put("password", password);

                OutputStream os = new BufferedOutputStream(urlConnection.getOutputStream());
                os.write(jsonParam.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = urlConnection.getResponseCode();

                return responseCode == HttpURLConnection.HTTP_OK;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        protected void onPostExecute(Boolean success) {
            if (success) {
                // Handle success
                Toast.makeText(updateuser.this, "User details updated successfully", Toast.LENGTH_SHORT).show();
                // Redirect to admin activity
                Intent intent = new Intent(updateuser.this, admin.class);
                startActivity(intent);
                finish(); // Finish the current activity
            } else {
                // Handle failure
                Toast.makeText(updateuser.this, "Failed to update user details", Toast.LENGTH_SHORT).show();
            }
        }

    }


    private class FetchUserDetailsTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String userId = params[0];
            String apiUrl = "http://3.109.34.34:8080/user-details/" + userId;
            String result = "";

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }

                bufferedReader.close();
                inputStream.close();
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                // Populate EditText fields with the retrieved user details
                userIdEditText.setText(jsonObject.getString("user_id"));
                userNameEditText.setText(jsonObject.getString("user_name"));
                tenantIdEditText.setText(String.valueOf(jsonObject.getInt("tenant_id")));
                passwordEditText.setText(jsonObject.getString("password"));
                roleEditText.setText(String.valueOf(jsonObject.getInt("role")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
