package com.example.osmzhttpserver.controller;

import android.os.Environment;
import com.example.osmzhttpserver.http.HttpRequest;
import com.example.osmzhttpserver.http.HttpResponse;
import com.example.osmzhttpserver.service.FileService;
import java.io.File;

public class UploadController {

  public UploadController() {

  }

  public HttpResponse uploadFile(HttpRequest req) {
    try {
      byte[] fileData = req.getBody();
      String fileName = req.getUri().getRawPath().replace("/upload/", "");

      FileService fileService = new FileService();

      fileService.saveFile(fileName, fileData);

      return new HttpResponse.Builder()
          .setStatusCode(200)
          .build();

    } catch (Exception e) {
      e.printStackTrace();
      return new HttpResponse.Builder()
          .setStatusCode(500)
          .build();
    }
  }

}
