package com.example.gujaturas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gujaturas.Producto
import com.example.gujaturas.ProductoAdapter
import com.google.firebase.database.*

class Inventario : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    private lateinit var adapter: ProductoAdapter
    private val lista = mutableListOf<Producto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventario)

        // Firebase RTDB
        dbRef = FirebaseDatabase.getInstance().getReference("productos")

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

        // Search icon
        findViewById<ImageView>(R.id.iconFilter).setOnClickListener {
            Toast.makeText(this, "Filtro no disponible", Toast.LENGTH_SHORT).show()
        }

        // Add product buttons
        findViewById<FrameLayout>(R.id.btnAgregarProductoContainer)
            .setOnClickListener {
                startActivity(Intent(this, AgregarProducto::class.java))
            }
        findViewById<Button>(R.id.btnAgregarPrimerProducto)
            .setOnClickListener {
                startActivity(Intent(this, AgregarProducto::class.java))
            }

        // RecyclerView
        val recycler: RecyclerView    = findViewById(R.id.recyclerInventario)
        val emptyView: LinearLayout   = findViewById(R.id.emptyInventario)

        recycler.layoutManager = GridLayoutManager(this, 2)
        adapter = ProductoAdapter(lista,
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

        // Start listening
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
                adapter.notifyDataSetChanged()
                emptyView.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Inventario, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
