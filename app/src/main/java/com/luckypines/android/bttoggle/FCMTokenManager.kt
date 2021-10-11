package com.luckypines.android.bttoggle

import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FCMTokenManager {
  suspend fun getToken() = suspendCancellableCoroutine<String?> {
    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
      if (!task.isSuccessful) {
        Log.w(">>>>", "Fetching FCM registration token failed", task.exception)
        it.resume(null)
        return@OnCompleteListener
      }

      // Get new FCM registration token
      val token = task.result
      it.resume((token))
    })
  }
}