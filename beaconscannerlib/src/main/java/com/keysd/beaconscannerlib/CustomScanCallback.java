package com.keysd.beaconscannerlib;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.keysd.beaconscannerlib.utils.Constants;

import java.util.List;

import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class CustomScanCallback extends ScanCallback {

  Context context;

  public CustomScanCallback(Context context) {
    this.context = context;
  }

  @Override
  public void onScanResult(int callbackType, ScanResult result) {
    super.onScanResult(callbackType, result);
    Log.d(Constants.TAG, "onScanResult - " + callbackType + " - " + result);
  }

  @Override
  public void onBatchScanResults(List<ScanResult> results) {
    super.onBatchScanResults(results);
    for (ScanResult result : results) {
      ScanRecord scanRecord = result.getScanRecord();
      if (scanRecord != null) {

        byte[] beaconContent = scanRecord.getManufacturerSpecificData(76);
        Intent beaconIntent = new Intent(BLeScanService.ACTION_BEACON_FOUND);
        beaconIntent.putExtra(BLeScanService.EXTRA_BEACON_CONTENT, beaconContent);
        context.sendBroadcast(beaconIntent);
      }
    }

  }

  @Override
  public void onScanFailed(int errorCode) {
    super.onScanFailed(errorCode);
    Log.d(Constants.TAG, "onScanFailed - " + errorCode);
  }
}
