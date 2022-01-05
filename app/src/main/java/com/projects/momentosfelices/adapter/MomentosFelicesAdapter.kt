package com.projects.momentosfelices.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.projects.momentosfelices.activities.AddMomentosFelicesActivity
import com.projects.momentosfelices.activities.MainActivity
import com.projects.momentosfelices.databaseRoom.MomentosEntity
import com.projects.momentosfelices.databinding.ItemMomentosBinding

//ArrayList son todos los datos de la Entity
class MomentosFelicesAdapter(private val context: Context,
                             private val items: ArrayList<MomentosEntity>,
                             private val onClicklistener : OnItemClickListener,
                             private val deleteListener:(id:Int)->Unit
                             //private val upadteListener: (id:Int)->Unit,
                             //private val deleteListener: (id:Int)->Unit
):RecyclerView.Adapter<MomentosFelicesAdapter.ViewHolder>() {


    //para acceder a los valores del constructor de la clase padre tiene que ser inner
    inner class ViewHolder(binding : ItemMomentosBinding): RecyclerView.ViewHolder(binding.root),View.OnClickListener{
        val llmain = binding?.llMain
        val ivPlaceImage = binding?.ivPlaceImage
        val tvTitle = binding?.tvTitle
        val tvDescription = binding?.tvDescription

        //constructor
        init {
            binding?.llMain?.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position: Int = adapterPosition
            val item = items[position]
            if(position != RecyclerView.NO_POSITION){
                onClicklistener.onItemClick(position,item)
            }
        }
    }

    interface OnItemClickListener{
        fun onItemClick(position: Int, entity: MomentosEntity)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemMomentosBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val item = items[position]

        //lo que queremos que aparezca en el recyclerView lo colocamos aqui
        holder.tvTitle.text = item.tiutlo
        holder.tvDescription.text = item.descripcion
        holder.ivPlaceImage.setImageURI(Uri.parse(item.urlImg))


        /* Esto por si queremos cambiar de color los recyclerView
        if (position % 2 == 0) {
            holder.llmain.setBackgroundColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.teal_700
                ))
        } else {
            holder.llmain.setBackgroundColor(ContextCompat.getColor(context, R.color.teal_200))
        }*/

        //Listener de los recyclerView
        /*
        holder.llmain.setOnClickListener {
            //Toast.makeText(context , "correcto", Toast.LENGTH_SHORT).show()
            val intent = Intent(context,MomentosFelicesDetallesActivity::class.java)

        }*/
    }
    //
    fun removeAt(position: Int){
        val item = items[position]
        deleteListener.invoke(item.id)
        notifyItemChanged(position)
    }


    //
    fun notifyEditItem(activity: Activity, position: Int, requestCode: Int){
        val intent = Intent(context, AddMomentosFelicesActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS,items[position])
        activity.startActivity(intent)
        //activity.startActivityForResult(intent,requestCode)// Activity is started with requestCode
        notifyItemChanged(position)// Para que no se quede verde al momento de deslizar
    }

    override fun getItemCount(): Int {
        return items.size
    }
}



