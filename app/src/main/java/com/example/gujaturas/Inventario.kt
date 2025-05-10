package com.example.gujaturas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Inventario : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventario)

        // Footer
        val btnInv: LinearLayout       = findViewById(R.id.navInventario)
        val btnVen: LinearLayout       = findViewById(R.id.navVentas)
        val btnEst: LinearLayout       = findViewById(R.id.navEstadisticas)
        val bgInv: FrameLayout         = findViewById(R.id.bgCircleInventario)
        val bgVen: FrameLayout         = findViewById(R.id.bgCircleVentas)
        val bgEst: FrameLayout         = findViewById(R.id.bgCircleEstad)

        bgInv.setBackgroundResource(R.drawable.bg_circle_nav_selected)

        btnInv.setOnClickListener {
            bgInv.setBackgroundResource(R.drawable.bg_circle_nav_selected)
            bgVen.setBackgroundResource(R.drawable.bg_circle_nav_unselected)
            bgEst.setBackgroundResource(R.drawable.bg_circle_nav_unselected)
        }
        btnVen.setOnClickListener {
            bgInv.setBackgroundResource(R.drawable.bg_circle_nav_unselected)
            bgVen.setBackgroundResource(R.drawable.bg_circle_nav_selected)
            bgEst.setBackgroundResource(R.drawable.bg_circle_nav_unselected)
            startActivity(Intent(this, Ventas::class.java))
        }
        btnEst.setOnClickListener {
            bgInv.setBackgroundResource(R.drawable.bg_circle_nav_unselected)
            bgVen.setBackgroundResource(R.drawable.bg_circle_nav_unselected)
            bgEst.setBackgroundResource(R.drawable.bg_circle_nav_selected)
            startActivity(Intent(this, Estadisticas::class.java))
        }

        // Search
        findViewById<ImageView>(R.id.iconFilter).setOnClickListener {
            Toast.makeText(this, "Filtro no disponible", Toast.LENGTH_SHORT).show()
        }

        // Add product
        findViewById<FrameLayout>(R.id.btnAgregarProductoContainer)
            .setOnClickListener {
                startActivity(Intent(this, AgregarProducto::class.java))
            }
        findViewById<Button>(R.id.btnAgregarPrimerProducto)
            .setOnClickListener {
                startActivity(Intent(this, AgregarProducto::class.java))
            }

        // RecyclerView setup
        val recycler: RecyclerView = findViewById(R.id.recyclerInventario)
        recycler.layoutManager = LinearLayoutManager(this)
        // TODO: inicializar adapter y listener de Firebase
    }
}
