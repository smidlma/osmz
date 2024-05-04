package com.example.osmzhttpserver;

import android.content.Context;

import android.graphics.Rect;

import android.graphics.YuvImage;

import android.hardware.Camera;

import android.util.Log;

import android.view.SurfaceHolder;

import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;

import java.io.IOException;

/** A basic Camera preview class */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

  static SurfaceHolder mHolder;

  private Camera mCamera;

  static boolean takePic = false;

  public final String TAG = "CameraPreview";

  public CameraPreview(Context context, Camera camera) {

    super(context);

    mCamera = camera;

    // Install a SurfaceHolder.Callback so we get notified when the

    // underlying surface is created and destroyed.

    mHolder = getHolder();

    mHolder.addCallback(this);

    mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

  }

  public void surfaceCreated(SurfaceHolder holder) {

    // The Surface has been created, now tell the camera where to draw the preview.

    try {

      mCamera.setPreviewDisplay(holder);

      mCamera.startPreview();

      mCamera.setPreviewCallback(this);

    } catch (IOException e) {

      Log.d(TAG, "Error setting camera preview: " + e.getMessage());

    }

  }

  public void surfaceDestroyed(SurfaceHolder holder) {

    // empty. Take care of releasing the Camera preview in your activity.

    mCamera.stopPreview();

    mCamera.setPreviewCallback(null);

    mCamera.release();

    mCamera = null;

  }

  public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

    // If your preview can change or rotate, take care of those events here.

    // Make sure to stop the preview before resizing or reformatting it.

    if (mHolder.getSurface() == null){

      // preview surface does not exist

      return;

    }

    // stop preview before making changes

    try {

      mCamera.stopPreview();

    } catch (Exception e){

      // ignore: tried to stop a non-existent preview

    }

    // set preview size and make any resize, rotate or

    // reformatting changes here

    // start preview with new settings

    try {

      mCamera.setPreviewDisplay(mHolder);

      mCamera.setPreviewCallback(this);

      mCamera.startPreview();

      MainActivity.safeToTakePicture = true;

    } catch (Exception e){

      Log.d(TAG, "Error starting camera preview: " + e.getMessage());

    }

  }

  @Override

  public void onPreviewFrame(byte[] data, Camera camera) {

    if (takePic) {

      Camera.Parameters params = camera.getParameters();

      int width = params.getPreviewSize().width;

      int height = params.getPreviewSize().height;

      int format = params.getPreviewFormat();

      // convert the preview data to a YuvImage object

      YuvImage yuvImage = new YuvImage(data, format, width, height, null);

      // compress the YuvImage to a JPEG byte array

      ByteArrayOutputStream outStream = new ByteArrayOutputStream();

      yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, outStream);

      MainActivity.img = outStream.toByteArray();

    }

  }

}