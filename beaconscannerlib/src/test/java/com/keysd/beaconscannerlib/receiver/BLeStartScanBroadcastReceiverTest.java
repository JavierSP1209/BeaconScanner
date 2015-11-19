package com.keysd.beaconscannerlib.receiver;

import android.content.Context;
import android.content.Intent;

import com.keysd.beaconscannerlib.BLeScanService;
import com.keysd.beaconscannerlib.BuildConfig;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class BLeStartScanBroadcastReceiverTest {

  BLeStartScanBroadcastReceiver subject;
  Context applicationContext;
  ShadowApplication shadowApplication;

  @Before
  public void setUp() throws Exception {
    subject = new BLeStartScanBroadcastReceiver();
    shadowApplication = ShadowApplication.getInstance();
    applicationContext = shadowApplication.getApplicationContext();
  }

  @Test
  public void onReceive_shouldStartService() throws Exception {

    subject.onReceive(applicationContext, null);

    Intent serviceIntent = shadowApplication.peekNextStartedService();
    Assert.assertNotNull("Service not restarted", serviceIntent);
    Assert.assertEquals("Expected the BLeScanService service to be invoked",
        BLeScanService.class.getCanonicalName(),
        serviceIntent.getComponent().getClassName());
  }
}