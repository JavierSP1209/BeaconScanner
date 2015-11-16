package com.keysd.beaconscannerlib;

import android.content.Context;
import android.content.Intent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.stub;

public class BLeScanServiceTest {

  Intent serviceIntent;

  @Before
  public void setUp() throws Exception {
    serviceIntent = new Intent(RuntimeEnvironment.application, BLeScanService.class);
  }

  @Test
  public void onHandleIntent_doSomething(){
    BLeScanServiceMock service = new BLeScanServiceMock();
    service.onCreate();
    service.onHandleIntent(serviceIntent);
  }

  class BLeScanServiceMock extends BLeScanService {
    @Override
    public void onHandleIntent(Intent intent) {
      super.onHandleIntent(intent);
    }
  }
}