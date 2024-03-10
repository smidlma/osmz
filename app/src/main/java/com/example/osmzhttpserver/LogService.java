package com.example.osmzhttpserver;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.IOException;

public class LogService {

    private static final String ACCESS_LOG_FILE = "access.log";
    private static final String ERRORS_LOG_FILE = "errors.log";
    private final FileService fileService;

    private final Handler handler;

    public LogService(FileService fileService, Handler handler) {
        this.handler = handler;
        this.fileService = new FileService();
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

    public void sendToHandler(String type, String msg){
        Bundle bundle = new Bundle();
        bundle.putString("serverLog", msg);
        Message message =  handler.obtainMessage();
        message.setData(bundle);
        message.sendToTarget();
    }


}
