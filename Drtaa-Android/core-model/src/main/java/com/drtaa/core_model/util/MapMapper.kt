package com.drtaa.core_model.util

import com.drtaa.core_model.map.Search
import com.drtaa.core_model.network.SearchItem

fun SearchItem.toSearch(): Search {
    return Search(
        title = this.title.removeHtmlTags(),
        category = this.category.removeTextBeforeArrow(),
        roadAddress = this.roadAddress,
        lng = this.mapx.toDouble() / 10000000,
        lat = this.mapy.toDouble() / 10000000
    )
}

/**
 * HTML 태그를 찾아 제거
 */
fun String.removeHtmlTags(): String {
    return this.replace(Regex("<.*?>"), "")
}

/**
 * 첫 글자부터 ">"까지의 문자열을 제거
 */
fun String.removeTextBeforeArrow(): String {
    return this.substringAfterLast(">")
}