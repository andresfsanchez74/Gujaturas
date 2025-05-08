package com.example.gujaturas

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import android.widget.LinearLayout
import androidx.cardview.widget.CardView

class Footer : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_footer)

        // Obtener los botones del footer
        val btnInventario = findViewById<LinearLayout>(R.id.navInventario)
        val btnVentas = findViewById<LinearLayout>(R.id.navVentas)
        val btnEstadisticas = findViewById<LinearLayout>(R.id.navEstadisticas)

        // Obtener los círculos del fondo para cambiar el color
        val bgCircleInventario = findViewById<FrameLayout>(R.id.bgCircleInventario)
        val bgCircleVentas = findViewById<FrameLayout>(R.id.bgCircleVentas)
        val bgCircleEstadisticas = findViewById<FrameLayout>(R.id.bgCircleEstad)

        // Establecer la acción al hacer clic en Inventario
        btnInventario.setOnClickListener {
            // Cambiar fondo de los botones
            bgCircleInventario.setBackgroundResource(R.drawable.bg_circle_nav_selected)
            bgCircleVentas.setBackgroundResource(R.drawable.bg_circle_nav_unselected)
            bgCircleEstadisticas.setBackgroundResource(R.drawable.bg_circle_nav_unselected)

            // Navegar a la vista de inventario
            val intent = Intent(this, Inventario::class.java)
            startActivity(intent)
        }

        // Establecer la acción al hacer clic en Ventas
        btnVentas.setOnClickListener {
            // Cambiar fondo de los botones
            bgCircleInventario.setBackgroundResource(R.drawable.bg_circle_nav_unselected)
            bgCircleVentas.setBackgroundResource(R.drawable.bg_circle_nav_selected)
            bgCircleEstadisticas.setBackgroundResource(R.drawable.bg_circle_nav_unselected)

            // Navegar a la vista de ventas
            val intent = Intent(this, Ventas::class.java)
            startActivity(intent)
        }

        // Establecer la acción al hacer clic en Estadísticas
        btnEstadisticas.setOnClickListener {
            // Cambiar fondo de los botones
            bgCircleInventario.setBackgroundResource(R.drawable.bg_circle_nav_unselected)
            bgCircleVentas.setBackgroundResource(R.drawable.bg_circle_nav_unselected)
            bgCircleEstadisticas.setBackgroundResource(R.drawable.bg_circle_nav_selected)

            // Navegar a la vista de estadísticas
            val intent = Intent(this, Estadisticas::class.java)
            startActivity(intent)
        }
    }
}
