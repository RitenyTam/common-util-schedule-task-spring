package com.riteny.entity

import java.util.*
import java.util.concurrent.TimeUnit

class FixedDelayScheduleTask(
    name: String, isCancel: Boolean, createdTime: Date, val fixedRate: Long, val execute: () -> Unit
) : ScheduleTask(name, isCancel, createdTime), Runnable {

    constructor(name: String, fixedRate: Long, execute: () -> Unit)
            : this(name, false, Date(), fixedRate, execute)

    override fun run() = execute()
}