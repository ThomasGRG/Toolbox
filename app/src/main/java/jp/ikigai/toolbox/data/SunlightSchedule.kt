package jp.ikigai.toolbox.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import jp.ikigai.toolbox.enums.LogMessage
import jp.ikigai.toolbox.enums.ServiceType
import jp.ikigai.toolbox.receivers.AlarmReceiver
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

data class SunlightSchedule(
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int
)

fun SunlightSchedule.setupAlarms(context: Context) {
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    if (!alarmManager.canScheduleExactAlarms()) return

    val startIntent = Intent(context, AlarmReceiver::class.java)
    val startPendingIntent = PendingIntent.getBroadcast(
        context,
        92160,
        startIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    val stopIntent = Intent(context, AlarmReceiver::class.java)
    val stopPendingIntent = PendingIntent.getBroadcast(
        context,
        36848,
        stopIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    alarmManager.cancel(startPendingIntent)
    alarmManager.cancel(stopPendingIntent)

    val (startMillis, endMillis) = getTimeRangeMillis()

    alarmManager.set(
        AlarmManager.RTC_WAKEUP,
        startMillis,
        startPendingIntent,
    )
    alarmManager.setExact(
        AlarmManager.RTC_WAKEUP,
        endMillis,
        stopPendingIntent,
    )

    AppModule.serviceLogDao.insert(
        serviceType = ServiceType.SUNLIGHT_MON,
        logMessage = LogMessage.ALARMS_SETUP,
    )
}

fun SunlightSchedule.getTimeRangeMillis(): Pair<Long, Long> {
    val now = LocalDateTime.now()
    val startTime = LocalTime.of(startHour, startMinute)
    val endTime = LocalTime.of(endHour, endMinute)

    var startDate = now.toLocalDate()

    var start = startDate.atTime(startTime)
    if (start <= now) {
        startDate = startDate.plusDays(1)
        start = startDate.atTime(startTime)
    }

    val endDate = if (endTime <= startTime) {
        startDate.plusDays(1)
    } else {
        startDate
    }

    val end = endDate.atTime(endTime)

    val zone = ZoneId.systemDefault()

    return Pair(
        start.atZone(zone).toInstant().toEpochMilli(),
        end.atZone(zone).toInstant().toEpochMilli()
    )
}

fun SunlightSchedule.isTimeBetween(): Boolean {
    val now = LocalTime.now()

    val start = LocalTime.of(startHour, startMinute)
    val end = LocalTime.of(endHour, endMinute)

    return if (start <= end) {
        if (now in start..end) {
            AppModule.serviceLogDao.insert(
                serviceType = ServiceType.SUNLIGHT_MON,
                logMessage = LogMessage.TIME_BETWEEN,
            )
            true
        } else false
    } else {
        if (now >= start || now <= end) {
            AppModule.serviceLogDao.insert(
                serviceType = ServiceType.SUNLIGHT_MON,
                logMessage = LogMessage.TIME_BETWEEN,
            )
            true
        } else false
    }
}
