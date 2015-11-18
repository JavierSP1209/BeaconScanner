package com.keysd.beaconscannerlib;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.keysd.beaconscannerlib.utils.Constants;

public class BLeScanService extends IntentService {

  private static final String TAG = "BLeScanService";
  private Handler restartServiceHandler;
  private Runnable serviceStarter = new Runnable() {
    @Override
    public void run() {
      Log.d(TAG, "Runnable...");
      restartService();
    }
  };

  public Handler getRestartServiceHandler() {
    return restartServiceHandler;
  }

  void setRestartServiceHandler(Handler restartServiceHandler) {
    this.restartServiceHandler = restartServiceHandler;
  }

  public BLeScanService() {
    super(TAG);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    restartServiceHandler = new Handler();
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Log.d(TAG, "onHandleIntent");

    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    restartServiceHandler.postDelayed(serviceStarter, Constants.DEFAULT_BLE_SCAN_INTERVAL_MS);

    Log.d(TAG, "finish");
  }

  private void restartService() {
    Intent serviceRestart = new Intent(getApplicationContext(), BLeScanService.class);
    startService(serviceRestart);
  }
}
