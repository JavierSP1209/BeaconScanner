package com.keysd.beaconscannerlib;

import android.content.pm.PackageManager;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.res.builder.RobolectricPackageManager;

import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class BLeManagerUtilsTest {

  private RobolectricPackageManager pm;
  private BLeManagerUtils subject;

  @Before
  public void setUp() {
    subject = BLeManagerUtils.getInstance();
    pm = (RobolectricPackageManager) RuntimeEnvironment.application.getPackageManager();
    pm.setSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE, true);
  }

  @Test
  public void getInstance() {

  }
}