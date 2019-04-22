package com.ash2osh.fetchtest

import android.app.*
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import androidx.preference.PreferenceManager
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.Func
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import kotlin.coroutines.CoroutineContext


class DownloadService : IntentService("download-service")
    , KodeinAware, CoroutineScope {

    override val kodein: Kodein by closestKodein()
    private val fetch: Fetch by instance()
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val ONGOING_NOTIFICATION_ID = 989
    private val TAG = "DownloadService->"
    private var isUpAndRunning = false
    private var downloadCount = 1
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MyApp.context)

    companion object {
        const val DOWNLOAD_GROUP_ID = 7777
        const val BROADCAST_ACTION = "com.ash2osh.fetchtest.DOWNLOAD_NOTIFICATION"
        const val COMMAND = "CMD"
        const val COMMAND_STOP = 333
        const val COMMAND_PAUSE = 111
        const val COMMAND_RESUME = 222
        const val COMMAND_ADD = 1
        const val COMMAND_NOTIFY = 2 //changes notification text
        const val COMMAND_UPDATE_NOTIFICATION = 3
        fun downloadFilter(download: Download): Boolean {
            return (download.status == Status.DOWNLOADING
                    || download.status == Status.QUEUED
                    || download.status == Status.ADDED
                    || download.status == Status.PAUSED
                    || download.status == Status.COMPLETED)
        }
    }

    private var notificationManager: NotificationManager? = null
    //    private var contentView: RemoteViews? = null

    private val receiver = DownloadBroadcastReceiver(block = { i: Intent? ->
        onDownloadBroadcasted(i)
    })

    private fun onDownloadBroadcasted(intent: Intent?) {
        Log.d(TAG, "action received" + intent?.action)
        val action = intent?.action
        if (action == BROADCAST_ACTION) {
            val cmd = intent.getIntExtra(COMMAND, 0)
            launch {
                if (cmd != null) {
                    handleCommand(cmd, intent)
                }
            }
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "onHandleIntent fired")
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service Created")
        //notification stuff
        notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val filter = IntentFilter()
        filter.addAction(BROADCAST_ACTION)
        registerReceiver(receiver, filter)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand fired : $this")
        launch {
            if (!isUpAndRunning) {
                Log.d(TAG, "first start")

                val title = getString(R.string.downloading)
                val text = downloadCount.toString() + " " + getString(R.string.downloads_remaining)
                val icon = R.drawable.ic_file_download_black_24dp
                val notification = createNotification(title, text, icon)
                startForeground(ONGOING_NOTIFICATION_ID, notification)
                isUpAndRunning = true
            }
            if (intent != null && intent.hasExtra(COMMAND)) {
                val cmd = intent.getIntExtra(COMMAND, 0)
                Log.d(TAG, "COMMAND->$cmd")
                handleCommand(cmd, intent)
            }
        }

        // Return "sticky" for services that are explicitly
        // started and stopped as needed by the app.
        return START_STICKY
    }

    private fun handleCommand(cmd: Int, intent: Intent?) {
        Log.d(TAG, "command handled $cmd")
        when (cmd) {
            COMMAND_PAUSE -> {
                fetch.pauseGroup(DOWNLOAD_GROUP_ID)
                updateNotification("Paused")
            }
            COMMAND_RESUME -> {
                fetch.resumeGroup(DOWNLOAD_GROUP_ID)
                updateNotification(null)
            }
            COMMAND_STOP -> {
                fetch.cancelGroup(DOWNLOAD_GROUP_ID)
                stopThisService()
            }
            COMMAND_ADD -> {
                val url = intent?.getStringExtra("URL")
                val file = intent?.getStringExtra("FILE")
                val request = Request(url ?: "", file ?: "")


                val wifi = sharedPreferences.getBoolean("wifi_only", false)
                request.priority = Priority.HIGH
                request.networkType = if (wifi) NetworkType.WIFI_ONLY else NetworkType.ALL
                request.groupId = DOWNLOAD_GROUP_ID
                //request.addHeader("clientKey", "SD78DF93_3947&MANGE1WONG")

                fetch.enqueue(request, Func { result ->
                    Log.d("->", "download enqueued" + result.id)
                }, Func { error ->
                    //TODO handle error better
                    Log.e("-->", error.name, error.throwable)
                    Toast.makeText(MyApp.context, "Download fail" + error.name, Toast.LENGTH_SHORT).show()
                })
                updateNotification(null)
            }
            COMMAND_UPDATE_NOTIFICATION -> updateNotification(null)
            COMMAND_NOTIFY -> updateNotification(intent?.getStringExtra("TXT"))
        }
    }

    private fun updateNotification(stringExtra: String?) {
        Log.i(TAG, "update Notification")
        var txt = ""
        if (!stringExtra.isNullOrBlank()) {
            txt = stringExtra
        }
        refreshDownloadsCount()
        val title = getString(R.string.downloading)
        val text = downloadCount.toString() + getString(R.string.downloads_remaining) + " " + txt
        val icon = R.drawable.ic_file_download_black_24dp
        val notification = createNotification(title, text, icon)

        launch {
            notificationManager?.notify(ONGOING_NOTIFICATION_ID, notification)
        }
    }


    private fun createNotification(title: String, text: String, icon: Int): Notification? {
        val intent = Intent(this, MainActivity::class.java)
        val requestID = System.currentTimeMillis().toInt()

        // Prepare the pending intent, while specifying the graph and destination
        val pIntent = NavDeepLinkBuilder(MyApp.context)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.queueFragment)
