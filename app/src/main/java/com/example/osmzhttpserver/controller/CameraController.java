package com.example.osmzhttpserver.controller;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import com.example.osmzhttpserver.http.HttpRequest;
import com.example.osmzhttpserver.http.HttpResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CameraController {

  private Camera camera;
  private ExecutorService executor = Executors.newSingleThreadExecutor();

  public CameraController() {

  }

  public HttpResponse getCameraStream(HttpRequest req) {
    try {
      camera = Camera.open();
//      Camera.Parameters parameters = camera.getParameters();
//      parameters.setPreviewFormat(ImageFormat.JPEG);
//      camera.setParameters(parameters);
      camera.startPreview();

      Future<byte[]> future = executor.submit(() -> captureImage());

      byte[] imageData = future.get(); // Blocking call to wait for image capture

      return new HttpResponse.Builder()
          .setStatusCode(200)
          .setEntity(imageData)
          .addHeader("Content-Type", "image/jpeg")
          .build();

    } catch (Exception e) {
      e.printStackTrace();
      return new HttpResponse.Builder()
          .setStatusCode(500)
          .build();
    } finally {
      if (camera != null) {
        camera.stopPreview();
        camera.release();
      }
    }

  }

  private byte[] captureImage() {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      Camera.PictureCallback jpegCallback = (data, camera) -> {
        try {
          out.write(data);
          out.close();
        } catch (IOException e) {
          Log.e("SERVER", "Failed to write data: " + e.getMessage());
        }
      };

      camera.takePicture(null, null, jpegCallback);

      // Wait for a short time to ensure the picture is captured
      Thread.sleep(2000);

      return out.toByteArray();
    } catch (Exception e) {
      Log.e("SERVER", "Error capturing image: " + e.getMessage());
      return null;
    }
  }
}

