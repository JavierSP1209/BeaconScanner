package com.keysd.beaconscannerlib;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.keysd.beaconscannerlib.provider.BluetoothAdapterProvider;
import com.keysd.beaconscannerlib.utils.Constants;
import com.keysd.beaconscannerlib.utils.ScanAlarmManager;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

import static com.keysd.beaconscannerlib.utils.Constants.TAG;

/**
 * BLe cycled scanner, this service will be restarted when the scan is finished unless BLe is not
 * enabled
 */
public class BLeScanService extends IntentService {

    private static final byte[] MASK = new byte[]{0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0};
    public static final String ACTION_BEACON_FOUND = "com.keysd.beaconscannerlib.BEACON_FOUND";
    public static final String ACTION_SCAN_START = "com.keysd.beaconscannerlib.SCAN_START";
    public static final String ACTION_SCAN_STOP = "com.keysd.beaconscannerlib.SCAN_STOP";

    public static final String EXTRA_BEACON_CONTENT = "com.keysd.beaconscannerlib.BEACON_CONTENT";
    public static final String EXTRA_SCAN_PERIOD = "com.keysd.beaconscannerlib.SCAN_PERIOD";
    public static final String EXTRA_SCAN_INTERVAL = "com.keysd.beaconscannerlib.SCAN_INTERVAL";
    public static final String EXTRA_FILTER_UUID = "com.keysd.beaconscannerlib.FILTER_UUID";

    private BluetoothAdapterProvider bluetoothAdapterProvider;
    private Handler stopScanHandler;
    private BluetoothLeScannerCompat scanner;
    private CustomScanCallback scanCallback;
    private byte[] filterData;
    private long scanInterval;

    private Runnable serviceStarter = new Runnable() {
        @Override
        public void run() {
            scanner.stopScan(scanCallback);
            sendStateLocalBroadcast(ACTION_SCAN_STOP);
        }
    };
    private long scanPeriod;

    void setStopScanHandler(Handler stopScanHandler) {
        this.stopScanHandler = stopScanHandler;
    }

    void setBluetoothAdapterProvider(BluetoothAdapterProvider bluetoothAdapterProvider) {
        this.bluetoothAdapterProvider = bluetoothAdapterProvider;
    }

    void setScanner(BluetoothLeScannerCompat scanner) {
        this.scanner = scanner;
    }

    public BLeScanService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        stopScanHandler = new Handler();
        bluetoothAdapterProvider = new BluetoothAdapterProvider();
        scanner = BluetoothLeScannerCompat.getScanner();
        scanCallback = new CustomScanCallback(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        filterData = intent.getByteArrayExtra(EXTRA_FILTER_UUID);
        scanPeriod = intent.getLongExtra(EXTRA_SCAN_PERIOD,
                Constants.DEFAULT_BLE_SCAN_PERIOD_MS);
        scanInterval = intent.getLongExtra(EXTRA_SCAN_INTERVAL,
                Constants.DEFAULT_BLE_SCAN_INTERVAL_MS);
        if (isBLeEnabled()) {
            startScan();
            restartService();
            stopScanHandler.postDelayed(serviceStarter, scanPeriod);
        }
    }

    private void startScan() {
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED).setReportDelay(
                        Constants.SCAN_RESULTS_DELAY)
                .setUseHardwareBatchingIfSupported(false).build();
        List<ScanFilter> filters = new ArrayList<>();
        ScanFilter.Builder builder = new ScanFilter.Builder();
        if (filterData != null) {
            builder.setManufacturerData(89, filterData, MASK);
        }
        filters.add(builder.build());
        scanner.startScan(filters, settings, scanCallback);
        sendStateLocalBroadcast(ACTION_SCAN_START);
    }

    private void restartService() {
        ScanParameters scanParameters = new ScanParameters.Builder()
                .setScanInterval(scanInterval)
                .setScanPeriod(scanPeriod)
                .setFilterUUIDData(filterData)
                .build();
        ScanAlarmManager.startScanAlarm(getApplicationContext(), scanParameters);
    }

    private boolean isBLeEnabled() {
        BluetoothAdapter adapter = bluetoothAdapterProvider.getInstance();
        return adapter != null && adapter.isEnabled();
    }

    private void sendStateLocalBroadcast(String action) {
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(
                new Intent(action));
    }
}
