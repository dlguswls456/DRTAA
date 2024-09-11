package com.drtaa.core_map

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.snackbar.Snackbar
import com.naver.maps.map.MapView

/**
 * 지도 스크롤이 부모 뷰의 스크롤에 영향 주지 않게 하는 메서드
 */
@SuppressLint("ClickableViewAccessibility")
fun MapView.ignoreParentScroll() {
    this.setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                this.parent.requestDisallowInterceptTouchEvent(true)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                this.parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        false
    }
}