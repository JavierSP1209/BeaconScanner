package com.keysd.beaconscannerlib.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.keysd.beaconscannerlib.BLeScanService;
import com.keysd.beaconscannerlib.receiver.BLeStartScanBroadcastReceiver;

public class ScanAlarmManager {
  public static void startScanAlarm(Context context) {
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    Intent queryIntent = new Intent(context, BLeStartScanBroadcastReceiver.class);
    PendingIntent pendingQueryIntent = PendingIntent.getBroadcast(context, 0, queryIntent,
        PendingIntent.FLAG_UPDATE_CURRENT);

    // schedule the intent for future delivery
    alarmManager.set(AlarmManager.RTC,
        System.currentTimeMillis() + Constants.DEFAULT_BLE_SCAN_INTERVAL_MS, pendingQueryIntent);
  }

  public static void cancelScanAlarm(Context context) {
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    Intent queryIntent = new Intent(context, BLeStartScanBroadcastReceiver.class);
    PendingIntent pendingQueryIntent = PendingIntent.getBroadcast(context, 0, queryIntent,
        PendingIntent.FLAG_UPDATE_CURRENT);
    alarmManager.cancel(pendingQueryIntent);
  }
}
