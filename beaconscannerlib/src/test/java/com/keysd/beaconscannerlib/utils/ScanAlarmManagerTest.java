package com.keysd.beaconscannerlib.utils;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;

import com.keysd.beaconscannerlib.BuildConfig;
import com.keysd.beaconscannerlib.ScanParameters;
import com.keysd.beaconscannerlib.receiver.BLeStartScanBroadcastReceiver;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlarmManager;
import org.robolectric.shadows.ShadowPendingIntent;

import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class ScanAlarmManagerTest {

    private ShadowAlarmManager shadowAlarmManager;
    private Context context;
    private static final long INTERVAL = 20000L;
    private ScanParameters scanParameters = new ScanParameters.Builder().setScanInterval(
            INTERVAL).build();

    @Before
    public void setUp() {
        AlarmManager alarmManager = (AlarmManager) RuntimeEnvironment.application.getSystemService(
                Context.ALARM_SERVICE);
        shadowAlarmManager = shadowOf(alarmManager);
        context = RuntimeEnvironment.application.getApplicationContext();
    }

    @Test
    public void startScanAlarm_shouldScheduleAlarm() {
        Assert.assertNull("Previous alarm exists", shadowAlarmManager.getNextScheduledAlarm());
        ScanAlarmManager.startScanAlarm(context, scanParameters);
        ShadowAlarmManager.ScheduledAlarm repeatingAlarm = shadowAlarmManager.getNextScheduledAlarm();
        Assert.assertNotNull("Alarm not scheduled", repeatingAlarm);
    }

    @Test
    public void startScanAlarm_shouldScheduleAlarmEachInterval() {
        ScanAlarmManager.startScanAlarm(context, scanParameters);
        long startTime = System.currentTimeMillis();
        ShadowAlarmManager.ScheduledAlarm repeatingAlarm = shadowAlarmManager.getNextScheduledAlarm();
        assertThat(AlarmManager.RTC, is(repeatingAlarm.type));
        long expectedTriggerTime = startTime + INTERVAL;
        long timeDifference = expectedTriggerTime - repeatingAlarm.triggerAtTime;
        assertThat(timeDifference, lessThan(5L));
    }

    @Test
    public void startScanAlarm_shouldScheduleOnlyOneTime() throws Exception {
        ScanAlarmManager.startScanAlarm(context, scanParameters);
        ScanAlarmManager.startScanAlarm(context, scanParameters);
        ScanAlarmManager.startScanAlarm(context, scanParameters);

        assertThat(1, is(shadowAlarmManager.getScheduledAlarms().size()));
    }

    @Test
    public void startScanAlarm_shouldTriggerBroadcastReceiverWhenTimeElapsed()
            throws Exception {
        Intent expectedIntent = new Intent(context, BLeStartScanBroadcastReceiver.class);

        ScanAlarmManager.startScanAlarm(context, scanParameters);

        ShadowAlarmManager.ScheduledAlarm scheduledAlarm = shadowAlarmManager.getNextScheduledAlarm();
        ShadowPendingIntent shadowPendingIntent = shadowOf(scheduledAlarm.operation);
        assertThat(shadowPendingIntent.isBroadcastIntent(), is(true));
        assertThat(1, is(shadowPendingIntent.getSavedIntents().length));
        assertThat(expectedIntent.getComponent(), is(
                shadowPendingIntent.getSavedIntents()[0].getComponent()));
    }

    @Test
    public void startScanAlarm_setScanParameterExtras() {
        long expectedPeriod = 5L;
        long expectedInterval = 5L;
        byte[] expectedFilterUUIDData = new byte[]{0, 1, 1, 0};
        ScanParameters scanParameters = new ScanParameters.Builder()
                .setScanInterval(expectedInterval)
                .setScanPeriod(expectedPeriod)
                .setFilterUUIDData(expectedFilterUUIDData)
                .build();
        ScanAlarmManager.startScanAlarm(context, scanParameters);
        ShadowAlarmManager.ScheduledAlarm repeatingAlarm = shadowAlarmManager.getNextScheduledAlarm();
        ShadowPendingIntent pendingIntent = shadowOf(repeatingAlarm.operation);
        Intent intent = pendingIntent.getSavedIntent();
        assertThat(intent.hasExtra("com.keysd.beaconscannerlib.SCAN_PERIOD"), is(true));
        assertThat(intent.hasExtra("com.keysd.beaconscannerlib.SCAN_INTERVAL"), is(true));
        assertThat(intent.hasExtra("com.keysd.beaconscannerlib.FILTER_UUID"), is(true));

        assertThat(intent.getLongExtra("com.keysd.beaconscannerlib.SCAN_PERIOD", 0L),
                is(expectedPeriod));
        assertThat(intent.getLongExtra("com.keysd.beaconscannerlib.SCAN_INTERVAL", 0L),
                is(expectedInterval));
        assertThat(intent.getByteArrayExtra("com.keysd.beaconscannerlib.FILTER_UUID"),
                is(expectedFilterUUIDData));
    }

    @Test
    public void cancelSHealthSyncAlarm_shouldRemoveAlarm() {
        ScanAlarmManager.startScanAlarm(context, scanParameters);
        ScanAlarmManager.cancelScanAlarm(context);
        ShadowAlarmManager.ScheduledAlarm repeatingAlarm = shadowAlarmManager.getNextScheduledAlarm();
        Assert.assertNull("Alarm scheduled", repeatingAlarm);
    }
}