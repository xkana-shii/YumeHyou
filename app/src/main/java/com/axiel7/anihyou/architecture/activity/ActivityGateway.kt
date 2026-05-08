package com.axiel7.anihyou.architecture.activity

import com.axiel7.anihyou.core.domain.repository.ActivityRepository
import com.axiel7.anihyou.core.domain.repository.NotificationRepository
import org.koin.dsl.module

interface ActivityGateway {
    val activityRepository: ActivityRepository
    val notificationRepository: NotificationRepository
}

class AniListActivityGateway(
    override val activityRepository: ActivityRepository,
    override val notificationRepository: NotificationRepository,
) : ActivityGateway

val activityModule = module {
    single<ActivityGateway> {
        AniListActivityGateway(
            activityRepository = get(),
            notificationRepository = get(),
        )
    }
}
