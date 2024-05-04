package com.example.osmzhttpserver.http;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class HttpRequest {
  private final HttpMethod httpMethod;
  private final URI uri;
  private final Map<String, List<String>> requestHeaders;
  private final byte[] body;

  private HttpRequest(HttpMethod opCode,
      URI uri,
      Map<String, List<String>> requestHeaders, byte[] body)
  {
    this.httpMethod = opCode;
    this.uri = uri;
    this.requestHeaders = requestHeaders;
    this.body = body;
  }

  public URI getUri() {
    return uri;
  }

  public HttpMethod getHttpMethod() {
    return httpMethod;
  }

  public Map<String, List<String>> getRequestHeaders() {
    return requestHeaders;
  }

  public byte[] getBody() {
    return body;
  }

  public static class Builder {
    private HttpMethod httpMethod;
    private URI uri;
    private Map<String, List<String>> requestHeaders;
    private byte[] body;

    public Builder() {
    }

    public void setHttpMethod(HttpMethod httpMethod) {
      this.httpMethod = httpMethod;
    }

    public void setUri(URI uri) {
      this.uri = uri;
    }

    public void setRequestHeaders(Map<String, List<String>> requestHeaders) {
      this.requestHeaders = requestHeaders;
    }

    public void setBody(byte[] body) {
      this.body = body;
    }

    public HttpRequest build() {
      return new HttpRequest(httpMethod, uri, requestHeaders, body);
    }
  }
}
