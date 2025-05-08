package com.example.gujaturas

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class Ventas : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ventas)

        val btnInventario = findViewById<LinearLayout>(R.id.navInventario)
        val btnVentas = findViewById<LinearLayout>(R.id.navVentas)
        val btnEstadisticas = findViewById<LinearLayout>(R.id.navEstadisticas)

        val bgCircleInventario = findViewById<FrameLayout>(R.id.bgCircleInventario)
        val bgCircleVentas = findViewById<FrameLayout>(R.id.bgCircleVentas)
        val bgCircleEstadisticas = findViewById<FrameLayout>(R.id.bgCircleEstad)

        bgCircleInventario.setBackgroundResource(R.drawable.bg_circle_nav_unselected)
        bgCircleVentas.setBackgroundResource(R.drawable.bg_circle_nav_selected)
        bgCircleEstadisticas.setBackgroundResource(R.drawable.bg_circle_nav_unselected)

        // Establecer la acción al hacer clic en Inventario
        btnInventario.setOnClickListener {
            bgCircleInventario.setBackgroundResource(R.drawable.bg_circle_nav_selected)
            bgCircleVentas.setBackgroundResource(R.drawable.bg_circle_nav_unselected)
            bgCircleEstadisticas.setBackgroundResource(R.drawable.bg_circle_nav_unselected)

            val intent = Intent(this, Inventario::class.java)
            startActivity(intent)
        }

        // Establecer la acción al hacer clic en Ventas
        btnVentas.setOnClickListener {
            // Ya estamos en la vista de Ventas, no hacemos nada aquí.
        }

        // Establecer la acción al hacer clic en Estadísticas
        btnEstadisticas.setOnClickListener {
            bgCircleInventario.setBackgroundResource(R.drawable.bg_circle_nav_unselected)
            bgCircleVentas.setBackgroundResource(R.drawable.bg_circle_nav_unselected)
            bgCircleEstadisticas.setBackgroundResource(R.drawable.bg_circle_nav_selected)

            val intent = Intent(this, Estadisticas::class.java)
            startActivity(intent)
        }
    }
}
