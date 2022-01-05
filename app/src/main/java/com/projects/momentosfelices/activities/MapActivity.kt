package com.projects.momentosfelices.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.projects.momentosfelices.R
import com.projects.momentosfelices.databaseRoom.MomentosEntity

class MapActivity : AppCompatActivity(),OnMapReadyCallback {

    private var mFelicesLugaresDetalles : MomentosEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        //Recibimos los datos del adapter y guardaremos en esa variable el obj MomentosEntity para ir rescatando el titulo, descripcion..etc
        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mFelicesLugaresDetalles = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as MomentosEntity
        }


        if(mFelicesLugaresDetalles != null){
            setSupportActionBar(findViewById(R.id.toolbar_map))
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = ""
            findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_map).setNavigationOnClickListener {
                onBackPressed()
            }

            val supportMapFragment: SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            supportMapFragment.getMapAsync(this)
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        //val position = LatLng(mFelicesLugaresDetalles!!.latitud,mFelicesLugaresDetalles!!.longitud)
        val position = LatLng( 	2.4763998985291, 	-76.571601867676)
        p0!!.addMarker(MarkerOptions().position(position).title(mFelicesLugaresDetalles!!.location))
        //zoom
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position,10f)
        p0.animateCamera(newLatLngZoom)
    }
}