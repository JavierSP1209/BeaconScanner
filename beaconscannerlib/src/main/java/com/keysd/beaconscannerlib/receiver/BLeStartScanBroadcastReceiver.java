package com.keysd.beaconscannerlib.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.keysd.beaconscannerlib.BLeScanService;
import com.keysd.beaconscannerlib.utils.Constants;


public class BLeStartScanBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent queryIntent = new Intent(context, BLeScanService.class);
        queryIntent.putExtra(BLeScanService.EXTRA_SCAN_PERIOD,
                intent.getLongExtra(BLeScanService.EXTRA_SCAN_PERIOD,
                        Constants.DEFAULT_BLE_SCAN_PERIOD_MS));
        queryIntent.putExtra(BLeScanService.EXTRA_SCAN_INTERVAL,
                intent.getLongExtra(BLeScanService.EXTRA_SCAN_INTERVAL,
                        Constants.DEFAULT_BLE_SCAN_INTERVAL_MS));
        queryIntent.putExtra(BLeScanService.EXTRA_FILTER_UUID,
                intent.getByteArrayExtra(BLeScanService.EXTRA_FILTER_UUID));
        context.startService(queryIntent);
    }
}
