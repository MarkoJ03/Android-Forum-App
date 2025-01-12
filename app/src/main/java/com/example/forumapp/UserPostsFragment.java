package com.example.forumapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserPostsFragment extends Fragment {

    private RecyclerView recyclerViewUserPosts;
    private PostAdapter postAdapter;
    private List<Post> postList = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private static final String TAG = "UserPostsFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_posts, container, false);

        recyclerViewUserPosts = view.findViewById(R.id.recyclerViewUserPosts);
        recyclerViewUserPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        sharedPreferences = requireContext().getSharedPreferences("UserPrefs", getContext().MODE_PRIVATE);

        fetchUserPosts();

        return view;
    }

    private void fetchUserPosts() {
        String url = MainActivity.URL + "/api/user/posts";
        String token = sharedPreferences.getString("access_token", "");
        int loggedInUserId = sharedPreferences.getInt("user_id", -1);

        if (token.isEmpty()) {
            Toast.makeText(getContext(), "Token not found. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        postList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject postObject = response.getJSONObject(i);

                            int id = postObject.getInt("id");
                            String title = postObject.getString("Title");
                            String content = postObject.getString("Content");
                            String author = postObject.getString("username");
                            int userId = postObject.getInt("User.id");

                            Post post = new Post(id, title, content, author, userId);
                            postList.add(post);
                        }

                        postAdapter = new PostAdapter(postList, loggedInUserId);
                        recyclerViewUserPosts.setAdapter(postAdapter);

                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error: " + e.getMessage());
                        Toast.makeText(getContext(), "Error parsing user posts", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
            Log.e(TAG, "Error fetching user posts: " + error.toString());
            Toast.makeText(getContext(), "Failed to fetch user posts", Toast.LENGTH_SHORT).show();
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        queue.add(jsonArrayRequest);
    }
}
