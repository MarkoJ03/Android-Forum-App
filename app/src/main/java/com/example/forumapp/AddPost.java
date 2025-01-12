package com.example.forumapp;

import static androidx.core.content.ContentProviderCompat.requireContext;

import static java.security.AccessController.getContext;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddPost extends AppCompatActivity {

    private EditText editTextTitle, editTextContent;
    private Spinner spinnerTopics;
    private Button buttonSubmitPost;
    private SharedPreferences sharedPreferences;


    private ArrayList<String> topicTitles = new ArrayList<>();
    private ArrayList<Integer> topicIds = new ArrayList<>();
    private int selectedTopicId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextContent = findViewById(R.id.editTextContent);
        spinnerTopics = findViewById(R.id.spinnerTopics);
        buttonSubmitPost = findViewById(R.id.buttonSubmitPost);
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        Intent intent = getIntent();
        if (intent.hasExtra("post_id")) {
            // Postavljanje u rezim uredjivanja
            int postId = intent.getIntExtra("post_id", -1);
            String postTitle = intent.getStringExtra("post_title");
            String postContent = intent.getStringExtra("post_content");

            // Popunjavanje postojecih podataka
            editTextTitle.setText(postTitle);
            editTextContent.setText(postContent);

            // Izmena teksta dugmeta
            buttonSubmitPost.setText("Izmeni Post");

            buttonSubmitPost.setOnClickListener(v -> updatePost(postId));
        } else {
            // Rezim za kreiranje novog posta
            buttonSubmitPost.setText("Dodaj Post");
            buttonSubmitPost.setOnClickListener(v -> {
                String title = editTextTitle.getText().toString().trim();
                String content = editTextContent.getText().toString().trim();
                String token = sharedPreferences.getString("access_token", "");

                if (title.isEmpty() || content.isEmpty() || selectedTopicId == -1) {
                    Toast.makeText(AddPost.this, "Popunite sva polja", Toast.LENGTH_SHORT).show();
                    return;
                }

                submitPost(title, content, selectedTopicId, token);
            });
        }

        // Povlacenje tema za Spinner
        fetchTopics();


        spinnerTopics.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedTopicId = topicIds.get(position); // Dobijamo ID iz liste tema
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedTopicId = -1;
            }
        });



        // Slusalac za izbor teme iz Spinner-a
        spinnerTopics.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedTopicId = topicIds.get(position); // Dobijamo ID iz liste tema
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedTopicId = -1;
            }
        });
    }

    private void fetchTopics() {
        String url = MainActivity.URL + "/api/topic";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            topicTitles.clear();
                            topicIds.clear();

                            for (int i = 0; i < response.length(); i++) {
                                JSONObject topic = response.getJSONObject(i);
                                int id = topic.getInt("id");
                                String title = topic.getString("title");

                                topicIds.add(id);
                                topicTitles.add(title);
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(AddPost.this,
                                    android.R.layout.simple_spinner_item, topicTitles);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerTopics.setAdapter(adapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(AddPost.this, "Error parsing topics", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(AddPost.this, "Error fetching topics", Toast.LENGTH_SHORT).show();
                    }
                });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void submitPost(String title, String content, int topicId, String token) {
        String url = MainActivity.URL + "/api/post";

        StringRequest createPostRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        int postId = jsonResponse.getInt("post_id");

                        // Nakon kreiranja posta, dodaj temu
                        addTopicToPost(postId, topicId);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("AddPost", "Error parsing post response: " + e.getMessage());
                        Toast.makeText(AddPost.this, "Error parsing post response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Log.e("AddPost", "Error creating post: " + error.getMessage());
                    Toast.makeText(AddPost.this, "Error creating post", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public byte[] getBody() {
                try {
                    JSONObject params = new JSONObject();
                    params.put("title", title);
                    params.put("content", content);
                    return params.toString().getBytes("utf-8");
                } catch (JSONException | java.io.UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(createPostRequest);
    }


    private void addTopicToPost(int postId, int topicId) {
        String url = MainActivity.URL + "/api/post_has_topic/" + postId + "/" + topicId;

        StringRequest addTopicRequest = new StringRequest(Request.Method.POST, url,
                response -> Toast.makeText(AddPost.this, "Topic added successfully", Toast.LENGTH_SHORT).show(),
                error -> {
                    error.printStackTrace();
                    Log.e("AddPost", "Error adding topic to post: " + error.getMessage());
                    Toast.makeText(AddPost.this, "Error adding topic to post", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + sharedPreferences.getString("access_token", ""));
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(addTopicRequest);
    }
    private void updatePost(int postId) {
        String url = MainActivity.URL + "/api/post/" + postId;

        String title = editTextTitle.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(AddPost.this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest updateRequest = new StringRequest(Request.Method.PUT, url,
                response -> {
                    Toast.makeText(AddPost.this, "Post updated successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Zatvori aktivnost i vrati se nazad
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(AddPost.this, "Error updating post", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public byte[] getBody() {
                try {
                    JSONObject params = new JSONObject();
                    params.put("title", title);
                    params.put("content", content);
                    return params.toString().getBytes("utf-8");
                } catch (JSONException | java.io.UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = sharedPreferences.getString("access_token", "");
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(updateRequest);
    }


}
