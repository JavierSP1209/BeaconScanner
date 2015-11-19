package com.keysd.beaconscanner;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.keysd.beaconscannerlib.BLeScanService;
import com.keysd.beaconscannerlib.utils.Constants;
import com.keysd.beaconscannerlib.utils.ScanAlarmManager;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class ScanTesterActivity extends AppCompatActivity {

  public static final String TAG = "BEACON_SCANNER_TESTER";

  private TextView txtStatus;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_scan_tester);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        ScanSettings settings = new ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(1000)
            .setUseHardwareBatchingIfSupported(false).build();
        List<ScanFilter> filters = new ArrayList<>();
//        filters.add(new ScanFilter.Builder().setServiceUuid(mUuid).build());
        //scanner.startScan(null, settings, scanCallback);

        ScanAlarmManager.startScanAlarm(getApplicationContext());
        txtStatus.setText("Starting Service...");
      }
    });

    txtStatus = (TextView) findViewById(R.id.txtStatus);
    txtStatus.setText("Waiting...");
  }

  @Override
  protected void onResume() {
    super.onResume();
    Log.d(TAG, "Entering onResume");
    validateBleSupport();

  }

  private void validateBleSupport() {
    /*
    * Check for Bluetooth LE Support.  In production, our manifest entry will keep this
    * from installing on these devices, but this will allow test devices or other
    * sideloads to report whether or not the feature exists.
    */
    if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
      Snackbar.make(txtStatus, "No LE Support.", Snackbar.LENGTH_LONG).show();
    } else {

      /*
      * We need to enforce that Bluetooth is first enabled, and take the
      * user to settings to enable it if they have not done so.
      */
      BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
          .getDefaultAdapter();

      if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
        Log.d(TAG, "onResume: Bluetooth is disabled");
        //Bluetooth is disabled, request enabling it
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivity(enableBtIntent);
      }
    }

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_scan_tester, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private ScanCallback scanCallback = new ScanCallback() {
    @Override
    public void onScanResult(int callbackType, ScanResult result) {
      super.onScanResult(callbackType, result);
      Log.d(TAG, "onScanResult - " + callbackType + " - " + result);
      txtStatus.setText("Scan Finish...");
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
      super.onBatchScanResults(results);
      Log.d(TAG, "onBatchScanResults - " + results.size());
      txtStatus.setText("Batch scan Results...");
      for (ScanResult result : results) {
        Log.d(TAG, "Result: " + result);
        ScanRecord scanRecord = result.getScanRecord();
        if (scanRecord != null) {
          SparseArray<byte[]> manufacturerSpecificData = scanRecord.getManufacturerSpecificData();
          for (int i = 0; i < manufacturerSpecificData.size(); i++) {
            int key = manufacturerSpecificData.keyAt(i);
            // get the object by the key.
            byte[] obj = manufacturerSpecificData.get(key);
            Log.d(TAG, "ManufacturerData: " + bytesToHex(obj));
          }
        }
      }

    }

    @Override
    public void onScanFailed(int errorCode) {
      super.onScanFailed(errorCode);
      Log.d(TAG, "onScanFailed - " + errorCode);
      txtStatus.setText("Scan Failed...");
    }
  };

  final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

  public static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = hexArray[v >>> 4];
      hexChars[j * 2 + 1] = hexArray[v & 0x0F];
    }
    return new String(hexChars);
  }
}