package com.luckypines.android.bttoggle

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

  private val seletedAddress = "04:5D:4B:66:BF:E1"

  private lateinit var bluetoothAdapter: AndroidBluetoothAdapter
  private lateinit var listAdapter: DeviceViewAdapter
  private lateinit var itemTouchListener: ItemTouchListener
  private lateinit var devices: List<SelectableBluetoothDevice>
  private          var itemSelectJob: Job? = null
  private          var listUpdatedJob: Job? = null
  private          var selectedIndex: Int = -1

  private fun initDevices(devices: Set<BluetoothDevice>, selectedAddress: String): Pair<Int, List<SelectableBluetoothDevice>> {
    val selectables = mutableListOf<SelectableBluetoothDevice>()
    var selectedIndex = -1
    devices.forEachIndexed { index, device ->
      selectables.add(SelectableBluetoothDevice(device, device.address.equals(selectedAddress)))
      if (device.address.equals(selectedAddress)) selectedIndex = index
    }
    return Pair(selectedIndex, selectables.toList())
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    bluetoothAdapter = AndroidBluetoothAdapter(this)
    val pair = initDevices(bluetoothAdapter.getBondedDevices(), seletedAddress)
    selectedIndex = pair.first
    devices = pair.second

    val list = findViewById<RecyclerView>(R.id.devicesList)
    list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    listAdapter = DeviceViewAdapter(devices)
    list.adapter = listAdapter

    val gestureDetector = GestureDetector(this, object : SimpleOnGestureListener() {
      override fun onSingleTapUp(e: MotionEvent): Boolean = true
    })
    itemTouchListener = ItemTouchListener(gestureDetector)
    list.addOnItemTouchListener(itemTouchListener)

    val button = findViewById<Button>(R.id.toggleButton)
    button.setOnClickListener {
      val device = devices[selectedIndex]
      bluetoothAdapter.toggle(device.address)
    }
  }

  override fun onResume() {
    super.onResume()
    itemSelectJob = CoroutineScope(Dispatchers.Main).launch {
      itemTouchListener.selectedIndex.collect { pos ->
        if (pos < 0) return@collect
        if (selectedIndex >= 0) {
          devices[selectedIndex].isSelected = !devices[selectedIndex].isSelected
          listAdapter.notifyItemChanged(selectedIndex)
        }
        selectedIndex = pos
        devices[pos].isSelected = !devices[pos].isSelected
        listAdapter.notifyItemChanged(pos)
      }
    }
    listUpdatedJob = CoroutineScope(Dispatchers.Main).launch {

    }
  }

  override fun onPause() {
    super.onPause()
    itemSelectJob?.cancel()
    listUpdatedJob?.cancel()
  }

  class ItemTouchListener(val gestureDetector: GestureDetector) : RecyclerView.SimpleOnItemTouchListener() {
    private val _selectedIndex = MutableStateFlow<Int>(-1)
    val selectedIndex = _selectedIndex.asStateFlow()

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
      val view = rv.findChildViewUnder(e.x, e.y)
      if (view != null && gestureDetector.onTouchEvent(e)) {
        val index = rv.getChildAdapterPosition(view)
        _selectedIndex.value = index
        return true
      }

      return super.onInterceptTouchEvent(rv, e)
    }
  }
}

class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
  val nameText: TextView = view.findViewById(R.id.nameText)
  val addressText: TextView = view.findViewById(R.id.addressText)
}

class DeviceViewAdapter(val devices: List<SelectableBluetoothDevice>) : RecyclerView.Adapter<DeviceViewHolder>() {
  companion object {
    const val DEFAULT = 0
    const val SELECTED = 1
  }
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
    val layout =
      if (viewType == SELECTED)
        R.layout.list_item_bt_device_selected
      else
        R.layout.list_item_bt_device
    val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
    return DeviceViewHolder(view)
  }

  override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
    val device = devices.get(position)
    holder.nameText.text = device.name
    holder.addressText.text = device.address
  }

  override fun getItemCount(): Int = devices.size

  override fun getItemViewType(position: Int): Int {
    val device = devices[position]
    return if (device.isSelected) SELECTED else DEFAULT
  }
}