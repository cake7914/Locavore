package com.example.locavore.Models;


import static android.location.LocationManager.NETWORK_PROVIDER;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.parse.ParseUser;

import org.json.JSONArray;

import java.util.List;

import javax.annotation.Generated;

@Generated("jsonschema2pojo")
public class User {

    public static final String FARM_USER_TYPE = "farms";
    public static final String CONSUMER_USER_TYPE = "locavore";
    public static final String FARMERS_MARKET_USER_TYPE = "farmersmarket";
    public static final String KEY_NAME = "name";
    public static final String KEY_BIO = "bio";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_PROFILE_PHOTO = "profilePhoto";
    public static final String KEY_PROFILE_BACKDROP = "profileBackdrop";
    public static final String KEY_USER_TYPE = "userType";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_FARMS_FOLLOWING = "following";
    public static final String KEY_TAGS = "tags";
    public static final String KEY_LOCATION = "location";
    public static final String KEY_YELP_ID = "yelpID";
    public static final String KEY_RADIUS = "radius";
    public static final String KEY_EVENTS = "events";

    private ParseUser user;
    public boolean expanded;

    @SerializedName("rating")
    @Expose
    private Double rating;
    @SerializedName("price")
    @Expose
    private String price;
    @SerializedName("phone")
    @Expose
    private String phone;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("alias")
    @Expose
    private String alias;
    @SerializedName("is_closed")
    @Expose
    private Boolean isClosed;
    @SerializedName("categories")
    @Expose
    private List<YelpCategory> categories = null;
    @SerializedName("review_count")
    @Expose
    private Integer reviewCount;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("coordinates")
    @Expose
    private LatLng coordinates;
    @SerializedName("image_url")
    @Expose
    private String imageUrl;
    @SerializedName("location")
    @Expose
    private YelpLocation location;
    @SerializedName("distance")
    @Expose
    private Double distance;
    @SerializedName("transactions")
    @Expose
    private List<String> transactions = null;

    private List<Event> events = null;

    public Double getRating() {
        return rating;
    }
    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getPrice() {
        return price;
    }
    public void setPrice(String price) {
        this.price = price;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getAlias() {
        return alias;
    }
    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Boolean getIsClosed() {
        return isClosed;
    }
    public void setIsClosed(Boolean isClosed) {
        this.isClosed = isClosed;
    }

    public List<YelpCategory> getCategories() {
        return categories;
    }
    public void setCategories(List<YelpCategory> categories) {
        this.categories = categories;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }
    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }
    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public YelpLocation getLocation() {
        return location;
    }
    public void setLocation(YelpLocation location) {
        this.location = location;
    }

    public Double getDistance() {
        return distance;
    }
    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public List<String> getTransactions() {
        return transactions;
    }
    public void setTransactions(List<String> transactions) {
        this.transactions = transactions;
    }

    public ParseUser getUser() { return user; }
    public void setUser(ParseUser user) { this.user = user; }

    public JSONArray getEvents() { return user.getJSONArray(KEY_EVENTS); }

    //public void setEvents(JSONArray ) { }

    public JSONArray getFollowedFarms() { return user.getJSONArray(KEY_FARMS_FOLLOWING); }

    public void setFollowedFarms(JSONArray farms) { user.put(KEY_FARMS_FOLLOWING, farms); }

    public void followFarm(ParseUser farm) {
        user.add(KEY_FARMS_FOLLOWING, farm.getObjectId());
    }

    public void unfollowFarm(int pos) {
        JSONArray farms = getFollowedFarms();
        farms.remove(pos);
        setFollowedFarms(farms);
    }

    public User(ParseUser newUser, Location currentLocation) {
        user = newUser;
        coordinates = new LatLng(user.getParseGeoPoint(User.KEY_LOCATION).getLatitude(), user.getParseGeoPoint(User.KEY_LOCATION).getLongitude());
        Location location = new Location(NETWORK_PROVIDER);
        location.setLatitude(coordinates.latitude);
        location.setLongitude(coordinates.longitude);
        distance = (double) currentLocation.distanceTo(location);
        name = user.getString(User.KEY_NAME);
        imageUrl = user.getString(User.KEY_PROFILE_BACKDROP);
        id = user.getString(User.KEY_YELP_ID);
    }

}
