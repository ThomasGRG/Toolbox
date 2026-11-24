package jp.ikigai.toolbox.data

import jp.ikigai.toolbox.enums.LogMessage
import jp.ikigai.toolbox.enums.ServiceType
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

data class SunlightSchedule(
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int
) {
    fun getTimeRangeMillis(): Pair<Long, Long> {
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

    fun isTimeBetween(): Boolean {
        val now = LocalTime.now()

        val start = LocalTime.of(startHour, startMinute)
        val end = LocalTime.of(endHour, endMinute)

        return if (start <= end) {
            if (now in start..end) {
                ToolboxServiceState.log(
                    serviceType = ServiceType.SUNLIGHT_MON,
                    logMessage = LogMessage.TIME_BETWEEN,
                )
                true
            } else false
        } else {
            if (now >= start || now <= end) {
                ToolboxServiceState.log(
                    serviceType = ServiceType.SUNLIGHT_MON,
                    logMessage = LogMessage.TIME_BETWEEN,
                )
                true
            } else false
        }
    }
}
