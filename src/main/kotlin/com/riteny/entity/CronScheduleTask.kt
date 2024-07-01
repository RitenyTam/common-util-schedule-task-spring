package com.riteny.entity

import java.util.*

class CronScheduleTask(
    name: String,
    isCancel: Boolean,
    createdTime: Date,
    val cron: String,
    val execute: () -> Unit
) : ScheduleTask(name, isCancel, createdTime), Runnable {

    constructor(name: String, cron: String, execute: () -> Unit)
            : this(name, false, Date(), cron, execute)

    override fun run() = execute()
}