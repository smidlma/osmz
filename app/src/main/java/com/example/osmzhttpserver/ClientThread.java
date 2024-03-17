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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Semaphore;

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
//            String serverLogMsg = socketService.getSocket().getInetAddress().getHostAddress() +
//                    " - - " + new Date() + " "
//                    + request.getHeader() + " - " + socketService.getSocket().getInetAddress().getHostName();
//
//            logService.logAccess(serverLogMsg);
//
//            Log.d("SERVER", "ThreadId: " + this.getId() +
//                    " Available connections: " +
//                    semaphore.availablePermits());
//
//
      handleConnection(clientSocket.getInputStream(),
          clientSocket.getOutputStream());

    } catch (IOException e) {
      Log.e("SERVER", "Connection interrupted");
//      logService.logError("Connection interrupted, closing the socket");
    } finally {
      try {
        clientSocket.close();
      } catch (IOException ignore) {
//        throw new RuntimeException(e);
      } finally {
        semaphore.release();
      }
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

    if (routes.containsKey(routeKey)) {
      ResponseWriter.writeResponse(bufferedWriter, routes.get(routeKey).run(request));
    } else {
      ResponseWriter.writeResponse(bufferedWriter, new FileController().getStaticAsset(request));
    }
  }

  private void handleInvalidRequest(final BufferedOutputStream bufferedWriter) {
    ResponseWriter.writeResponse(bufferedWriter,
        new HttpResponse.Builder().setStatusCode(400).build());
  }
}
