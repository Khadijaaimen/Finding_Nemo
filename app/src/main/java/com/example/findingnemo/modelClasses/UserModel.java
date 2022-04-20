package com.example.findingnemo.modelClasses;

public class UserModel {
    public String userId;
    String userName, email, code, uri, isSharing;
    Double  userLatitude, userLongitude, geofenceLat, geofenceLong;

    public UserModel(String userId, String userName, String email, String code, String uri, String isSharing, Double userLatitude, Double userLongitude, Double geofenceLat, Double geofenceLong) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.code = code;
        this.uri = uri;
        this.isSharing = isSharing;
        this.userLatitude = userLatitude;
        this.userLongitude = userLongitude;
        this.geofenceLat = geofenceLat;
        this.geofenceLong = geofenceLong;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getIsSharing() {
        return isSharing;
    }

    public void setIsSharing(String isSharing) {
        this.isSharing = isSharing;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Double getUserLatitude() {
        return userLatitude;
    }

    public void setUserLatitude(Double userLatitude) {
        this.userLatitude = userLatitude;
    }

    public Double getUserLongitude() {
        return userLongitude;
    }

    public void setUserLongitude(Double userLongitude) {
        this.userLongitude = userLongitude;
    }

    public Double getGeofenceLat() {
        return geofenceLat;
    }

    public void setGeofenceLat(Double geofenceLat) {
        this.geofenceLat = geofenceLat;
    }

    public Double getGeofenceLong() {
        return geofenceLong;
    }

    public void setGeofenceLong(Double geofenceLong) {
        this.geofenceLong = geofenceLong;
    }
}
