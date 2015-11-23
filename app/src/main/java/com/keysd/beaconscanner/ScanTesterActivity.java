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
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.keysd.beaconscannerlib.BLeScanService;
import com.keysd.beaconscannerlib.utils.BLeServiceUtils;
import com.keysd.beaconscannerlib.utils.ScanAlarmManager;

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
        ScanAlarmManager.startScanAlarm(getApplicationContext());
        txtStatus.setText("Starting Service...");
      }
    });

    txtStatus = (TextView) findViewById(R.id.txtStatus);
    txtStatus.setText("Waiting...");

    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(BLeScanService.ACTION_BEACON_FOUND);
    registerReceiver(bReceiver, intentFilter);
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

  private BroadcastReceiver bReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.getAction().equals(BLeScanService.ACTION_BEACON_FOUND)) {
        byte[] beaconContent = intent.getByteArrayExtra(BLeScanService.EXTRA_BEACON_CONTENT);
        Log.d(TAG, "BeaconFound: " + BLeServiceUtils.bytesToHex(beaconContent));
      }
    }
  };
}