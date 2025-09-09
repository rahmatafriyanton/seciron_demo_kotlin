package com.seciron.secirondemo.security

import android.app.Notification
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class CallNotificationListener : NotificationListenerService() {

    private val voipApps = setOf(
        "com.whatsapp", "com.whatsapp.w4b",
        "org.telegram.messenger",
        "com.skype.raider",
        "com.viber.voip",
        "com.facebook.orca",
        "us.zoom.videomeetings",
        "com.google.android.apps.meet",
        "com.microsoft.teams"
    )

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val nf = sbn.notification ?: return
        if (sbn.packageName in voipApps &&
            (nf.category == Notification.CATEGORY_CALL ||
                    nf.fullScreenIntent != null ||
                    (Build.VERSION.SDK_INT >= 31 && nf.fullScreenIntent != null))
        ) {
            sendBroadcast(Intent("SECIRON_VOIP_CALL").putExtra("active", true))
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        if (sbn.packageName in voipApps) {
            sendBroadcast(Intent("SECIRON_VOIP_CALL").putExtra("active", false))
        }
    }
}
