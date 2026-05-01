package com.example.classcourse2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "course_reminder_channel";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onReceive(Context context, Intent intent) {
        // 获取课程信息
        long courseId = intent.getLongExtra("course_id", 0);
        String courseName = intent.getStringExtra("course_name");
        String courseLocation = intent.getStringExtra("course_location");
        int remindMinutes = intent.getIntExtra("remind_minutes", 10);
        String startTime = intent.getStringExtra("start_time");

        if (courseName == null || courseLocation == null) {
            return;
        }

        // 创建通知渠道
        createNotificationChannel(context);

        // 创建跳转Intent
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 创建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("📚 上课提醒")
                .setContentText(String.format("%s 即将开始", courseName))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(String.format("课程：%s\n地点：%s\n时间：%s\n提前%d分钟提醒",
                                courseName, courseLocation, startTime, remindMinutes)))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        // 显示通知
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID + (int) courseId, builder.build());

        // 发送广播更新UI
        Intent updateIntent = new Intent("com.example.classcourse2.REMINDER_TRIGGERED");
        updateIntent.putExtra("course_id", courseId);
        context.sendBroadcast(updateIntent);
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "课程提醒";
            String description = "上课前提醒通知";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500});

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }
}