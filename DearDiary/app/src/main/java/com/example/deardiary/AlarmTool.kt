package com.example.deardiary

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.deardiary.Activity.DetailActivity
import com.example.deardiary.data.DiaryDao
import io.realm.Realm
import java.util.*

class AlarmTool : BroadcastReceiver() {

    companion object {
        private const val ACTION_RUN_ALARM = "RUN_ALARM"

        private fun createAlarmIntent(context: Context, id : String) : PendingIntent {
            val intent = Intent(context, AlarmTool::class.java)
            intent.data = Uri.parse("id :" + id)
            intent.putExtra("DIARY_ID", id)
            intent.action = ACTION_RUN_ALARM

            return PendingIntent.getBroadcast(context, 0 , intent, 0)
        }

        fun addAlarm(context: Context, id: String, alarmTime: Date) {
            val alarmIntent = createAlarmIntent(context, id)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime.time, alarmIntent)
        }

        fun deleteAlarm(context: Context, id: String) {
            val alarmIntent = createAlarmIntent(context, id)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(alarmIntent)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action)
        {
            AlarmTool.ACTION_RUN_ALARM -> {
                val diaryId = intent.getStringExtra("DIARY_ID")
                val realm = Realm.getDefaultInstance()
                val diaryData = DiaryDao(realm).selectDiary(diaryId)

                val notificationIntent = Intent(context, DetailActivity::class.java)
                notificationIntent.putExtra("DIARY_ID", diaryId)

                val pendingIntent = PendingIntent.getActivity(context, 0,
                    notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                val builder = NotificationCompat.Builder(context, "alarm")
                    .setContentTitle(diaryData.title)
                    .setContentText(diaryData.content)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    builder.setSmallIcon(R.drawable.ic_launcher_foreground)
                    val channel = NotificationChannel("alarm", "알람 메시지",
                        NotificationManager.IMPORTANCE_HIGH)

                    notificationManager.createNotificationChannel(channel)
                }
                else {
                    builder.setSmallIcon(R.mipmap.ic_launcher)
                }

                notificationManager.notify(1, builder.build())
            }
            Intent.ACTION_BOOT_COMPLETED ->
            {
                val realm = Realm.getDefaultInstance()
                val activeAlarms = DiaryDao(realm).getActiveAlarms()

                for(DiaryData in activeAlarms) {
                    addAlarm(context, DiaryData.id, DiaryData.alarmTime)
                }
            }
        }

    }
}