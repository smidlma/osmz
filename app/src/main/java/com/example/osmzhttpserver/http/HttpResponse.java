package com.example.osmzhttpserver.http;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HttpResponse {

  private final Map<String, List<String>> responseHeaders;
  private final int statusCode;

  private final Optional<Object> entity;

  /**
   * Headers should contain the following: Date: < date > Server: < my server > Content-Type:
   * text/plain, application/json etc... Content-Length: size of payload
   */
  private HttpResponse(final Map<String, List<String>> responseHeaders, final int statusCode,
      final Optional<Object> entity) {
    this.responseHeaders = responseHeaders;
    this.statusCode = statusCode;
    this.entity = entity;
  }

  public Map<String, List<String>> getResponseHeaders() {
    return responseHeaders;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public Optional<Object> getEntity() {
    return entity;
  }

  public static class Builder {

    private final Map<String, List<String>> responseHeaders;
    private int statusCode;

    private Optional<Object> entity;

    public Builder() {
      // Create default headers - server etc
      responseHeaders = new HashMap<>();
      responseHeaders.put("Server", Collections.singletonList("MyServer"));
      responseHeaders.put("Date",
          Collections.singletonList(String.valueOf(new Date())));

      entity = Optional.empty();
    }

    public Builder setStatusCode(final int statusCode) {
      this.statusCode = statusCode;
      return this;
    }

    public Builder addHeader(final String name, final String value) {
      responseHeaders.put(name,Collections.singletonList(value));
      return this;
    }

    public Builder setEntity(final Object entity) {
      if (entity != null) {
        this.entity = Optional.of(entity);
      }
      return this;
    }

    public HttpResponse build() {
      return new HttpResponse(responseHeaders, statusCode, entity);
    }
  }
}
