package com.keysd.beaconscanner;

import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class ScanTesterActivityTest {

  private ScanTesterActivity subject;

  @Before
  public void setUp() throws Exception {
    subject = Robolectric.buildActivity(ScanTesterActivity.class).create().get();

  }

  @Test
  public void onCreate_shouldAddFabOnClickListener() {
    View fabView = subject.findViewById(R.id.fab);
    assertThat("FAB does not have onClickListener", fabView.hasOnClickListeners(), is(true));
  }
}