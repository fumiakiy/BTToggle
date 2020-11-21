package com.luckypines.android.bttoggle

data class BluetoothDevice(val name: String, val address: String)
data class SelectableBluetoothDevice(val name: String, val address: String, var isSelected: Boolean = false)