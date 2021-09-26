package com.livelocationtracker

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.livelocationtracker.utils.TrackingUtility
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private val REQUEST_CODE_LOCATION = 100


    private val POWERMANAGER_INTENTS = arrayOf(
        Intent().setComponent(
            ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.letv.android.letvsafe",
                "com.letv.android.letvsafe.AutobootManageActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.optimize.process.ProtectActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.coloros.safecenter",
                "com.coloros.safecenter.permission.startup.StartupAppListActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.coloros.safecenter",
                "com.coloros.safecenter.startupapp.StartupAppListActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.oppo.safe",
                "com.oppo.safe.permission.startup.StartupAppListActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.iqoo.secure",
                "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.iqoo.secure",
                "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.vivo.permissionmanager",
                "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.samsung.android.lool",
                "com.samsung.android.sm.battery.ui.BatteryActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.samsung.android.lool",
                "com.samsung.android.sm.ui.battery.BatteryActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.htc.pitroad",
                "com.htc.pitroad.landingpage.activity.LandingPageActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.asus.mobilemanager",
                "com.asus.mobilemanager.MainActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.transsion.phonemanager",
                "com.itel.autobootmanager.activity.AutoBootMgrActivity"
            )
        )
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        for (intent in POWERMANAGER_INTENTS) if (packageManager.resolveActivity(
//                intent!!,
//                PackageManager.MATCH_DEFAULT_ONLY
//            ) != null
//        ) {
//            startActivity(intent)
//            break
//        }
        requestPermissions()
    }

    private fun requestPermissions() {
        if (TrackingUtility.hasLocationPermissions(this)) {
            sendCommandService()
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app",
                REQUEST_CODE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

        } else {

            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app",
                REQUEST_CODE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )

        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        sendCommandService()
    }


    private fun sendCommandService() =
        Intent(this, LocationTrackerService::class.java).also {
            this.startService(it)
        }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)

    }
}