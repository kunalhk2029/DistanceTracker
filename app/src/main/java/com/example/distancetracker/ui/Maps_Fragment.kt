package com.example.distancetracker.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.distancetracker.R
import com.example.distancetracker.TrackerService
import com.example.distancetracker.TrackerService.Companion.pauseenabled
import com.example.distancetracker.TrackerService.Companion.resumeenabled
import com.example.distancetracker.TrackerService.Companion.start_time
import com.example.distancetracker.TrackerService.Companion.stop_time
import com.example.distancetracker.databinding.FragmentMapsBinding
import com.example.distancetracker.utils.Constants.SERVICE_ACTION_PAUSE
import com.example.distancetracker.utils.Constants.SERVICE_ACTION_RESUME
import com.example.distancetracker.utils.Constants.SERVICE_ACTION_START
import com.example.distancetracker.utils.Constants.SERVICE_ACTION_STOP
import com.example.distancetracker.utils.Extensions.disable
import com.example.distancetracker.utils.Extensions.enable
import com.example.distancetracker.utils.Extensions.hide
import com.example.distancetracker.utils.Extensions.show
import com.example.distancetracker.Permission.Permission.RequestBackgroundLocation
import com.example.distancetracker.Permission.Permission.hasBackgroundLocationService
import com.example.distancetracker.ResultSave.Results
import com.example.distancetracker.Store
import com.example.distancetracker.utils.sphericalUtil.MovecamToLatestLocation
import com.example.distancetracker.utils.sphericalUtil.distance
import com.example.distancetracker.utils.sphericalUtil.mapstyle
import com.example.distancetracker.utils.sphericalUtil.timeElapsed

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.JointType.ROUND
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
//import pub.devrel.easypermissions.EasyPermissions
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*


