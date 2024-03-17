package com.example.osmzhttpserver.http;

import java.io.BufferedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ResponseWriter {

  public static void writeResponse(final BufferedOutputStream outputStream,
      final HttpResponse response) {
    try {
      final int statusCode = response.getStatusCode();
      final String statusCodeMeaning = HttpStatusCode.STATUS_CODES.get(statusCode);
      final List<String> responseHeaders = buildHeaderStrings(response.getResponseHeaders());

      outputStream.write(("HTTP/1.1 " + statusCode + " " + statusCodeMeaning + "\r\n").getBytes());

      for (String header : responseHeaders) {
        outputStream.write(header.getBytes());
      }

      final Optional<byte[]> entityString = response.getEntity()
          .flatMap(ResponseWriter::getResponseString);
      if (entityString.isPresent()) {
        final byte[] encodedBody = entityString.get();
        outputStream.write(("Content-Length: " + encodedBody.length + "\r\n").getBytes());
        outputStream.write("\r\n".getBytes());
        outputStream.write(encodedBody);
      } else {
        outputStream.write("\r\n".getBytes());
      }
    } catch (Exception ignored) {

    }
  }

  private static List<String> buildHeaderStrings(final Map<String, List<String>> responseHeaders) {
    final List<String> responseHeadersList = new ArrayList<>();

    responseHeaders.forEach((name, values) -> {
      final StringBuilder valuesCombined = new StringBuilder();
      values.forEach(valuesCombined::append);
      valuesCombined.append(";");

      responseHeadersList.add(name + ": " + valuesCombined + "\r\n");
    });

    return responseHeadersList;
  }

  private static Optional<byte[]> getResponseString(final Object entity) {
    // Currently only supporting Strings
    if (entity instanceof String) {
      try {
        return Optional.of(((String) entity).getBytes());
      } catch (Exception ignored) {
      }
    }
    if (entity instanceof byte[]) {
      return Optional.of((byte[]) entity);
    }

    return Optional.empty();
  }
}
