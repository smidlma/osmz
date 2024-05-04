package com.example.osmzhttpserver;

import android.hardware.Camera;

import android.os.Environment;

import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.os.Handler;

import android.os.Looper;

import android.os.Message;

import android.util.Log;

import android.view.View;

import android.widget.Button;

import android.widget.TextView;

import com.example.osmzhttpserver.controller.CameraController;

import com.example.osmzhttpserver.controller.SystemController;

import com.example.osmzhttpserver.controller.TelemetryController;

import com.example.osmzhttpserver.controller.UploadController;
import com.example.osmzhttpserver.http.HttpMethod;

import com.example.osmzhttpserver.http.HttpResponse;

import com.example.osmzhttpserver.service.LocationService;

import com.example.osmzhttpserver.service.SensorService;

import java.io.File;

import java.io.FileNotFoundException;

import java.io.FileOutputStream;

import java.io.IOException;

import java.util.Timer;

import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

  private Camera mCamera;

  private CameraPreview mPreview;

  static boolean safeToTakePicture = false;

  public static byte[] img;

  public static boolean taken = false;

  public final String TAG = "MainActivity";

  private Timer mTimer = new Timer();

  private TimerTask mTimerTask = new TimerTask() {

    @Override

    public void run() {

      // process the current frame

      CameraPreview.takePic = true;

      Log.d("Timer", "Start TIMER");

    }

  };

  Handler handler = new Handler(Looper.getMainLooper()) {

    @Override

    public void handleMessage(@NonNull Message msg) {

      String logMsg = msg.getData().getString("serverLog", "No data");

      Log.d("serverLog", logMsg);

      TextView logs = (TextView) findViewById(R.id.logTextView);

      logs.append("\n" + logMsg);

    }

  };

  private SocketServer s;

  @Override

  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    Button btn1 = (Button) findViewById(R.id.button1);

    Button btn2 = (Button) findViewById(R.id.button2);

    btn1.setOnClickListener(this);

    btn2.setOnClickListener(this);

    // Create an instance of Camera

    mCamera = getCameraInstance();

    // Create our Preview view and set it as the content of our activity.

    mPreview = new CameraPreview(this, mCamera);

    FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);

    preview.addView(mPreview);

  }

  @Override

  public void onClick(View v) {

    if (v.getId() == R.id.button1) {

      s = new SocketServer(this, handler);

      TelemetryController telemetryController = new TelemetryController(new LocationService(this),
          new SensorService(this));

      SystemController systemController = new SystemController();

      s.addRoute(HttpMethod.GET, "/streams/telemetry",

          (req) -> new HttpResponse.Builder().setEntity(

                  "<a href='/streams/telemetry/location'> /streams/telemetry/location </a> "

                      + "<br> "

                      + "<a href='/streams/telemetry/sensors'> /streams/telemetry/sensors </a>")

              .setStatusCode(200).build());

      s.addRoute(HttpMethod.GET, "/streams/telemetry/location",

          (req) -> telemetryController.getLocation());

      s.addRoute(HttpMethod.GET, "/streams/telemetry/sensors",

          (req) -> telemetryController.getSensorsData());

      s.addRoute(HttpMethod.GET, "/cmd/.*", systemController::processCommand);

      UploadController uploadController = new UploadController();
      s.addRoute(HttpMethod.POST, "/upload/.*", uploadController::uploadFile);

      s.start();

    }

    if (v.getId() == R.id.button2) {

      s.close();

      try {

        s.join();

      } catch (InterruptedException e) {

        e.printStackTrace();

      }

    }

  }

  @Override

  protected void onPause() {

    super.onPause();

    mTimer.cancel();

    if (mPreview != null) {

      //mIsCameraAvailable = false;

      mPreview = null;

    }

    if (mCamera == null) {

      //mIsCameraAvailable = false;

      mCamera.release();

      mCamera = null;

    }

  }

  @Override

  protected void onResume() {

    super.onResume();

    if (mPreview == null) {

      mCamera = Camera.open();

      mPreview = new CameraPreview(this, mCamera);

    }

    mTimer.schedule(mTimerTask, 2000, 10);

  }

  private final Camera.PictureCallback mPicture = new Camera.PictureCallback() {

    @Override

    public void onPictureTaken(byte[] data, Camera camera) {

      Log.d(TAG, "Taking picture");

      if (taken || img == null) {

        img = data;

        Log.d("MAINAC + ", img.toString());

        safeToTakePicture = true;

      }

      File pictureFile = new File(

          Environment.getExternalStorageDirectory().getAbsolutePath() + "/camera.jpg");

      try {

        FileOutputStream fos = new FileOutputStream(pictureFile);

        fos.write(data);

        fos.close();

      } catch (FileNotFoundException e) {

        Log.d(TAG, "File not found: " + e.getMessage());

      } catch (IOException e) {

        Log.d(TAG, "Error accessing file: " + e.getMessage());

      }

      safeToTakePicture = true;

    }

  };

  public static Camera getCameraInstance() {

    Camera c = null;

    try {

      c = Camera.open(0); // attempt to get a Camera instance

    } catch (Exception e) {

      // Camera is not available (in use or does not exist)

    }

    return c; // returns null if camera is unavailable

  }

}