package com.keysd.beaconscannerlib;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class BLeScanService extends IntentService {

  private static final String TAG = "BLeScanService";

  public BLeScanService() {
    super(TAG);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Log.d(TAG, "onHandleIntent");
  }
}
