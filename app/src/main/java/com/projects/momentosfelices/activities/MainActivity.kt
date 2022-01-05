package com.projects.momentosfelices.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.happyplaces.utils.SwipeToDeleteCallback
import com.projects.momentosfelices.databaseRoom.MomentosApp
import com.projects.momentosfelices.databaseRoom.MomentosDao
import com.projects.momentosfelices.databaseRoom.MomentosEntity
import com.projects.momentosfelices.adapter.MomentosFelicesAdapter
import com.projects.momentosfelices.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import pl.kitek.rvswipetodelete.SwipeToEditCallback

class MainActivity : AppCompatActivity(), MomentosFelicesAdapter.OnItemClickListener{

    private var binding : ActivityMainBinding? = null
    private var mFelicesLugaresDetalles : MomentosEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val momentosDao = (application as MomentosApp).db.momentosDao()

        binding?.fabAddPlace?.setOnClickListener{
            val intent = Intent(this, AddMomentosFelicesActivity::class.java)
            startActivity(intent)
        }
        if(intent.hasExtra(EXTRA_PLACE_DETAILS)){
            mFelicesLugaresDetalles = intent.getSerializableExtra(EXTRA_PLACE_DETAILS) as MomentosEntity
        }

        //
            lifecycleScope.launch {
                //Como vemos aqui ya solicitamos los datos fuera del hilo principal gracias a los flow del metodo .collect
                momentosDao.fetchAllMoments().collect {
                    val list = ArrayList(it)
                    setupListOfDataIntoRecyclerView(list, momentosDao)
                }
            }
    }//onCreate

    //Aqui mostramos ya los data dentro del recyclerView
    private fun setupListOfDataIntoRecyclerView(momentosList:ArrayList<MomentosEntity>, momentosDao: MomentosDao){
        if(momentosList.isNotEmpty()){
            //instancia de la clase MomentosFelicesAdapter
            val itemAdapter = MomentosFelicesAdapter(this,momentosList,this,{deleteId -> delete(deleteId,momentosDao)})
            binding?.rvHappyPlacesList?.layoutManager = LinearLayoutManager(this)
            binding?.rvHappyPlacesList?.adapter = itemAdapter
            binding?.rvHappyPlacesList?.visibility = View.VISIBLE
            binding?.tvNoRecordsAvailable?.visibility = View.GONE
        }else{
            binding?.rvHappyPlacesList?.visibility = View.GONE
            binding?.tvNoRecordsAvailable?.visibility = View.VISIBLE
        }

        //Aqui uniremos swipe en nuestros recyvlerView y editaremos deslizando a la derecha
        val editSwiperHandler = object : SwipeToEditCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding?.rvHappyPlacesList?.adapter as MomentosFelicesAdapter
                adapter.notifyEditItem(this@MainActivity,viewHolder.adapterPosition, ADD_PLACE_ACTIVITY_REQUEST_CODE)
            }
        }

        val editItemTouchHelper = ItemTouchHelper(editSwiperHandler)
        editItemTouchHelper.attachToRecyclerView(binding?.rvHappyPlacesList)

        //Aqui uniremos swipe en nuestros recyvlerView y eliminaremos deslizando a la izquierda
        val deleteSwiperHandler = object : SwipeToDeleteCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding?.rvHappyPlacesList?.adapter as MomentosFelicesAdapter
                adapter.removeAt(viewHolder.adapterPosition)

            }
        }

        val deleteItemTouchHelper = ItemTouchHelper(deleteSwiperHandler)
        deleteItemTouchHelper.attachToRecyclerView(binding?.rvHappyPlacesList)
    }

    //Delete
    private fun delete(id:Int,placesDao: MomentosDao){
        lifecycleScope.launch {
            placesDao.delete(MomentosEntity(id))
            Toast.makeText(applicationContext, "Record deleted successfully.", Toast.LENGTH_LONG).show()
        }
    }

    //listener de los recyclerView
    override fun onItemClick(position: Int,entity: MomentosEntity) {
        //Toast.makeText(this,"click",Toast.LENGTH_SHORT).show()
        val intent = Intent(this,MomentosFelicesDetallesActivity::class.java)
        //le pasamos los datos al otro activity, el 'name' y el 'value'
        intent.putExtra(EXTRA_PLACE_DETAILS,entity)
        startActivity(intent)
    }

    companion object{
        private const val ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
        internal const val EXTRA_PLACE_DETAILS = "extra_place_details"
    }

}