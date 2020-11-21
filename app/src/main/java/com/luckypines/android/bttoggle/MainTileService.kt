package com.luckypines.android.bttoggle

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class MainTileService: TileService() {

  private var coroutineJob: Job? = null

  private fun getAddress(): String? {
    val sharedPreferencesAdapter = SharedPreferencesAdapter(getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE))
    return sharedPreferencesAdapter.getLastSelectedAddress()
  }

  override fun onStartListening() {
    super.onStartListening()
    if (coroutineJob != null) coroutineJob?.cancel()
    coroutineJob = Job()
    val coroutineContext = Dispatchers.IO + coroutineJob!!

    val address = getAddress() ?: return
    val bluetoothAdapter = AndroidBluetoothAdapter(this)

    CoroutineScope(coroutineContext).launch {
      bluetoothAdapter.onReady()
      withContext(Dispatchers.Main) {
        val tile = qsTile
        tile.state = if (bluetoothAdapter.isConnected(address)) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
        bluetoothAdapter.release()
      }
    }
  }

  override fun onStopListening() {
    super.onStopListening()
    coroutineJob?.cancel()
  }

  override fun onClick() {
    if (coroutineJob != null) coroutineJob?.cancel()
    coroutineJob = Job()
    val coroutineContext = Dispatchers.IO + coroutineJob!!

    val address = getAddress() ?: return
    val bluetoothAdapter = AndroidBluetoothAdapter(this)
    CoroutineScope(coroutineContext).launch {
      bluetoothAdapter.onReady()
      val connected = bluetoothAdapter.toggle(address)
      withContext(Dispatchers.Main) {
        val tile = qsTile
        tile.state = if (connected) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
        bluetoothAdapter.release()
      }
    }
  }
}