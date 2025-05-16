package com.example.gujaturas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class VentaAdapterEstadisticas(
    private val lista: MutableList<Venta>,
    private val dbRefProductos: DatabaseReference
) : RecyclerView.Adapter<VentaAdapterEstadisticas.VentaVH>() {

    inner class VentaVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val containerProductos: LinearLayout = itemView.findViewById(R.id.containerProductos)
        private val txtTotalVenta: TextView = itemView.findViewById(R.id.tvPrecioVenta)

        fun bind(venta: Venta) {
            containerProductos.removeAllViews()
            venta.productos.forEach { (idProducto, detalleVenta) ->
                val row = LayoutInflater.from(itemView.context).inflate(R.layout.item_producto_en_venta, containerProductos, false)
                val txtNombreProducto: TextView = row.findViewById(R.id.tvNombreProductoEnVenta)
                dbRefProductos.child(idProducto).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val producto = snapshot.getValue(Producto::class.java)
                        producto?.let {
                            val nombreConTallaYCantidad = "${it.nombre} - Talla ${it.talla} x${detalleVenta.cantidad}"
                            txtNombreProducto.text = nombreConTallaYCantidad
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
                containerProductos.addView(row)
            }
            txtTotalVenta.text = "Total: $${venta.totalCompra}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VentaVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_venta_estadisticas, parent, false)
        return VentaVH(view)
    }

    override fun onBindViewHolder(holder: VentaVH, position: Int) {
        holder.bind(lista[position])
    }

    override fun getItemCount(): Int = lista.size
}
