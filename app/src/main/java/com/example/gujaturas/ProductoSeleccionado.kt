package com.example.gujaturas

data class ProductoSeleccionado(
    val producto: Producto,
    var cantidad: Int = 1
)
{
    var precioUnitario: Double = producto.valor
    var precioConDescuento: Double = precioUnitario
    var descuentoAplicado: Boolean = false

    fun aplicarPrecioNuevo(nuevoPrecio: Double) {
        precioConDescuento = nuevoPrecio
        descuentoAplicado = true
    }

    fun aplicarDescuentoPorcentaje(porcentaje: Double) {
        descuentoAplicado = porcentaje > 0
        precioConDescuento = precioUnitario * (1 - porcentaje / 100)
    }
    fun totalLinea(): Double = precioConDescuento * cantidad
}