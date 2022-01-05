package com.projects.momentosfelices.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.projects.momentosfelices.databaseRoom.MomentosApp
import com.projects.momentosfelices.databaseRoom.MomentosDao
import com.projects.momentosfelices.databaseRoom.MomentosEntity
import com.projects.momentosfelices.R
import com.projects.momentosfelices.databinding.ActivityAddMomentosFelicesBinding
import com.projects.momentosfelices.utils.GetAddressFromLatLng
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddMomentosFelicesActivity : AppCompatActivity(), View.OnClickListener {

    /**/
    private var calendario = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var binding : ActivityAddMomentosFelicesBinding? = null
    private var saveImageToInternalStorage: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0
    private var mFelicesLugaresDetalles : MomentosEntity? = null
    private lateinit var mFusedLocationClient: FusedLocationProviderClient


    private val openGalleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        //Aqui obtendremos los resultados
            result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null){
                    //Aqui fijamos la imagen que seleccionamos en la galeria
                    binding?.ivImage?.setImageURI(result.data?.data)

                    //Guardamos la imagen en el dispositivo
                    val image = findViewById<androidx.appcompat.widget.AppCompatImageView>(R.id.ivImage)
                    //path Donde va quedar guardada la img
                    saveImageToInternalStorage = saveImageToInternalStorage(getBitmapFromView(image))
                    //Log.e("save image","path:: $saveImageToInternalStorage")

                }
    }

    private val openCameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        //Aqui obtendremos los resultados
            result ->
                if (result.resultCode == Activity.RESULT_OK){
                    //Toast.makeText(this,"Tdo bien", Toast.LENGTH_LONG).show()
                    val data = result.data
                    val nail: Bitmap = data!!.extras!!.get("data") as Bitmap
                    binding?.ivImage?.setImageBitmap(nail)

                    //Guardamos la imagen en el dispositivo
                    saveImageToInternalStorage = saveImageToInternalStorage(nail)
                    Log.e("save image","path:: $saveImageToInternalStorage")
                }
    }

    private val googleMapsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Toast.makeText(this,"Todo bien", Toast.LENGTH_LONG).show()

                val data = result.data
                val place : Place = Autocomplete.getPlaceFromIntent(data!!)

                binding?.etLocation?.setText(place.address)
                mLatitude = place.latLng!!.latitude
                mLongitude = place.latLng!!.longitude
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMomentosFelicesBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        //aqui fijamos la flecha para ir hacia atras
        setSupportActionBar(binding?.toolbarAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "back"
        binding?.toolbarAddPlace?.setNavigationOnClickListener{
            onBackPressed()
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //si places no es inicializado -> inicializate
        if(!Places.isInitialized()){
            Places.initialize(this@AddMomentosFelicesActivity,resources.getString(R.string.google_maps_api_key))
        }

        //Recibimos los datos del adapter
        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mFelicesLugaresDetalles = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as MomentosEntity
        }


        dateSetListener = DatePickerDialog.OnDateSetListener {
                _, year, month, dayOfMonth ->
            calendario.set(Calendar.YEAR, year)
            calendario.set(Calendar.MONTH, month)
            calendario.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            //Con esta funcion fijaremos la fecha en el campo de texto Date
            updateDateInView()
        }
        //Para que se agrege por default la fecha del dia actual
        updateDateInView()

        //Aqui son diferentes de null por que ya tenemos  fijados en la Entity el titulo,descripcion ..etc
        if(mFelicesLugaresDetalles != null){
            supportActionBar?.title = "Editar Lugares Felices"

            binding?.etTitle?.setText(mFelicesLugaresDetalles!!.tiutlo)
            binding?.etDescription?.setText(mFelicesLugaresDetalles!!.descripcion)
            binding?.etdate?.setText(mFelicesLugaresDetalles!!.date)
            binding?.etLocation?.setText(mFelicesLugaresDetalles!!.location)


            saveImageToInternalStorage = Uri.parse(mFelicesLugaresDetalles!!.urlImg)

            binding?.ivImage?.setImageURI(saveImageToInternalStorage)

            binding?.btnSave?.text = "Actualizar"
        }

        //Estos listener hacen parte de la funcion override fun onClick..
        binding?.btnSave?.setOnClickListener(this)
        binding?.etdate?.setOnClickListener(this)
        binding?.tvAddImage?.setOnClickListener(this)
        binding?.etLocation?.setOnClickListener(this)
        binding?.tvSelectCurrentLocation?.setOnClickListener(this)
    }//onCreate

    private fun isLocationEnabled():Boolean{
        val locationManager : LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }



    @SuppressLint("missingPermission")
    private fun requestNewLocationData(){
        var mLocationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000
            numUpdates = 1

        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallBack, Looper.myLooper())
    }

    private val mLocationCallBack = object : LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            val mLastLocation: Location = p0!!.lastLocation
            mLatitude = mLastLocation.latitude
            Log.i("Current latitude","$mLatitude")
            mLongitude = mLastLocation.longitude
            Log.i("Current longitud","$mLongitude")

            val addressTask = GetAddressFromLatLng(this@AddMomentosFelicesActivity,mLatitude,mLongitude)
            addressTask.setAddressListener(object: GetAddressFromLatLng.AddressListener{
                override fun onAddresFound(addres: String) {
                    Log.e("Address ::", "" + addres)
                    binding?.etLocation?.setText(addres)
                }

                override fun onError() {
                    Log.e("Get Address ::", "Something is wrong...")
                }
            })
            addressTask.getAddres()
        }
    }

    private fun getBitmapFromView(view: View): Bitmap {

        //Define a bitmap with the same size as the view.
        // CreateBitmap : Returns a mutable bitmap with the specified width and height
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        //Bind a canvas to it
        //La clase Canvas representa una superficie donde podemos dibujar.
        val canvas = Canvas(returnedBitmap)
        //Get the view's background
        val bgDrawable = view.background
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas)
        } else {
            //no tiene fondo dibujable, entonces dibuja un fondo blanco en el lienzo
            canvas.drawColor(Color.WHITE)
        }
        // draw the view on the canvas
        view.draw(canvas)
        //return the bitmap
        return returnedBitmap
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        //var result = ""
        val wrapper = ContextWrapper(applicationContext)
        //El modo privado es para que este archivo solo sea accesible desde la aplicación de llamada
        //Entonces, básicamente, otras aplicaciones no podrán acceder a este directorio.
        var file = wrapper.getDir("Lugares", Context.MODE_PRIVATE)

        /*
        //val bytes = ByteArrayOutputStream() // Creates a new byte array output stream.
        // The buffer capacity is initially 32 bytes, though its size increases if necessary.

        //bitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)
        //var file = File(externalCacheDir?.absoluteFile.toString() + File.separator + "Lugares Felices" + System.currentTimeMillis() / 1000 + ".jpg")

        //Entonces decimos, OK, el archivo debe estar en este directorio y luego debe tener un identificador único aleatorio
        //Y con esto nos aseguramos de que cada imagen sea única.
        */
        file = File(file, "${UUID.randomUUID()}.jpg")

        /*
        val fo =
            FileOutputStream(file) // Creates a file output stream to write to the file represented by the specified object.
        fo.write(bytes.toByteArray()) // Writes bytes from the specified byte array to this file output stream.
        fo.close() // Closes this file output stream and releases any system resources associated with this stream. This file output stream may no longer be used for writing bytes.
        result = file.absolutePath // The file absolute path is return as a result.
        */

        try {
            val stream : OutputStream = FileOutputStream(file)
            //Aqui comprimimos JPEG aquí la calidad, voy a usar el 100 por ciento
            bitmap.compress(Bitmap.CompressFormat.JPEG,100, stream)
            //debemos vaciar el flujo una vez que hayamos terminado de comprimir, así que vaciarlo y luego cerrar
            stream.flush()
            stream.close()
        }catch (e: IOException){
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)

        //return result
    }

    override fun onClick(v: View?) {
        val momentosDao = (application as MomentosApp).db.momentosDao()
        //cuando el boton seleccionado v!!.id == R.id.etdate mostrar el calendario
        //
        when(v!!.id){
            R.id.etdate -> {
                DatePickerDialog(this,
                    dateSetListener,
                    calendario.get(Calendar.YEAR),
                    calendario.get(Calendar.MONTH),
                    calendario.get(Calendar.DAY_OF_MONTH)).show()
            }//
            R.id.tv_add_image -> {
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Selecciona Accion")
                val pictureDialogItems = arrayOf("Selecciona foto de la galeria","capture foto de la camara")
                pictureDialog.setItems(pictureDialogItems){
                        _, which ->
                    when(which){
                        0 -> escogerFotoDeGaleria()
                        1 -> takePhotoFromCamera()
                    }
                }
                pictureDialog.show()
            }//
            R.id.btn_save -> {
                when{
                    binding?.etTitle?.text.isNullOrEmpty()->{
                        Toast.makeText(this,"Por favor selecciona un titulo", Toast.LENGTH_LONG).show()
                    }binding?.etDescription?.text.isNullOrEmpty()->{
                    Toast.makeText(this,"Por favor selecciona una Descripcion", Toast.LENGTH_LONG).show()
                    }binding?.etLocation?.text.isNullOrEmpty()->{
                        Toast.makeText(this,"Por favor selecciona una Descripcion", Toast.LENGTH_LONG).show()
                    }saveImageToInternalStorage == null ->{
                        Toast.makeText(this, "Por favor selecciona una imagen", Toast.LENGTH_SHORT).show()
                    }else ->{
                        if(mFelicesLugaresDetalles == null){
                            addMomento(momentosDao)
                        }else{
                            updateMomento(mFelicesLugaresDetalles!!.id,momentosDao)
                        }
                    }
                }
            }R.id.tv_select_current_location ->{
                if(!isLocationEnabled()){
                    Toast.makeText(this,"Mal",Toast.LENGTH_SHORT).show()
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }else{
                    Dexter.withContext(this).withPermissions(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION).withListener(object : MultiplePermissionsListener{
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?){
                            if(report!!.areAllPermissionsGranted()){
                                //Toast.makeText(this@AddMomentosFelicesActivity,"Bien",Toast.LENGTH_SHORT).show()
                                requestNewLocationData()
                            }
                        }
                        //mostramos un dialogo para los permisos
                        override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken){
                            showRationalDialogForPermissions()
                        }
                    }).onSameThread().check()
                }

            }R.id.et_location ->{
                try {
                    // Create a new PlacesClient instance
                    val placesClient = Places.createClient(this)

                    val fields = listOf(Place.Field.ID,Place.Field.NAME,Place.Field.LAT_LNG,Place.Field.ADDRESS)
                    val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this)
                    googleMapsLauncher.launch(intent)
                }catch(e:Exception){
                    e.printStackTrace()
                    Toast.makeText(this, "Error!!", Toast.LENGTH_SHORT).show()
                }
            }
        }//when
    }//onClick

    private fun updateMomento(id:Int,placesDao: MomentosDao){
        val title = binding?.etTitle?.text.toString()
        val description = binding?.etDescription?.text.toString()
        val date = binding?.etdate?.text.toString()
        val location = binding?.etLocation?.text.toString()
        val urlImg = saveImageToInternalStorage.toString()

        lifecycleScope.launch {
            placesDao.update(MomentosEntity(id, title, description, date, urlImg, location))
            Toast.makeText(applicationContext, "Updated.", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun addMomento(placesDao: MomentosDao){
        val title = binding?.etTitle?.text.toString()
        val description = binding?.etDescription?.text.toString()
        val date = binding?.etdate?.text.toString()
        val location = binding?.etLocation?.text.toString()

        lifecycleScope.launch{
            //Insertamos en la base de datos el titulo,descripcion el date ... que llenamos en los campos
            val error = placesDao.insert(MomentosEntity(tiutlo = title, descripcion = description, date = date, urlImg = saveImageToInternalStorage.toString(), location = location))
            Toast.makeText(applicationContext, "correcto", Toast.LENGTH_SHORT).show()
        }
    }

    private fun takePhotoFromCamera(){
        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?){
                //SI Tienes los permisos otorgados
                if(report!!.areAllPermissionsGranted()){
                    //En galleyIntent guardaremos la foto que seleccionamos en la galery
                    val galleryIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    openCameraLauncher.launch(galleryIntent)
                }
            }
            //mostramos un dialogo para los permisos
            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken){
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()
    }

    private fun escogerFotoDeGaleria(){
        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE).withListener(object:
            MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?){
                //SI Tienes los permisos otorgados
                if(report!!.areAllPermissionsGranted()){
                    //En galleyIntent guardaremos la foto que seleccionamos en la galery
                    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

                    openGalleryLauncher.launch(galleryIntent)
                }
            }
            override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken){
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()
    }

    private fun showRationalDialogForPermissions(){
        AlertDialog.Builder(this).setMessage(""+"parece que has desactivado el permiso requerido"+
                "para esta caracteristica puedes"+
                "habilitarlo en la configuracion").setPositiveButton("Ir a configuracion") {
                _, _ ->
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
        }.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }.show()
    }

    private fun updateDateInView(){
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat,Locale.getDefault())
        binding?.etdate?.setText(sdf.format(calendario.time).toString())
    }
}