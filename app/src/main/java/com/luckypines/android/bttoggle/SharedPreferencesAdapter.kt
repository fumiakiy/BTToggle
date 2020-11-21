package com.luckypines.android.bttoggle

import android.content.SharedPreferences

const val LAST_SELECTED_ADDRESS = "last_selected_address"
class SharedPreferencesAdapter(private val sharedPreferences: SharedPreferences) {
  fun getLastSelectedAddress(): String? = sharedPreferences.getString(LAST_SELECTED_ADDRESS, null)

  fun setLastSelectedAddress(address: String) {
    sharedPreferences.edit().putString(LAST_SELECTED_ADDRESS, address).apply()
  }
}