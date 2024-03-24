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

  public HttpResponse accessFileSystem(HttpRequest req) {
    File fileOrDir = fileService.getFileOrDirFromExternalStorage(req.getUri().getRawPath());
    if (fileOrDir == null) {
      return new HttpResponse.Builder().setStatusCode(404).setEntity("Resource not found 404")
          .build();
    }

    if (fileOrDir.isFile()) {
      String mimeType = fileService.getMimeType(fileOrDir.getName());
      byte[] content = fileService.getFileContent(fileOrDir);
      return new HttpResponse.Builder().setStatusCode(200).addHeader("Content-Type", mimeType)
          .setEntity(content).build();
    }

    String dirEntity = fileService.getDirectoryExplorer(fileOrDir);
    return new HttpResponse.Builder().setStatusCode(200).addHeader("Content-Type", "text/html")
        .setEntity(dirEntity).build();
  }
}
