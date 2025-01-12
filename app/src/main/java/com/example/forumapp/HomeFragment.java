package com.example.forumapp;

import static android.content.Context.MODE_PRIVATE;

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
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewAllPosts;
    private PostAdapter postAdapter;
    private List<Post> postList = new ArrayList<>();
    private List<Post> originalPostList = new ArrayList<>();
    private static final String TAG = "HomeFragment";
    private SharedPreferences sharedPreferences;

    public void setOriginalPostList(List<Post> posts) {
        originalPostList.clear();
        originalPostList.addAll(posts);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerViewAllPosts = view.findViewById(R.id.recyclerViewAllPosts);
        recyclerViewAllPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        sharedPreferences = requireContext().getSharedPreferences("UserPrefs", MODE_PRIVATE);

        fetchAllPosts();

        return view;
    }

    private void fetchAllPosts() {
        String url = MainActivity.URL + "/api/post";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        int loggedInUserId = sharedPreferences.getInt("user_id", -1);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        postList.clear();
                        originalPostList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject postObject = response.getJSONObject(i);

                            int id = postObject.getInt("id");
                            String title = postObject.getString("Title");
                            String content = postObject.getString("Content");
                            String author = postObject.getString("username");
                            int userId = postObject.getInt("User.id");

                            Post post = new Post(id, title, content, author, userId);
                            postList.add(post);
                            originalPostList.add(post); // Dodavanje u originalnu listu
                        }

                        postAdapter = new PostAdapter(postList, loggedInUserId);
                        recyclerViewAllPosts.setAdapter(postAdapter);

                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error: " + e.getMessage());
                        Toast.makeText(getContext(), "Error parsing posts", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
            Log.e(TAG, "Error fetching posts: " + error.toString());
            Toast.makeText(getContext(), "Failed to fetch posts", Toast.LENGTH_SHORT).show();
        });

        queue.add(jsonArrayRequest);
    }

    public void filterPosts(String query) {
        if (query.isEmpty()) {
            postAdapter.updatePosts(originalPostList); // Vrati originalne postove
        } else {
            List<Post> filteredPosts = new ArrayList<>();
            for (Post post : originalPostList) {
                if (post.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        post.getContent().toLowerCase().contains(query.toLowerCase())) {
                    filteredPosts.add(post);
                }
            }
            postAdapter.updatePosts(filteredPosts);
        }
    }
}
