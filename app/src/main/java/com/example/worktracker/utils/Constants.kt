package com.example.worktracker.utils

import com.google.firebase.auth.FirebaseAuth

object Constants{
    const val PACKAGE_NAME = "com.example.soluemergencias"
    internal const val ACTION_LOCATION_BROADCAST = "$PACKAGE_NAME.action.LOCATION_BROADCAST"
    internal const val EXTRA_LOCATION = "$PACKAGE_NAME.extra.LOCATION"
    const val NOTIFICATION_CHANNEL_ID = "localizacion"
    val firebaseAuth = FirebaseAuth.getInstance()
}
