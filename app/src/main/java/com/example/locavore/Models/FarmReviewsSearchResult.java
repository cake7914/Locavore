package com.example.locavore.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import javax.annotation.Generated;

@Generated("jsonschema2pojo")
public class FarmReviewsSearchResult {
    @SerializedName("reviews")
    @Expose
    private List<Review> reviews = null;
    @SerializedName("total")
    @Expose
    private Integer total;
    @SerializedName("possible_languages")
    @Expose
    private List<String> possibleLanguages = null;

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<String> getPossibleLanguages() {
        return possibleLanguages;
    }

    public void setPossibleLanguages(List<String> possibleLanguages) {
        this.possibleLanguages = possibleLanguages;
    }

}