class Maps_Fragment : Fragment() , OnMapReadyCallback,GoogleMap.OnMyLocationButtonClickListener,
    com.vmadalin.easypermissions.EasyPermissions.PermissionCallbacks{

private lateinit var  map : GoogleMap
    private  lateinit var vibrator : Vibrator
    var list : MutableList<LatLng> = mutableListOf()
    var notification_intentn = MutableLiveData<Boolean>()
     private  lateinit var lay : LatLng
   companion object{
         var num : Double =0.00
   }

//    val instance = Room.databaseBuilder(
//        requireContext().applicationContext,Store.class   ,"hb").build()
//    val database : RoomDatabase = Room.databaseBuilder(requireContext(), Store.class,"gdh","").build()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)

        val view=inflater.inflate(R.layout.fragment_maps_, container, false)


//        FragmentMapsBinding.inflate(inflater,container,false)

        view.findViewById<Button>(R.id.button_reset).setOnClickListener {
        }

        view.findViewById<Button>(R.id.button_stop).setOnClickListener {
            StopForegroundService()

        }
        view.findViewById<Button>(R.id.button_start).setOnClickListener {
            onStartButtonClicked()
        }
        view.findViewById<Button>(R.id.button_pause).setOnClickListener {
           PauseForegroundService()
        }

        view.findViewById<Button>(R.id.button_resume).setOnClickListener {
            ResumeForegroundService()
        }

        return view
    }

    private fun ResumeForegroundService() {
        view?.findViewById<Button>(R.id.button_pause)!!.show()
        view?.findViewById<Button>(R.id.button_pause)!!.enable()
        view?.findViewById<Button>(R.id.button_resume)!!.hide()
        view?.findViewById<Button>(R.id.button_resume)!!.disable()
        Send_Action_Command_Service(SERVICE_ACTION_RESUME)
    }

    private fun PauseForegroundService() {
        Send_Action_Command_Service(SERVICE_ACTION_PAUSE)
        view?.findViewById<Button>(R.id.button_pause)!!.hide()
        view?.findViewById<Button>(R.id.button_pause)!!.disable()
        view?.findViewById<Button>(R.id.button_resume)!!.show()
        view?.findViewById<Button>(R.id.button_resume)!!.enable()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_style,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        mapstyle(item,map)
        return super.onOptionsItemSelected(item)
    }
    private fun StopForegroundService() {

        view?.findViewById<Button>(R.id.button_start)!!.show()
        view?.findViewById<Button>(R.id.button_stop)!!.hide()
        view?.findViewById<Button>(R.id.button_resume)!!.hide()
        view?.findViewById<Button>(R.id.button_pause)!!.hide()
        view?.findViewById<Button>(R.id.button_reset)!!.show()
        view?.findViewById<Button>(R.id.button_reset)!!.enable()

        Send_Action_Command_Service(SERVICE_ACTION_STOP)
        share_SaveResult()
        showWholeTrackedPath()

    }

    private fun showWholeTrackedPath() {
        val bound = LatLngBounds.Builder()
        for(location in list)
        {
          bound.include(location)
        }
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bound.build(),200),2000,null)
    }


    private fun share_SaveResult() {
//        if(!((stop_time.value!!)==0)) {

            lifecycleScope.launch{
                delay(500)
                val result = Results(timeElapsed(start_time.value!!, stop_time.value!!), distance(list))
                delay(2500)
                val action = Maps_FragmentDirections.actionMapsFragmentToResultFragment(result)
                findNavController().navigate(action)

            }
            println("Start="+start_time.value+"end="+ stop_time.value)
//        }
    }

    @SuppressLint("MissingPermission", "SimpleDateFormat")
    override fun onMapReady(Map: GoogleMap?) {

        map = Map!!
        lay = LatLng(28.7041, 77.1025)
        map.addMarker(MarkerOptions().position(lay).title("Marker in Sydney"))
        map.moveCamera(CameraUpdateFactory.newLatLng(lay))

        map.uiSettings.apply {
            isMyLocationButtonEnabled=true
            isZoomControlsEnabled=false
            isZoomGesturesEnabled=true
            isRotateGesturesEnabled=true
            isTiltGesturesEnabled = false
            isCompassEnabled= false
            isScrollGesturesEnabled=true
        }
        map.isMyLocationEnabled = true
        map.setOnMyLocationButtonClickListener(this)

        trackerseervice()
        val date = System.currentTimeMillis()
        val reqformat = SimpleDateFormat("dd-MMM-yy")
//        val fromformat = SimpleDateFormat(Date(date))
    val datee = reqformat.format(Date(date))
        println("DAteeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"+datee.toString())
    }
    fun trackerseervice()
    {
        val listdis = TrackerService.distance_list
      listdis.observe(viewLifecycleOwner,{
          println(list.toString())

          if(it!=null)
          {
              list.addAll(it)
//            binding.buttonStop.enable()
          }
          if(list.size>0) {
              addPolyline(list)
            map.animateCamera(CameraUpdateFactory.newCameraPosition(MovecamToLatestLocation(list.last())),1000,null)
              list.addAll(it)

          }

          if(list.size>3)
          {
              addPolyline(list)
              map.animateCamera(CameraUpdateFactory.newCameraPosition(MovecamToLatestLocation(list.last())),1000,null)
              view?.findViewById<Button>(R.id.button_stop)!!.enable()
              list.addAll(it)
              view?.findViewById<Button>(R.id.button_pause)!!.enable()
          }
      })

        notification_intentn = TrackerService.initial
        if((notification_intentn).value==true)
        {
            view?.findViewById<Button>(R.id.button_stop)!!.show()

            if(pauseenabled==1) {
                view?.findViewById<Button>(R.id.button_pause)!!.show()
            }
            if(resumeenabled==1){
                view?.findViewById<Button>(R.id.button_resume)!!.show()
            }

            map.animateCamera(CameraUpdateFactory.newCameraPosition(MovecamToLatestLocation(list.last())),1000,null)
            view?.findViewById<TextView>(R.id.my_location)!!.hide()
        }
    }

    private fun addPolyline(list : List<LatLng>) {
        val pattern = listOf(Dot(),Gap(30f))

        val polyline = PolylineOptions().apply {
            addAll(list)
            jointType(ROUND)
            startCap(RoundCap())
            endCap(RoundCap())
            color(Color.BLUE)
            width(40f)
            pattern(pattern)
        }
        map.addPolyline(polyline)

    }

    private fun onStartButtonClicked() {
        if(hasBackgroundLocationService(requireContext())) {
            StartTimer()
            view?.findViewById<Button>(R.id.button_start)!!.hide()
            view?.findViewById<Button>(R.id.button_start)!!.disable()
            view?.findViewById<Button>(R.id.button_stop)!!.disable()
            view?.findViewById<Button>(R.id.button_pause)!!.disable()

            view?.findViewById<Button>(R.id.button_stop)!!.show()
            view?.findViewById<Button>(R.id.button_pause)!!.show()

        }
        else {
            RequestBackgroundLocation(this)
        }
    }

    private fun StartTimer() {

        val timer : CountDownTimer = object : CountDownTimer(5000,1000){
            override fun onTick(millisUntilFinished: Long) {
                val timedisplay =  millisUntilFinished/1000
                view?.findViewById<Button>(R.id.button_stop)!!.disable()
                view?.findViewById<TextView>(R.id.timer_text)!!.show()
                if(timedisplay==0L)
                {
                   view?.findViewById<TextView>(R.id.timer_text)!!.text = "GO"
                    view?.findViewById<TextView>(R.id.timer_text)!!.setTextColor(ContextCompat.getColor(requireContext(),
                        R.color.black
                    ))

                }
                else {
                    view?.findViewById<TextView>(R.id.timer_text)!!.text = timedisplay.toString()
                    view?.findViewById<TextView>(R.id.timer_text)!!.setTextColor(ContextCompat.getColor(requireContext(),
                        R.color.red))
                }
            }

            override fun onFinish() {
                view?.findViewById<TextView>(R.id.timer_text)!!.hide()
                Send_Action_Command_Service(SERVICE_ACTION_START)
            }
        }
        timer.start()

    }

    fun Send_Action_Command_Service(action : String)
    {
       val intent = Intent(requireContext(), TrackerService::class.java).apply {
          this.action = action

      }
        requireContext().startService(intent)

    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
     if(com.vmadalin.easypermissions.EasyPermissions.somePermissionPermanentlyDenied(this,perms[0]))
     {
         SettingsDialog.Builder(requireContext()).build().show()
     }
        else
     {
         RequestBackgroundLocation(this)
     }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        onStartButtonClicked()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }


    @SuppressLint("MissingPermission")
    override fun onMyLocationButtonClick(): Boolean {

        view?.findViewById<TextView>(R.id.my_location)!!.animate().alpha(0f).duration = 2000
        lifecycleScope.launch {
        delay(2050)
        view?.findViewById<TextView>(R.id.my_location)!!.hide()
        view?.findViewById<Button>(R.id.button_start)!!.show()
        map.isMyLocationEnabled=true
    }
        return false
    }

}