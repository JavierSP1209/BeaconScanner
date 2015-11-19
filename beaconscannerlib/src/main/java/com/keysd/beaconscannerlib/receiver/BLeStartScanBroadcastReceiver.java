package com.keysd.beaconscannerlib.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.keysd.beaconscannerlib.BLeScanService;
import com.keysd.beaconscannerlib.utils.Constants;


public class BLeStartScanBroadcastReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    Log.d(Constants.TAG, "OnReceive Broadcast");
    Intent queryIntent = new Intent(context, BLeScanService.class);
    context.startService(queryIntent);
  }
}
