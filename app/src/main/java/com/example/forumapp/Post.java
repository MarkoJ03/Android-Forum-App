package com.example.forumapp;

public class Post {
    private int id;
    private String title;
    private String content;
    private String author;
    private int userId;

    // Constructor
    public Post(int id, String title, String content, String author, int userId) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public int getUserId() {
        return userId;
    }
}

