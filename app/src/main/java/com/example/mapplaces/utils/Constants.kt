package com.example.mapplaces.utils

import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.Toast
import com.example.mapplaces.R
import com.example.mapplaces.models.Point

object Constants {
    private var mProgressDialog: Dialog? = null
    const val BASE_URL: String = "https://catalog.api.2gis.com/"
    const val API_KEY: String = "ruzyuk0593"
    var ADDRESS_DATA: String = "ADDRESS_DATA"
    var RESULT_POINT = Point(0.0,0.0)

     fun makeToast(text: String, context: Context, longShow: Boolean = false){
        if(longShow)
            Toast.makeText(context, text, Toast.LENGTH_LONG).show()
        else
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }
    fun showCustomProgressDialog(context: Context){
        mProgressDialog = Dialog(context)
        mProgressDialog!!.setContentView(R.layout.dialog_custom_progress)
        mProgressDialog!!.show()
    }

    fun hideProgressDialog(){
        if(mProgressDialog != null){
            mProgressDialog!!.dismiss()
        }
    }
    fun isNetWorkAvailable(context: Context): Boolean{
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when{
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ->  true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ->  true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ->  true
                else ->  false
            }
        }else{
            val networkInfo = connectivityManager.activeNetworkInfo
            return  networkInfo != null && networkInfo.isConnectedOrConnecting
        }


    }
}