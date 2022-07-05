package com.example.locavore.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FarmSearchResult {
    @SerializedName("total")
    @Expose
    private Integer total;
    @SerializedName("businesses")
    @Expose
    private List<User> farms;

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<User> getFarms() {
        return farms;
    }

    public void setFarms(List<User> farms) {
        this.farms = farms;
    }
}
