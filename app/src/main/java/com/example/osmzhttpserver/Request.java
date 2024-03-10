package com.example.osmzhttpserver;

import java.util.List;

public class Request {

    private final String method;
    private final String version;
    private final String path;

    public Request(List<String> requestHeaders){
       String[] firstLine = requestHeaders.get(0).split(" ");
       this.method = firstLine[0];
       this.path = firstLine[1].equals("/") ? "/index.html" : firstLine[1];
       this.version = firstLine[2];
    }

    public String getMethod() {
        return method;
    }

    public String getVersion() {
        return version;
    }

    public String getPath() {
        return path;
    }

    public String getHeader(){
        return method + " " + path + " " + version;
    }
}
