package com.example.osmzhttpserver;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.Semaphore;

public class SocketServer extends Thread {

    ServerSocket serverSocket;
    private static final int MAX_CONNECTIONS = 2;
    private final Semaphore semaphore = new Semaphore(MAX_CONNECTIONS, true);
    public final int port = 12345;
    boolean bRunning;

    Context context;

    Handler handler;

    final String UNAVAILABLE_RESP = "HTTP/1.0 503 Service Unavailable\n" +
            "Date: " + new Date() + "\n" +
            "Content-Type: text/html\n" +
            "Content-Length: 1354\n" +
            "\n" +
            "<html>\n" +
            "<body>\n" +
            "<h1>Service Unavailable</h1>\n" +
            "Unable to locate the file\n" +
            "  .\n" +
            "  .\n" +
            "  .\n" +
            "</body>\n" +
            "</html>";


    public SocketServer(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
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
                Socket s = serverSocket.accept();
                Log.d("SERVER", "Socket Accepted");
                SocketService socketService = new SocketService(s);
                if (semaphore.tryAcquire()) {
                    new ClientThread(socketService, semaphore, handler).start();
                } else {
                    socketService.writeToSocket(Collections.singletonList(UNAVAILABLE_RESP.getBytes()));
                    s.close();
                }


            }
        } catch (IOException e) {
            if (serverSocket != null && serverSocket.isClosed())
                Log.d("SERVER", "Normal exit");
            else {
                Log.d("SERVER", "Error");
                e.printStackTrace();
            }
        } finally {
            serverSocket = null;
            bRunning = false;
        }
    }


}

