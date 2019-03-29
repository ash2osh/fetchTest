package com.ash2osh.fetchtest

import android.app.*
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2core.Func
import kotlinx.coroutines.*
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import kotlin.coroutines.CoroutineContext
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent


class DownloadService : IntentService("download-service"), KodeinAware, CoroutineScope {
    override val kodein: Kodein by closestKodein()
    private val fetch: Fetch by instance()
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    val ONGOING_NOTIFICATION_ID = 989
    private val TAG = "DownloadService->"
    private var isUpAndRunning = false

    companion object {
        val DOWNLOAD_GROUP_ID = 7777
        val COMMAND = "CMD"
        val COMMAND_STOP = 333
        val COMMAND_PAUSE = 111
        val COMMAND_RESUME = 222
        val COMMAND_ADD = 1
    }

    private var notificationManager: NotificationManager? = null
    //    private var contentView: RemoteViews? = null
    private var notification: Notification? = null

    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "onHandleIntent fired")
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service Created")

    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand fired : $this")
        if (intent != null && intent.hasExtra(COMMAND)) {
            val cmd = intent?.getIntExtra(COMMAND, 0)
            Log.d(TAG, "COMMAND->$cmd")
            when (cmd) {
                COMMAND_PAUSE -> fetch.pauseGroup(DOWNLOAD_GROUP_ID)
                COMMAND_RESUME -> fetch.resumeGroup(DOWNLOAD_GROUP_ID)
                COMMAND_STOP -> {
                    fetch.cancelAll()
                    stopThisService()
                }
                COMMAND_ADD -> {
                }

            }
        }
        if (!isUpAndRunning) {
            Log.d(TAG, "first start")
            launch {
                createNotification()
                startForeground(ONGOING_NOTIFICATION_ID, notification)
                isUpAndRunning = true
            }
        }
        // Return "sticky" for services that are explicitly
        // started and stopped as needed by the app.
        return START_STICKY
    }


    private fun createNotification() {
        val intent = Intent(this, MainActivity::class.java)
        val requestID = System.currentTimeMillis().toInt()
        val flags = PendingIntent.FLAG_CANCEL_CURRENT // cancel old intent and create new one
        val pIntent = PendingIntent.getActivity(applicationContext, requestID, intent, flags)


        val closeIntent = Intent(applicationContext, DownloadService::class.java)
        closeIntent.putExtra(COMMAND, COMMAND_STOP)
        val closePendingIntent =
            PendingIntent.getService(applicationContext, COMMAND_STOP, closeIntent, FLAG_UPDATE_CURRENT)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //create notification channel for api >=26
            val channel = NotificationChannel(
                "dsc",
                "Academy Download Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )//low means no sound
            channel.description = "Academy Downloads Notification Channel"
            notificationManager?.createNotificationChannel(channel)
            //Notification for api >= 26

            val actionClose = Notification.Action.Builder(
                R.drawable.ic_file_download_black_24dp,
                "Cancel all Downloads",
                closePendingIntent
            )
                .build()

            val builder = Notification.Builder(applicationContext, channel.id)
                .setSmallIcon(R.drawable.ic_file_download_black_24dp)
//                .setCustomContentView(contentView)
                .setContentTitle(getString(R.string.downloading))
                .setContentText(getDownloadsCount().toString() + getString(R.string.downloads_remaining))
                .setContentIntent(pIntent)
                .setAutoCancel(false)
                .addAction(actionClose)
//                .addAction(actionClose)
            //TODO add actions

            notification = builder.build()
        } else {
            //NotificationCompat for api < 25
            val actionClose =
                NotificationCompat.Action.Builder(
                    R.drawable.ic_file_download_black_24dp,
                    "Cancel all Downloads",
                    closePendingIntent
                )
                    .build()

            val builder = NotificationCompat.Builder(applicationContext, "default")
                .setSound(null)
                .setSmallIcon(R.drawable.ic_file_download_black_24dp)
                //.setCustomContentView(contentView)
                .setContentTitle(getString(R.string.downloading))
                .setContentText(getDownloadsCount().toString() + getString(R.string.downloads_remaining))
                .setContentIntent(pIntent)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(actionClose)
            //TODO add actions

            notification = builder.build()
        }


    }

    fun getDownloadsCount(): Int {
        var downloads = 0
        fetch.getDownloadsInGroup(DOWNLOAD_GROUP_ID, Func {
            downloads = it.size
        })
        return downloads
    }


    private fun stopThisService() {
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy fired")
        job.cancel()
    }
}