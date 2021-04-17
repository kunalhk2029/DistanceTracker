package com.example.distancetracker.NotificationMoudule

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.example.distancetracker.R
import com.example.distancetracker.ui.MainActivity
import com.example.distancetracker.utils.Constants.ACTION_NAVIGATE_TO_FRAGMENT_MAPS
import com.example.distancetracker.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.example.distancetracker.utils.Constants.PENDING_INTENT
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@InstallIn(ServiceComponent::class)
@Module
object NotificationModule {



    @ServiceScoped
    @Provides
    fun NotificationBuilder(@ApplicationContext context : Context,pendingIntent: PendingIntent) : NotificationCompat.Builder
    {

        return  NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID.toString())
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_baseline_walk_24)
            .setContentIntent(pendingIntent)
            .setColor(Color.BLUE)
            .setLights(Color.BLUE,100,50)
    }

    @ServiceScoped
    @Provides
    fun Pending_Intent(@ApplicationContext context : Context) : PendingIntent
    {
        return PendingIntent.getActivity(context, PENDING_INTENT,
        Intent(context, MainActivity::class.java).apply {
           this.action = ACTION_NAVIGATE_TO_FRAGMENT_MAPS
       },PendingIntent.FLAG_UPDATE_CURRENT)


    }


    @ServiceScoped
    @Provides
    fun Notification_Manager(@ApplicationContext context: Context) : NotificationManager
    {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @ServiceScoped
    @Provides
    fun Notification_Vibrator(@ApplicationContext context: Context) : Vibrator
    {
        return context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
}