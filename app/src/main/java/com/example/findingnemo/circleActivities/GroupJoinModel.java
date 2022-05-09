package com.example.findingnemo.circleActivities;

public class GroupJoinModel {
    String groupMemberId, name, email;
    Double updatedLatitude, updatedLongitude;

    public GroupJoinModel(String groupMemberId, String name, String email, Double updatedLatitude, Double updatedLongitude) {
        this.groupMemberId = groupMemberId;
        this.name = name;
        this.email = email;
        this.updatedLatitude = updatedLatitude;
        this.updatedLongitude = updatedLongitude;
    }


    public GroupJoinModel(){}
}
