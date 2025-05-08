package com.example.gujaturas

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.LinearLayout

class Inicio : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)

        // Botón para gestionar inventario
        val btnInventario = findViewById<LinearLayout>(R.id.btnInventario)
        btnInventario.setOnClickListener {
            val intent = Intent(this, Inventario::class.java) // Cambia 'InventarioActivity' por el nombre de tu clase de inventario
            startActivity(intent)
        }

        // Botón para gestionar ventas
        val btnVentas = findViewById<LinearLayout>(R.id.btnVentas)
        btnVentas.setOnClickListener {
            val intent = Intent(this, Ventas::class.java) // Cambia 'VentasActivity' por el nombre de tu clase de ventas
            startActivity(intent)
        }

        // Botón para ver estadísticas
        val btnEstadisticas = findViewById<LinearLayout>(R.id.btnEstadisticas)
        btnEstadisticas.setOnClickListener {
            val intent = Intent(this, Estadisticas::class.java) // Cambia 'EstadisticasActivity' por el nombre de tu clase de estadísticas
            startActivity(intent)
        }
    }
}
