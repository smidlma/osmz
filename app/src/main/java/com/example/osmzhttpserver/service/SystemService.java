package com.example.osmzhttpserver.service;

import android.util.Pair;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SystemService {

  public List<String> getCommandFromUrl(URI uri)  {
    String[] splitPaths = uri.getPath().split("cmd/");
    if (splitPaths.length < 2) {
      return new ArrayList<>();
    }
    String[] params = splitPaths[1].split("&");

    return new ArrayList<>(Arrays.asList(params).
        subList(0, params.length));
  }

  public String executeCommands(List<String> commands) throws IOException, InterruptedException {

    ProcessBuilder processBuilder = new ProcessBuilder(commands);
    processBuilder.redirectErrorStream(true);  // Redirect stderr to stdout
    Process process = processBuilder.start();

    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    List<String> lines = reader.lines().collect(Collectors.toList());
    int exitCode = process.waitFor();

    return String.join("<br>", lines);
  }
}
