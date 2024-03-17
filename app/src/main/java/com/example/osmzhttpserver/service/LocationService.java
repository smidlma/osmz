package com.example.osmzhttpserver.service;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import java.util.Objects;

public class LocationService {

  private final LocationManager locationManager;

  private final LocationListener locationListener = new LocationListener() {
    @Override
    public void onLocationChanged(@NonNull Location location) {
      // Do nothing, just need to implement this method for the listener
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
  };

  public LocationService(Context context) {
    this.locationManager = (LocationManager) context.getSystemService(
        Context.LOCATION_SERVICE);
  }

  public Location getCurrentLocation() {
    try {
      // Request a single location update
      locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener,
          Looper.getMainLooper());
      // Retrieve the last known location
      return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    } catch (SecurityException e) {
      // Handle security exception
      Log.d("ERROR", Objects.requireNonNull(e.getMessage()));
      return null;
    }
  }
}
