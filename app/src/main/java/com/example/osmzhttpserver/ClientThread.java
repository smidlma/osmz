package com.example.osmzhttpserver;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.Semaphore;

public class ClientThread extends Thread {
    private final SocketService socketService;
    private final FileService fileService;

    private final Semaphore semaphore;

    private final LogService logService;

    final String NOT_FOUND_RESP = "HTTP/1.0 404 Not Found\n" +
            "Date: Fri, 31 Dec 1999 23:59:59 GMT\n" +
            "Content-Type: text/html\n" +
            "Content-Length: 1354\n" +
            "\n" +
            "<html>\n" +
            "<body>\n" +
            "<h1>404 Not Found</h1>\n" +
            "Unable to locate the file\n" +
            "  .\n" +
            "  .\n" +
            "  .\n" +
            "</body>\n" +
            "</html>";

    public ClientThread(SocketService socketService, Semaphore semaphore, Handler handler) {
        this.socketService = socketService;
        this.semaphore = semaphore;
        this.fileService = new FileService();
        this.logService = new LogService(fileService, handler);
    }


    @Override
    public void run() {
        try {
            Request request = socketService.getRequestFromSocket();
            String mimeType = fileService.getMimeType(request.getPath());
            File file = fileService.getFileFromExternalStorage(request.getPath());

            String serverLogMsg = socketService.getSocket().getInetAddress().getHostAddress() +
                    " - - " + new Date() + " "
                    + request.getHeader() + " - " + socketService.getSocket().getInetAddress().getHostName();

            logService.logAccess(serverLogMsg);

            Log.d("SERVER", "ThreadId: " + this.getId() +
                    " Available connections: " +
                    semaphore.availablePermits());


            if (file != null) {
                byte[] content = fileService.getFileContent(file);
                String responseHeader = getResponseHeader(mimeType, content.length);
                socketService.writeToSocket(Arrays.asList(responseHeader.getBytes(), content));
            } else {
                socketService.writeToSocket(Collections.singletonList(NOT_FOUND_RESP.getBytes()));
            }

        } catch (IOException e) {
            Log.e("SERVER", "Connection interrupted");
            logService.logError("Connection interrupted, closing the socket");
        } finally {
            socketService.close();
            semaphore.release();
        }

    }

    String getResponseHeader(String contentType, int contentLength) {
        return "HTTP/1.0 200 OK\n" +
                "Date: " + new Date() + "\n" +
                "Content-Type: " + contentType + "\n" +
                "Content-Length: " + contentLength + "\n" +
                "\n";
    }
}
