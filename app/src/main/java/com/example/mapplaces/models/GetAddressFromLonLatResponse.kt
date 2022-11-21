package com.example.mapplaces.models

import java.io.Serializable

data class GetAddressFromLonLatResponse (
    val meta: Meta,
    val result: MResult
): Serializable

data class MResult(
    val items: ArrayList<Item>,
    val total: Int
): Serializable

data class Item (
    val address_name: String,
    val building_name: String,
    val full_name: String,
    val id: String,
    val name: String,
    val point: Point,
    val purpose_name: String,
    val type: String
): Serializable

data class Point (
    var lat: Double,
    var lon: Double
): Serializable

data class Meta (
    val api_version: String,
    val code: Int,
    val issue_date: String
): Serializable
