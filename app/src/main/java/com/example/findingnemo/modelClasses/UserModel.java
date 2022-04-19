package com.example.findingnemo.modelClasses;

public class UserModel {
    public String userId;
    String userName, email, code, uri, isSharing, userLatitude, userLongitude;

    public UserModel(String userName, String email, String code, String uri, String isSharing, String latitude, String longitude, String userId) {
        this.userName = userName;
        this.email = email;
        this.code = code;
        this.uri = uri;
        this.isSharing = isSharing;
        this.userLatitude = latitude;
        this.userLongitude = longitude;
        this.userId = userId;
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

    public String getUserLatitude() {
        return userLatitude;
    }

    public void setUserLatitude(String latitude) {
        this.userLatitude = latitude;
    }

    public String getUserLongitude() {
        return userLongitude;
    }

    public void setUserLongitude(String longitude) {
        this.userLongitude = longitude;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
