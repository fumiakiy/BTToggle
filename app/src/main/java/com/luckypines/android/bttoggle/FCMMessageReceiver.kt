package com.luckypines.android.bttoggle

import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMMessageReceiver: FirebaseMessagingService() {

  override fun onMessageReceived(p0: RemoteMessage) {
    Log.d(">>>>>", "Incoming ")
    val work = OneTimeWorkRequest.Builder(BTToggleWorker::class.java).build()
    WorkManager.getInstance(this).beginWith(work).enqueue()
  }
}