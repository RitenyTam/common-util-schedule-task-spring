package com.riteny.entity

import java.util.*
import java.util.concurrent.ScheduledFuture

open class ScheduleTask(
    val name: String,
    var isCancel: Boolean,
    val createdTime: Date,
    var scheduledFuture: ScheduledFuture<*>? = null
)