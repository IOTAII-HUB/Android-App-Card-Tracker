package com.iotaii.card_tracker;

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

public class DialogUserDeleteActivityActivity extends AppCompatActivity {

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.dialog_user_delete_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Retrieve user ID from intent extras
        userId = getIntent().getStringExtra("USER_ID");

        // Find the "No" button
        Button noButton = findViewById(R.id.no_user);
        // Set OnClickListener to finish the activity when "No" button is clicked
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Finish the activity to remove the screen
            }
        });

        // Find the "Yes" button
        Button yesButton = findViewById(R.id.yes_user);
        // Set OnClickListener to delete the user when "Yes" button is clicked
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Execute async task to delete the user
                new DeleteUserTask().execute();
            }
        });
    }

    private class DeleteUserTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // Construct the URL for the delete user API
                URL url = new URL("http://3.109.34.34:8080/delete-tenant/" + userId);
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
                // Deletion successful, return to admin activity
                Intent intent = new Intent(DialogUserDeleteActivityActivity.this, DisplayAdminActivity.class);
                Toast.makeText(DialogUserDeleteActivityActivity.this, "Successfully deleted.", Toast.LENGTH_SHORT).show();
                startActivity(intent);
                finish(); // Finish the current activity
            } else {
                // Handle deletion failure
                // You can show a toast or dialog to notify the user
                Toast.makeText(DialogUserDeleteActivityActivity.this, "Failed to delete user", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
