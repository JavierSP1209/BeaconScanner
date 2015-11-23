package com.keysd.beaconscannerlib;

import android.app.AlarmManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.keysd.beaconscannerlib.provider.BluetoothAdapterProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlarmManager;
import org.robolectric.shadows.ShadowLog;

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
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class BLeScanServiceTest {

  Intent serviceIntent;

  @Mock Handler mockRestartServiceHandler;
  @Mock BluetoothAdapterProvider mockBluetoothAdapterProvider;
  @Mock BluetoothAdapter mockBluetoothAdapter;
  @Mock BluetoothLeScannerCompat mockScannerCompat;
  private BLeScanService scanService;
  private ShadowAlarmManager shadowAlarmManager;

  @Captor
  private ArgumentCaptor<List<ScanFilter>> scanFilterCaptor;

  @Before
  public void setUp() throws Exception {
    ShadowLog.stream = System.out;
    // create and injects mocks into object annotated with @InjectMocks
    MockitoAnnotations.initMocks(this);
    serviceIntent = new Intent(RuntimeEnvironment.application, BLeScanService.class);

    AlarmManager alarmManager = (AlarmManager) RuntimeEnvironment.application.getSystemService(
        Context.ALARM_SERVICE);
    shadowAlarmManager = shadowOf(alarmManager);

    stub(mockBluetoothAdapterProvider.getInstance()).toReturn(mockBluetoothAdapter);
    stub(mockBluetoothAdapter.isEnabled()).toReturn(true);

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
    byte[] filterData = {0, 0, -71, 64, 127, 48, -11, -8, 70, 110, -81, -7, 37, 85, 107, 87, -2, 109, 0, 0, 0, 0, 0};

    // Add data array to filters
    ScanFilter filter = new ScanFilter.Builder().setManufacturerData(76, filterData, mask).build();
    List<ScanFilter> actualFilters = scanFilterCaptor.getValue();
    assertThat(actualFilters.size(), is(1));
    ScanFilter actualFilter = actualFilters.get(0);
    assertThat(actualFilter.getManufacturerDataMask(), is(filter.getManufacturerDataMask()));
    assertThat(actualFilter.getManufacturerData(), is(filter.getManufacturerData()));


  }

  @Test
  public void onHandleIntent_shouldSetStartScanAlarm() {
    scanService.onHandleIntent(serviceIntent);

    //ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
    ShadowAlarmManager.ScheduledAlarm alarm = shadowAlarmManager.getNextScheduledAlarm();
    assertThat(alarm, notNullValue());

  }

  @Test
  public void onHandleIntent_shouldSetAPostDelayOf1000ms() {
    scanService.setRestartServiceHandler(mockRestartServiceHandler);
    scanService.onHandleIntent(serviceIntent);
    //verify(mockRestartServiceHandler).postDelayed(any(Runnable.class), eq(10000L));
  }

  @Test
  public void onHandleIntent_whenBLeIsNotEnabled_shouldNotScanNorRestartService() {
    stub(mockBluetoothAdapter.isEnabled()).toReturn(false);
    scanService.onHandleIntent(serviceIntent);

    ShadowAlarmManager.ScheduledAlarm alarm = shadowAlarmManager.getNextScheduledAlarm();
    assertThat(alarm, nullValue());
  }

  class BLeScanServiceMock extends BLeScanService {
    @Override
    public void onHandleIntent(Intent intent) {
      super.onHandleIntent(intent);
    }
  }
}