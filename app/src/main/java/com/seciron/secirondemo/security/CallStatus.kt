package com.seciron.secirondemo.security


import android.content.Context
import android.media.AudioManager
import android.telephony.TelephonyManager

/**
 * Utility untuk mengecek apakah sedang ada panggilan aktif,
 * baik itu panggilan seluler ataupun VoIP (WhatsApp, Telegram, dll).
 */
object CallStatus {
    @Volatile
    private var voipFromNotifActive: Boolean = false

    // Dipanggil dari NotificationListener untuk update status panggilan VoIP
    internal fun setVoipActive(active: Boolean) {
        voipFromNotifActive = active
    }

    /**
     * @return true jika ada panggilan yang sedang berlangsung
     */
    fun isAnyCallActive(context: Context): Boolean {
        // 1. Panggilan seluler (GSM/VoLTE)
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val cellularActive = tm.callState == TelephonyManager.CALL_STATE_OFFHOOK

        // 2. Audio mode komunikasi → indikasi ada voice chat / VoIP
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val voipByAudioMode =
            am.mode == AudioManager.MODE_IN_COMMUNICATION || am.mode == AudioManager.MODE_RINGTONE

        // 3. Status dari NotificationListener
        val voipByNotif = voipFromNotifActive

        return cellularActive || voipByAudioMode || voipByNotif
    }
}
