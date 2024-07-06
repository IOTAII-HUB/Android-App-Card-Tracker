package com.iotaii.card_tracker;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.iotaii.card_tracker.R;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class DialogDeviceDeleteActivity extends AppCompatActivity {

    private String userId;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.dialog_device_delete);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Retrieve user ID and device ID from intent extras
        userId = getIntent().getStringExtra("USER_ID");
        deviceId = getIntent().getStringExtra("DEVICE_ID");

        // Find the "No" button
        Button noButton = findViewById(R.id.no_device);
        // Set OnClickListener to finish the activity when "No" button is clicked
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Finish the activity to remove the screen
            }
        });

        // Find the "Yes" button
        Button yesButton = findViewById(R.id.yes_device);
        // Set OnClickListener to delete the user when "Yes" button is clicked
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Execute async task to delete the user
                new DeleteUserTask(DialogDeviceDeleteActivity.this).execute(userId, deviceId);
            }
        });
    }

    private static class DeleteUserTask extends AsyncTask<String, Void, Boolean> {

        private Context mContext;

        DeleteUserTask(Context context) {
            mContext = context;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String userId = params[0];
            String deviceId = params[1];
            try {
                // Construct the URL for the delete user API
                URL url = new URL("http://3.109.34.34:8080/delete-user/" + userId + "?device_id=" + deviceId);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("DELETE");
                int responseCode = urlConnection.getResponseCode();
                return responseCode == HttpURLConnection.HTTP_OK;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override

        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                // Deletion successful, show a toast message
                Toast.makeText(mContext, "User deleted successfully", Toast.LENGTH_SHORT).show();

                // Navigate to admin.java
                Intent intent = new Intent(mContext, DisplayAdminActivity.class);
                mContext.startActivity(intent);
            } else {
                // Handle deletion failure
                // You can show a toast or dialog to notify the user
                Toast.makeText(mContext, "Failed to delete user", Toast.LENGTH_SHORT).show();
            }
            // Finish the activity to remove the screen
            ((AppCompatActivity) mContext).finish();
        }

    }
}
