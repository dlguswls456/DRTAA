package com.drtaa.feature_plan

import android.view.View
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import com.drtaa.core_ui.base.BaseFragment
import com.drtaa.feature_plan.adapter.ItemTouchHelperCallback
import com.drtaa.feature_plan.adapter.PlanListAdapter
import com.drtaa.feature_plan.databinding.FragmentDayPlanBinding
import com.drtaa.feature_plan.viewmodel.PlanViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class DayPlanFragment(
    private val day: Int
) : BaseFragment<FragmentDayPlanBinding>(R.layout.fragment_day_plan) {

    private val planViewModel: PlanViewModel by hiltNavGraphViewModels(R.id.nav_graph_plan)

    private val dayPlan by lazy {
        planViewModel.plan.value?.let { plan ->
            plan.datesDetail.find { it.travelDatesId == day }
        }
    }

    private val planListAdapter = PlanListAdapter()
    private val itemTouchHelperCallback = ItemTouchHelperCallback(planListAdapter)
    private val helper = ItemTouchHelper(itemTouchHelperCallback)

    override fun initView() {
        initObserve()
        initEvent()
        initRVAdapter()
    }

    private fun initObserve() {
        planViewModel.isEditMode.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { isEditMode ->
                planListAdapter.enableEditMode(isEditMode)
                helper.attachToRecyclerView(
                    if (isEditMode) binding.rvDayPlan else null
                )
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun initRVAdapter() {
        binding.rvDayPlan.adapter = planListAdapter
        planListAdapter.submitList(dayPlan!!.placesDetail)

//        planListAdapter.setItemClickListener(object : PlanListAdapter.ItemClickListener {
//            override fun onItemClicked(planItem: DayPlan.PlanItem) {
//
//            }
//        })

        if (dayPlan!!.placesDetail.isEmpty()) {
            binding.llNoPlan.visibility = View.VISIBLE
        } else {
            binding.clDayPlan.visibility = View.VISIBLE
        }
    }

    private fun initEvent() {
        binding.btnAddPlan.setOnClickListener {
            navigateDestination(R.id.action_planListFragment_to_planSearchFragment)
        }
    }
}