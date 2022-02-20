package com.martinlacorrona.ryve.mobile.model

/**
 * Fichero para la respuesta de la API de Directions
 */
data class DirectionsModel(
    var pointsEncoded: String?,
    var northeast: BoundsModel,
    var southwest: BoundsModel,
    var distance: Int, //in meters
    var duration: Int, //in minutes
)