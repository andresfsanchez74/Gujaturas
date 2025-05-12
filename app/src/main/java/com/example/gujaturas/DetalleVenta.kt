package com.example.gujaturas

data class DetalleVenta(
    var idProducto: String = "",
    var cantidad: Int = 0,
    var descuentoAplicado: Boolean = false,
    var precioLinea: Double = 0.0
)