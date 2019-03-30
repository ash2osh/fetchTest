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
import android.content.BroadcastReceiver
import android.content.Context
import com.tonyodev.fetch2.NetworkType
import com.tonyodev.fetch2.Priority
import com.tonyodev.fetch2.Request
import android.content.IntentFilter
import android.widget.Toast


class DownloadService : IntentService("download-service")
    , KodeinAware, CoroutineScope {

    override val kodein: Kodein by closestKodein()
    private val fetch: Fetch by instance()
    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    val ONGOING_NOTIFICATION_ID = 989
    private val TAG = "DownloadService->"
    private var isUpAndRunning = false

    companion object {
        const val DOWNLOAD_GROUP_ID = 7777
        const val BROADCAST_ACTION = "com.ash2osh.fetchtest.DOWNLOAD_NOTIFACTION";
        const val COMMAND = "CMD"
        const val COMMAND_STOP = 333
        const val COMMAND_PAUSE = 111
        const val COMMAND_RESUME = 222
        const val COMMAND_ADD = 1
        const val COMMAND_NOTIFY = 2 //changes notifacation text
        const val COMMAND_UPDATE_NOTIFACATION = 3
    }

    private var notificationManager: NotificationManager? = null
    //    private var contentView: RemoteViews? = null
    private var notification: Notification? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "action received" + intent.action)
            val action = intent.action
            if (action == BROADCAST_ACTION) {
                val cmd = intent.getIntExtra(COMMAND, 0)
                launch { handleCommand(cmd, intent) }
            }

        }
    }

    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "onHandleIntent fired")
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service Created")
        val filter = IntentFilter()
        filter.addAction(BROADCAST_ACTION)
        registerReceiver(receiver, filter)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand fired : $this")
        if (!isUpAndRunning) {
            Log.d(TAG, "first start")
            launch {
                val title = getString(R.string.downloading)
                val text = getDownloadsCount().toString() + getString(R.string.downloads_remaining)
                val icon = R.drawable.ic_file_download_black_24dp
                createNotification(title, text, icon)
                startForeground(ONGOING_NOTIFICATION_ID, notification)
                isUpAndRunning = true
            }
        }
        if (intent != null && intent.hasExtra(COMMAND)) {
            val cmd = intent.getIntExtra(COMMAND, 0)
            Log.d(TAG, "COMMAND->$cmd")
            launch { handleCommand(cmd, intent) }
        }
        // Return "sticky" for services that are explicitly
        // started and stopped as needed by the app.
        return START_STICKY
    }

    private fun handleCommand(cmd: Int, intent: Intent?) {
        Log.d(TAG, "command handled" + cmd)
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
                fetch.cancelAll()
                stopThisService()
            }
            COMMAND_ADD -> {
                val url = intent?.getStringExtra("URL")
                val file = intent?.getStringExtra("FILE")
                val request = Request(url ?: "", file ?: "")
                request.priority = Priority.HIGH
                request.networkType = NetworkType.WIFI_ONLY //TODO get from shared prefs
                request.groupId = DOWNLOAD_GROUP_ID
                //request.addHeader("clientKey", "SD78DF93_3947&MVNGHE1WONG")

                fetch!!.enqueue(request, Func { result ->
                    Log.d("->", "dowload enqueued" + result.id)
                }, Func { error ->
                    //TODO handle error better
                    Log.e("-->", error.name, error.throwable)
                    Toast.makeText(MyApp.context, "Download fail" + error.name, Toast.LENGTH_SHORT).show()
                })
                updateNotification(null)
            }
            COMMAND_UPDATE_NOTIFACATION -> updateNotification(null)
            COMMAND_NOTIFY -> updateNotification(intent?.getStringExtra("TXT"))
        }
    }

    private fun updateNotification(stringExtra: String?) {
        Log.i(TAG, "update Notification")
        var txt = ""
        if (!stringExtra.isNullOrBlank()) {
            txt = stringExtra
        }
        val title = getString(R.string.downloading)
        val text = getDownloadsCount().toString() + getString(R.string.downloads_remaining) + " " + txt
        val icon = R.drawable.ic_file_download_black_24dp
        createNotification(title, text, icon)

        launch {
            notificationManager?.notify(ONGOING_NOTIFICATION_ID, notification)
        }
    }


    private fun createNotification(title: String, text: String, icon: Int) {
        val intent = Intent(this, MainActivity::class.java)
        val requestID = System.currentTimeMillis().toInt()
        val flags = PendingIntent.FLAG_CANCEL_CURRENT // cancel old intent and create new one
        val pIntent = PendingIntent.getActivity(applicationContext, requestID, intent, flags)

        ///////pending intents
        val closeIntent = Intent(applicationContext, DownloadService::class.java)
        closeIntent.putExtra(COMMAND, COMMAND_STOP)
        val closePendingIntent =
            PendingIntent.getService(applicationContext, COMMAND_STOP, closeIntent, FLAG_UPDATE_CURRENT)

        val pauseIntent = Intent(applicationContext, DownloadService::class.java)
        closeIntent.putExtra(COMMAND, COMMAND_PAUSE)
        val pausePendingIntent =
            PendingIntent.getService(applicationContext, COMMAND_PAUSE, pauseIntent, FLAG_UPDATE_CURRENT)

        val resumeIntent = Intent(applicationContext, DownloadService::class.java)
        closeIntent.putExtra(COMMAND, COMMAND_RESUME)
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
                R.drawable.ic_file_download_black_24dp,
                "pause", pausePendingIntent
            ).build()

            val actionResume = Notification.Action.Builder(
                R.drawable.ic_file_download_black_24dp,
                "Resume", resumePendingIntent
            ).build()

            val actionClose = Notification.Action.Builder(
                R.drawable.ic_file_download_black_24dp,
                "Cancel all Downloads", closePendingIntent
            ).build()


            val builder = Notification.Builder(applicationContext, channel.id)
                //.setCustomContentView(contentView)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(icon)

                .setContentIntent(pIntent)
                .setAutoCancel(false)
                .addAction(actionPause)
                .addAction(actionResume)
                .addAction(actionClose)
//                .addAction(actionClose)
            //TODO add actions

            notification = builder.build()
        }
        /////////////////////////////////////////////////////////////////////
        else {            //NotificationCompat for api < 25
            //actions
            val actionPause = NotificationCompat.Action.Builder(
                R.drawable.ic_file_download_black_24dp,
                "pause", pausePendingIntent
            ).build()

            val actionResume = NotificationCompat.Action.Builder(
                R.drawable.ic_file_download_black_24dp,
                "Resume", resumePendingIntent
            ).build()
            val actionClose =
                NotificationCompat.Action.Builder(
                    R.drawable.ic_file_download_black_24dp,
                    "Cancel all Downloads", closePendingIntent
                ).build()

            val builder = NotificationCompat.Builder(applicationContext, "default")
                .setSound(null)
                //.setCustomContentView(contentView)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(icon)

                .setContentIntent(pIntent)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

                .addAction(actionPause)
                .addAction(actionResume)
                .addAction(actionClose)


            notification = builder.build()
        }


    }

    private fun getDownloadsCount(): Int {
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
        unregisterReceiver(receiver)
    }
}