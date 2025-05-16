package com.example.gujaturas

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.database.*
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class Estadisticas : AppCompatActivity() {

    private lateinit var tvFechaSeleccionada: TextView
    private lateinit var btnCalendario: ImageButton
    private lateinit var tvPrimeroMasVendido: TextView
    private lateinit var tvSegundoMasVendido: TextView
    private lateinit var tvTerceroMasVendido: TextView
    private lateinit var tvGanancias: TextView
    private lateinit var rvVentas: RecyclerView

    private lateinit var adapter: RecyclerView.Adapter<*>
    private val ventasList = mutableListOf<Venta>()

    private lateinit var dbRefVentas: DatabaseReference
    private lateinit var dbRefProductos: DatabaseReference

    private val sdfDisplay = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var fechaInicioSeleccionada: Long? = null
    private var fechaFinSeleccionada: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estadisticas)

        tvFechaSeleccionada = findViewById(R.id.tvFechaSeleccionada)
        btnCalendario = findViewById(R.id.btnCalendario)
        tvPrimeroMasVendido = findViewById(R.id.PrimeroMasVendido)
        tvSegundoMasVendido = findViewById(R.id.SegundoMasVendido)
        tvTerceroMasVendido = findViewById(R.id.TerceroMasVendido)
        tvGanancias = findViewById(R.id.tvGanancias)
        rvVentas = findViewById(R.id.recyclerVentasEstadisticas) // Asegúrate de agregar este RecyclerView en el layout

        dbRefVentas = FirebaseDatabase.getInstance().getReference("ventas")
        dbRefProductos = FirebaseDatabase.getInstance().getReference("productos")

        setupFooter()

        tvFechaSeleccionada.text = "Toda la historia"

        adapter = VentaAdapter(
            ventasList,
            onEdit = { venta ->
                if (venta.productos.isNotEmpty()) {
                    Intent(this, EditarVenta::class.java).also {
                        it.putExtra("VENTA_ID", venta.id)
                        startActivity(it)
                    }
                }
            },
            onDelete = { venta ->
                if (venta.productos.isNotEmpty()) {
                    dbRefVentas.child(venta.id).removeValue()
                    Toast.makeText(this, "Venta eliminada", Toast.LENGTH_SHORT).show()
                }
            }
        )

        adapter = VentaAdapterEstadisticas(ventasList, dbRefProductos)
        rvVentas.layoutManager = LinearLayoutManager(this)
        rvVentas.adapter = adapter


        btnCalendario.setOnClickListener {
            val builder = MaterialDatePicker.Builder.dateRangePicker()
            builder.setTitleText("Selecciona rango de fechas")
            val constraintsBuilder = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())
            builder.setCalendarConstraints(constraintsBuilder.build())

            val picker = builder.build()
            picker.show(supportFragmentManager, picker.toString())

            picker.addOnPositiveButtonClickListener { selection ->
                val startDate = selection.first
                val endDate = selection.second
                fechaInicioSeleccionada = startDate
                fechaFinSeleccionada = endDate?.plus(24*60*60*1000 - 1) // Incluir toda la fecha final

                val startStr = sdfDisplay.format(Date(startDate))
                val endStr = sdfDisplay.format(Date(endDate ?: startDate))
                tvFechaSeleccionada.text = "$startStr - $endStr"

                filtrarEstadisticasPorFechas(fechaInicioSeleccionada!!, fechaFinSeleccionada!!)
            }
        }

        cargarTodoElHistorial()
    }

    private fun setupFooter() {
        val btnInventario = findViewById<LinearLayout>(R.id.navInventario)
        val btnVentas = findViewById<LinearLayout>(R.id.navVentas)
        val btnEstadisticas = findViewById<LinearLayout>(R.id.navEstadisticas)

        val bgCircleInventario = findViewById<FrameLayout>(R.id.bgCircleInventario)
        val bgCircleVentas = findViewById<FrameLayout>(R.id.bgCircleVentas)
        val bgCircleEstadisticas = findViewById<FrameLayout>(R.id.bgCircleEstad)

        bgCircleInventario.setBackgroundResource(R.drawable.bg_circle_nav_unselected)
        bgCircleVentas.setBackgroundResource(R.drawable.bg_circle_nav_unselected)
        bgCircleEstadisticas.setBackgroundResource(R.drawable.bg_circle_nav_selected)

        btnInventario.setOnClickListener {
            bgCircleInventario.setBackgroundResource(R.drawable.bg_circle_nav_selected)
            bgCircleVentas.setBackgroundResource(R.drawable.bg_circle_nav_unselected)
            bgCircleEstadisticas.setBackgroundResource(R.drawable.bg_circle_nav_unselected)
            startActivity(Intent(this, Inventario::class.java))
        }

        btnVentas.setOnClickListener {
            bgCircleInventario.setBackgroundResource(R.drawable.bg_circle_nav_unselected)
            bgCircleVentas.setBackgroundResource(R.drawable.bg_circle_nav_selected)
            bgCircleEstadisticas.setBackgroundResource(R.drawable.bg_circle_nav_unselected)
            startActivity(Intent(this, Ventas::class.java))
        }

        btnEstadisticas.setOnClickListener {
            // Ya estamos aquí, no hacemos nada
        }
    }

    private fun cargarTodoElHistorial() {
        dbRefVentas.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ventas = mutableListOf<Venta>()
                snapshot.children.forEach { snap ->
                    snap.getValue(Venta::class.java)?.let { venta ->
                        ventas.add(venta)
                    }
                }
                actualizarUIconVentas(ventas)
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Estadisticas, "Error al cargar datos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filtrarEstadisticasPorFechas(startDate: Long, endDate: Long) {
        dbRefVentas.orderByChild("fechaVenta")
            .startAt(startDate.toDouble())
            .endAt(endDate.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val ventasFiltradas = mutableListOf<Venta>()
                    snapshot.children.forEach { snap ->
                        snap.getValue(Venta::class.java)?.let { venta ->
                            ventasFiltradas.add(venta)
                        }
                    }
                    actualizarUIconVentas(ventasFiltradas)
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Estadisticas, "Error al filtrar: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun actualizarUIconVentas(ventas: List<Venta>) {
        val productoCantidadMap = mutableMapOf<String, Int>()
        var gananciasTotales = 0.0

        ventasList.clear()
        ventasList.addAll(ventas)
        adapter.notifyDataSetChanged()

        ventas.forEach { venta ->
            gananciasTotales += venta.totalCompra
            venta.productos.forEach { (idProducto, detalleVenta) ->
                dbRefProductos.child(idProducto).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val producto = snapshot.getValue(Producto::class.java)
                        producto?.let {
                            val nombreCompleto = "${it.nombre} - ${it.descripcion} - ${it.color}"
                            val cantidadActual = productoCantidadMap.getOrDefault(nombreCompleto, 0)
                            productoCantidadMap[nombreCompleto] = cantidadActual + detalleVenta.cantidad

                            val top3 = productoCantidadMap.entries.sortedByDescending { it.value }.take(3)

                            tvPrimeroMasVendido.text = top3.getOrNull(0)?.let { "1. ${it.key} x${it.value}" } ?: ""
                            tvSegundoMasVendido.text = top3.getOrNull(1)?.let { "2. ${it.key} x${it.value}" } ?: ""
                            tvTerceroMasVendido.text = top3.getOrNull(2)?.let { "3. ${it.key} x${it.value}" } ?: ""
                            tvGanancias.text = "$${"%,.2f".format(gananciasTotales)}"
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }
        }
    }
}
