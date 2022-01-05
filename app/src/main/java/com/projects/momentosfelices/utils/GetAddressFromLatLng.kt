package com.projects.momentosfelices.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class GetAddressFromLatLng(context: Context, private val latitude: Double, private val longitude: Double){

    //Esto para tener como valor determinado en la variable corutineScope en el hilo principal o interfaz de usuario
    private var coroutineScope = CoroutineScope(Dispatchers.Main.immediate)

    private val geocoder: Geocoder = Geocoder(context, Locale.getDefault())
    private lateinit var mAddresListener: AddressListener

    fun main() = coroutineScope.launch {
        val result = doInBackground()
        onPostExecute(result)
    }

    private suspend fun doInBackground():String= withContext(Dispatchers.IO){
        try {
            val addresList: List<Address>? = geocoder.getFromLocation(latitude,longitude,1)
            if (addresList!=null && addresList.isNotEmpty()){

                val address : Address = addresList[0]
                val sb = StringBuilder()

                for(i in 0..address.maxAddressLineIndex){
                    sb.append(address.getAddressLine(i)).append(" ")
                }
                sb.deleteCharAt(sb.length -1)

                return@withContext sb.toString()
            }
        }catch (e:Exception){
            Log.e("HappyPlaces", "Unable connect to Geocoder")
        }
        return@withContext ""
    }

    private fun onPostExecute(resultString: String?){
        if (resultString == null) {
            mAddresListener.onError()
        } else {
            mAddresListener.onAddresFound(resultString)
        }
    }

    //Get y Set
    fun setAddressListener(addressListener: AddressListener) {
        mAddresListener = addressListener
    }

    fun getAddres(){
        main()
    }

    interface AddressListener{
        fun onAddresFound(addres: String)
        fun onError()
    }

}