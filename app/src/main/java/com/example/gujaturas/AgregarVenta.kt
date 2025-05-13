package com.example.gujaturas

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import java.text.NumberFormat
import java.util.*

class AgregarVenta : AppCompatActivity() {

    private lateinit var spinnerProducto: Spinner
    private lateinit var containerItems: LinearLayout
    private lateinit var tvTotalCompra: TextView
    private lateinit var btnGuardar: Button

    private lateinit var dbProductos: DatabaseReference
    private lateinit var dbVentas: DatabaseReference

    private val listaProductos = mutableListOf<Producto>()
    private val productosSeleccionados = mutableListOf<ProductoSeleccionado>()
    private var totalCompra: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_venta)

        // Footer navigation
        findViewById<LinearLayout>(R.id.navInventario)
            .setOnClickListener { startActivity(Intent(this, Inventario::class.java)) }
        findViewById<LinearLayout>(R.id.navVentas).setOnClickListener { /* ya estamos */ }
        findViewById<LinearLayout>(R.id.navEstadisticas)
            .setOnClickListener { startActivity(Intent(this, Estadisticas::class.java)) }

        // Bind views
        spinnerProducto = findViewById(R.id.spinnerProducto)
        containerItems  = findViewById(R.id.containerItems)
        tvTotalCompra   = findViewById(R.id.labelTotalCompra)
        btnGuardar      = findViewById(R.id.btnGuardarVenta)

        // Firebase refs
        dbProductos = FirebaseDatabase.getInstance().getReference("productos")
        dbVentas    = FirebaseDatabase.getInstance().getReference("ventas")

        // Carga productos en el Spinner
        dbProductos.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaProductos.clear()
                snapshot.children.mapNotNull {
                    it.getValue(Producto::class.java)?.apply { id = it.key ?: "" }
                }.also { listaProductos.addAll(it) }

                val nombres = mutableListOf("Seleccionar producto") +
                        listaProductos.sortedBy { it.nombre }
                            .map { "${it.nombre} - ${it.color} - ${it.talla}" }

                spinnerProducto.adapter = ArrayAdapter(
                    this@AgregarVenta,
                    android.R.layout.simple_spinner_item,
                    nombres
                ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Al seleccionar un producto, agregamos una fila
        spinnerProducto.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                if (pos == 0) return
                val prod = listaProductos.sortedBy { it.nombre }[pos - 1]
                val sel  = ProductoSeleccionado(prod)
                productosSeleccionados.add(sel)
                addItemRow(sel)
                calculateTotal()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Guardar todas las ventas
        btnGuardar.setOnClickListener {
            // Crear una lista de detalles de venta para cada producto seleccionado
            val detallesVenta = mutableMapOf<String, DetalleVenta>()
            productosSeleccionados.forEach { sel ->
                detallesVenta[sel.producto.id] = DetalleVenta(
                    idProducto = sel.producto.id,
                    cantidad = sel.cantidad,
                    precioUnitario = sel.precioUnitario,
                    descuentoAplicado = sel.descuentoAplicado,
                    precioConDescuento = sel.precioConDescuento,
                    nombre = sel.producto.nombre
                )
            }

            // Crear la venta con los detalles de los productos
            val vid = dbVentas.push().key ?: return@setOnClickListener
            val venta = Venta(
                id = vid,
                productos = detallesVenta,
                totalCompra = detallesVenta.values.sumOf { it.precioConDescuento * it.cantidad },
                fechaVenta = ServerValue.TIMESTAMP
            )

            // Guardar la venta
            dbVentas.child(vid).setValue(venta)

            // Restar stock de los productos
            productosSeleccionados.forEach { sel ->
                restarStock(sel.producto.id, sel.cantidad)
            }

            Toast.makeText(this, "Venta(s) registrada(s)", Toast.LENGTH_SHORT).show()
            finish()
        }

    }

    private fun addItemRow(sel: ProductoSeleccionado) {
        val row = layoutInflater.inflate(R.layout.item_venta_row, containerItems, false)
        setupRow(row, sel)
        containerItems.addView(row)
    }

    private fun setupRow(row: View, sel: ProductoSeleccionado) {
        val tvDesc             = row.findViewById<TextView>(R.id.tvDescripcion)
        val tvPrecio           = row.findViewById<TextView>(R.id.tvPrecioLinea)
        val btnPlus            = row.findViewById<ImageButton>(R.id.btnSumar)
        val btnMinus           = row.findViewById<ImageButton>(R.id.btnRestar)
        val btnDescuento       = row.findViewById<ImageButton>(R.id.btnDescuento)
        val containerDescuento = row.findViewById<LinearLayout>(R.id.containerDescuento)
        val etPrecioNuevo      = row.findViewById<EditText>(R.id.etPrecioNuevo)
        val etDescuento        = row.findViewById<EditText>(R.id.etDescuento)
        val btnCerrar          = row.findViewById<ImageButton>(R.id.btnCerrarDescuento)

        fun refresh() {
            tvDesc.text   = "${sel.producto.nombre} - ${sel.producto.talla} x${sel.cantidad}"
            tvPrecio.text = formatCurrency(sel.totalLinea())
            calculateTotal()
        }

        // Flags para evitar recursión entre watchers
        var editingPrecio    = false
        var editingDescuento = false

        // Inicialización de campos
        etPrecioNuevo.setText(sel.precioUnitario.toString())
        etDescuento.setText("0")
        containerDescuento.visibility = View.GONE

        // + / –
        btnPlus.setOnClickListener {
            sel.cantidad++
            refresh()
        }
        btnMinus.setOnClickListener {
            if (sel.cantidad > 1) {
                sel.cantidad--
                refresh()
            }
        }

        // Toggle sección de descuento
        btnDescuento.setOnClickListener {
            containerDescuento.visibility =
                if (containerDescuento.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        // TextWatcher para Precio Nuevo → % Descuento
        val precioWatcher = object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (editingPrecio) return
                val np = s.toString().toDoubleOrNull() ?: return

                if (np > sel.precioUnitario) {
                    editingPrecio = true
                    etPrecioNuevo.setText(sel.precioUnitario.toString())
                    editingPrecio = false
                    return
                }

                sel.aplicarPrecioNuevo(np)
                sel.descuentoAplicado = true
                val porc = (1 - sel.precioConDescuento / sel.precioUnitario) * 100

                editingDescuento = true
                etDescuento.setText(String.format(Locale.getDefault(), "%.1f", porc))
                editingDescuento = false

                refresh()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        // TextWatcher para % Descuento → Precio Nuevo
        val descuentoWatcher = object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (editingDescuento) return
                val pct = s.toString().toDoubleOrNull() ?: return

                if (pct < 0 || pct > 100) {
                    editingDescuento = true
                    etDescuento.setText("0")
                    editingDescuento = false
                    return
                }

                sel.aplicarDescuentoPorcentaje(pct)
                sel.descuentoAplicado = true

                editingPrecio = true
                etPrecioNuevo.setText(String.format(Locale.getDefault(), "%.2f", sel.precioConDescuento))
                editingPrecio = false

                refresh()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        // Asociar watchers
        etPrecioNuevo.addTextChangedListener(precioWatcher)
        etDescuento.addTextChangedListener(descuentoWatcher)

        // Botón para cerrar descuento
        btnCerrar.setOnClickListener {
            try {
                // Bloquear ambos watchers mientras reseteamos
                editingPrecio = true
                editingDescuento = true

                sel.descuentoAplicado  = false
                sel.precioConDescuento = sel.precioUnitario

                etPrecioNuevo.setText(sel.precioUnitario.toString())
                etDescuento.setText("0")
                containerDescuento.visibility = View.GONE
                refresh()
            } catch (e: Exception) {
                Log.e("AgregarVenta", "Error al cerrar descuento", e)
                Toast.makeText(this, "Error al procesar el descuento", Toast.LENGTH_SHORT).show()
            } finally {
                editingPrecio = false
                editingDescuento = false
            }
        }

        // Inicializa la UI con valores correctos
        refresh()
    }

    private fun calculateTotal() {
        totalCompra = productosSeleccionados.sumOf { it.totalLinea() }
        tvTotalCompra.text = "Total de la Compra: ${formatCurrency(totalCompra)}"
    }

    private fun restarStock(id: String, qty: Int) {
        val ref = dbProductos.child(id)
        ref.child("cantidad").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(ds: DataSnapshot) {
                val actual = ds.getValue(Int::class.java) ?: return
                ref.child("cantidad").setValue(actual - qty)
            }
            override fun onCancelled(err: DatabaseError) {}
        })
    }

    private fun formatCurrency(value: Double): String =
        NumberFormat.getCurrencyInstance(Locale.getDefault()).format(value)
}
