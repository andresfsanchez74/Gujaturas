package com.example.gujaturas

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gujaturas.Producto
import com.example.gujaturas.ProductoAdapter
import com.google.firebase.database.*

class Inventario : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    private lateinit var adapter: ProductoAdapter
    private val lista = mutableListOf<Producto>()
    private val listaFiltrada = mutableListOf<Producto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventario)

        dbRef = FirebaseDatabase.getInstance().getReference("productos")

        val btnInv: LinearLayout = findViewById(R.id.navInventario)
        val btnVen: LinearLayout = findViewById(R.id.navVentas)
        val btnEst: LinearLayout = findViewById(R.id.navEstadisticas)
        val bgInv: FrameLayout = findViewById(R.id.bgCircleInventario)
        val bgVen: FrameLayout = findViewById(R.id.bgCircleVentas)
        val bgEst: FrameLayout = findViewById(R.id.bgCircleEstad)

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

        findViewById<ImageView>(R.id.iconFilter).setOnClickListener {
            showFiltroDialog()
        }

        findViewById<FrameLayout>(R.id.btnAgregarProductoContainer)
            .setOnClickListener {
                startActivity(Intent(this, AgregarProducto::class.java))
            }
        findViewById<Button>(R.id.btnAgregarPrimerProducto)
            .setOnClickListener {
                startActivity(Intent(this, AgregarProducto::class.java))
            }

        val recycler: RecyclerView = findViewById(R.id.recyclerInventario)
        val emptyView: LinearLayout = findViewById(R.id.emptyInventario)

        recycler.layoutManager = GridLayoutManager(this, 2)
        adapter = ProductoAdapter(listaFiltrada,
            onEdit = { prod ->
                Intent(this, EditarProducto::class.java).apply {
                    putExtra("ID_PRODUCTO", prod.id)
                    startActivity(this)
                }
            },
            onDelete = { prod ->
                dbRef.child(prod.id).removeValue()
            }
        )
        recycler.adapter = adapter

        fetchProductos(emptyView)
    }

    private fun fetchProductos(emptyView: View) {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val temp = mutableListOf<Producto>()
                snapshot.children.forEach { child ->
                    child.getValue(Producto::class.java)?.let { prod ->
                        prod.id = child.key ?: ""
                        temp.add(prod)
                    }
                }
                lista.clear()
                lista.addAll(temp)

                listaFiltrada.clear()
                listaFiltrada.addAll(lista)

                adapter.notifyDataSetChanged()
                emptyView.visibility = if (listaFiltrada.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Inventario, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showFiltroDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_filtro_busqueda)
        dialog.setCancelable(true)

        val spinnerCategoria = dialog.findViewById<Spinner>(R.id.spinnerCategoria)
        val spinnerColor = dialog.findViewById<Spinner>(R.id.spinnerColor)
        val spinnerTalla = dialog.findViewById<Spinner>(R.id.spinnerTalla)
        val btnAplicarFiltro = dialog.findViewById<Button>(R.id.btnAplicarFiltro)

        val categorias = listOf("", "Tejido", "Crochet", "Bordado")
        val colores = listOf("", "Rojo", "Azul", "Verde", "Blanco", "Negro", "Gris", "Rosado", "Café", "Morado", "Naranja", "Otro")
        val tallas = listOf("", "S", "M", "L", "XL")

        spinnerCategoria.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categorias)
        spinnerColor.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, colores)
        spinnerTalla.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tallas)

        btnAplicarFiltro.setOnClickListener {
            val catSeleccionada = spinnerCategoria.selectedItem.toString()
            val colorSeleccionado = spinnerColor.selectedItem.toString()
            val tallaSeleccionada = spinnerTalla.selectedItem.toString()

            filtrarProductos(
                if (catSeleccionada.isEmpty()) emptyList() else listOf(catSeleccionada),
                if (colorSeleccionado.isEmpty()) emptyList() else listOf(colorSeleccionado),
                if (tallaSeleccionada.isEmpty()) emptyList() else listOf(tallaSeleccionada)
            )
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun filtrarProductos(categorias: List<String>, colores: List<String>, tallas: List<String>) {
        val listaFiltradaTemp = lista.filter { prod ->
            val matchCategoria = categorias.isEmpty() || categorias.contains(prod.descripcion)
            val matchColor = colores.isEmpty() || colores.contains(prod.color)
            val matchTalla = tallas.isEmpty() || tallas.contains(prod.talla)
            matchCategoria && matchColor && matchTalla
        }
        listaFiltrada.clear()
        listaFiltrada.addAll(listaFiltradaTemp)
        adapter.notifyDataSetChanged()
    }
}
