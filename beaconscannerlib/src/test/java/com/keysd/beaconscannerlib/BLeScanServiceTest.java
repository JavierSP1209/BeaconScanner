package com.keysd.beaconscannerlib;

import android.app.AlarmManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.keysd.beaconscannerlib.provider.BluetoothAdapterProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlarmManager;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.ShadowLooper;

import java.util.List;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class BLeScanServiceTest {

    @Mock
    Handler mockStopScanHandler;

    @Mock
    BluetoothAdapterProvider mockBluetoothAdapterProvider;
    @Mock
    BluetoothAdapter mockBluetoothAdapter;
    @Mock
    BluetoothLeScannerCompat mockScannerCompat;
    @Captor
    private ArgumentCaptor<List<ScanFilter>> scanFilterCaptor;
    private BLeScanService scanService;

    private Intent serviceIntent;
    private byte[] filterData = {0, 0, -71, 64, 127, 48, -11, -8, 70, 110, -81, -7, 37, 85, 107, 87, -2, 109, 0, 0, 0, 0, 0};
    private long scanPeriod = 1000L;
    private ShadowAlarmManager shadowAlarmManager;
    private boolean broadcastSent;

    @Before
    public void setUp() throws Exception {
        ShadowLog.stream = System.out;
        // create and injects mocks into object annotated with @InjectMocks
        MockitoAnnotations.initMocks(this);
        serviceIntent = new Intent(RuntimeEnvironment.application, BLeScanService.class);
        serviceIntent.putExtra("com.keysd.beaconscannerlib.FILTER_UUID", filterData);
        long scanInterval = 2000L;
        serviceIntent.putExtra("com.keysd.beaconscannerlib.SCAN_INTERVAL", scanInterval);
        serviceIntent.putExtra("com.keysd.beaconscannerlib.SCAN_PERIOD", scanPeriod);

        AlarmManager alarmManager = (AlarmManager) RuntimeEnvironment.application.getSystemService(
                Context.ALARM_SERVICE);
        shadowAlarmManager = shadowOf(alarmManager);

        when(mockBluetoothAdapterProvider.getInstance()).thenReturn(mockBluetoothAdapter);
        when(mockBluetoothAdapter.isEnabled()).thenReturn(true);
        broadcastSent = false;

        scanService = new BLeScanServiceMock();
        scanService.onCreate();
        scanService.setBluetoothAdapterProvider(mockBluetoothAdapterProvider);
        scanService.setScanner(mockScannerCompat);
    }

    @Test
    public void onHandleIntent_shouldStartScanning() {
        ArgumentCaptor<ScanSettings> settingsArgumentCaptor = ArgumentCaptor.forClass(
                ScanSettings.class);
        scanService.onHandleIntent(serviceIntent);

        verify(mockScannerCompat).startScan(scanFilterCaptor.capture(),
                settingsArgumentCaptor.capture(),
                any(CustomScanCallback.class));

        ScanSettings actualSettings = settingsArgumentCaptor.getValue();
        assertThat(actualSettings.getScanMode(), is(equalTo(ScanSettings.SCAN_MODE_BALANCED)));
        assertThat(actualSettings.getReportDelayMillis(), is(equalTo(3000L)));
        assertThat(actualSettings.getUseHardwareBatchingIfSupported(), is(false));

        byte[] mask = new byte[]{0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0};


        // Add data array to filters
        ScanFilter filter = new ScanFilter.Builder().setManufacturerData(76, filterData,
                mask).build();
        List<ScanFilter> actualFilters = scanFilterCaptor.getValue();
        assertThat(actualFilters.size(), is(1));
        ScanFilter actualFilter = actualFilters.get(0);
        assertThat(actualFilter.getManufacturerDataMask(), is(filter.getManufacturerDataMask()));
        assertThat(actualFilter.getManufacturerData(), is(filter.getManufacturerData()));
    }

    @Test
    public void onHandleIntent_filterUUIDNull_shouldSetNullFilter() {
        ArgumentCaptor<ScanSettings> settingsArgumentCaptor = ArgumentCaptor.forClass(
                ScanSettings.class);
        serviceIntent.removeExtra("com.keysd.beaconscannerlib.FILTER_UUID");
        scanService.onHandleIntent(serviceIntent);

        verify(mockScannerCompat).startScan(scanFilterCaptor.capture(),
                settingsArgumentCaptor.capture(),
                any(CustomScanCallback.class));

        // Add data array to filters
        List<ScanFilter> actualFilters = scanFilterCaptor.getValue();
        assertThat(actualFilters.size(), is(1));
        assertThat(actualFilters.get(0), nullValue());


    }

    @Test
    public void onHandleIntent_shouldSetStartScanAlarm() {
        scanService.onHandleIntent(serviceIntent);

        ShadowAlarmManager.ScheduledAlarm alarm = shadowAlarmManager.getNextScheduledAlarm();
        assertThat(alarm, notNullValue());
    }

    @Test
    public void onHandleIntent_shouldSetScanPeriodAsPostDelay() {
        scanService.setStopScanHandler(mockStopScanHandler);
        scanService.onHandleIntent(serviceIntent);
        verify(mockStopScanHandler).postDelayed(any(Runnable.class), eq(scanPeriod));
    }

    @Test
    public void onHandleIntent_scanPeriodNotSet_shouldSetDefaultScanPeriodAsPostDelay() {
        scanService.setStopScanHandler(mockStopScanHandler);
        serviceIntent.removeExtra("com.keysd.beaconscannerlib.SCAN_PERIOD");
        scanService.onHandleIntent(serviceIntent);
        verify(mockStopScanHandler).postDelayed(any(Runnable.class), eq(5000L));
    }

    @Test
    public void onHandleIntent_whenBLeIsNotEnabled_shouldNotScanNorRestartService() {
        when(mockBluetoothAdapter.isEnabled()).thenReturn(false);
        scanService.onHandleIntent(serviceIntent);

        ShadowAlarmManager.ScheduledAlarm alarm = shadowAlarmManager.getNextScheduledAlarm();
        assertThat(alarm, nullValue());
    }

    @Test
    public void onHandleIntent_sendStartLocalBroadcast() {
        registerLocalReceiver("com.keysd.beaconscannerlib.SCAN_START");
        scanService.onHandleIntent(serviceIntent);
        assertThat(broadcastSent, is(true));
    }

    @Test
    public void stopScanHandler_stopScansAfterPeriodFinish() {
        scanService.onHandleIntent(serviceIntent);
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        verify(mockScannerCompat).stopScan(any(CustomScanCallback.class));
    }

    @Test
    public void stopScanHandler_sendLocalStopBroadcast() {
        registerLocalReceiver("com.keysd.beaconscannerlib.SCAN_STOP");
        scanService.onHandleIntent(serviceIntent);
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
        assertThat(broadcastSent, is(true));
    }

    private void registerLocalReceiver(String action) {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(
                RuntimeEnvironment.application);
        broadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent intent) {
                broadcastSent = true;
            }
        }, new IntentFilter(action));
    }

    class BLeScanServiceMock extends BLeScanService {
        @Override
        public void onHandleIntent(Intent intent) {
            super.onHandleIntent(intent);
        }
    }
}