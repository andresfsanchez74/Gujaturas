package com.example.gujaturas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
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
        private val containerProductos: LinearLayout = itemView.findViewById(R.id.containerProductos)  // Contenedor de los productos
        private val txtTotalVenta: TextView = itemView.findViewById(R.id.tvPrecioVenta)  // Mostrar total de la venta
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditarVenta)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnBorrarVenta)

        fun bind(venta: Venta) {
            // Limpiar los productos previos en el contenedor
            containerProductos.removeAllViews()

            // Iterar sobre los productos de la venta y agregarlos al contenedor
            venta.productos.forEach { (idProducto, detalleVenta) ->
                // Inflar el layout para cada producto (usando el mismo layout de venta)
                val row = LayoutInflater.from(itemView.context).inflate(R.layout.item_venta, null, false)

                // Obtener las vistas del producto
                val txtNombreTallaCantidad: TextView = row.findViewById(R.id.tvNombreTallaCantidadProducto)
                val txtPrecio: TextView = row.findViewById(R.id.tvPrecioVenta)

                // Obtener el producto desde Firebase usando el idProducto
                dbRefProductos.child(idProducto).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // Verificamos si el producto existe y obtenemos sus datos
                        val producto = snapshot.getValue(Producto::class.java)
                        producto?.let {
                            // Mostrar el nombre del producto, talla y cantidad vendida
                            val nombreConTallaYCantidad = "${it.nombre} - Talla ${it.talla} x${detalleVenta.cantidad}"
                            txtNombreTallaCantidad.text = nombreConTallaYCantidad

                            // Mostrar el precio del producto
                            txtPrecio.text = "$${detalleVenta.precioLinea()}"
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Manejo de error en caso de que no se pueda acceder al producto
                    }
                })

                // Agregar la fila al contenedor de productos (containerProductos)
                containerProductos.addView(row)
            }

            // Mostrar el total de la venta
            txtTotalVenta.text = "Total: $${venta.totalCompra}"

            // Establecer las acciones de los botones de editar y borrar
            btnEdit.setOnClickListener { onEdit(venta) }
            btnDelete.setOnClickListener { onDelete(venta) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VentaVH {
        // Inflar el layout para cada venta
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
