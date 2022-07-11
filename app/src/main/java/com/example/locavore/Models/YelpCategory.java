package com.example.locavore.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.List;

@Parcel
public class YelpCategory {
    @SerializedName("title")
    @Expose
    private String title;
}