//            .setArguments(arguments)
            .createPendingIntent()

//        val flags = PendingIntent.FLAG_CANCEL_CURRENT // cancel old intent and create new one
//        val pIntent = PendingIntent.getActivity(applicationContext, requestID, intent, flags)


        ///////pending intents
        val closeIntent = Intent(applicationContext, DownloadService::class.java)
        closeIntent.putExtra(COMMAND, COMMAND_STOP)
        val closePendingIntent =
            PendingIntent.getService(applicationContext, COMMAND_STOP, closeIntent, FLAG_UPDATE_CURRENT)

        val pauseIntent = Intent(applicationContext, DownloadService::class.java)
        pauseIntent.putExtra(COMMAND, COMMAND_PAUSE)
        val pausePendingIntent =
            PendingIntent.getService(applicationContext, COMMAND_PAUSE, pauseIntent, FLAG_UPDATE_CURRENT)

        val resumeIntent = Intent(applicationContext, DownloadService::class.java)
        resumeIntent.putExtra(COMMAND, COMMAND_RESUME)
        val resumePendingIntent =
            PendingIntent.getService(applicationContext, COMMAND_RESUME, resumeIntent, FLAG_UPDATE_CURRENT)


        /////////////////////////////////////////////////////////////////////
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //create notification channel for api >=26
            val channel = NotificationChannel(
                "dsc",
                "Academy Download Service Channel",
                NotificationManager.IMPORTANCE_HIGH
            )//low means no sound
            channel.description = "Academy Downloads Notification Channel"
            notificationManager?.createNotificationChannel(channel)
            //Notification for api >= 26


            //actions

            val actionPause = Notification.Action.Builder(
                Icon.createWithResource(applicationContext, R.drawable.ic_pause_black_24dp),
                "pause", pausePendingIntent
            ).build()

            val actionResume = Notification.Action.Builder(
                Icon.createWithResource(applicationContext, R.drawable.ic_play_arrow_black_24dp),
                "Resume", resumePendingIntent
            ).build()

            val actionClose = Notification.Action.Builder(
                Icon.createWithResource(applicationContext, R.drawable.ic_cancel_black_24dp),
                "Cancel all Downloads", closePendingIntent
            ).build()


            val builder = Notification.Builder(applicationContext, channel.id)
                //.setCustomContentView(contentView)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(icon)
                .setOnlyAlertOnce(true)//avoid noise
                .setContentIntent(pIntent)
                .setAutoCancel(false)
                .addAction(actionPause)
                .addAction(actionResume)
                .addAction(actionClose)
//                .addAction(actionClose)


            return builder.build()
        }
        /////////////////////////////////////////////////////////////////////
        else {            //NotificationCompat for api < 25
            //actions
            val actionPause = NotificationCompat.Action.Builder(
                R.drawable.ic_pause_black_24dp,
                "pause", pausePendingIntent
            ).build()

            val actionResume = NotificationCompat.Action.Builder(
                R.drawable.ic_play_arrow_black_24dp,
                "Resume", resumePendingIntent
            ).build()
            val actionClose =
                NotificationCompat.Action.Builder(
                    R.drawable.ic_cancel_black_24dp,
                    "Cancel all Downloads", closePendingIntent
                ).build()

            val builder = NotificationCompat.Builder(applicationContext, "default")
                .setSound(null)
                //.setCustomContentView(contentView)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(icon)
                .setOnlyAlertOnce(true)//avoid noise
                .setContentIntent(pIntent)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

                .addAction(actionPause)
                .addAction(actionResume)
                .addAction(actionClose)


            return builder.build()
        }


    }

    private fun refreshDownloadsCount() {


        fetch.getDownloadsInGroup(DOWNLOAD_GROUP_ID, Func {
            val c = it.filter { download ->
                downloadFilter(download)
            }.size
            if (c == 0) {
                stopThisService()
            }
            downloadCount = c
        })
    }


    private fun stopThisService() {
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy fired")
        Toast.makeText(applicationContext, getString(R.string.download_finished_toast), Toast.LENGTH_SHORT).show()
        job.cancel()
        unregisterReceiver(receiver)
        stopForeground(true)//extra precaution is good
        notificationManager?.cancel(ONGOING_NOTIFICATION_ID)//just in case
    }
}