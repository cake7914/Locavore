package com.example.locavore;

import com.example.locavore.Models.FarmSearchResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface YelpService {

    @GET("businesses/search")
    Call<FarmSearchResult> searchFarms(
             @Header("Authorization") String authHeader,
            @Query("latitude") double latitude,
             @Query("longitude") double longitude,
             @Query("categories") String categories,
             @Query("limit") int limit,
             @Query("radius") int radius
    );

    @GET("businesses/{id}")
    Call<FarmSearchResult> findBusinessDetails(
            @Path("id") String id,
            @Header("Authorization") String authHeader,
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("categories") String categories,
            @Query("limit") int limit,
            @Query("radius") int radius
    );
}
