package com.example.gujaturas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import android.widget.Button
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

        // Firebase RTDB Reference
        dbRef = FirebaseDatabase.getInstance().getReference("ventas")

        // Footer navigation
        val btnInventario: LinearLayout = findViewById(R.id.navInventario)
        val btnVentas: LinearLayout = findViewById(R.id.navVentas)
        val btnEstadisticas: LinearLayout = findViewById(R.id.navEstadisticas)

        val bgCircleInventario: FrameLayout = findViewById(R.id.bgCircleInventario)
        val bgCircleVentas: FrameLayout = findViewById(R.id.bgCircleVentas)
        val bgCircleEstadisticas: FrameLayout = findViewById(R.id.bgCircleEstad)

        bgCircleInventario.setBackgroundResource(R.drawable.bg_circle_nav_unselected)
        bgCircleVentas.setBackgroundResource(R.drawable.bg_circle_nav_selected)
        bgCircleEstadisticas.setBackgroundResource(R.drawable.bg_circle_nav_unselected)

        // Set footer click listeners
        btnInventario.setOnClickListener {
            startActivity(Intent(this, Inventario::class.java))
        }
        btnVentas.setOnClickListener {
            // Already in ventas activity
        }
        btnEstadisticas.setOnClickListener {
            startActivity(Intent(this, Estadisticas::class.java))
        }

        // RecyclerView for displaying sales
        val recyclerView: RecyclerView = findViewById(R.id.recyclerVentas)
        val emptyView: LinearLayout = findViewById(R.id.emptyVentas)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Adapter setup
        adapter = VentaAdapter(ventasList,
            onEdit = { venta ->
                val intent = Intent(this, EditarVenta::class.java)
                intent.putExtra("VENTA_ID", venta.id)
                startActivity(intent)
            },
            onDelete = { venta ->
                dbRef.child(venta.id).removeValue()
                Toast.makeText(this, "Venta eliminada", Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView.adapter = adapter

        // Fetch ventas from Firebase
        fetchVentas(emptyView)

        // Botón de "Agregar Venta"
        val btnAgregarVenta: Button = findViewById(R.id.btnAgregarVenta)
        btnAgregarVenta.setOnClickListener {
            // Iniciar la actividad "AgregarVenta"
            val intent = Intent(this, AgregarVenta::class.java)
            startActivity(intent)
        }
    }

    private fun fetchVentas(emptyView: LinearLayout) {
        dbRef.orderByChild("fechaVenta").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempList = mutableListOf<Venta>()
                snapshot.children.forEach { child ->
                    child.getValue(Venta::class.java)?.let { venta ->
                        venta.id = child.key ?: ""
                        tempList.add(venta)
                    }
                }

                // Agrupar las ventas por fecha
                val ventasPorFecha = groupByDate(tempList)

                ventasList.clear()

                // Ahora agregamos las ventas agrupadas al RecyclerView
                ventasPorFecha.forEach { (fecha, ventas) ->
                    ventasList.add(Venta(productoNombre = fecha)) // Aquí añadimos la fecha
                    ventasList.addAll(ventas)
                }

                adapter.notifyDataSetChanged()

                // Show empty message if no ventas are found
                emptyView.visibility = if (ventasList.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Ventas, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Agrupar ventas por fecha (hoy, ayer, fecha específica)
    private fun groupByDate(ventas: List<Venta>): Map<String, List<Venta>> {
        val groupedVentas = mutableMapOf<String, MutableList<Venta>>()

        val currentDate = getFormattedDate(Date())
        val yesterdayDate = getFormattedDate(getYesterday())

        ventas.forEach { venta ->
            val ventaFecha = getFormattedDate(venta.fechaVenta ?: 0)
            val fechaKey = when {
                ventaFecha == currentDate -> "Hoy"
                ventaFecha == yesterdayDate -> "Ayer"
                else -> ventaFecha // Fecha específica
            }

            if (groupedVentas.containsKey(fechaKey)) {
                groupedVentas[fechaKey]?.add(venta)
            } else {
                groupedVentas[fechaKey] = mutableListOf(venta)
            }
        }

        return groupedVentas
    }

    private fun getFormattedDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun getFormattedDate(date: Date): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(date)
    }

    private fun getYesterday(): Date {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1)
        return cal.time
    }
}

