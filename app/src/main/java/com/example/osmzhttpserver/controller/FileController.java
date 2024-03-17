package com.example.osmzhttpserver.controller;

import com.example.osmzhttpserver.http.HttpRequest;
import com.example.osmzhttpserver.http.HttpResponse;
import com.example.osmzhttpserver.service.FileService;
import java.io.File;

public class FileController {

  private final FileService fileService;


  public FileController() {
    this.fileService = new FileService();
  }

  public HttpResponse getStaticAsset(HttpRequest req) {
    File file = fileService.getFileFromExternalStorage(req.getUri().getRawPath());
    String mimeType = fileService.getMimeType(req.getUri().getRawPath());

    if (file != null) {
      byte[] content = fileService.getFileContent(file);
      return new HttpResponse.Builder().setStatusCode(200).addHeader("Content-Type", mimeType)
          .setEntity(content).build();
    } else {
      return new HttpResponse.Builder().setStatusCode(404).setEntity("Resource not found 404")
          .build();
    }
  }
}
