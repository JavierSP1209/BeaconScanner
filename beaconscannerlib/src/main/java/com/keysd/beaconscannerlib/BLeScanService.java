package com.keysd.beaconscannerlib;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.keysd.beaconscannerlib.utils.Constants;
import com.keysd.beaconscannerlib.utils.ScanAlarmManager;

public class BLeScanService extends IntentService {

  private Handler restartServiceHandler;
  private Runnable serviceStarter = new Runnable() {
    @Override
    public void run() {
      Log.d(Constants.TAG, "Runnable...");
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
    super(Constants.TAG);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    restartServiceHandler = new Handler();
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Log.d(Constants.TAG, "onHandleIntent");

    try {
      Thread.sleep(Constants.DEFAULT_BLE_SCAN_PERIOD_MS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    restartService();
    //restartServiceHandler.postDelayed(serviceStarter, Constants.DEFAULT_BLE_SCAN_INTERVAL_MS);

    Log.d(Constants.TAG, "finish");
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.d(Constants.TAG, "Destroyed");
  }

  private void restartService() {
    ScanAlarmManager.startScanAlarm(getApplicationContext());
  }
}
