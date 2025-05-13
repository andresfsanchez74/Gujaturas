package com.example.gujaturas

import com.google.firebase.database.ServerValue

data class Venta(
    var id: String = "",
    var totalCompra: Double = 0.0,
    var fechaVenta: Any = ServerValue.TIMESTAMP,
    var productos: Map<String, DetalleVenta> = mutableMapOf()  // Cambié la lista por un mapa con la clave como producto
)
