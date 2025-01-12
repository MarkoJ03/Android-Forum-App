package com.example.forumapp;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postList;
    private int loggedInUserId;



    public PostAdapter(List<Post> postList, int loggedInUserId) {
        this.postList = postList;
        this.loggedInUserId = loggedInUserId;
    }

    public void deletePost(int position) {
        postList.remove(position);
        notifyItemRemoved(position);
    }

    public void updatePosts(List<Post> newPosts) {
        this.postList = newPosts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.tvPostTitle.setText(post.getTitle());
        holder.tvPostContent.setText(post.getContent());
        holder.tvPostAuthor.setText(post.getAuthor());

        // Provera da li je post od ulogovanog korisnika
        if (post.getUserId() == loggedInUserId) {
            holder.buttonEdit.setVisibility(View.VISIBLE);
            holder.buttonDelete.setVisibility(View.VISIBLE);
        } else {
            holder.buttonEdit.setVisibility(View.GONE);
            holder.buttonDelete.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), PostDetailsActivity.class);
            intent.putExtra("post_id", post.getId()); // Prosleđivanje ID-a posta
            intent.putExtra("post_title", post.getTitle());
            intent.putExtra("post_content", post.getContent());
            intent.putExtra("post_author", post.getAuthor());
            v.getContext().startActivity(intent);
        });
        Log.d("PostAdapter", "Post UserId: " + post.getUserId() + ", LoggedInUserId: " + loggedInUserId);

        // Klik na dugme za izmenu
        holder.buttonEdit.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), AddPost.class);
            intent.putExtra("post_id", post.getId());
            intent.putExtra("post_title", post.getTitle());
            intent.putExtra("post_content", post.getContent());
            v.getContext().startActivity(intent);
        });


        // Klik na dugme za brisanje
        holder.buttonDelete.setOnClickListener(v -> {
            int postId = post.getId();



            String url = MainActivity.URL + "/api/post/" + postId;

            StringRequest deleteRequest = new StringRequest(Request.Method.DELETE, url,
                    response -> {
                        Toast.makeText(v.getContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show();
                        deletePost(position); // Ukloni post iz liste
                    },
                    error -> {
                        Log.e("PostAdapter", "Error deleting post: " + error.getMessage());
                        Toast.makeText(v.getContext(), "Failed to delete post", Toast.LENGTH_SHORT).show();
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    SharedPreferences prefs = v.getContext().getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    String token = prefs.getString("access_token", "");
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };

            RequestQueue queue = Volley.newRequestQueue(v.getContext());
            queue.add(deleteRequest);
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvPostTitle, tvPostContent, tvPostAuthor;
        ImageButton buttonEdit, buttonDelete;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPostTitle = itemView.findViewById(R.id.postTitle);
            tvPostContent = itemView.findViewById(R.id.postContent);
            tvPostAuthor = itemView.findViewById(R.id.postAuthor);
            buttonEdit = itemView.findViewById(R.id.buttonEditPost);
            buttonDelete = itemView.findViewById(R.id.buttonDeletePost);
        }
    }
}



