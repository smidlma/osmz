package com.example.osmzhttpserver.service;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import org.json.JSONException;
import org.json.JSONObject;

public class SensorService {
  private final JSONObject sensorData = new JSONObject();
  private final SensorManager sensorManager;

  SensorEventListener sensorEventListener = new SensorEventListener() {
    @Override
    public void onSensorChanged(SensorEvent event) {
      try {
        JSONObject sensorValues = new JSONObject();
        for (int i = 0; i < event.values.length; i++) {
          sensorValues.put("value" + (i + 1), event.values[i]);
        }
        sensorData.put(event.sensor.getStringType(), sensorValues);
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
      // Handle changes in sensor accuracy
    }
  };

  public SensorService(Context context) {
    sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    registerAllSensors();
  }

  private void registerAllSensors() {
    for (Sensor sensor : sensorManager.getSensorList(Sensor.TYPE_ALL)) {
      sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
  }

  public JSONObject getSensorData() {
    return sensorData;
  }
}