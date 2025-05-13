package com.example.gujaturas

data class DetalleVenta(
    var idProducto: String = "",
    var cantidad: Int = 0,
    var precioUnitario: Double = 0.0,
    var descuentoAplicado: Boolean = false,
    var precioConDescuento: Double = 0.0,
    var nombre: String = ""
){
    // Para calcular el precio final de la línea (precio con descuento)
    fun precioLinea(): Double = precioConDescuento * cantidad
}