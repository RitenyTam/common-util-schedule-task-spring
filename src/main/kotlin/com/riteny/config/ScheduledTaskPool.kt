package com.riteny.config

import com.riteny.entity.ScheduleTask
import com.riteny.exception.ScheduleTaskException
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import java.util.concurrent.ConcurrentHashMap


object ScheduledTaskPool {

    val scheduleTaskMap: MutableMap<String, ScheduleTask> = ConcurrentHashMap()

    fun addTask(task: ScheduleTask) {

        if (scheduleTaskMap[task.name] != null) {
            throw ScheduleTaskException("Task [${task.name}] already exists]")
        }

        scheduleTaskMap[task.name] = task
    }
}