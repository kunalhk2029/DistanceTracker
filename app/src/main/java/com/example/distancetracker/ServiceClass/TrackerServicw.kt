package com.example.distancetracker

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.distancetracker.ui.Maps_Fragment.Companion.num
import com.example.distancetracker.utils.Constants.LOCATION_FASTEST_INTERVAL
import com.example.distancetracker.utils.Constants.LOCATION_INTERVAL
import com.example.distancetracker.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.example.distancetracker.utils.Constants.NOTIFICATION_CHANNEL_ID_STATUS
import com.example.distancetracker.utils.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.distancetracker.utils.Constants.NOTIFICATION_CHANNEL_STATUS
import com.example.distancetracker.utils.Constants.SERVICE_ACTION_PAUSE
import com.example.distancetracker.utils.Constants.SERVICE_ACTION_RESUME
import com.example.distancetracker.utils.Constants.SERVICE_ACTION_START
import com.example.distancetracker.utils.Constants.SERVICE_ACTION_STOP
import com.example.distancetracker.utils.sphericalUtil.distance
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import dagger.android.AndroidInjectionKey
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TrackerService  : Service(){


    @Inject
    lateinit var notification_builder : NotificationCompat.Builder
    @Inject
    lateinit var notification_builder_status : NotificationCompat.Builder

    @Inject
    lateinit var notification_manager : NotificationManager
    @Inject
    lateinit var vibrator : Vibrator

    lateinit var fusedLocation : FusedLocationProviderClient

    companion object {
        var initial: MutableLiveData<Boolean> = MutableLiveData()
        var distance_list: MutableLiveData<MutableList<LatLng>> = MutableLiveData(mutableListOf())
        var start_time: MutableLiveData<Long> = MutableLiveData()
        var stop_time: MutableLiveData<Long> = MutableLiveData()
        var llist: MutableList<LatLng> = mutableListOf()
        var distance_list_measure: MutableList<LatLng> =mutableListOf()
        var pauseenabled: Int = 1
        var resumeenabled: Int = 0
    }
        var locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result!!)
            result!!.locations!!.let {
                for(location in it)
                {
                   val lat =LatLng(location.latitude,location.longitude)
                    llist.add(lat)
                    updatenotifiction()
                    updatelist(location)

                }
            }
        }
    }

    private fun updatenotifiction() {
        if (distance_list_measure.size == 3) {
            notification_builder.setContentTitle("Distance Walked").setContentText(
                distance(
                    distance_list_measure
                )
            ).setChannelId(
                NOTIFICATION_CHANNEL_ID.toString()
            )
            notification_manager.notify(NOTIFICATION_CHANNEL_ID, notification_builder.build())
        }
    }

    fun initialvalue()
    {
        initial.value=false
        distance_list.postValue(mutableListOf())
        start_time.value=0
        start_time.value=0

    }

   fun updatelist(location:Location)
   {


       if(distance_list_measure.size==3)
       {
           distance_list_measure.removeAt(0)
       }

       val list = distance_list
       list.value.apply {
           this?.add(LatLng(location.latitude,location.longitude))
           distance_list.postValue(this)
             distance_list_measure = this!!
         println(distance_list_measure.toString())
       }


   }
    override fun onCreate() {
        super.onCreate()
        initialvalue()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when(it.action)
            {
                SERVICE_ACTION_START->{
                    initial.value =true
                    startForegroundService()
                }

                SERVICE_ACTION_STOP->{
                    initial.value=false
                    stopForegroundService()

                }
                SERVICE_ACTION_PAUSE->{
                    initial.value=false
                    PauseForegroundService()
                }
                SERVICE_ACTION_RESUME->{
                    initial.value =true
                    ResumeForegroundService()
                }

                else-> {
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun ResumeForegroundService() {
        notification_builder_status.setContentTitle("Status").setContentText("Resumed").setAutoCancel(false).setOngoing(false).setChannelId(
            NOTIFICATION_CHANNEL_ID_STATUS.toString())
        notification_manager.cancel(NOTIFICATION_CHANNEL_ID_STATUS)
        startForegroundService()


    }

    private fun PauseForegroundService() {
        notification_builder_status.setContentTitle("Status").setContentText("Paused").setAutoCancel(false).setOngoing(false).setChannelId(
            NOTIFICATION_CHANNEL_ID_STATUS.toString())
        notification_manager.notify(NOTIFICATION_CHANNEL_ID_STATUS,notification_builder_status.build())
        stopForegroundService()
        resumeenabled=1

    }

    private fun createStatusNotificationChannel() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            val channel =NotificationChannel(NOTIFICATION_CHANNEL_ID_STATUS.toString(),
                NOTIFICATION_CHANNEL_STATUS, IMPORTANCE_HIGH)
            notification_manager.createNotificationChannel(channel)
        }
    }

    private fun stopForegroundService() {
        stopForeground(true)
        fusedLocation.removeLocationUpdates(locationCallback)
        notification_manager.cancel(NOTIFICATION_CHANNEL_ID)
        stopSelf()
        stop_time.value=(System.currentTimeMillis())
        startVibrator()
        initial.postValue(false)
        num =0.00


    }

    fun startForegroundService() {
        createStatusNotificationChannel()
        create_notification_channel()
        startForeground(NOTIFICATION_CHANNEL_ID,notification_builder.build())
        locationUpdates()
        startVibrator()
    }

    private fun startVibrator() {
        val pattern   = LongArray(4)
        pattern.set(0,0)
        pattern.set(1,380)
        pattern.set(2,220)
        pattern.set(3,380)

       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
           vibrator.vibrate(VibrationEffect.createWaveform(pattern,-1))
        }
    }

    @SuppressLint("MissingPermission")
    fun locationUpdates()
    {
        fusedLocation = LocationServices.getFusedLocationProviderClient(this)
        val locationrequest = LocationRequest().apply{
            interval = LOCATION_INTERVAL
            fastestInterval = LOCATION_FASTEST_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        fusedLocation.requestLocationUpdates(locationrequest,
        locationCallback, Looper.getMainLooper() )

        start_time.value=(System.currentTimeMillis())

    }

    @SuppressLint("NewApi")
    fun create_notification_channel()
    {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            val channel =NotificationChannel(NOTIFICATION_CHANNEL_ID.toString(),NOTIFICATION_CHANNEL_NAME,IMPORTANCE_LOW)
            notification_manager.createNotificationChannel(channel)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}