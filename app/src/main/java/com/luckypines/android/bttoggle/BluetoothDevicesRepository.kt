package com.luckypines.android.bttoggle

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

class BluetoothDevicesRepository(bluetoothAdapter: AndroidBluetoothAdapter) {
  private val devices = bluetoothAdapter.getBondedDevices().map {
    BluetoothDevice(it.name, it.address)
  }

  // Meaningless suspend is intentional for my learning the use of it
  suspend fun getDevices(): List<BluetoothDevice> = coroutineScope {
    val d = async { devices }
    d.await()
  }
}