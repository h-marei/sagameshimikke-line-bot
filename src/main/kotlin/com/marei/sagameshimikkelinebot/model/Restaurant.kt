package com.marei.sagameshimikkelinebot.model

data class Restaurant(
        val googleMapSrc: String,
        val name: String,
        val place: String,
        val url: String? // 必須項目ではないので null の可能性がある
)