package com.example.osmzhttpserver.http;

import com.example.osmzhttpserver.http.HttpRequest.Builder;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public class HttpDecoder {

  public static Optional<HttpRequest> decode(final InputStream inputStream) {
    return readMessage(inputStream).flatMap(HttpDecoder::buildRequest);
  }

  private static Optional<HttpRequest> buildRequest(List<String> message) {
    if (message.isEmpty()) {
      return Optional.empty();
    }

    String firstLine = message.get(0);
    String[] httpInfo = firstLine.split(" ");

    if (httpInfo.length != 3) {
      return Optional.empty();
    }

    String protocolVersion = httpInfo[2];
    if (!protocolVersion.equals("HTTP/1.1")) {
      return Optional.empty();
    }

    try {
      Builder requestBuilder = new Builder();
      requestBuilder.setHttpMethod(HttpMethod.valueOf(httpInfo[0]));
      requestBuilder.setUri(new URI(httpInfo[1].equals("/") ? "/index.html" : httpInfo[1].replace("%", "&")));

      List<String> data = message.stream().filter(line -> message.indexOf(line) > message.indexOf("\n")).collect(
          Collectors.toList());
      if(data.size() > 0) {
        requestBuilder.setBody(String.join("\n", data).getBytes());
      }

      return Optional.of(addRequestHeaders(message, requestBuilder));
    } catch (URISyntaxException | IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  private static Optional<List<String>> readMessage(final InputStream inputStream) {
    try {
      if (!(inputStream.available() > 0)) {
        return Optional.empty();
      }

      final char[] inBuffer = new char[inputStream.available()];
      final InputStreamReader inReader = new InputStreamReader(inputStream);
      final int read = inReader.read(inBuffer);

      List<String> message = new ArrayList<>();

      try (Scanner sc = new Scanner(new String(inBuffer))) {
        while (sc.hasNextLine()) {
          String line = sc.nextLine();
          message.add(line);
        }
      }

      return Optional.of(message);
    } catch (Exception ignored) {
      return Optional.empty();
    }
  }

  private static HttpRequest addRequestHeaders(final List<String> message, final Builder builder) {
    final Map<String, List<String>> requestHeaders = new HashMap<>();

    if (message.size() > 1) {
      for (int i = 1; i < message.size(); i++) {
        String header = message.get(i);
        int colonIndex = header.indexOf(':');

        if (!(colonIndex > 0 && header.length() > colonIndex + 1)) {
          break;
        }

        String headerName = header.substring(0, colonIndex);
        String headerValue = header.substring(colonIndex + 1);

        requestHeaders.compute(headerName, (key, values) -> {
          if (values != null) {
            values.add(headerValue);
          } else {
            values = new ArrayList<>();
          }
          return values;
        });
      }
    }

    builder.setRequestHeaders(requestHeaders);
    return builder.build();
  }
}
