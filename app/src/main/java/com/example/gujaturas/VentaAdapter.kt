package com.example.gujaturas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class VentaAdapter(
    private val lista: MutableList<Venta>,
    private val onEdit: (Venta) -> Unit,
    private val onDelete: (Venta) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Definimos tipos de vistas
    private val TYPE_HEADER = 0
    private val TYPE_VENTA = 1

    private val dbRefProductos: DatabaseReference = FirebaseDatabase.getInstance().getReference("productos")

    // Determinar el tipo de vista según el ítem
    override fun getItemViewType(position: Int): Int {
        val venta = lista[position]
        return if (venta.id.startsWith("header_")) {
            TYPE_HEADER
        } else {
            TYPE_VENTA
        }
    }

    // ViewHolder para encabezados de fecha
    inner class HeaderVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtFecha: TextView = itemView.findViewById(R.id.tvFecha)

        fun bind(venta: Venta) {
            // Extraer la fecha del ID (quitar el prefijo "header_")
            val fechaText = venta.id.replace("header_", "")
            txtFecha.text = fechaText
        }
    }

    // ViewHolder para ventas reales
    inner class VentaVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val containerProductos: LinearLayout = itemView.findViewById(R.id.containerProductos)
        private val txtTotalVenta: TextView = itemView.findViewById(R.id.tvPrecioVenta)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditarVenta)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnBorrarVenta)
        private val txtNombreTallaCantidad: TextView = itemView.findViewById(R.id.tvNombreTallaCantidadProducto)

        fun bind(venta: Venta) {
            // Ocultar el TextView principal que no queremos mostrar
            txtNombreTallaCantidad.visibility = View.GONE

            // Limpiar los productos previos en el contenedor
            containerProductos.removeAllViews()

            // Iterar sobre los productos de la venta y agregarlos al contenedor
            venta.productos.forEach { (idProducto, detalleVenta) ->
                // Inflar un layout simple para cada producto
                val row = LayoutInflater.from(itemView.context).inflate(R.layout.item_producto_en_venta, null, false)

                // Obtener la vista del producto
                val txtNombreProducto: TextView = row.findViewById(R.id.tvNombreProductoEnVenta)

                // Obtener el producto desde Firebase usando el idProducto
                dbRefProductos.child(idProducto).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val producto = snapshot.getValue(Producto::class.java)
                        producto?.let {
                            val nombreConTallaYCantidad = "${it.nombre} - Talla ${it.talla} x${detalleVenta.cantidad}"
                            txtNombreProducto.text = nombreConTallaYCantidad
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Manejo de error
                    }
                })

                // Agregar la fila al contenedor de productos
                containerProductos.addView(row)
            }

            // Mostrar el total de la venta
            txtTotalVenta.text = "Total: $${venta.totalCompra}"

            // Establecer las acciones de los botones
            btnEdit.setOnClickListener { onEdit(venta) }
            btnDelete.setOnClickListener { onDelete(venta) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_fecha_header, parent, false)
                HeaderVH(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_venta, parent, false)
                VentaVH(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val venta = lista[position]
        when (holder) {
            is HeaderVH -> holder.bind(venta)
            is VentaVH -> holder.bind(venta)
        }
    }

    override fun getItemCount(): Int = lista.size
}