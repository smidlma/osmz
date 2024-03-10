package com.example.osmzhttpserver;

import android.os.Environment;
import android.webkit.MimeTypeMap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

public class FileService {


    final private static String SERVER_CONTENT_DIR = "HttpServerStorage";

    public FileService() {

    }

    public File getFileFromExternalStorage(String filename) {
        File directory = new File(Environment.getExternalStorageDirectory(), SERVER_CONTENT_DIR);
        if (!directory.exists()) {
            // Directory does not exist
            return null;
        }
        File file = new File(directory, filename);
        if (!file.exists()) {
            // File does not exist
            return null;
        }
        return file;
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

    public String getMimeType(String filename) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(filename);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public void appendToFile(File file, String content) throws IOException {
        try (FileWriter fileWriter = new FileWriter(file, true)) {
            fileWriter.write(content);
            fileWriter.write('\n');
        }
    }
}
