package com.itachi1706.busarrivalsg.fragments

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.itachi1706.busarrivalsg.database.BusStopsDb
import com.itachi1706.busarrivalsg.databinding.FragmentBusStopsSearchBinding
import com.itachi1706.busarrivalsg.objects.gson.ltasg.BusStopJSON
import com.itachi1706.busarrivalsg.recyclerviews.BusStopRecyclerAdapter
import com.itachi1706.helperlib.helpers.LogHelper.d
import com.itachi1706.helperlib.helpers.LogHelper.e

class BusStopsSearchFragment : Fragment() {

    companion object {
        private const val TAG = "BSSearchF"
    }

    private var binding: FragmentBusStopsSearchBinding? = null
    private var adapter: BusStopRecyclerAdapter? = null
    private var db: BusStopsDb? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBusStopsSearchBinding.inflate(inflater, container, false)
        val v = binding?.root

        if (activity == null) {
            e(TAG, "No activity found")
            return v
        }

        binding?.rvNearestBusStops?.setHasFixedSize(true)
        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL
        binding?.rvNearestBusStops?.layoutManager = llm
        binding?.rvNearestBusStops?.itemAnimator = DefaultItemAnimator()

        adapter = BusStopRecyclerAdapter(mutableListOf<BusStopJSON>())
        binding?.rvNearestBusStops?.adapter = adapter

        // Blank population
        db = BusStopsDb(requireContext())
        val results = db?.getAllBusStops()
        adapter?.updateAdapter(results)
        adapter?.notifyItemRangeChanged(0, results?.size ?: 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity?.window?.decorView?.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
        }

        val inputWatcher = object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No action needed
            }

            override fun afterTextChanged(s: Editable) {
                val query = s.toString()
                d(TAG, "TextWatcher: Query searched: $query")
                val results = db?.getBusStopsByQuery(query)
                if (!results.isNullOrEmpty()) {
                    d(TAG, "TextWatcher: Finished Search. Size: ${results.size}")
                    adapter?.updateAdapter(results)
                    adapter?.notifyItemRangeChanged(0, results.size)
                }
            }
        }
        binding?.inputData?.addTextChangedListener(inputWatcher)
        return v
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}