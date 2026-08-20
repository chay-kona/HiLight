package com.hilight.control.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.hilight.control.data.RuleStore
import com.hilight.control.hardware.HiLightControllerProvider

class HiLightNotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val rule = RuleStore(this).getRule(sbn.packageName) ?: return
        if (!rule.enabled) return

        HiLightControllerProvider.get(this).play(rule)
    }
}
