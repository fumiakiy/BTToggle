package com.luckypines.android.bttoggle

import android.os.Bundle
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
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

  private lateinit var bluetoothAdapter: AndroidBluetoothAdapter
  private lateinit var sharedPreferencesAdapter: SharedPreferencesAdapter
  private lateinit var listAdapter: DeviceViewAdapter
  private lateinit var itemTouchListener: ItemTouchListener
  private          var itemSelectJob: Job? = null
  private          var previousIndexJob: Job? = null
  private          var selectedIndexJob: Job? = null

  private lateinit var viewModel: MainViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    bluetoothAdapter = AndroidBluetoothAdapter(this)
    val btRepo = BluetoothDevicesRepository(bluetoothAdapter)
    sharedPreferencesAdapter = SharedPreferencesAdapter(getPreferences(MODE_PRIVATE))
    val selectedAddress = sharedPreferencesAdapter.getLastSelectedAddress()
    viewModel = ViewModelProvider(this, MainViewModelFactory(btRepo, selectedAddress))
      .get(MainViewModel::class.java)

    val list = findViewById<RecyclerView>(R.id.devicesList)
    list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    listAdapter = DeviceViewAdapter(viewModel)
    list.adapter = listAdapter

    val gestureDetector = GestureDetector(this, object : SimpleOnGestureListener() {
      override fun onSingleTapUp(e: MotionEvent): Boolean = true
    })
    itemTouchListener = ItemTouchListener(gestureDetector)
    list.addOnItemTouchListener(itemTouchListener)

    val button = findViewById<Button>(R.id.toggleButton)
    button.setOnClickListener {
      val device = viewModel.getDevice(viewModel.selectedIndex.value)
      bluetoothAdapter.toggle(device.address)
    }
  }

  override fun onResume() {
    super.onResume()
    itemSelectJob = CoroutineScope(Dispatchers.Main).launch {
      itemTouchListener.selectedIndex.collect { index ->
        if (index < 0) return@collect
        viewModel.select(index)
      }
    }
    selectedIndexJob = CoroutineScope(Dispatchers.Main).launch {
      viewModel.selectedIndex.collect { index ->
        if (index < 0) return@collect
        listAdapter.notifyItemChanged(index)
        sharedPreferencesAdapter.setLastSelectedAddress(viewModel.getDevice(index).address)
      }
    }
    previousIndexJob = CoroutineScope(Dispatchers.Main).launch {
      viewModel.previousIndex.collect { index ->
        if (index < 0) return@collect
        listAdapter.notifyItemChanged(index)
      }
    }
  }

  override fun onPause() {
    super.onPause()
    itemSelectJob?.cancel()
    selectedIndexJob?.cancel()
    previousIndexJob?.cancel()
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

class DeviceViewAdapter(private val viewModel: MainViewModel): RecyclerView.Adapter<DeviceViewHolder>() {
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
    val device = viewModel.getDevice(position)
    holder.nameText.text = device.name
    holder.addressText.text = device.address
  }

  override fun getItemCount(): Int = viewModel.getSize()

  override fun getItemViewType(position: Int): Int {
    return if (viewModel.isSelected(position)) SELECTED else DEFAULT
  }
}