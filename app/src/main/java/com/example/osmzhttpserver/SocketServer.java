package com.example.osmzhttpserver;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SocketServer extends Thread {

    ServerSocket serverSocket;
    public final int port = 12345;
    boolean bRunning;

    Context context;

    final String SERVER_CONTENT_DIR = "HttpServerStorage";

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

    public SocketServer(Context context) {
        this.context = context;
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
                DataOutputStream out = new DataOutputStream(s.getOutputStream());

                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

                String tmp;
                List<String> requestHeaders = new ArrayList<>();

                while (!(tmp = in.readLine()).equals("")) {
                    Log.d("SERVER", tmp);
                    requestHeaders.add(tmp);
                }
                Request request = new Request(requestHeaders);
                String mimeType = getMimeType(request.getPath());
                File file = getFileFromExternalStorage(SERVER_CONTENT_DIR, request.getPath());

                if (file != null) {
                    byte[] content = getFileContent(file);
                    String responseHeader = getResponseHeader(mimeType, content.length);
                    out.write(responseHeader.getBytes());
                    out.write(content);
                } else {
                    out.write(NOT_FOUND_RESP.getBytes());
                }

                out.flush();
                s.close();
                Log.d("SERVER", "Socket Closed");
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

    String getResponseHeader(String contentType, int contentLength) {
        return "HTTP/1.0 200 OK\n" +
                "Date: " + new Date() + "\n" +
                "Content-Type: " + contentType + "\n" +
                "Content-Length: " + contentLength + "\n" +
                "\n";
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public byte[] getFileContent(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            // Read the binary file into a byte array
            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public File getFileFromExternalStorage(String directoryName, String fileName) {
        File directory = new File(Environment.getExternalStorageDirectory(), directoryName);
        if (!directory.exists()) {
            // Directory does not exist
            return null;
        }
        File file = new File(directory, fileName);
        if (!file.exists()) {
            // File does not exist
            return null;
        }
        return file;
    }

}

