package com.example.gujaturas

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.gujaturas.Producto
import com.google.firebase.database.*

class EditarProducto : AppCompatActivity() {

    private lateinit var etProducto: EditText
    private lateinit var spinnerCategoria: Spinner
    private lateinit var spinnerTalla: Spinner
    private lateinit var spinnerColor: Spinner
    private lateinit var spinnerCantidad: Spinner
    private lateinit var etPrecioBase: EditText
    private lateinit var btnImagen: LinearLayout
    private lateinit var btnActualizar: Button

    private lateinit var dbRef: DatabaseReference
    private var productoId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_producto)

        // Recoge el ID pasado en el Intent
        productoId = intent.getStringExtra("ID_PRODUCTO")
        if (productoId == null) {
            finish()
            return
        }

        // Referencia a Firebase
        dbRef = FirebaseDatabase.getInstance()
            .getReference("productos")
            .child(productoId!!)

        // Referencias a vistas
        etProducto       = findViewById(R.id.etProducto)
        spinnerCategoria = findViewById(R.id.spinnerCategoria)
        spinnerTalla     = findViewById(R.id.spinnerTalla)
        spinnerColor     = findViewById(R.id.spinnerColor)
        spinnerCantidad  = findViewById(R.id.spinnerCantidad)
        etPrecioBase     = findViewById(R.id.etPrecioBase)
        btnImagen        = findViewById(R.id.btnImagen)
        btnActualizar    = findViewById(R.id.btnActualizarProducto)

        // Poblar spinners (ejemplo; sustituye tus datos reales)
        listOf("Tejido", "Crochet", "Bordado").also {
            spinnerCategoria.adapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, it)
        }
        listOf("S","M","L","XL").also {
            spinnerTalla.adapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, it)
        }
        listOf("Rojo","Azul","Verde", "Blanco", "Negro", "Gris", "Rosado", "Café", "Morado", "Naranja", "Otro").also {
            spinnerColor.adapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, it)
        }
        listOf("1","2","3","4","5").also {
            spinnerCantidad.adapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, it)
        }

        btnImagen.setOnClickListener {
            Toast.makeText(this, "Selección de imagen no implementada", Toast.LENGTH_SHORT).show()
        }

        btnActualizar.setOnClickListener {
            updateProducto()
        }

        cargarDatosExistentes()
    }

    private fun cargarDatosExistentes() {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(Producto::class.java)?.let { prod ->
                    etProducto.setText(prod.nombre)
                    etPrecioBase.setText(prod.valor.toString())

                    (spinnerCategoria.adapter as ArrayAdapter<String>)
                        .let { spinnerCategoria.setSelection(it.getPosition(prod.descripcion)) }
                    (spinnerTalla.adapter as ArrayAdapter<String>)
                        .let { spinnerTalla.setSelection(it.getPosition(prod.talla)) }
                    (spinnerColor.adapter as ArrayAdapter<String>)
                        .let { spinnerColor.setSelection(it.getPosition(prod.color)) }
                    (spinnerCantidad.adapter as ArrayAdapter<String>)
                        .let { spinnerCantidad.setSelection(it.getPosition(prod.cantidad.toString())) }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EditarProducto, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateProducto() {
        val nombre    = etProducto.text.toString().trim()
        val categoria = spinnerCategoria.selectedItem.toString()
        val talla     = spinnerTalla.selectedItem.toString()
        val color     = spinnerColor.selectedItem.toString()
        val cantidad  = spinnerCantidad.selectedItem.toString().toIntOrNull() ?: 0
        val precio    = etPrecioBase.text.toString().toDoubleOrNull()

        if (nombre.isEmpty() || precio == null) {
            Toast.makeText(this, "Nombre y precio obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = mapOf<String, Any>(
            "nombre"      to nombre,
            "descripcion" to categoria,
            "talla"       to talla,
            "color"       to color,
            "cantidad"    to cantidad,
            "valor"       to precio
        )

        dbRef.updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
