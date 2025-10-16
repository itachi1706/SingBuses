package com.itachi1706.busarrivalsg

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.itachi1706.busarrivalsg.adapters.BusStopRecyclerAdapter
import com.itachi1706.busarrivalsg.database.BusStopsDb
import com.itachi1706.busarrivalsg.databinding.ActivityListAllBusStopsBinding
import com.itachi1706.helperlib.helpers.EdgeToEdgeHelper

class ListAllBusStopsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityListAllBusStopsBinding.inflate(layoutInflater)
        EdgeToEdgeHelper.setEdgeToEdgeWithContentView(binding.root, this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.rvAllBusStops.setHasFixedSize(true)
        val llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.VERTICAL
        binding.rvAllBusStops.layoutManager = llm
        binding.rvAllBusStops.itemAnimator = DefaultItemAnimator()

        val db = BusStopsDb(this)
        val data = db.getAllBusStops()
        binding.tvCount.text = getString(R.string.label_bus_stops_count, data.size)

        val view = BusStopRecyclerAdapter(data.toMutableList())
        binding.rvAllBusStops.adapter = view
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}