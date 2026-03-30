package com.example.farmdirectoryupgraded

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

class OAuthActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Temporarily show a loading screen while we process the intent
        setContent {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        // Handle the incoming intent that started this activity
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // This is called if the activity is already running and receives a new intent
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val action = intent?.action
        val data: Uri? = intent?.data

        if (Intent.ACTION_VIEW == action && data != null) {
            // Check if the scheme and host match what we expect from AndroidManifest.xml
            // e.g., com.houserfarms.farmdirectorypro://oauth
            if (data.scheme == "com.houserfarms.farmdirectorypro" && data.host == "oauth") {
                
                // The URL will look something like:
                // com.houserfarms.farmdirectorypro://oauth?code=4/0AeaY...&scope=email...
                // We need to extract the 'code' parameter
                val authorizationCode = data.getQueryParameter("code")
                val error = data.getQueryParameter("error")

                if (authorizationCode != null) {
                    Log.d("OAuthActivity", "Received Authorization Code: $authorizationCode")
                    
                    // TODO: Send this authorizationCode to your backend server,
                    // OR exchange it for an Access Token and Refresh Token right here on the client
                    // using a library like AppAuth or a standard HTTP request to Google's token endpoint.
                    
                    // Once handled, return the user to the MainActivity
                    navigateToMain(success = true)
                } else if (error != null) {
                    Log.e("OAuthActivity", "OAuth Error: $error")
                    navigateToMain(success = false)
                } else {
                    Log.e("OAuthActivity", "No code or error found in the deep link URL")
                    navigateToMain(success = false)
                }
            } else {
                // Not our expected scheme/host, just go to main
                navigateToMain(success = false)
            }
        } else {
            // Not a View intent, just go to main
            navigateToMain(success = false)
        }
    }

    private fun navigateToMain(success: Boolean) {
        val intent = Intent(this, MainActivity::class.java).apply {
            // Clear the activity stack so the user can't press 'back' into the loading screen
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Optional: Pass data to MainActivity letting it know if login succeeded
            putExtra("OAUTH_SUCCESS", success)
        }
        startActivity(intent)
        finish() // Destroy this OAuthActivity
    }
}
