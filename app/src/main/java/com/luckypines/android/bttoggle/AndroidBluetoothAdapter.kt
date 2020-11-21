package com.luckypines.android.bttoggle

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.Context

class AndroidBluetoothAdapter(context: Context) {

  private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

  private var headsetProxy: BluetoothHeadset? = null
  private var a2dpProxy: BluetoothA2dp? = null

  private val headsetProfileListener = object: BluetoothProfile.ServiceListener {
    override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
      if (profile != BluetoothProfile.HEADSET) return
      headsetProxy = proxy as BluetoothHeadset
    }

    override fun onServiceDisconnected(profile: Int) {
      if (profile != BluetoothProfile.HEADSET) return
      headsetProxy = null
    }
  }

  private val a2dpProfileListener = object: BluetoothProfile.ServiceListener {
    override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
      if (profile != BluetoothProfile.A2DP) return
      a2dpProxy = proxy as BluetoothA2dp
    }

    override fun onServiceDisconnected(profile: Int) {
      if (profile != BluetoothProfile.A2DP) return
      a2dpProxy = null
    }
  }

  init {
    bluetoothAdapter.getProfileProxy(context, headsetProfileListener, BluetoothProfile.HEADSET)
    bluetoothAdapter.getProfileProxy(context, a2dpProfileListener, BluetoothProfile.A2DP)
  }

  fun getBondedDevices(): Set<BluetoothDevice> {
    return bluetoothAdapter.bondedDevices
  }

  fun isConnected(macAddress: String): Boolean {
    val device = bluetoothAdapter.getRemoteDevice(macAddress)
    if (device == null) return false
    if (headsetProxy != null) {
      return headsetProxy?.connectedDevices?.findLast { it.address.equals(macAddress) } != null
    } else if (a2dpProxy != null) {
      return a2dpProxy?.connectedDevices?.findLast { it.address.equals(macAddress) } != null
    }
    return false
  }

  fun toggle(macAddress: String) {
    if (isConnected(macAddress)) {
      disconnect(macAddress)
    } else {
      connect(macAddress)
    }
  }

  fun connect(macAddress: String) {
    if (headsetProxy == null) return
    val device = bluetoothAdapter.getRemoteDevice(macAddress)
    if (device != null) {
      val connect = BluetoothHeadset::class.java.declaredMethods.findLast {
        it.name.equals("connect")
      }
      connect?.setAccessible(true)
      connect?.invoke(headsetProxy, device)
    }
  }

  fun disconnect(macAddress: String) {
    if (headsetProxy == null && a2dpProxy == null) return
    val device = bluetoothAdapter.getRemoteDevice(macAddress)
    if (device != null) {
      if (a2dpProxy != null) {
        val disconnect2 = BluetoothA2dp::class.java.declaredMethods.findLast {
          it.name.equals("disconnect")
        }
        disconnect2?.setAccessible(true)
        disconnect2?.invoke(a2dpProxy, device)
      }
      if (headsetProxy != null) {
        val disconnect1 = BluetoothHeadset::class.java.declaredMethods.findLast {
          it.name.equals("disconnect")
        }
        disconnect1?.setAccessible(true)
        disconnect1?.invoke(headsetProxy, device)
      }
    }
  }
}
