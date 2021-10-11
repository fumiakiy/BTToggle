package com.luckypines.android.bttoggle

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.messaging.FirebaseMessagingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class BTToggleWorker(private val appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

  private fun getAddress(): String? {
    val sharedPreferencesAdapter = SharedPreferencesAdapter(appContext.getSharedPreferences(SHARED_PREFERENCES_NAME,
      FirebaseMessagingService.MODE_PRIVATE
    ))
    return sharedPreferencesAdapter.getLastSelectedAddress()
  }

  override fun doWork(): Result {
    val address = getAddress() ?: return Result.failure()
    val bluetoothAdapter = AndroidBluetoothAdapter(appContext)
    Log.d(">>>>>", "Launching ")
    runBlocking(Dispatchers.IO) {
      bluetoothAdapter.onReady()
      val connected = bluetoothAdapter.toggle(address)
      Log.d(">>>>>", "Done " + connected)
    }
    return Result.success()
  }
}