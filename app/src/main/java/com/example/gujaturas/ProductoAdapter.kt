package com.example.gujaturas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import androidx.recyclerview.widget.RecyclerView
import com.example.gujaturas.R
import com.example.gujaturas.Producto

class ProductoAdapter(
    private val lista: MutableList<Producto>,
    private val onEdit: (Producto) -> Unit,
    private val onDelete: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoAdapter.ProductoVH>() {

    inner class ProductoVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val img: ImageView        = itemView.findViewById(R.id.imgProducto)
        private val txtNombre: TextView   = itemView.findViewById(R.id.txtNombreProducto)
        private val txtTalla: TextView    = itemView.findViewById(R.id.txtTallaProducto)
        private val txtPrecio: TextView   = itemView.findViewById(R.id.txtPrecioProducto)
        private val btnEdit: ImageButton  = itemView.findViewById(R.id.btnEditarProducto)
        private val btnDelete: ImageButton= itemView.findViewById(R.id.btnEliminarProducto)

        fun bind(prod: Producto) {
            txtNombre.text = prod.nombre
            txtTalla.text  = "Talla: ${prod.talla}"
            txtPrecio.text = "$${prod.valor}"
            prod.imagenUrl?.let { url ->
                Glide.with(itemView.context)
                    .load(R.drawable.logo)
                    .into(img)
            }
            btnEdit.setOnClickListener { onEdit(prod) }
            btnDelete.setOnClickListener { onDelete(prod) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto_inventario, parent, false)
        return ProductoVH(view)
    }

    override fun onBindViewHolder(holder: ProductoVH, position: Int) {
        holder.bind(lista[position])
    }

    override fun getItemCount(): Int = lista.size

    fun update(newList: List<Producto>) {
        lista.clear()
        lista.addAll(newList)
        notifyDataSetChanged()
    }
}
