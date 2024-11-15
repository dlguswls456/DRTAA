package com.drtaa.feature_tour

import android.view.View
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.drtaa.core_model.plan.PlanItem
import com.drtaa.core_model.util.toPlanItem
import com.drtaa.core_ui.base.BaseFragment
import com.drtaa.core_ui.component.LocationHelper
import com.drtaa.core_ui.dpToPx
import com.drtaa.core_ui.expandLayout
import com.drtaa.core_ui.foldLayout
import com.drtaa.feature_plan.adapter.PlanListAdapter
import com.drtaa.feature_tour.component.TourAdapter
import com.drtaa.feature_tour.databinding.FragmentTourBinding
import com.drtaa.feature_tour.viewmodel.TourViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class TourFragment : BaseFragment<FragmentTourBinding>(R.layout.fragment_tour) {

    @Inject
    lateinit var locationHelper: LocationHelper
    private val tourViewModel: TourViewModel by hiltNavGraphViewModels(R.id.nav_graph_tour)

    private val tourAdapter by lazy {
        TourAdapter(onTourClickListener = { tourItem ->
            moveToTravel(tourItem.toPlanItem())
        })
    }

    private val planAdapter by lazy {
        PlanListAdapter(
            context = requireActivity(),
            onPlanSelectListener = { },
            onPlanClickListener = { planItem ->
                moveToTravel(planItem)
            }
        )
    }

    override fun initView() {
        initUI()
        getLocation()
        initObserve()
        initEvent()
    }

    private fun initEvent() {
        binding.apply {
            llExpandPlan.setOnClickListener {
                if (clPlan.visibility == View.GONE) {
                    tourViewModel.getPlanList()
                    expandLayout(ivExpendPlan, clPlan)
                } else {
                    foldLayout(ivExpendPlan, clPlan)
                }
            }
        }
    }

    private fun initUI() {
        binding.rvTour.adapter = tourAdapter
        binding.rvPlan.adapter = planAdapter

        setRVMaxHeight()
    }

    private fun setRVMaxHeight() {
        // RecyclerView의 maxHeight를 250dp로 설정
        binding.apply {
            rvPlan.viewTreeObserver.addOnGlobalLayoutListener {
                val maxHeightInPx = MAX_HEIGHT.dpToPx()
                if (rvPlan.height > maxHeightInPx) {
                    val layoutParams = rvPlan.layoutParams
                    layoutParams.height = maxHeightInPx
                    rvPlan.layoutParams = layoutParams
                }
            }
        }
    }

    private fun getLocation() {
        viewLifecycleOwner.lifecycleScope.launch {
            locationHelper.getLastLocation().let { location ->
                location?.let {
                    tourViewModel.getLocationBasedList(
                        location.longitude.toString(),
                        location.latitude.toString(),
                        "1000" // 반경 2키로 검색
                    )
                }
            }
        }
    }

    private fun moveToTravel(planItem: PlanItem) {
        navigateDestination(
            TourFragmentDirections.actionFragmentTourToNavGraphTravel(
                planItem = planItem
            )
        )
    }

    private fun initObserve() {
        tourViewModel.planList.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { planList ->
                binding.apply {
                    if (planList == null) {
                        rvPlan.visibility = View.GONE
                        clPlanEmpty.visibility = View.VISIBLE
                        tvPlanHelp.text = "오류가 발생했습니다.\n다시 시도해주세요."
                    } else if (planList.isEmpty()) {
                        rvPlan.visibility = View.GONE
                        clPlanEmpty.visibility = View.VISIBLE
                        tvPlanHelp.text = "일정이 비어있어요!"
                    } else {
                        planAdapter.submitList(planList)
                        rvPlan.visibility = View.VISIBLE
                        clPlanEmpty.visibility = View.GONE
                    }
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        tourViewModel.pagedTour.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { pagingData ->
                if (pagingData == null) {
                    binding.rvTour.visibility = View.GONE
                    binding.tvTourError.visibility = View.VISIBLE
                } else {
                    tourAdapter.submitData(pagingData)

                    binding.rvTour.visibility = View.VISIBLE
                    binding.tvTourError.visibility = View.GONE
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        tourAdapter.loadStateFlow.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { loadStates ->
                if (loadStates.source.refresh is LoadState.Error) {
                    Timber.tag("tourFragment").d("Network error occurred")
                    binding.rvTour.visibility = View.GONE
                    binding.tvTourError.visibility = View.VISIBLE
                    dismissLoading()

                    return@onEach
                }

                val isLoading =
                    loadStates.source.refresh is LoadState.Loading || loadStates.source.append is LoadState.Loading
                if (!isLoading) dismissLoading() else showLoading()
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    companion object {
        const val MAX_HEIGHT = 250
    }
}