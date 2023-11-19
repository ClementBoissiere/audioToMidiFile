package com.purplerock.audiotomidifile.handler

import android.app.Activity
import androidx.core.app.ActivityCompat

class PermissionHandler {
    companion object Permissions {
        fun addPermissions(activity: Activity,permissions: Array<String>) {
            ActivityCompat.requestPermissions(activity, permissions, 0)
        }
    }
}