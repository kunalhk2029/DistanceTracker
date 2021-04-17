package com.example.distancetracker.Permission

import android.content.Context
import android.os.Build
import androidx.fragment.app.Fragment
import com.example.distancetracker.utils.Constants.RequestBackgroundPermissionCode
import com.example.distancetracker.utils.Constants.RequestPermissionCode
import com.vmadalin.easypermissions.EasyPermissions

object Permission {


        fun checkLocationPermission(context : Context) =

            (EasyPermissions.hasPermissions(context,android.Manifest.permission.ACCESS_FINE_LOCATION))

        fun Request_Permission(activtiy : Fragment)
        {
            EasyPermissions.requestPermissions(activtiy,"This Application will not work without permission"
                ,RequestPermissionCode,
            android.Manifest.permission.ACCESS_FINE_LOCATION)
        }

       fun hasBackgroundLocationService(context: Context) : Boolean
       {

           if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q)
           {
               return EasyPermissions.hasPermissions(context,
                   android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
           }
           return true
       }

     fun RequestBackgroundLocation(fragment: Fragment)
     {
         EasyPermissions.requestPermissions(fragment,"The app will not provide you with our service without this permission"
             ,RequestBackgroundPermissionCode
             ,android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
     }

}