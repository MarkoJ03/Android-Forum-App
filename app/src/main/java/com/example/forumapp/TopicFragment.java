package com.example.forumapp;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TopicFragment extends Fragment {

    private static final String ARG_TOPIC_ID = "topic_id";
    private int topicId;
    private List<Post> postList = new ArrayList<>();
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private SharedPreferences sharedPreferences;

    public static TopicFragment newInstance(int topicId) {
        TopicFragment fragment = new TopicFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TOPIC_ID, topicId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            topicId = getArguments().getInt(ARG_TOPIC_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_topic, container, false);
        sharedPreferences = requireContext().getSharedPreferences("UserPrefs", getContext().MODE_PRIVATE);
        recyclerView = view.findViewById(R.id.recyclerViewPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        int loggedInUserId = sharedPreferences.getInt("user_id", -1);
        postAdapter = new PostAdapter(postList, loggedInUserId);
        recyclerView.setAdapter(postAdapter);

        fetchPostsForTopic(topicId);
        return view;
    }

    private void fetchPostsForTopic(int topicId) {
        String url = MainActivity.URL + "/api/posts/topic/" + topicId;

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
                            String username = postObject.getString("username");
                            int userId = postObject.getInt("User.id");

                            Post post = new Post(id, title, content, username, userId);
                            postList.add(post);
                        }
                        postAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Error parsing posts.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(requireContext(), "Error fetching posts for topic.", Toast.LENGTH_SHORT).show();
                }
        );

        queue.add(jsonArrayRequest);
    }
}
