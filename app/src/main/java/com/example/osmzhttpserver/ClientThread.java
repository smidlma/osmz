package com.example.osmzhttpserver;

import android.os.Handler;
import android.util.Log;
import com.example.osmzhttpserver.controller.FileController;
import com.example.osmzhttpserver.http.HttpDecoder;
import com.example.osmzhttpserver.http.HttpRequest;
import com.example.osmzhttpserver.http.HttpResponse;
import com.example.osmzhttpserver.http.RequestRunner;
import com.example.osmzhttpserver.http.ResponseWriter;
import com.example.osmzhttpserver.service.LogService;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class ClientThread extends Thread {

  private final Map<String, RequestRunner> routes;
  private final Socket clientSocket;
  private final Semaphore semaphore;

  private final LogService logService;


  public ClientThread(Socket clientSocket, Semaphore semaphore, Handler handler,
      Map<String, RequestRunner> routes, LogService logService) {
    this.clientSocket = clientSocket;
    this.semaphore = semaphore;
    this.routes = routes;
    this.logService = logService;
  }

  @Override
  public void run() {
    try {
      Log.d("SERVER", "ThreadId: " + this.getId() +
          " Available connections: " +
          semaphore.availablePermits());

      handleConnection(clientSocket.getInputStream(),
          clientSocket.getOutputStream());

    } catch (IOException e) {
      Log.e("SERVER", "Connection interrupted");
    } finally {
      semaphore.release();
    }
  }

  public void handleConnection(final InputStream inputStream, final OutputStream outputStream)
      throws IOException {
    final BufferedOutputStream bufferedWriter = new BufferedOutputStream(
        new DataOutputStream(outputStream));
    Optional<HttpRequest> request = HttpDecoder.decode(inputStream);
    if (request.isPresent()) {
      logService.logAccess(request.get(), clientSocket);
      handleRequest(request.get(), bufferedWriter);
    } else {
      handleInvalidRequest(bufferedWriter);
    }

    bufferedWriter.close();
    inputStream.close();
  }

  private void handleRequest(final HttpRequest request, final BufferedOutputStream bufferedWriter) {
    final String routeKey = request.getHttpMethod().name().concat(request.getUri().getRawPath());

    List<String> matchedRoutes = routes.keySet().stream().filter(key -> routeKey.matches(key))
        .collect(Collectors.toList());
    if (!matchedRoutes.isEmpty()) {
      String routeKeyName = matchedRoutes.get(0);
      ResponseWriter.writeResponse(bufferedWriter, routes.get(routeKeyName).run(request));
    } else {
      ResponseWriter.writeResponse(bufferedWriter, new FileController().accessFileSystem(request));
    }
  }

  private void handleInvalidRequest(final BufferedOutputStream bufferedWriter) {
    ResponseWriter.writeResponse(bufferedWriter,
        new HttpResponse.Builder().setStatusCode(400).build());
  }
}
