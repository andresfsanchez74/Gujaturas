package com.example.gujaturas

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.gujaturas.Producto
import com.google.firebase.database.FirebaseDatabase

class AgregarProducto : AppCompatActivity() {

    private lateinit var etProducto: EditText
    private lateinit var spinnerCategoria: Spinner
    private lateinit var spinnerTalla: Spinner
    private lateinit var spinnerColor: Spinner
    private lateinit var spinnerCantidad: Spinner
    private lateinit var etPrecioBase: EditText
    private lateinit var btnImagen: LinearLayout
    private lateinit var btnGuardar: Button
    private val dbRef by lazy { FirebaseDatabase.getInstance().getReference("productos") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_producto)

        etProducto         = findViewById(R.id.etProducto)
        spinnerCategoria   = findViewById(R.id.spinnerCategoria)
        spinnerTalla       = findViewById(R.id.spinnerTalla)
        spinnerColor       = findViewById(R.id.spinnerColor)
        spinnerCantidad    = findViewById(R.id.spinnerCantidad)
        etPrecioBase       = findViewById(R.id.etPrecioBase)
        btnImagen          = findViewById(R.id.btnImagen)
        btnGuardar         = findViewById(R.id.btnGuardarProducto)

        // Ejemplo de llenado de spinners; reemplaza por tus datos reales
        listOf("Tejido", "Crochet", "Bordado").let {
            spinnerCategoria.adapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, it)
        }
        listOf("S","M","L","XL").let {
            spinnerTalla.adapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, it)
        }
        listOf("Rojo","Azul","Verde", "Blanco", "Negro", "Gris", "Rosado", "Café", "Morado", "Naranja", "Otro").let {
            spinnerColor.adapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, it)
        }
        listOf("1","2","3","4","5").let {
            spinnerCantidad.adapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, it)
        }

        btnImagen.setOnClickListener {
            Toast.makeText(this, "Selección de imagen no implementada", Toast.LENGTH_SHORT).show()
        }

        btnGuardar.setOnClickListener {
            val nombre      = etProducto.text.toString().trim()
            val categoria   = spinnerCategoria.selectedItem.toString()
            val talla       = spinnerTalla.selectedItem.toString()
            val color       = spinnerColor.selectedItem.toString()
            val cantidadStr = spinnerCantidad.selectedItem.toString()
            val precioStr   = etPrecioBase.text.toString().trim()

            if (nombre.isEmpty() || precioStr.isEmpty()) {
                Toast.makeText(this, "Completa nombre y precio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cantidad  = cantidadStr.toIntOrNull() ?: 0
            val precio    = precioStr.toDoubleOrNull()
            if (precio == null) {
                Toast.makeText(this, "Precio inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val key = dbRef.push().key
            if (key == null) {
                Toast.makeText(this, "Error generando ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val producto = Producto(
                id = key,
                nombre = nombre,
                descripcion = categoria,   // o "" si no quieres usar categoría
                cantidad = cantidad,
                valor = precio,
                color = color,
                talla = talla,
                precioMinimo = precio,
                imagenUrl = null
            )

            dbRef.child(key).setValue(producto)
                .addOnSuccessListener {
                    Toast.makeText(this, "Producto agregado", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
