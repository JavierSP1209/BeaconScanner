package com.keysd.beaconscannerlib.utils;

public class Constants {

  /**
   * Default scan interval for the scan service in milliseconds, a period is the time between scans
   */
  public static final long DEFAULT_BLE_SCAN_INTERVAL_MS = 10000;

  /**
   * Default scan period, the actual scanning time for each scan
   */
  public static final long DEFAULT_BLE_SCAN_PERIOD_MS = 5000;

  public static final long SCAN_RESULTS_DELAY = 3000;

  public static final String TAG = "BLeScanService";

  public static final String ACTION_START_BLE_SCAN = "com.keysd.beaconscannerlib.START_BLE_SCAN";
  public static final String PERMISSION_BLE_SCAN = "com.keysd.beaconscannerlib.START_BLE_SCAN.PERMISSION";
}
