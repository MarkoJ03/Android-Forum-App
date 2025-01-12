package com.example.forumapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Proveri da li je korisnik ulogovan
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            // Ako je korisnik ulogovan, prebaci na MainActivity
            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Ako nije ulogovan, prebaci na RegisterLoginActivity
            Intent intent = new Intent(StartActivity.this, RegisterLogin.class);
            startActivity(intent);
            finish();
        }
    }
}
