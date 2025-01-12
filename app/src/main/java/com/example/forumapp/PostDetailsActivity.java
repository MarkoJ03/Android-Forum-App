package com.example.forumapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.List;
import java.util.Map;

public class PostDetailsActivity extends AppCompatActivity {

    private static final String TAG = "PostDetailsActivity";

    private TextView postTitle, postContent;
    private RecyclerView commentsRecyclerView;
    private EditText commentInput;
    private Button commentSubmitButton;
    private CommentsAdapter commentsAdapter;
    private List<Comment> commentsList = new ArrayList<>();
    private int postId;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);


        postTitle = findViewById(R.id.postDetailsTitle);
        postContent = findViewById(R.id.postDetailsContent);
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        commentInput = findViewById(R.id.commentInput);
        commentSubmitButton = findViewById(R.id.commentSubmitButton);


        commentsAdapter = new CommentsAdapter(commentsList);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentsAdapter);


        requestQueue = Volley.newRequestQueue(this);

        // Dohvatanje post ID-a iz Intent-a
        postId = getIntent().getIntExtra("post_id", -1);
        if (postId == -1) {
            Toast.makeText(this, "Invalid post ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Prikaz detalja posta
        postTitle.setText(getIntent().getStringExtra("post_title"));
        postContent.setText(getIntent().getStringExtra("post_content"));

        // Ucitavanje komentara
        fetchComments();

        // Dodavanje komentara
        commentSubmitButton.setOnClickListener(v -> submitComment());
    }

    private void fetchComments() {
        String url = MainActivity.URL + "/api/comment/post/" + postId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    commentsList.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject commentObject = response.getJSONObject(i);
                            int id = commentObject.getInt("id");
                            String content = commentObject.getString("content");
                            // Provera da li `username` postoji u JSON odgovoru
                            String author = commentObject.has("username") ?
                                    commentObject.getString("username") :
                                    "Unknown Author";

                            commentsList.add(new Comment(id, content, author));
                        }
                        commentsAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error: " + e.getMessage());
                        Toast.makeText(this, "Error parsing comments", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Error fetching comments: " + error.getMessage());
                    Toast.makeText(this, "Failed to fetch comments", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(request);
    }


    private void submitComment() {
        String commentText = commentInput.getText().toString().trim();
        if (commentText.isEmpty()) {
            Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = MainActivity.URL + "/api/comment/" + postId;
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, "Comment added successfully", Toast.LENGTH_SHORT).show();
                    commentInput.setText("");
                    fetchComments(); // Ponovno učitavanje komentara
                },
                error -> {
                    Log.e(TAG, "Error submitting comment: " + error.getMessage());
                    Toast.makeText(this, "Failed to add comment", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public byte[] getBody() {
                try {
                    JSONObject params = new JSONObject();
                    params.put("content", commentText);
                    return params.toString().getBytes("utf-8");
                } catch (JSONException | java.io.UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String token = prefs.getString("access_token", "");
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json"); // Dodajemo Content-Type za JSON
                return headers;
            }
        };

        requestQueue.add(request);
    }

}
