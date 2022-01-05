package com.projects.momentosfelices.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.projects.momentosfelices.databaseRoom.MomentosEntity
import com.projects.momentosfelices.databinding.ActivityMomentosFelicesDetallesBinding

class MomentosFelicesDetallesActivity : AppCompatActivity() {

    private var binding : ActivityMomentosFelicesDetallesBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMomentosFelicesDetallesBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        //aqui fijamos la flecha para ir hacia atras
        setSupportActionBar(binding?.toolbarHappyDetalles)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        binding?.toolbarHappyDetalles?.setNavigationOnClickListener {
            onBackPressed()
        }

        //Recibimos los datos del adapter y guardaremos en esa variable el obj MomentosEntity para ir rescatando el titulo, descripcion..etc
        val MomentosfelicesDetalles = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as MomentosEntity

        binding?.ivPlaceImage?.setImageURI(Uri.parse(MomentosfelicesDetalles.urlImg))
        binding?.tvDescription?.text = MomentosfelicesDetalles.descripcion
        binding?.tvLocation?.text = MomentosfelicesDetalles.location


        binding?.btnViewOnMap?.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            //le pasamos el obj MomentosfelicesDetalles y vamos para MapActivity
            intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS,MomentosfelicesDetalles)
            startActivity(intent)
        }

        //Toast.makeText(this,"->${imagenUri}",Toast.LENGTH_SHORT).show()
    }//onCreate
}