package com.example.osmzhttpserver;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SocketService {
    private final Socket socket;

    public Socket getSocket() {
        return socket;
    }

    public SocketService(Socket socket) {
        this.socket = socket;
    }

    public Request getRequestFromSocket() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String tmp;
        List<String> requestHeaders = new ArrayList<>();
        while (!(tmp = in.readLine()).equals("")) {
            Log.d("SERVER", tmp);
            requestHeaders.add(tmp);
        }
        return new Request(requestHeaders);
    }

    public void writeToSocket(List<byte[]> bytes) throws IOException {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        for (byte[] data : bytes) {
            out.write(data);
        }
        out.flush();
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            Log.d("SERVER", "Error, while closing the socket");
            throw new RuntimeException(e);
        }
    }
}
