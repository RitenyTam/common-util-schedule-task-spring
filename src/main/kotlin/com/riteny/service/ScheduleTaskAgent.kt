package com.riteny.service

import com.riteny.config.ScheduledTaskPool
import com.riteny.config.ScheduledTaskPool.addTask
import com.riteny.entity.*
import com.riteny.exception.ScheduleTaskException
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.support.CronTrigger


class ScheduleTaskAgent(private val threadPoolTaskScheduler: ThreadPoolTaskScheduler) {

    /**
     * 查詢所有cron表達式的定時任務視圖對象
     *
     * @return 定時任務視圖對象
     */
    fun searchCronScheduleTask(): ArrayList<CronScheduleTaskViewEntity> {

        val viewEntities = ArrayList<CronScheduleTaskViewEntity>()

        val scheduleTaskMap = ScheduledTaskPool.scheduleTaskMap.filter { (_, v) -> v is CronScheduleTask }

        scheduleTaskMap.forEach { (_, task) ->

            val cronTask = task as CronScheduleTask

            viewEntities.add(
                CronScheduleTaskViewEntity(
                    cronTask.name,
                    cronTask.cron,
                    cronTask.isCancel,
                    cronTask.createdTime.time
                )
            )
        }

        return viewEntities
    }

    /**
     * 查詢所有固定延遲執行的定時任務視圖對象
     *
     * @return 定時任務視圖對象
     */
    fun searchFixedDelayScheduleTask(): ArrayList<FixedDelayScheduleTaskViewEntity> {

        val viewEntities = ArrayList<FixedDelayScheduleTaskViewEntity>()

        val scheduleTaskMap = ScheduledTaskPool.scheduleTaskMap.filter { (_, v) -> v is FixedDelayScheduleTask }

        scheduleTaskMap.forEach { (_, task) ->

            val fixedDelayTask = task as FixedDelayScheduleTask

            viewEntities.add(
                FixedDelayScheduleTaskViewEntity(
                    fixedDelayTask.name,
                    fixedDelayTask.fixedRate,
                    fixedDelayTask.isCancel,
                    fixedDelayTask.createdTime.time
                )
            )
        }

        return viewEntities
    }


    /**
     * 查詢所有定時循環執行的定時任務視圖對象
     *
     * @return 定時任務視圖對象
     */
    fun searchFixedRateScheduleTask(): ArrayList<FixedRateScheduleTaskViewEntity> {

        val viewEntities = ArrayList<FixedRateScheduleTaskViewEntity>()

        val scheduleTaskMap = ScheduledTaskPool.scheduleTaskMap.filter { (_, v) -> v is FixedRateScheduleTask }

        scheduleTaskMap.forEach { (_, task) ->

            val fixedRateTask = task as FixedRateScheduleTask

            viewEntities.add(
                FixedRateScheduleTaskViewEntity(
                    fixedRateTask.name,
                    fixedRateTask.fixedRate,
                    fixedRateTask.isCancel,
                    fixedRateTask.createdTime.time
                )
            )
        }

        return viewEntities
    }

    /**
     * 提交一個根據cron表達式定時執行的任務
     *
     * @param name 任務名稱
     * @param cronExp cron 表達式
     * @param execute 定時任務執行的具體内容
     */
    fun addCronScheduleTask(name: String, cronExp: String, execute: () -> Unit) =
        CronScheduleTask(name, cronExp, execute).let {
            threadPoolTaskScheduler.poolSize += 1
            val future = threadPoolTaskScheduler.schedule(it, CronTrigger(it.cron))
            it.scheduledFuture = future
            addTask(it)
        }


    /**
     * 提交一個周期内循環執行的定時任務
     *
     * @param name 任務名稱
     * @param fixedRate 執行的周期
     * @param timeUnit 周期設定數字的時間單位
     * @param execute 定時任務執行的具體内容
     */
    fun addFixedRateScheduleTask(name: String, fixedRate: Long, execute: () -> Unit) =
        FixedRateScheduleTask(name, fixedRate, execute).let {
            with(ScheduledTaskPool) {
                threadPoolTaskScheduler.poolSize += 1
                it.scheduledFuture = threadPoolTaskScheduler.scheduleAtFixedRate(it, fixedRate)
                addTask(it)
            }
        }

    /**
     * 提交一個周期内循環執行的定時任務
     * 該任務執行完成后的{fixedRate}時間后執行下次任務
     *
     * @param name 任務名稱
     * @param fixedRate 執行的周期
     * @param timeUnit 周期設定數字的時間單位
     * @param execute 定時任務執行的具體内容
     */
    fun addFixedDelayScheduleTask(name: String, fixedRate: Long, execute: () -> Unit) =
        FixedDelayScheduleTask(name, fixedRate, execute).let {
            with(ScheduledTaskPool) {
                threadPoolTaskScheduler.poolSize += 1
                it.scheduledFuture = threadPoolTaskScheduler.scheduleWithFixedDelay(it, fixedRate)
                addTask(it)
            }
        }

    /**
     * 取消任務，任務取消后，留存在任務列表中
     *
     * @param name 任務名稱
     */
    fun cancelTask(name: String) =
        with(ScheduledTaskPool.scheduleTaskMap[name] ?: throw ScheduleTaskException("Task [$name] not found .")) {
            val scheduledFuture = scheduledFuture ?: throw ScheduleTaskException("Incorrect task $name configuration .")
            isCancel = scheduledFuture.cancel(false)
            if (isCancel) {
                threadPoolTaskScheduler.poolSize -= 1
            }
        }

    /**
     * 刪除任務
     * 該任務必須處於已經取消的狀態
     *
     * @param name 任務名稱
     */
    fun removeTask(name: String) =
        with(ScheduledTaskPool.scheduleTaskMap[name] ?: throw ScheduleTaskException("Task [$name] not found .")) {
            if (isCancel) {
                ScheduledTaskPool.scheduleTaskMap.remove(name)
            } else {
                throw throw ScheduleTaskException("Task $name is still active .")
            }
        }

    /**
     * 按照之前的配置參數重新啓動任務
     * 該任務必須還留存在任務列表
     *
     * @param name 任務名稱
     */
    fun restartTask(name: String) {

        val task = ScheduledTaskPool.scheduleTaskMap[name] ?: throw ScheduleTaskException("Task [$name] not found .")
        ScheduledTaskPool.scheduleTaskMap.remove(name)

        when (task) {
            is CronScheduleTask -> {
                addCronScheduleTask(name, task.cron, task.execute)
            }

            is FixedRateScheduleTask -> {
                addFixedRateScheduleTask(name, task.fixedRate, task.execute)
            }

            is FixedDelayScheduleTask -> {
                addFixedDelayScheduleTask(name, task.fixedRate, task.execute)
            }
        }
    }
}

fun scheduleTaskAgent(scheduleTaskAgent: ScheduleTaskAgent, block: ScheduleTaskAgent.() -> Unit): ScheduleTaskAgent =
    scheduleTaskAgent.apply(block)