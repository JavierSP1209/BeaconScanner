package com.keysd.beaconscannerlib;

import android.content.Context;
import android.content.Intent;

import java.util.List;

import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

class CustomScanCallback extends ScanCallback {

    private Context context;

    CustomScanCallback(Context context) {
        this.context = context;
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        super.onBatchScanResults(results);
        for (ScanResult result : results) {
            ScanRecord scanRecord = result.getScanRecord();
            if (scanRecord != null) {
                byte[] beaconContent = scanRecord.getManufacturerSpecificData(89);
                Intent beaconIntent = new Intent(BLeScanService.ACTION_BEACON_FOUND);
                beaconIntent.putExtra(BLeScanService.EXTRA_BEACON_CONTENT, beaconContent);
                context.sendBroadcast(beaconIntent);
            }
        }
    }
}
