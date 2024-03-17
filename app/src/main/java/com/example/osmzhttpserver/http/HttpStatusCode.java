package com.example.osmzhttpserver.http;

import java.util.HashMap;
import java.util.Map;

public class HttpStatusCode {

  public static final Map<Integer, String> STATUS_CODES = new HashMap<Integer, String>() {{
    put(200, "OK");
    put(400, "BAD_REQUEST");
    put(404, "NOT_FOUND");
    put(500, "INTERNAL_SERVER_ERROR");
    put(503, "SERVICE_UNAVAILABLE");
  }};
}
