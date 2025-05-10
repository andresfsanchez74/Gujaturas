package com.example.gujaturas

import com.google.firebase.database.ServerValue

data class Venta(
    var id: String = "",  // ID único generado por Firebase
    var idProducto: Int = 0,  // ID del producto relacionado
    var cantidadVendida: Int = 0,
    var descuentoAplicado: Boolean = false,
    var precioVenta: Double = 0.0,
    var fechaVenta: Long = System.currentTimeMillis(), // Fecha en que se realiza la venta
    // El nombre del producto se obtiene dinámicamente a través de la relación con idProducto
    var productoNombre: String? = null  // Este campo se llena cuando se obtiene el producto de la base de datos
)
