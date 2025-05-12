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

    private lateinit var btnSumar: ImageButton
    private lateinit var btnRestar: ImageButton
    private lateinit var btnCerrarDescuento: ImageButton

    private lateinit var itemRow: LinearLayout
    private lateinit var separadorPrecio: View
    private lateinit var llAddDescuento: LinearLayout
    private lateinit var rowDescuento: LinearLayout

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

        // Footer nav…
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
        btnVen.setOnClickListener { /* aquí */ }
        btnEst.setOnClickListener { startActivity(Intent(this, Estadisticas::class.java)) }

        // Bind vistas principales
        spinnerProducto   = findViewById(R.id.spinnerProducto)
        tvItemDescripcion = findViewById(R.id.tvItemDescripcion)
        tvPrecioItem      = findViewById(R.id.tvPrecioItem)
        etPrecioNuevo     = findViewById(R.id.etPrecioNuevo)
        etDescuento       = findViewById(R.id.etDescuento)
        tvTotalCompra     = findViewById(R.id.labelTotalCompra)
        btnGuardar        = findViewById(R.id.btnGuardarVenta)

        // Bind controles de cantidad y cerrar descuento
        btnSumar            = findViewById(R.id.btnSumar)
        btnRestar           = findViewById(R.id.btnRestar)
        btnCerrarDescuento  = findViewById(R.id.btnCerrarDescuento)

        // Bind y oculta bloques
        itemRow          = findViewById(R.id.itemRow)
        separadorPrecio  = findViewById(R.id.separadorPrecio)
        llAddDescuento   = findViewById(R.id.llAddDescuento)
        rowDescuento     = findViewById(R.id.rowDescuento)
        itemRow.visibility         = View.GONE
        tvPrecioItem.visibility    = View.GONE
        separadorPrecio.visibility = View.GONE
        llAddDescuento.visibility  = View.GONE
        rowDescuento.visibility    = View.GONE

        // Firebase
        dbProductos = FirebaseDatabase.getInstance().getReference("productos")
        dbVentas    = FirebaseDatabase.getInstance().getReference("ventas")

        // Cargar spinner…
        dbProductos.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                listaProductos.clear()
                snap.children.mapNotNull {
                    it.getValue(Producto::class.java)?.apply { id = it.key ?: "" }
                }.also { listaProductos.addAll(it) }
                val nombres = mutableListOf("Seleccionar producto") +
                        listaProductos.sortedBy { it.nombre }.map {
                            "${it.nombre} - ${it.color} - ${it.talla}"
                        }
                spinnerProducto.adapter = ArrayAdapter(
                    this@AgregarVenta,
                    android.R.layout.simple_spinner_item,
                    nombres
                ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            }
            override fun onCancelled(err: DatabaseError) {
                Toast.makeText(this@AgregarVenta, "Error: ${err.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Selección spinner
        spinnerProducto.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                if (pos == 0) {
                    // limpiar y ocultar todo
                    itemRow.visibility = View.GONE
                    tvPrecioItem.visibility = View.GONE
                    separadorPrecio.visibility = View.GONE
                    llAddDescuento.visibility = View.GONE
                    rowDescuento.visibility = View.GONE
                    return
                }
                itemRow.visibility = View.VISIBLE
                tvPrecioItem.visibility = View.VISIBLE
                separadorPrecio.visibility = View.VISIBLE
                llAddDescuento.visibility = View.VISIBLE
                rowDescuento.visibility = View.VISIBLE

                val prod = listaProductos.sortedBy { it.nombre }.getOrNull(pos - 1) ?: return
                val sel = ProductoSeleccionado(prod)
                productosSeleccionados.add(sel)
                updateItemViews(sel)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // + / –
        btnSumar.setOnClickListener {
            val sel = productosSeleccionados.lastOrNull() ?: return@setOnClickListener
            sel.cantidad++
            updateItemViews(sel)
        }
        btnRestar.setOnClickListener {
            val sel = productosSeleccionados.lastOrNull() ?: return@setOnClickListener
            if (sel.cantidad > 1) {
                sel.cantidad--
                updateItemViews(sel)
            }
        }

        // TextWatchers…
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

        // **2) Cerrar descuento**
        btnCerrarDescuento.setOnClickListener {
            Toast.makeText(this, "Cerrando sección de descuento", Toast.LENGTH_SHORT).show()

            // 1) Deshabilita ambos watchers
            editingPrecioNuevo = true
            editingDescuento   = true

            // 2) Oculta la sección
            llAddDescuento.visibility = View.GONE
            rowDescuento.visibility   = View.GONE

            // 3) Restaura el último producto a precio original
            productosSeleccionados.lastOrNull()?.let { sel ->
                sel.descuentoAplicado  = false
                sel.precioConDescuento = sel.precioUnitario

                // 4) Resetea los campos SIN disparar watchers
                etDescuento.setText("0")
                etPrecioNuevo.setText(sel.precioUnitario.toString())

                // 5) Actualiza línea y total
                updateItemViews(sel)
            }

            // 6) Vuelve a habilitar los watchers
            editingPrecioNuevo = false
            editingDescuento   = false
        }


        // Guardar
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

    private fun updateItemViews(sel: ProductoSeleccionado) {
        tvItemDescripcion.text = "${sel.producto.nombre} - ${sel.producto.talla} x${sel.cantidad}"
        tvPrecioItem.text = formatCurrency(sel.totalLinea())
        actualizarTotal()
    }

    private fun editarTotalYVisibilidades() {
        itemRow.visibility = View.VISIBLE
        separadorPrecio.visibility = View.VISIBLE
        llAddDescuento.visibility = View.VISIBLE
        rowDescuento.visibility = View.VISIBLE
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
