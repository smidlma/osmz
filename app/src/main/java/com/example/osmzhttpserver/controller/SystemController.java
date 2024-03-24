package com.example.osmzhttpserver.controller;

import android.util.Pair;
import com.example.osmzhttpserver.http.HttpRequest;
import com.example.osmzhttpserver.http.HttpResponse;
import com.example.osmzhttpserver.service.SystemService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SystemController {

  private final SystemService systemService;

  public SystemController() {
    systemService = new SystemService();
  }

  public HttpResponse processCommand(HttpRequest req) {
    try {
      List<String> commands = systemService.getCommandFromUrl(req.getUri());
      if (commands.isEmpty()) {
        return new HttpResponse.Builder().setStatusCode(400)
            .setEntity("BAD_REQUEST: No command in URL ").build();
      }
      String commandHtmlOutput = systemService.executeCommands(commands);

      String htmlEntity = "<html lang=\"en\">\n"
          + "  <head>\n"
          + "    <meta charset=\"UTF-8\" />\n"
          + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n"
          + "    <title>Android Web Server</title></head><body>" + commandHtmlOutput
          + "</body></html>";

      return new HttpResponse.Builder().setStatusCode(200).setEntity(htmlEntity)
          .addHeader("Content-Type", "text/html").build();

    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
      return new HttpResponse.Builder().setStatusCode(500).build();
    }

  }
}
