package com.seciron.secirondemo

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.seciron.secirondemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var telephonyManager: TelephonyManager
    private var callStateCallback: TelephonyCallback? = null
    private var securityDialogShowing = false

    // ✅ Launcher untuk permission
    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach { entry ->
                val perm = entry.key
                val granted = entry.value
                if (granted) {
                    println("✅ Permission granted: $perm")
                } else {
                    println("❌ Permission denied: $perm")
                }
            }
        }

    private fun initPermissions() {
        val dangerousPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.POST_NOTIFICATIONS // Android 13+
        )

        val toRequest = dangerousPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (toRequest.isNotEmpty()) {
            requestPermissionsLauncher.launch(toRequest.toTypedArray())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        initPermissions()

        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
    }

    // ===================== Listener Seluler =====================
    override fun onStart() {
        super.onStart()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                callStateCallback = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                    override fun onCallStateChanged(state: Int) {
                        handleCallState(state)
                    }
                }
                telephonyManager.registerTelephonyCallback(mainExecutor, callStateCallback!!)
            } else {
                @Suppress("DEPRECATION")
                telephonyManager.listen(object : PhoneStateListener() {
                    override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                        handleCallState(state)
                    }
                }, PhoneStateListener.LISTEN_CALL_STATE)
            }
        }

        // ✅ Receiver VoIP (WA/Zoom/Telegram)
        registerReceiver(voipReceiver, IntentFilter("SECIRON_VOIP_CALL"), RECEIVER_EXPORTED)
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            callStateCallback?.let { telephonyManager.unregisterTelephonyCallback(it) }
        } else {
            @Suppress("DEPRECATION")
            telephonyManager.listen(null, PhoneStateListener.LISTEN_NONE)
        }
        unregisterReceiver(voipReceiver)
    }

    private fun handleCallState(state: Int) {
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                showSecurityPopupIfNeeded("Security Notice", "A call is in progress. For your security, sensitive features may be limited.")
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                showSecurityPopupIfNeeded("Security Notice", "A call is in progress. For your security, sensitive features may be limited.")
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                securityDialogShowing = false
            }
        }
    }

    // ===================== Listener VoIP =====================
    private val voipReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val active = intent?.getBooleanExtra("active", false) ?: false
            if (active) {
                showSecurityPopupIfNeeded("Security Notice", "A call is in progress. For your security, sensitive features may be limited.")
            } else {
                securityDialogShowing = false
            }
        }
    }


    // ===================== Pop-up Security =====================
    private fun showSecurityPopupIfNeeded(title: String, message: String) {
        if (securityDialogShowing || isFinishing || isDestroyed) return
        securityDialogShowing = true

        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { d, _ -> d.dismiss() }
            .show()
    }
}
