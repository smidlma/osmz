package com.example.osmzhttpserver.service;

import android.os.Environment;
import android.webkit.MimeTypeMap;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class FileService {


  final private static String SERVER_CONTENT_DIR = "HttpServerStorage";

  public FileService() {

  }

  public String getRelativePathToServer(String path) {
    String regex = "storage\\/emulated\\/0\\/HttpServerStorage\\/?";
    return path.replaceAll(regex, "");
  }

  public String getDirectoryExplorer(File directory) {

    boolean isSeverRoot = directory.getName().equals(SERVER_CONTENT_DIR);
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("<ul>");
    stringBuilder.append(
        "<li><a href='" + getRelativePathToServer(directory.getPath()) + "'>./</li>");
    if (!isSeverRoot) {
      stringBuilder.append(
          "<li><a href='" + getRelativePathToServer(directory.getParent()) + "'>../</li>");
    }
    for (File f : directory.listFiles()) {
      String path = getRelativePathToServer(f.getPath());
      stringBuilder.append("<li><a href='").append(path).append("'>" + path + "</a></li>");
    }
    stringBuilder.append("</ul>");

    return stringBuilder.toString();
  }

  private boolean isIndexFilePresent(File directory) {
    File[] files = directory.listFiles((f) -> f.getName().equals("index.html"));

    return files != null && files.length > 0;
  }

  public File getFileOrDirFromExternalStorage(String filename) {
    File directory = new File(Environment.getExternalStorageDirectory(), SERVER_CONTENT_DIR);
    File fileOrDir = new File(directory, filename);
    if (!fileOrDir.exists()) {
      return null;
    }

    if (fileOrDir.isDirectory() && isIndexFilePresent(fileOrDir)) {
      return new File(fileOrDir, "index.html");
    }

    return fileOrDir;
  }

  public byte[] getFileContent(File file) {
    try (FileInputStream fis = new FileInputStream(
        file); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

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
