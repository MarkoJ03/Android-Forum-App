package com.example.forumapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private SharedPreferences sharedPreferences;
    public final static String URL = "http://192.168.0.107:5000";
    private EditText searchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawerLayout);
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        ImageButton menuButton = findViewById(R.id.btnSidebar);
        ImageButton profileButton = findViewById(R.id.btnProfile);
        NavigationView navigationView = findViewById(R.id.navigationView);
        ImageButton searchButton = findViewById(R.id.btnSearch);
        searchInput = findViewById(R.id.searchInput);

        FloatingActionButton buttonAddPost = findViewById(R.id.buttonAddPost);

        // Otvaranje aktivnosti za kreiranje posta
        buttonAddPost.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddPost.class);
            startActivity(intent);
        });

        // Listener za navigation view
        navigationView.setNavigationItemSelectedListener(this);

        // Defaultni fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        // Listener za otvaranje drawer-a
        menuButton.setOnClickListener(v -> {
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Prikaz i sakrivanje polja za pretragu
        searchButton.setOnClickListener(v -> {
            if (searchInput.getVisibility() == View.GONE) {
                searchInput.setVisibility(View.VISIBLE);
            } else {
                searchInput.setVisibility(View.GONE);
            }
        });

        // Logika za pretragu
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            String query = searchInput.getText().toString().trim();
            if (!query.isEmpty()) {
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
                if (currentFragment instanceof HomeFragment) {
                    ((HomeFragment) currentFragment).filterPosts(query);
                }
            } if(query.isEmpty()){
                loadFragment(new HomeFragment());
            }
            return true;
        });

        // Učitavanje UserPostsFragment-a na klik profila
        profileButton.setOnClickListener(v -> loadFragment(new UserPostsFragment()));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;

        if (item.getItemId() == R.id.nav_home) {
            selectedFragment = new HomeFragment();
        } else if (item.getItemId() == R.id.theme1) {
            selectedFragment = TopicFragment.newInstance(1);
        } else if (item.getItemId() == R.id.theme2) {
            selectedFragment = TopicFragment.newInstance(2);
        } else if (item.getItemId() == R.id.theme3) {
            selectedFragment = TopicFragment.newInstance(3);
        } else if (item.getItemId() == R.id.nav_logout) {
            logoutUser();
            return true;
        }

        if (selectedFragment != null) {
            loadFragment(selectedFragment);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void performSearch(String query) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (currentFragment instanceof HomeFragment) {
            ((HomeFragment) currentFragment).filterPosts(query);
        } else {
            Toast.makeText(this, "Search is available only on the Home screen", Toast.LENGTH_SHORT).show();
        }
    }

    private void logoutUser() {
        // Očisti SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Poruka
        Toast.makeText(MainActivity.this, "Logged out successfully.", Toast.LENGTH_SHORT).show();

        // Prebaci na login stranicu
        Intent intent = new Intent(MainActivity.this, Login.class);
        startActivity(intent);
        finish();
    }
}
