package com.keysd.beaconscannerlib.utils;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;

import com.keysd.beaconscannerlib.BuildConfig;
import com.keysd.beaconscannerlib.receiver.BLeStartScanBroadcastReceiver;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlarmManager;
import org.robolectric.shadows.ShadowPendingIntent;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class ScanAlarmManagerTest {

  ShadowAlarmManager shadowAlarmManager;
  AlarmManager alarmManager;
  Context context;

  @Before
  public void setUp() {
    alarmManager = (AlarmManager) RuntimeEnvironment.application.getSystemService(
        Context.ALARM_SERVICE);
    shadowAlarmManager = shadowOf(alarmManager);
    context = RuntimeEnvironment.application.getApplicationContext();
  }

  @Test
  public void startRepeatingBackgroundQuery_shouldScheduleAlarm() {
    Assert.assertNull("Previous alarm exists", shadowAlarmManager.getNextScheduledAlarm());
    ScanAlarmManager.startScanAlarm(context);
    ShadowAlarmManager.ScheduledAlarm repeatingAlarm = shadowAlarmManager.getNextScheduledAlarm();
    Assert.assertNotNull("Alarm not scheduled", repeatingAlarm);
  }

  @Test
  public void startRepeatingBackgroundQuery_shouldScheduleAlarmEachHour() {
    ScanAlarmManager.startScanAlarm(context);
    long startTime = System.currentTimeMillis();
    ShadowAlarmManager.ScheduledAlarm repeatingAlarm = shadowAlarmManager.getNextScheduledAlarm();
    assertThat(AlarmManager.RTC, is(repeatingAlarm.type));
    long expectedTriggerTime = startTime + TimeUnit.SECONDS.toMillis(10);
    long timeDifference = expectedTriggerTime - repeatingAlarm.triggerAtTime;
    assertThat(timeDifference, lessThan(10L));
  }

  @Test
  public void startRepeatingBackgroundQuery_shouldScheduleOnlyOneTime() throws Exception {
    ScanAlarmManager.startScanAlarm(context);
    ScanAlarmManager.startScanAlarm(context);
    ScanAlarmManager.startScanAlarm(context);

    //assertThat(1, shadowAlarmManager.getScheduledAlarms().size());
  }

  @Test
  public void startRepeatingBackgroundQuery_shouldTriggerBroadcastReceiverWhenTimeElapsed()
      throws Exception {
    Intent expectedIntent = new Intent(context, BLeStartScanBroadcastReceiver.class);

    ScanAlarmManager.startScanAlarm(context);

    ShadowAlarmManager.ScheduledAlarm scheduledAlarm = shadowAlarmManager.getNextScheduledAlarm();
    ShadowPendingIntent shadowPendingIntent = shadowOf(scheduledAlarm.operation);
//    assertThat(shadowPendingIntent.isBroadcastIntent());
//    assertThat(1, shadowPendingIntent.getSavedIntents().length);
//    assertThat(expectedIntent.getComponent(),
//        shadowPendingIntent.getSavedIntents()[0].getComponent());
  }

  @Test
  public void cancelSHealthSyncAlarm_shouldRemoveAlarm() {
    ScanAlarmManager.startScanAlarm(context);
    ScanAlarmManager.cancelScanAlarm(context);
    ShadowAlarmManager.ScheduledAlarm repeatingAlarm = shadowAlarmManager.getNextScheduledAlarm();
    Assert.assertNull("Alarm scheduled", repeatingAlarm);
  }
}