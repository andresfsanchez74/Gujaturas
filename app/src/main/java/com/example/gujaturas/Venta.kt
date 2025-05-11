package com.example.gujaturas

import com.google.firebase.database.ServerValue

data class Venta(
    var id: String = "",                // clave Firebase
    var idProducto: String = "",        // PASAMOS A String
    var cantidadVendida: Int = 0,
    var descuentoAplicado: Boolean = false,
    var precioVenta: Double = 0.0,
    var fechaVenta: Any = ServerValue.TIMESTAMP,  // Any para TIMESTAMP
    var productoNombre: String = ""
)
