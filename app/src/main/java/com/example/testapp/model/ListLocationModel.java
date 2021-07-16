package com.example.testapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ListLocationModel {
    @SerializedName("posts")
    private List<LocationModel> mPosts;

    public List<LocationModel> getmPosts() {
        return mPosts;
    }
}
