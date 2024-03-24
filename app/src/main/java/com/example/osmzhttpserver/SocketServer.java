package com.example.osmzhttpserver;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.example.osmzhttpserver.http.HttpMethod;
import com.example.osmzhttpserver.http.HttpResponse;
import com.example.osmzhttpserver.http.RequestRunner;
import com.example.osmzhttpserver.http.ResponseWriter;
import com.example.osmzhttpserver.service.LogService;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class SocketServer extends Thread {

  private ServerSocket serverSocket;
  private final Map<String, RequestRunner> routes;

  private static final int MAX_CONNECTIONS = 30;
  private final Semaphore semaphore = new Semaphore(MAX_CONNECTIONS, true);
  public final int port = 12345;
  boolean bRunning;

  Context context;

  Handler handler;

  private final LogService logService;

  public SocketServer(Context context, Handler handler) {
    this.routes = new HashMap<>();
    this.context = context;
    this.handler = handler;
    this.logService = new LogService(handler);
  }

  public void close() {
    try {
      serverSocket.close();
    } catch (IOException e) {
      Log.d("SERVER", "Error, probably interrupted in accept(), see log");
      e.printStackTrace();
    }
    bRunning = false;
  }

  public void run() {
    try {
      Log.d("SERVER", "Creating Socket");
      serverSocket = new ServerSocket(port);
      bRunning = true;

      while (bRunning) {
        Log.d("SERVER", "Socket Waiting for connection");
        Socket clientConnection = serverSocket.accept();
        Log.d("SERVER", "Socket Accepted");
        handleConnection(clientConnection);
      }
    } catch (IOException e) {
      if (serverSocket != null && serverSocket.isClosed()) {
        Log.d("SERVER", "Normal exit");
      } else {
        Log.d("SERVER", "Error");
        e.printStackTrace();
      }
    } finally {
      serverSocket = null;
      bRunning = false;
    }
  }

  private void handleConnection(Socket clientConnection) {
    try {
      if (semaphore.tryAcquire()) {
        new ClientThread(clientConnection, semaphore, handler, routes, logService).start();
      } else {
        ResponseWriter.writeResponse(new BufferedOutputStream(clientConnection.getOutputStream()),
            new HttpResponse.Builder().setStatusCode(503).setEntity("Service Unavailable").build());
        clientConnection.close();
      }
    } catch (IOException ignored) {
    }
  }

  public void addRoute(final HttpMethod opCode, final String route, final RequestRunner runner) {
    routes.put(opCode.name().concat(route), runner);
  }
}

