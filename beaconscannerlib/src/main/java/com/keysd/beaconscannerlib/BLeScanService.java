package com.keysd.beaconscannerlib;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.keysd.beaconscannerlib.provider.BluetoothAdapterProvider;
import com.keysd.beaconscannerlib.utils.Constants;
import com.keysd.beaconscannerlib.utils.ScanAlarmManager;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

/**
 * BLe cycled scanner, this service will be restarted when the scan is finished unless BLe is not enabled
 */
public class BLeScanService extends IntentService {

  public static final String ACTION_BEACON_FOUND = "com.keysd.beaconscannerlib.BEACON_FOUND";
  public static final String ACTION_SCAN_FAILED = "com.keysd.beaconscannerlib.SCAN_FAILED";
  public static final String ACTION_SCAN_STARTED = "com.keysd.beaconscannerlib.SCAN_STARTED";
  public static final String ACTION_SCAN_ENDED = "com.keysd.beaconscannerlib.SCAN_ENDED";

  public static final String EXTRA_BEACON_CONTENT = "com.keysd.beaconscannerlib.beacon_content";

  private BluetoothAdapterProvider bluetoothAdapterProvider;
  private Handler restartServiceHandler;
  private BluetoothLeScannerCompat scanner;
  private CustomScanCallback scanCallback;

  byte[] filterData = {0, 0, -71, 64, 127, 48, -11, -8, 70, 110, -81, -7, 37, 85, 107, 87, -2, 109, 0, 0, 0, 0, 0};
  byte[] mask = new byte[]{0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0};

  private Runnable serviceStarter = new Runnable() {
    @Override
    public void run() {
      Log.d(Constants.TAG, "Stopping scanner...");
      scanner.stopScan(scanCallback);
    }
  };

  public Handler getRestartServiceHandler() {
    return restartServiceHandler;
  }

  void setRestartServiceHandler(Handler restartServiceHandler) {
    this.restartServiceHandler = restartServiceHandler;
  }

  void setBluetoothAdapterProvider(BluetoothAdapterProvider bluetoothAdapterProvider) {
    this.bluetoothAdapterProvider = bluetoothAdapterProvider;
  }

  void setScanner(BluetoothLeScannerCompat scanner) {
    this.scanner = scanner;
  }

  void setScanCallback(CustomScanCallback scanCallback) {
    this.scanCallback = scanCallback;
  }

  public BLeScanService() {
    super(Constants.TAG);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    restartServiceHandler = new Handler();
    bluetoothAdapterProvider = new BluetoothAdapterProvider();
    scanner = BluetoothLeScannerCompat.getScanner();
    scanCallback = new CustomScanCallback(this);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Log.d(Constants.TAG, "onHandleIntent");

    if (isBLeEnabled()) {
      startScan();
      restartService();
      restartServiceHandler.postDelayed(serviceStarter, Constants.DEFAULT_BLE_SCAN_PERIOD_MS);
    }
  }

  private void startScan() {
    ScanSettings settings = new ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_BALANCED).setReportDelay(Constants.SCAN_RESULTS_DELAY)
        .setUseHardwareBatchingIfSupported(false).build();
    List<ScanFilter> filters = new ArrayList<>();
    filters.add(new ScanFilter.Builder().setManufacturerData(76, filterData, mask).build());
    scanner.startScan(filters, settings, scanCallback);
  }

  private void restartService() {
    ScanAlarmManager.startScanAlarm(getApplicationContext());
  }

  private boolean isBLeEnabled() {
    BluetoothAdapter adapter = bluetoothAdapterProvider.getInstance();
    return adapter != null && adapter.isEnabled();
  }
}
