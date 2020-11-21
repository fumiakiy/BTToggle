package com.luckypines.android.bttoggle

import android.bluetooth.BluetoothDevice

data class SelectableBluetoothDevice(val device: BluetoothDevice, var isSelected: Boolean = false) {
  val name = device.name
  val address = device.address
}