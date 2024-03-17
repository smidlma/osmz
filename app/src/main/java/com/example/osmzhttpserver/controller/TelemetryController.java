package com.example.osmzhttpserver.controller;

import android.location.Location;
import com.example.osmzhttpserver.http.HttpResponse;
import com.example.osmzhttpserver.service.LocationService;
import com.example.osmzhttpserver.service.SensorService;
import org.json.JSONException;
import org.json.JSONObject;

public class TelemetryController {

  private final LocationService locationService;
  private final SensorService sensorService;

  public TelemetryController(LocationService locationService, SensorService sensorService) {
    this.locationService = locationService;
    this.sensorService = sensorService;
  }

  public HttpResponse getLocation() {
    Location location = locationService.getCurrentLocation();
    double latitude = location.getLatitude();
    double longitude = location.getLongitude();

    JSONObject entity = new JSONObject();
    try {
      entity.put("latitude", latitude);
      entity.put("longitude", longitude);
      return new HttpResponse.Builder().setStatusCode(200).
          setEntity(entity.toString()).addHeader("Content-Type", "application/json").build();
    } catch (JSONException e) {
      return new HttpResponse.Builder().setStatusCode(500).
          setEntity(entity.toString()).build();
    }
  }

  public HttpResponse getSensorsData() {
    JSONObject entity = sensorService.getSensorData();

    return new HttpResponse.Builder().setStatusCode(200)
        .addHeader("Content-Type", "application/json").setEntity(entity.toString()).build();
  }
}
