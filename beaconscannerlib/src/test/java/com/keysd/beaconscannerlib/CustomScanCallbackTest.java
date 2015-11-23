package com.keysd.beaconscannerlib;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.LinkedList;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class CustomScanCallbackTest {

  @Mock Context context;
  CustomScanCallback subject;

  @Before
  public void setUp() throws Exception {
    initMocks(this);
    subject = new CustomScanCallback(context);
  }

  @Test
  public void onBatchScanResults_whenResultHasScanRecord_shouldSendBroadcastWithBeaconInformation() {
    List<ScanResult> resultList = new LinkedList<>();
    ScanRecord scanRecord = mock(ScanRecord.class);
    byte[] beaconContent = {1, 2, 3, 4, 5, 6};
    stub(scanRecord.getManufacturerSpecificData(anyInt())).toReturn(beaconContent);
    resultList.add(new ScanResult(mock(BluetoothDevice.class), scanRecord, 0, 10000));

    ArgumentCaptor<Intent> intentArgumentCaptor = ArgumentCaptor.forClass(Intent.class);
    subject.onBatchScanResults(resultList);
    verify(context).sendBroadcast(intentArgumentCaptor.capture());

    Intent actualIntent = intentArgumentCaptor.getValue();
    assertThat(actualIntent.getAction(), is(equalTo(BLeScanService.ACTION_BEACON_FOUND)));
    assertThat(actualIntent.getByteArrayExtra(BLeScanService.EXTRA_BEACON_CONTENT),
        is(equalTo(beaconContent)));
  }
}