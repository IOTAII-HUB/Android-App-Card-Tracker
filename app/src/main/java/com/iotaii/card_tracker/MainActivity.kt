package com.iotaii.card_tracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var userIdEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var rememberCheckBox: CheckBox
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var connectivityReceiver: ConnectivityReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.statusBarColor = resources.getColor(R.color.teal_700)
        supportActionBar?.setBackgroundDrawable(resources.getDrawable(R.drawable.gradient_background))

        userIdEditText = findViewById(R.id.user)
        passwordEditText = findViewById<EditText>(R.id.pass)
        rememberCheckBox = findViewById(R.id.remember_checkbox)
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        // Initialize ConnectivityReceiver
        connectivityReceiver = ConnectivityReceiver()

        // Check internet connection status when the app launches
        val isConnected = connectivityReceiver.checkInternetConnection(this)
        if (!isConnected) {
            Toast.makeText(this, "Please Turn On Your Internet!", Toast.LENGTH_SHORT).show()
        }

        // Register ConnectivityReceiver to monitor network changes
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(connectivityReceiver, filter)

        // Check if Remember Me was checked previously
        if (sharedPreferences.getBoolean("rememberChecked", false)) {
            userIdEditText.setText(sharedPreferences.getString("userId", ""))
            passwordEditText.setText(sharedPreferences.getString("password", ""))
            rememberCheckBox.isChecked = true
        }

        val btnConnect = findViewById<Button>(R.id.btn)
        btnConnect.setOnClickListener {
            val userId = userIdEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val rememberChecked = rememberCheckBox.isChecked

            if (userId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter user name and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save username and password in shared preferences if "Remember Me" is checked
            with(sharedPreferences.edit()) {
                putString("userId", userId)
                putString("password", password)
                putBoolean("rememberChecked", rememberChecked)
                apply()
            }

            // Execute network task
            NetworkTask().execute(userId, password, rememberChecked.toString())
        }

        val addDeviceText = findViewById<TextView>(R.id.add_device_text)
        addDeviceText.setOnClickListener {
            val username = userIdEditText.text.toString().trim()
            if (username.isEmpty()) {
                Toast.makeText(this@MainActivity, "Please enter a username", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Make API call to retrieve user_id
            val getUserIdUrl = "http://3.109.34.34:8080/get-user-id/$username"
            AsyncTask.execute {
                try {
                    val url = URL(getUserIdUrl)
                    val urlConnection = url.openConnection() as HttpURLConnection
                    urlConnection.requestMethod = "GET"

                    val responseCode = urlConnection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val inputStream = urlConnection.inputStream
                        val reader = BufferedReader(InputStreamReader(inputStream))
                        val response = StringBuilder()
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            response.append(line)
                        }
                        reader.close()
                        inputStream.close()
                        val jsonResponse = JSONObject(response.toString())
                        val userId = jsonResponse.getString("user_id")
                        // Launch extraphone activity with userId
                        runOnUiThread {
                            val intent = Intent(this@MainActivity, extraphone::class.java)
                            intent.putExtra("userId", userId)
                            startActivity(intent)
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Failed to retrieve user_id", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Failed to retrieve user_id: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(connectivityReceiver)
    }

    inner class NetworkTask : AsyncTask<String, Void, Pair<String, String>>() {

        override fun doInBackground(vararg params: String?): Pair<String, String> {
            val username = params[0]
            val password = params[1]
            val rememberChecked = params[2]

            val urlString = "http://3.109.34.34:8080/login_name"
            val jsonObject = JSONObject()
            jsonObject.put("username", username)
            jsonObject.put("password", password)
            jsonObject.put("remember", rememberChecked)

            return try {
                val url = URL(urlString)
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "POST"
                urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8")
                urlConnection.doInput = true
                urlConnection.doOutput = true

                val outputStream = urlConnection.outputStream
                val writer = BufferedWriter(OutputStreamWriter(outputStream, "UTF-8"))
                writer.write(jsonObject.toString())
                writer.flush()
                writer.close()
                outputStream.close()

                val responseCode = urlConnection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = urlConnection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()
                    inputStream.close()

                    val jsonResponse = JSONObject(response.toString())
                    val message = jsonResponse.getString("message")
                    val userId = jsonResponse.getString("user_id")

                    Pair(message, userId)
                } else {
                    Pair("Error: $responseCode", "")
                }
            } catch (e: Exception) {
                Pair("Turn on the Internet", "")
            }
        }

        override fun onPostExecute(result: Pair<String, String>) {
            val (message, userId) = result
            Log.d("NetworkResponse", "Message: $message, UserID: $userId")
            if (message == "Admin login successful") {
                val intent = Intent(this@MainActivity, DisplayAdminActivity::class.java)
                startActivity(intent)
                finish()
            } else if (message == "User login successful") {
                val intent = Intent(this@MainActivity, Phone::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
            } else if (message.startsWith("Error")) {
                Toast.makeText(this@MainActivity, "Username or password is incorrect", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Sorry For Your Inconveninece!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            super.onBackPressed()
        } else {
            finishAffinity()
        }
    }

    class ConnectivityReceiver : BroadcastReceiver() {

        companion object {
            private var isConnected = true
        }

        override fun onReceive(context: Context, intent: Intent) {
            checkInternetConnection(context)
        }

        fun checkInternetConnection(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetworkInfo
            val currentlyConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting

            if (currentlyConnected && !isConnected) {
                isConnected = true
                Toast.makeText(context, "Internet is turned on", Toast.LENGTH_SHORT).show()
            } else if (!currentlyConnected && isConnected) {
                isConnected = false
                Toast.makeText(context, "Please Turn On Your Internet!", Toast.LENGTH_SHORT).show()
            }

            return currentlyConnected
        }
    }
}
