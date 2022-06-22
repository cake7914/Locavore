package com.example.locavore.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class YelpCategory {
    @SerializedName("title")
    @Expose
    private String title;
}
