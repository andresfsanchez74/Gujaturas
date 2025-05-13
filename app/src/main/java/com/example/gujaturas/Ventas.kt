package com.example.gujaturas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class Ventas : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    private lateinit var adapter: VentaAdapter
    private val ventasList = mutableListOf<Venta>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ventas)

        // 1) Referencia a RTDB
        dbRef = FirebaseDatabase.getInstance().getReference("ventas")

        // 2) Footer navigation
        val btnInv: LinearLayout = findViewById(R.id.navInventario)
        val btnVen: LinearLayout = findViewById(R.id.navVentas)
        val btnEst: LinearLayout = findViewById(R.id.navEstadisticas)
        val bgInv: FrameLayout = findViewById(R.id.bgCircleInventario)
        val bgVen: FrameLayout = findViewById(R.id.bgCircleVentas)
        val bgEst: FrameLayout = findViewById(R.id.bgCircleEstad)

        // Marcar sección actual
        bgInv.setBackgroundResource(R.drawable.bg_circle_nav_unselected)
        bgVen.setBackgroundResource(R.drawable.bg_circle_nav_selected)
        bgEst.setBackgroundResource(R.drawable.bg_circle_nav_unselected)

        btnInv.setOnClickListener {
            startActivity(Intent(this, Inventario::class.java))
        }
        // btnVen: no hace nada, ya estamos aquí
        btnEst.setOnClickListener {
            startActivity(Intent(this, Estadisticas::class.java))
        }

        // 3) RecyclerView + Empty View
        val recyclerView: RecyclerView = findViewById(R.id.recyclerVentas)
        val emptyView: LinearLayout = findViewById(R.id.emptyVentas)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = VentaAdapter(
            ventasList,
            onEdit = { venta ->
                // Solo permitir editar ventas reales (no los encabezados)
                if (venta.productos.isNotEmpty()) {
                    Intent(this, EditarVenta::class.java).also {
                        it.putExtra("VENTA_ID", venta.id)
                        startActivity(it)
                    }
                }
            },
            onDelete = { venta ->
                // Solo permitir eliminar ventas reales (no los encabezados)
                if (venta.productos.isNotEmpty()) {
                    dbRef.child(venta.id).removeValue()
                    Toast.makeText(this, "Venta eliminada", Toast.LENGTH_SHORT).show()
                }
            }
        )
        recyclerView.adapter = adapter

        // 4) Botón "Agregar Venta"
        findViewById<Button>(R.id.btnAgregarVenta).setOnClickListener {
            startActivity(Intent(this, AgregarVenta::class.java))
        }

        // 5) Carga inicial de datos
        fetchVentas(emptyView)
    }

    private fun fetchVentas(emptyView: LinearLayout) {
        dbRef
            .orderByChild("fechaVenta")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val temp = mutableListOf<Venta>()
                    snapshot.children.forEach { child ->
                        child.getValue(Venta::class.java)?.let { venta ->
                            venta.id = child.key ?: ""
                            temp.add(venta)
                        }
                    }

                    // Mostrar el emptyView si no hay ventas
                    if (temp.isEmpty()) {
                        ventasList.clear()
                        adapter.notifyDataSetChanged()
                        emptyView.visibility = View.VISIBLE
                        return
                    }

                    // Agrupar por fecha y actualizar la lista
                    updateVentasList(temp)
                    emptyView.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@Ventas,
                        "Error: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    /** Actualiza la lista de ventas agrupadas por fecha */
    private fun updateVentasList(ventasFromDB: List<Venta>) {
        ventasList.clear()

        // Agrupar por fecha
        val porFecha = groupByDate(ventasFromDB)

        // Solo agregar fechas que tengan ventas
        porFecha.forEach { (fecha, ventas) ->
            if (ventas.isNotEmpty()) {
                // Crear objeto especial para el encabezado de fecha con un identificador especial
                val encabezado = Venta(
                    id = "header_$fecha",  // Identificador especial para diferenciar encabezados
                    fechaVenta = 0L,
                    totalCompra = 0.0,
                    productos = mutableMapOf()
                )
                ventasList.add(encabezado)

                // Agregar las ventas reales de esta fecha
                ventasList.addAll(ventas)
            }
        }

        adapter.notifyDataSetChanged()
    }

    /** Agrupa las ventas en un map clave="Hoy"/"Ayer"/fecha, valor=listado **/
    private fun groupByDate(ventas: List<Venta>): Map<String, List<Venta>> {
        val result = mutableMapOf<String, MutableList<Venta>>()
        val hoy = formatDate(Date())
        val ayer = formatDate(getYesterday())

        ventas.forEach { venta ->
            // Casteo seguro de la marca de tiempo
            val timestamp = (venta.fechaVenta as? Long) ?: 0L
            val key = when (formatDate(timestamp)) {
                hoy -> "Hoy"
                ayer -> "Ayer"
                else -> formatDate(timestamp)
            }
            // Solo agregar ventas que tienen productos
            if (venta.productos.isNotEmpty()) {
                result.getOrPut(key) { mutableListOf() }.add(venta)
            }
        }
        return result
    }

    // Formatea un timestamp (Long) a "dd MMM yyyy"
    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    // Formatea un objeto Date a "dd MMM yyyy"
    private fun formatDate(date: Date): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(date)
    }

    private fun getYesterday(): Date {
        return Calendar.getInstance().apply {
            add(Calendar.DATE, -1)
        }.time
    }
}