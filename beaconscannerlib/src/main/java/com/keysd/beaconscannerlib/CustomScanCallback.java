package com.keysd.beaconscannerlib;

import android.util.Log;
import android.util.SparseArray;

import com.keysd.beaconscannerlib.utils.Constants;
import com.keysd.beaconscannerlib.utils.ServiceUtils;

import java.util.List;

import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class CustomScanCallback extends ScanCallback {
  @Override
  public void onScanResult(int callbackType, ScanResult result) {
    super.onScanResult(callbackType, result);
    Log.d(Constants.TAG, "onScanResult - " + callbackType + " - " + result);
  }

  @Override
  public void onBatchScanResults(List<ScanResult> results) {
    super.onBatchScanResults(results);
    for (ScanResult result : results) {
      Log.d(Constants.TAG, "FoundFiltered device!");
      ScanRecord scanRecord = result.getScanRecord();
      if (scanRecord != null) {
        SparseArray<byte[]> manufacturerSpecificData = scanRecord.getManufacturerSpecificData();
        for (int i = 0; i < manufacturerSpecificData.size(); i++) {
          int key = manufacturerSpecificData.keyAt(i);
          // get the object by the key.
          byte[] obj = manufacturerSpecificData.get(key);
          Log.d(Constants.TAG, "Content: " + ServiceUtils.bytesToHex(obj));
        }
      }
    }

  }

  @Override
  public void onScanFailed(int errorCode) {
    super.onScanFailed(errorCode);
    Log.d(Constants.TAG, "onScanFailed - " + errorCode);
  }
}
