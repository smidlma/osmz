package com.example.osmzhttpserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.osmzhttpserver.controller.SystemController;
import com.example.osmzhttpserver.controller.TelemetryController;
import com.example.osmzhttpserver.http.HttpMethod;
import com.example.osmzhttpserver.http.HttpResponse;
import com.example.osmzhttpserver.service.LocationService;
import com.example.osmzhttpserver.service.SensorService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

  Handler handler = new Handler(Looper.getMainLooper()) {
    @Override
    public void handleMessage(@NonNull Message msg) {
      String logMsg = msg.getData().getString("serverLog", "No data");
      Log.d("serverLog", logMsg);

      TextView logs = (TextView) findViewById(R.id.logTextView);
      logs.append("\n" + logMsg);

    }
  };
  private SocketServer s;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Button btn1 = (Button) findViewById(R.id.button1);
    Button btn2 = (Button) findViewById(R.id.button2);

    btn1.setOnClickListener(this);
    btn2.setOnClickListener(this);

  }


  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.button1) {
      s = new SocketServer(this, handler);
      TelemetryController telemetryController = new TelemetryController(new LocationService(this),
          new SensorService(this));
      SystemController systemController = new SystemController();
      s.addRoute(HttpMethod.GET, "/streams/telemetry",
          (req) -> new HttpResponse.Builder().setEntity(
                  "<a href='/streams/telemetry/location'> /streams/telemetry/location </a> "
                      + "<br> "
                      + "<a href='/streams/telemetry/sensors'> /streams/telemetry/sensors </a>")
              .setStatusCode(200).build());
      s.addRoute(HttpMethod.GET, "/streams/telemetry/location",
          (req) -> telemetryController.getLocation());
      s.addRoute(HttpMethod.GET, "/streams/telemetry/sensors",
          (req) -> telemetryController.getSensorsData());
      s.addRoute(HttpMethod.GET, "/cmd/.*", (req) -> systemController.processCommand(req));
      s.start();
    }
    if (v.getId() == R.id.button2) {
      s.close();
      try {
        s.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

}
