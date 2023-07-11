package com.purplerock.audiotomidifile.handler

import android.app.Activity
import android.content.Context
import androidx.core.app.ActivityCompat

class PermissionHandler {
    companion object Permissions {
        fun addPermissions(activity: Activity,permissions: Array<String>) {
            ActivityCompat.requestPermissions(activity, permissions, 0)
        }

        fun checkPermission(context: Context, permission: String): Boolean {
            return ActivityCompat.checkSelfPermission(context, permission) == 0
        }
    }
}