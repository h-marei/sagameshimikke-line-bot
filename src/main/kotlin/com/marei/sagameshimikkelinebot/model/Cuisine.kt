package com.marei.sagameshimikkelinebot.model

data class Cuisine(
        val id: String,
        val name: String,
        val photo: Photo,
        val restaurant: Restaurant
)