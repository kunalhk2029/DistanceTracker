package com.example.distancetracker.utils
import android.app.Notification
import android.content.ClipData
import android.view.MenuItem
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.distancetracker.R
import com.example.distancetracker.TrackerService
import com.example.distancetracker.ui.Maps_Fragment.Companion.num
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat
import javax.inject.Inject




object sphericalUtil
{
    fun timeElapsed(starttime:Long,stoptime:Long): String   {
        val decimalFormat = DecimalFormat("##")
        val value = stoptime-starttime
        val sec = (value/1000L)
        val min = value/(1000*60).toLong()
        val hours = value/(100*60*60).toLong()
        decimalFormat.format(sec)
        decimalFormat.format(min)
        decimalFormat.format(hours)
        return  "$hours:$min:$sec"
    }

    fun MovecamToLatestLocation(location : LatLng) : CameraPosition
    {
        val position = CameraPosition.Builder()
            .target(location)
            .zoom(18f)
            . build()
        return  position
    }

    fun distance(list : List<LatLng>) : String
    {

        val value = SphericalUtil.computeDistanceBetween(list[0],list[1]).toDouble()
        num += value.toDouble()
        val format = DecimalFormat("#.##").format(num/1000)
        return  format +" KM"
    }

    fun mapstyle(item:MenuItem,map:GoogleMap)
    {

        when(item.itemId)
        {
            R.id.satellite->
            {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            }

            R.id.hybrid->
            {
                map.mapType = GoogleMap.MAP_TYPE_HYBRID
            }

            R.id.normal->
            {
                map.mapType = GoogleMap.MAP_TYPE_NORMAL
            }

            R.id.terrain->
            {
                map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            }
        }
    }

}