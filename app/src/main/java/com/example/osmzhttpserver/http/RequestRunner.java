package com.example.osmzhttpserver.http;

import android.content.Context;
import com.example.osmzhttpserver.http.HttpRequest;
import com.example.osmzhttpserver.http.HttpResponse;

public interface RequestRunner {
  HttpResponse run(HttpRequest request);
}
