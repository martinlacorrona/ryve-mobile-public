package com.martinlacorrona.ryve.mobile.model

import java.io.Serializable

/**
 * Fichero para la respuesta de la API de Directions
 */
data class BoundsModel(
    var lat: Double? = null,
    var lng: Double? = null,
): Serializable