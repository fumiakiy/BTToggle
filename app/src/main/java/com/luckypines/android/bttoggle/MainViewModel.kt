package com.luckypines.android.bttoggle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
  private val btRepo: BluetoothDevicesRepository,
  private val defaultAddress: String?
): ViewModel() {
  private val devices = mutableListOf<BluetoothDevice>()

  private val _previousIndex = MutableStateFlow(-1)
  val previousIndex = _previousIndex.asStateFlow()
  private val _selectedIndex = MutableStateFlow(-1)
  val selectedIndex = _selectedIndex.asStateFlow()

  init {
    viewModelScope.launch {
      val _devices = btRepo.getDevices()
      devices.addAll(_devices)
      if (defaultAddress == null) return@launch
      _devices.forEachIndexed { index, device ->
        if (device.address.equals(defaultAddress)) {
          select(index)
          return@forEachIndexed
        }
      }
    }
  }

  fun getDevice(index: Int) = devices[index]

  fun getSize() = devices.size

  fun isSelected(index: Int) = selectedIndex.value == index

  fun select(index: Int) {
    _previousIndex.value = selectedIndex.value
    _selectedIndex.value = index
  }
}

class MainViewModelFactory(
  private val btRepo: BluetoothDevicesRepository,
  private val defaultAddress: String?
): ViewModelProvider.Factory {
  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    @Suppress("UNCHECKED_CAST")
    return MainViewModel(btRepo, defaultAddress) as T
  }
}
