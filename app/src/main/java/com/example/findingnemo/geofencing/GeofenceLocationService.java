package com.example.findingnemo.geofencing;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.findingnemo.circleActivities.MyNavigationActivity;
import com.example.findingnemo.modelClasses.UpdatingLocations;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.collect.Iterables;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GeofenceLocationService extends Service {

    private FusedLocationProviderClient fusedLocationClient;
    List<Location> locationList;
    Location location;
    String id;
    Double lat, lng, geoLat, geoLng;

    GeofenceNotificationHelper notificationHelper;
    String name;
    List<Double> distanceList = new ArrayList<>();
    ArrayList<String> userIds = new ArrayList<>();

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);

            id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                location = Iterables.getLast(locationList);
                Toast.makeText(GeofenceLocationService.this, "Latitude: " + location.getLatitude() + '\n' +
                        "Longitude: " + location.getLongitude(), Toast.LENGTH_LONG).show();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
                reference.child(id).child("updated_latitude").setValue(location.getLatitude());
                reference.child(id).child("updated_longitude").setValue(location.getLongitude());

                FirebaseDatabase.getInstance().getReference("users").child(id)
                        .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            geoLat = snapshot.child("geofenceLat").getValue(Double.class);
                            geoLng = snapshot.child("geofenceLong").getValue(Double.class);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


                FirebaseDatabase.getInstance().getReference("users").child(id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(snapshot.child("GroupMembers").exists()){
                            for (DataSnapshot ds: snapshot.child("GroupMembers").getChildren()) {
                                lat = ds.child("updated_latitude").getValue(Double.class);
                                lng = ds.child("updated_longitude").getValue(Double.class);
                                name = ds.child("userName").getValue(String.class);
                                meterDistanceBetweenPoints(geoLat, geoLng, lat, lng);
                            }
                        }

                        if(snapshot.child("joinedGroupId").exists()){
                            for (DataSnapshot ds : snapshot.child("joinedGroupId").getChildren()) {
                                String uid = ds.child("adminId").getValue(String.class);
                                FirebaseDatabase.getInstance().getReference("users").child(uid).child("GroupMembers").child(id).child("updated_latitude").setValue(location.getLatitude());
                                FirebaseDatabase.getInstance().getReference("users").child(uid).child("GroupMembers").child(id).child("updated_longitude").setValue(location.getLongitude());
                            }
                        }
//
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        LocationRequest locationRequest = new LocationRequest();

        locationRequest.setInterval(500000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) createNotificationChanel();
        else startForeground(1, new Notification());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(getApplicationContext(), "Permission required", Toast.LENGTH_LONG).show();
            return;
        } else {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChanel() {
        String notificationChannelId = "Location channel id";
        String channelName = "Background Service";
        NotificationChannel channel = new NotificationChannel(notificationChannelId, channelName, NotificationManager.IMPORTANCE_NONE);
        channel.setLightColor(Color.BLUE);
        channel.setLockscreenVisibility(MODE_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, notificationChannelId);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Location updates:")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();

        startForeground(2, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    private void meterDistanceBetweenPoints(Double lat_a, Double lng_a, Double lat_b, Double lng_b) {
        double pk = (180.f / Math.PI);

        double a1 = lat_a / pk;
        double a2 = lng_a / pk;
        double b1 = lat_b / pk;
        double b2 = lng_b / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        double distance = 6366000 * tt;

        distanceList.add(distance);

        notificationHelper = new GeofenceNotificationHelper(GeofenceLocationService.this);
        if (distance < 400) {
            notificationHelper.sendHighPriorityNotification("Tracking Location", name + " has entered geofence.", MyNavigationActivity.class);
        } else {
            notificationHelper.sendHighPriorityNotification("Tracking Location", name + " has left geofence.", MyNavigationActivity.class);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
