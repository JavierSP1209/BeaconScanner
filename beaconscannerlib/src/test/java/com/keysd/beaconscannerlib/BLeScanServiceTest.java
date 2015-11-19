package com.keysd.beaconscannerlib;

import android.content.Intent;
import android.os.Handler;

import com.keysd.beaconscannerlib.utils.Constants;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.ShadowLooper;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class BLeScanServiceTest {

  Intent serviceIntent;

  @Mock Handler mockRestartServiceHandler;
  private BLeScanService scanService;

  @Before
  public void setUp() throws Exception {
    ShadowLog.stream = System.out;
    // create and injects mocks into object annotated with @InjectMocks
    MockitoAnnotations.initMocks(this);
    serviceIntent = new Intent(RuntimeEnvironment.application, BLeScanService.class);

    scanService = new BLeScanServiceMock();
    scanService.onCreate();
  }

  @Test
  public void onHandleIntent_shouldSendRestartBroadcastAfterDelayedTimeHasPassed() {

    scanService.onHandleIntent(serviceIntent);

    ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

    List<Intent> broadcastIntents = shadowOf(RuntimeEnvironment.application).getBroadcastIntents();

    Assert.assertThat("Broadcast not sent", broadcastIntents.size(), is(equalTo(1)));
    Assert.assertThat("Incorrect broadcast", broadcastIntents.get((0)).getAction(),
        is(equalTo(Constants.ACTION_START_BLE_SCAN)));

//    Intent serviceIntent = ShadowApplication.getInstance().peekNextStartedService();
//    Assert.assertNotNull("Service not restarted", serviceIntent);
//    Assert.assertEquals("Expected the BLeScanService service to be started",
//        BLeScanService.class.getCanonicalName(),
//        serviceIntent.getComponent().getClassName());
  }

  @Test
  public void onHandleIntent_shouldSetAPostDelayOf1000ms() {
    scanService.setRestartServiceHandler(mockRestartServiceHandler);
    scanService.onHandleIntent(serviceIntent);
    verify(mockRestartServiceHandler).postDelayed(any(Runnable.class), eq(10000L));
  }

  class BLeScanServiceMock extends BLeScanService {
    @Override
    public void onHandleIntent(Intent intent) {
      super.onHandleIntent(intent);
    }
  }
}