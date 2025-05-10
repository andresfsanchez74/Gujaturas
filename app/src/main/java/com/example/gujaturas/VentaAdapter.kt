package com.example.gujaturas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gujaturas.R
import com.example.gujaturas.Venta
import com.google.firebase.database.*

class VentaAdapter(
    private val lista: MutableList<Venta>,
    private val onEdit: (Venta) -> Unit,
    private val onDelete: (Venta) -> Unit
) : RecyclerView.Adapter<VentaAdapter.VentaVH>() {

    private val dbRefProductos: DatabaseReference = FirebaseDatabase.getInstance().getReference("productos")

    inner class VentaVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtNombreTallaCantidad: TextView = itemView.findViewById(R.id.tvNombreTallaCantidadProducto)
        private val txtPrecio: TextView = itemView.findViewById(R.id.tvPrecioVenta)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditarVenta)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnBorrarVenta)

        fun bind(venta: Venta) {
            // Obtener el producto de la base de datos usando idProducto
            dbRefProductos.child(venta.idProducto.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Verificamos si el producto existe y obtenemos sus datos
                    val producto = snapshot.getValue(Producto::class.java)
                    producto?.let {
                        // Concatena el nombre del producto, talla y cantidad vendida
                        val nombreConTallaYCantidad = "${it.nombre} - Talla ${it.talla} x${venta.cantidadVendida}"
                        txtNombreTallaCantidad.text = nombreConTallaYCantidad
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Manejo de error en caso de que no se pueda acceder al producto
                }
            })

            // Mostrar precio de la venta
            txtPrecio.text = "$${venta.precioVenta}"

            // Establecer las acciones de los botones
            btnEdit.setOnClickListener { onEdit(venta) }
            btnDelete.setOnClickListener { onDelete(venta) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VentaVH {
        // Inflar el layout para cada item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_venta, parent, false)
        return VentaVH(view)
    }

    override fun onBindViewHolder(holder: VentaVH, position: Int) {
        // Enlazar la venta con la vista correspondiente
        holder.bind(lista[position])
    }

    override fun getItemCount(): Int = lista.size
}
