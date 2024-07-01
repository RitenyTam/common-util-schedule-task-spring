package com.riteny.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler

/**
 * @author Riteny
 *
 *
 * 啓動類，導入該包時，自動初始化相關配置和對應的服務類
 * 2021/9/13  11:22
 */
@Configuration
open class ScheduleTaskConfiguration {

    @Bean
    open fun threadPoolTaskScheduler(): ThreadPoolTaskScheduler {
        return ThreadPoolTaskScheduler()
    }
}
