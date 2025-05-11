package com.example.gujaturas

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import java.text.NumberFormat
import java.util.*

class AgregarVenta : AppCompatActivity() {

    private lateinit var spinnerProducto: Spinner
    private lateinit var tvItemDescripcion: TextView
    private lateinit var tvPrecioItem: TextView
    private lateinit var etPrecioNuevo: EditText
    private lateinit var etDescuento: EditText
    private lateinit var tvTotalCompra: TextView
    private lateinit var btnGuardar: Button

    // Vistas que ocultamos al inicio
    private lateinit var itemRow: LinearLayout
    private lateinit var separadorPrecio: View
    private lateinit var llAddDescuento: LinearLayout
    private lateinit var rowDescuento: LinearLayout

    // Flags para evitar recursión en los TextWatchers
    private var editingPrecioNuevo = false
    private var editingDescuento   = false

    private lateinit var dbProductos: DatabaseReference
    private lateinit var dbVentas: DatabaseReference

    private val listaProductos = mutableListOf<Producto>()
    private val productosSeleccionados = mutableListOf<ProductoSeleccionado>()
    private var totalCompra: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_venta)

        // Footer navigation
        val btnInv: LinearLayout = findViewById(R.id.navInventario)
        val btnVen: LinearLayout = findViewById(R.id.navVentas)
        val btnEst: LinearLayout = findViewById(R.id.navEstadisticas)
        val bgInv: FrameLayout   = findViewById(R.id.bgCircleInventario)
        val bgVen: FrameLayout   = findViewById(R.id.bgCircleVentas)
        val bgEst: FrameLayout   = findViewById(R.id.bgCircleEstad)

        bgInv.setBackgroundResource(R.drawable.bg_circle_nav_unselected)
        bgVen.setBackgroundResource(R.drawable.bg_circle_nav_selected)
        bgEst.setBackgroundResource(R.drawable.bg_circle_nav_unselected)

        btnInv.setOnClickListener { startActivity(Intent(this, Inventario::class.java)) }
        btnVen.setOnClickListener { /* estamos aquí */ }
        btnEst.setOnClickListener { startActivity(Intent(this, Estadisticas::class.java)) }

        // Bind vistas principales
        spinnerProducto   = findViewById(R.id.spinnerProducto)
        tvItemDescripcion = findViewById(R.id.tvItemDescripcion)
        tvPrecioItem      = findViewById(R.id.tvPrecioItem)
        etPrecioNuevo     = findViewById(R.id.etPrecioNuevo)
        etDescuento       = findViewById(R.id.etDescuento)
        tvTotalCompra     = findViewById(R.id.labelTotalCompra)
        btnGuardar        = findViewById(R.id.btnGuardarVenta)

        // Bind y oculta bloques de ítem y descuento
        itemRow          = findViewById(R.id.itemRow)
        separadorPrecio  = findViewById(R.id.separadorPrecio)
        llAddDescuento   = findViewById(R.id.llAddDescuento)
        rowDescuento     = findViewById(R.id.rowDescuento)

        itemRow.visibility         = View.GONE
        tvPrecioItem.visibility    = View.GONE
        separadorPrecio.visibility = View.GONE
        llAddDescuento.visibility  = View.GONE
        rowDescuento.visibility    = View.GONE

        // Referencias Firebase
        dbProductos = FirebaseDatabase.getInstance().getReference("productos")
        dbVentas    = FirebaseDatabase.getInstance().getReference("ventas")

        // Cargar Spinner desde RTDB
        dbProductos.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaProductos.clear()
                snapshot.children.mapNotNull {
                    it.getValue(Producto::class.java)?.apply { id = it.key ?: "" }
                }.also { listaProductos.addAll(it) }

                val nombres = mutableListOf("Seleccionar producto") + listaProductos
                    .sortedBy { it.nombre }
                    .map { "${it.nombre} - ${it.color} - ${it.talla}" }

                spinnerProducto.adapter = ArrayAdapter(
                    this@AgregarVenta,
                    android.R.layout.simple_spinner_item,
                    nombres
                ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AgregarVenta, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Listener del Spinner
        spinnerProducto.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                if (pos == 0) {
                    // Placeholder: limpiar y ocultar todo
                    itemRow.visibility         = View.GONE
                    tvPrecioItem.visibility    = View.GONE
                    separadorPrecio.visibility = View.GONE
                    llAddDescuento.visibility  = View.GONE
                    rowDescuento.visibility    = View.GONE

                    tvItemDescripcion.text = ""
                    tvPrecioItem.text      = ""
                    etPrecioNuevo.setText("")
                    etDescuento.setText("")
                    return
                }

                // Producto real: mostrar bloques
                itemRow.visibility         = View.VISIBLE
                tvPrecioItem.visibility    = View.VISIBLE
                separadorPrecio.visibility = View.VISIBLE
                llAddDescuento.visibility  = View.VISIBLE
                rowDescuento.visibility    = View.VISIBLE

                // Offset -1 por el placeholder
                val prod = listaProductos.sortedBy { it.nombre }.getOrNull(pos - 1)
                    ?: return

                val sel = ProductoSeleccionado(prod)
                productosSeleccionados.add(sel)

                tvItemDescripcion.text = "${prod.nombre} - ${prod.talla} x${sel.cantidad}"
                tvPrecioItem.text      = formatCurrency(sel.precioUnitario)
                etPrecioNuevo.setText(sel.precioUnitario.toString())
                etDescuento.setText("0")
                actualizarTotal()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // TextWatcher: Precio Nuevo → % Descuento
        etPrecioNuevo.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (editingDescuento) return
                val sel = productosSeleccionados.lastOrNull() ?: return
                val np = s.toString().toDoubleOrNull() ?: return

                editingPrecioNuevo = true
                sel.aplicarPrecioNuevo(np)
                val porc = (1 - sel.precioConDescuento / sel.precioUnitario) * 100
                etDescuento.setText(String.format(Locale.getDefault(), "%.1f", porc))
                editarTotalYVisibilidades()
                editingPrecioNuevo = false
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // TextWatcher: % Descuento → Precio Nuevo
        etDescuento.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (editingPrecioNuevo) return
                val sel = productosSeleccionados.lastOrNull() ?: return
                val pct = s.toString().toDoubleOrNull() ?: return

                editingDescuento = true
                sel.aplicarDescuentoPorcentaje(pct)
                etPrecioNuevo.setText(String.format(Locale.getDefault(), "%.2f", sel.precioConDescuento))
                editarTotalYVisibilidades()
                editingDescuento = false
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Guardar venta(s)
        btnGuardar.setOnClickListener {
            productosSeleccionados.forEach { sel ->
                val vid = dbVentas.push().key ?: return@forEach
                val venta = Venta(
                    id                = vid,
                    idProducto        = sel.producto.id,
                    cantidadVendida   = sel.cantidad,
                    descuentoAplicado = sel.descuentoAplicado,
                    precioVenta       = sel.totalLinea(),
                    fechaVenta        = ServerValue.TIMESTAMP,
                    productoNombre    = sel.producto.nombre
                )
                dbVentas.child(vid).setValue(venta)
                restarStock(sel.producto.id, sel.cantidad)
            }
            Toast.makeText(this, "Venta(s) registrada(s)", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun editarTotalYVisibilidades() {
        // Asegura que los bloques estén visibles
        itemRow.visibility         = View.VISIBLE
        separadorPrecio.visibility = View.VISIBLE
        llAddDescuento.visibility  = View.VISIBLE
        rowDescuento.visibility    = View.VISIBLE
        // Actualiza el total
        actualizarTotal()
    }

    private fun actualizarTotal() {
        totalCompra = productosSeleccionados.sumOf { it.totalLinea() }
        tvTotalCompra.text = "Total de la Compra: ${formatCurrency(totalCompra)}"
    }

    private fun restarStock(idProducto: String, qty: Int) {
        val ref = dbProductos.child(idProducto)
        ref.child("cantidad").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val actual = snapshot.getValue(Int::class.java) ?: return
                ref.child("cantidad").setValue(actual - qty)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun formatCurrency(value: Double): String =
        NumberFormat.getCurrencyInstance(Locale.getDefault()).format(value)
}
