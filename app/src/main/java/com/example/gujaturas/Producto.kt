package com.example.gujaturas

data class Producto(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val cantidad: Int = 0,
    val valor: Double = 0.0,
    val color: String = "",
    val talla: String = "",
    val precioMinimo: Double = 0.0,
    val imagenUrl: String? = null
)
