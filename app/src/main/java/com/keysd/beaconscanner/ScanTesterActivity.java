package com.keysd.beaconscanner;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.prettysmarthomes.beaconscanner.BLeScanService;
import com.prettysmarthomes.beaconscanner.BLeScanServiceManager;
import com.prettysmarthomes.beaconscanner.BLeScanServiceUtils;
import com.prettysmarthomes.beaconscanner.ScanParameters;

public class ScanTesterActivity extends AppCompatActivity {

  public static final String TAG = "BEACON_SCANNER_TESTER";

  BLeScanServiceManager bleScanServiceManager = new BLeScanServiceManager();
  private FloatingActionButton fab;
  private TextView txtStatus;

  boolean isRunning;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_scan_tester);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    isRunning = false;
    fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ScanParameters parameters = new ScanParameters.Builder()
            .setScanInterval(1000)
            .setScanPeriod(2000)
            .build();
        txtStatus.setText("Starting Service...");
        isRunning = !isRunning;
        if (isRunning) {
          fab.setImageResource(R.drawable.ic_stop);
          bleScanServiceManager.startScanService(getApplicationContext(), parameters);
        } else {
          fab.setImageResource(R.drawable.ic_play);
          bleScanServiceManager.cancelScanService(getApplicationContext());
        }
      }
    });

    txtStatus = (TextView) findViewById(R.id.txtStatus);
    txtStatus.setText("Waiting...");

    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(BLeScanService.ACTION_BEACON_FOUNDED);
    registerReceiver(receiver, intentFilter);
  }

  @Override
  protected void onResume() {
    super.onResume();
    Log.d(TAG, "Entering onResume");
    validateBleSupport();

  }

  @Override
  protected void onStop() {
    super.onStop();
    unregisterReceiver(receiver);
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
      BluetoothAdapter bluetoothAdapter = BluetoothAdapter
          .getDefaultAdapter();

      if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
        Log.d(TAG, "onResume: Bluetooth is disabled");
        //Bluetooth is disabled, request enabling it
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivity(enableBtIntent);
      }
    }

  }

  private BroadcastReceiver receiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.getAction().equals(BLeScanService.ACTION_BEACON_FOUNDED)) {
        byte[] beaconContent = intent.getByteArrayExtra(BLeScanService.EXTRA_BEACON_CONTENT);
        Log.d(TAG, "BeaconFound: " + BLeScanServiceUtils.bytesToHex(beaconContent));
      }
    }
  };
}