package com.github.mateuszpach.diary;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Looper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

public class LocationGetter {

    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;
    private final ActivityResultLauncher<IntentSenderRequest> activityResultLauncher;
    private final ActivityResultLauncher<String> grantLocationPermissionsLauncher;
    private Location location;
    private CountDownLatch locationCallbackFinished;
    private LocationCallback locationCallback;
    private boolean isAbandoned;

    public LocationGetter(Context context,
                          ActivityResultLauncher<IntentSenderRequest> activityResultLauncher,
                          ActivityResultLauncher<String> grantLocationPermissionsLauncher) {
        this.context = context;
        this.activityResultLauncher = activityResultLauncher;
        this.grantLocationPermissionsLauncher = grantLocationPermissionsLauncher;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        isAbandoned = false;
    }

    public String getLocation() throws Exception {

        locationCallbackFinished = new CountDownLatch(1);

        LocationRequest locationRequest = getLocationRequest();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().setAlwaysShow(true)
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(context);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener((Activity) context, locationSettingsResponse -> {
            locationCallback = getLocationCallback();
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                grantLocationPermissionsLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                isAbandoned = true;
                locationCallbackFinished.countDown();
            } else {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            }
        });

        task.addOnFailureListener((Activity) context, e -> {
            if (e instanceof ResolvableApiException) {
                ResolvableApiException r = (ResolvableApiException) e;
                activityResultLauncher.launch(new IntentSenderRequest.Builder(r.getResolution().getIntentSender()).build());
                isAbandoned = true;
            } else {
                location = null;
            }
            locationCallbackFinished.countDown();
        });

        try {
            locationCallbackFinished.await();
        } catch (InterruptedException ignored) {
        }

        if (isAbandoned) {
            throw new Exception();
        }

        return getCityAndCountryOrCoordinates(location);
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private LocationCallback getLocationCallback() {
        return new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult.getLocations().size() == 0) {
                    return;
                }
                location = locationResult.getLocations().get(0);
                fusedLocationClient.removeLocationUpdates(this);
                locationCallbackFinished.countDown();
            }
        };
    }

    private String getCityAndCountryOrCoordinates(Location location) {
        if (location == null) {
            return "";
        }
        try {
            Geocoder geocoder = new Geocoder(context, Locale.UK);
            List<Address> addresses = geocoder
                    .getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && addresses.size() > 0) {
                return addresses.get(0).getLocality() + ", " + addresses.get(0).getCountryName();
            } else {
                return getCoordinatesString(location);
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private String getCoordinatesString(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        int latSeconds = (int) Math.round(latitude * 3600);
        int latDegrees = latSeconds / 3600;
        latSeconds = Math.abs(latSeconds % 3600);
        int latMinutes = latSeconds / 60;
        latSeconds %= 60;

        int longSeconds = (int) Math.round(longitude * 3600);
        int longDegrees = longSeconds / 3600;
        longSeconds = Math.abs(longSeconds % 3600);
        int longMinutes = longSeconds / 60;
        longSeconds %= 60;
        String latDegree = latDegrees >= 0 ? "N" : "S";
        String lonDegrees = longDegrees >= 0 ? "E" : "W";

        return Math.abs(latDegrees) + "°" + latMinutes + "'" + latSeconds
                + "\"" + latDegree + " " + Math.abs(longDegrees) + "°" + longMinutes
                + "'" + longSeconds + "\"" + lonDegrees;
    }

}