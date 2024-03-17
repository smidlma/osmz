package com.example.osmzhttpserver.service;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.example.osmzhttpserver.http.HttpRequest;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.Date;

public class LogService {

  private static final String ACCESS_LOG_FILE = "access.log";
  private static final String ERRORS_LOG_FILE = "errors.log";
  private final FileService fileService;

  private final Handler handler;

  public LogService(Handler handler) {
    this.handler = handler;
    this.fileService = new FileService();
  }

  public void logAccess(HttpRequest req, Socket socket) {
    String ipAddress = socket.getInetAddress().getHostAddress();
    String logEntry = String.format("%s - - [%s] \"%s %s %s\" \"%s\"",
        ipAddress, new Date(), req.getHttpMethod(), req.getUri(), "HTTP/1.1",
        req.getRequestHeaders().getOrDefault("User-Agent", Collections.singletonList("Unknown")));
    logAccess(logEntry);
  }


  public void logAccess(String msg) {
    File file = fileService.getFileFromExternalStorage(ACCESS_LOG_FILE);
    try {
      fileService.appendToFile(file, msg);
      sendToHandler("ACCESS", msg);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  public void logError(String msg) {
    File file = fileService.getFileFromExternalStorage(ERRORS_LOG_FILE);
    try {
      fileService.appendToFile(file, msg);
      sendToHandler("ERROR", msg);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void sendToHandler(String type, String msg) {
    Bundle bundle = new Bundle();
    bundle.putString("serverLog", msg);
    Message message = handler.obtainMessage();
    message.setData(bundle);
    message.sendToTarget();
  }


}
