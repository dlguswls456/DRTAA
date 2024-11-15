package com.drtaa.feature_car

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.drtaa.core_map.base.BaseMapFragment
import com.drtaa.core_map.moveCameraTo
import com.drtaa.core_ui.showSnackBar
import com.drtaa.feature_car.databinding.FragmentCarTrackingBinding
import com.drtaa.feature_car.viewmodel.CarViewModel
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

@AndroidEntryPoint
class CarTrackingFragment :
    BaseMapFragment<FragmentCarTrackingBinding>(R.layout.fragment_car_tracking) {
    private val viewModel: CarViewModel by hiltNavGraphViewModels<CarViewModel>(R.id.nav_graph_car)
    override var mapView: MapView? = null

    private var carMarker: Marker = Marker().apply {
        icon = OverlayImage.fromResource(R.drawable.ic_car_marker)
        width = ICON_SIZE
        height = ICON_SIZE
        position = LatLng(0.0, 0.0)
    }

    private var positionMarker: Marker = Marker().apply {
        icon = OverlayImage.fromResource(com.drtaa.core_ui.R.drawable.ic_center_marker)
        width = ICON_SIZE
        height = ICON_SIZE
    }

    private var destinationMarker: Marker = Marker().apply {
        icon = OverlayImage.fromResource(com.drtaa.core_ui.R.drawable.ic_destination)
        width = ICON_SIZE
        height = ICON_SIZE
    }

    private val pathOverlay by lazy {
        PathOverlay().apply {
            color = Color.RED
            outlineColor = Color.RED
            passedColor = ContextCompat.getColor(requireContext(), android.R.color.transparent)
            passedOutlineColor =
                ContextCompat.getColor(requireContext(), android.R.color.transparent)
            outlineWidth = 5
        }
    }

    override fun initMapView() {
        showLoading()
        mapView = binding.mapView
        mapView?.getMapAsync(this)
    }

    override fun initOnMapReady(naverMap: NaverMap) {
        observeMQTTOnMap(naverMap)
        observeCarTracking()
        carMarker.map = naverMap
        naverMap.moveCameraTo(jungryujang.latitude, jungryujang.longitude)
        binding.btnCall.setOnClickListener {
            showLoading()
            // isInProgressed -> 재호출, isReserved -> 첫 호출
            if (viewModel.isInProgress.value) {
                viewModel.destination.value?.let {
                    viewModel.recallAssignedCar(it)
                } ?: showSnackBar("호출장소를 설정해주세요")
            } else if (viewModel.isReserved.value) {
                // 첫 호출 시 로직 변경 필요
                viewModel.modifyFirstEvent()
            } else {
                showSnackBar("배정된 차량이 없습니다")
            }
        }

        naverMap.setOnMapLongClickListener { _, latLng ->
            positionMarker.apply {
                position = latLng
                map = naverMap
            }
            viewModel.setDestination(latLng)
        }

        binding.flTracking.setOnClickListener {
            viewModel.toggleTrackingState()
        }
    }

    private fun observeCarTracking() {
        viewModel.reverseGeocode.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { res ->
                if (res == null) {
                    binding.tvPinDesc.text = "호출할 위치를 길게 눌러주세요"
                    return@onEach
                }

                res.onSuccess { road ->
                    binding.tvPinDesc.text = road
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.trackingState.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { isTracking ->
                binding.ivTrackingOn.visibility = if (isTracking) View.VISIBLE else View.GONE
                binding.ivTrackingOff.visibility = if (isTracking) View.GONE else View.VISIBLE
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.isReturn.flowWithLifecycle(viewLifecycleOwner.lifecycle).onEach {
            if (it) {
                showSnackBar("반납 성공")
                navigatePopBackStack()
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.isFirst.flowWithLifecycle(viewLifecycleOwner.lifecycle).onEach {
            if (it) {
                dismissLoading()
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.isRecall.flowWithLifecycle(viewLifecycleOwner.lifecycle).onEach {
            if (it) {
                dismissLoading()
            } else {
                dismissLoading()
                showSnackBar("렌트카 호출 실패..")
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun observeMQTTOnMap(naverMap: NaverMap) {
        viewModel.mqttConnectionStatus.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { status ->
                Timber.tag("connect mqtt").d("$status")
                when (status) {
                    1 -> {
                        dismissLoading()
                        viewModel.startGPSPublish()
                        viewModel.getRoute()
                    }

                    -1 -> {
                        Timber.tag("mqtt").d("mqtt 연결 실패")
                    }

                    else -> {
                        dismissLoading()
                        navigatePopBackStack()
                        showSnackBar("다시 접속해 주세요")
                    }
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.gpsData.flowWithLifecycle(viewLifecycleOwner.lifecycle).onEach { gps ->
            Timber.tag("gps").d("$gps")
            pathOverlayProgress(gps)
            carMarker.apply {
                position = gps
                if (viewModel.trackingState.value) {
                    naverMap.moveCameraTo(position.latitude, position.longitude)
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.carPosition.flowWithLifecycle(viewLifecycleOwner.lifecycle).onEach {
            dismissLoading()
            if (it.latitude == 0.0 && it.longitude == 0.0) {
                showSnackBar("렌트한 차량이 없습니다")
            } else {
                carMarker.apply {
                    position = LatLng(it.latitude, it.longitude)
                    naverMap.moveCameraTo(position.latitude, position.longitude)
                    if (!viewModel.trackingState.value) viewModel.toggleTrackingState()
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.carDestination.flowWithLifecycle(viewLifecycleOwner.lifecycle).onEach {
            it?.let {
//                binding.clCarInfo.visibility = View.VISIBLE
//                val min = it.remainingTime / SEC
//                val sec = it.remainingTime % SEC
//                binding.tvTime.text = if (min > 0) {
//                    "${min}분 ${sec}초"
//                } else {
//                    "${sec}초"
//                }
//                binding.tvDistance.text = "${it.remainingDistance}m"
                destinationMarker.apply {
                    position = LatLng(it.latitude, it.longitude)
                    map = naverMap
                }
                mapView.apply {
                    InfoWindow().apply {
                        adapter = object : InfoWindow.DefaultTextAdapter(requireActivity()) {
                            override fun getText(infoWindow: InfoWindow): CharSequence {
                                val boldText = SpannableString("${it.name}").apply {
                                    setSpan(
                                        StyleSpan(Typeface.BOLD), 0, length,
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )
                                }
                                return boldText
                            }
                        }
                        open(destinationMarker)
                    }
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.routeData.flowWithLifecycle(viewLifecycleOwner.lifecycle).onEach { routeData ->
            if (routeData.isEmpty()) return@onEach
            Timber.tag("pathFrag").d("$routeData")

            pathOverlay.apply {
                coords = routeData.map { LatLng(it.lat, it.lon) }
                map = naverMap
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun pathOverlayProgress(gps: LatLng) {
        val path = viewModel.routeData.value
        if (path.isNotEmpty()) {
            val end = path.last().idx
            // 현재 GPS와 경로의 점들 간의 거리를 계산하여 THRESHOLD 범위 내에서 가장 가까운 지점을 찾음
            val closestPoint = path.minByOrNull { point ->
                gps.distanceTo(LatLng(point.lat, point.lon))
            }

            // 가장 가까운 지점과의 거리가 THRESHOLD 범위 이내일 경우에만 진행 상황 계산
            closestPoint?.let { matchedPoint ->
                val distanceToClosestPoint =
                    gps.distanceTo(LatLng(matchedPoint.lat, matchedPoint.lon))
                if (distanceToClosestPoint <= THRESHOLD) {
                    val progress = matchedPoint.idx
                    Timber.tag("gps progress")
                        .d("path and end: $path || $end -- ${(progress.toFloat() / end.toFloat()).toDouble()}")
                    pathOverlay.progress = (progress.toFloat() / end.toFloat()).toDouble()
                    if (pathOverlay.progress == 1.0) viewModel.clearPath()
                } else {
                    Timber.tag("gps progress")
                        .d("매칭 안됨: 가장 가까운 지점이 허용 범위 밖입니다. $distanceToClosestPoint")
                }
            } ?: run {
                Timber.tag("gps progress").d("매칭 안됨: 경로가 존재하지 않음.")
            }
        }
    }

    override fun onDestroy() {
        with(viewModel) {
            stopGPSPublish()
            clearDestination()
            clearReverseGeocode()
        }
        super.onDestroy()
    }

    override fun iniView() {
//
        viewModel.fetchPath()
    }

    companion object {
        private const val THRESHOLD = 20.0 // 너무 좌표가 튀어서 일단 크게 잡는다
        private const val ICON_SIZE = 100
//        private const val SEC = 60
        val jungryujang = LatLng(37.57578754990568, 126.90027478459672)
//        private val STARBUCKS = LatLng(37.576636819990284, 126.89879021208397)
//        private val SANGAM_LATLNG = LatLng(37.57569116736151, 126.90039723462993)
    }
}
