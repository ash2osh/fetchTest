package com.ash2osh.fetchtest

import android.content.Intent
import android.util.Log
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.FetchListener
import com.tonyodev.fetch2core.DownloadBlock


class CustomFetchListener : FetchListener {
    companion object {
        private const val TAG = "CustomFetchListener->"
    }

    override fun onAdded(download: Download) {
        Log.i(TAG, "download added :" + download.url)
    }

    override fun onWaitingNetwork(download: Download) {
        Log.i(TAG, "download waiting :$download")
        sendCommand("Waiting for network")
    }

    override fun onStarted(download: Download, list: List<DownloadBlock>, i: Int) {
        Log.i(TAG, "download started :$download")
        sendCommand()
    }

    override fun onError(download: Download, error: Error, throwable: Throwable?) {
        Log.e(TAG, "download error :" + download + "\n " + error.name, throwable)
        //TODO handle
    }

    override fun onRemoved(download: Download) {
        Log.i(TAG, "download removed :$download")
        sendCommand()
    }

    override fun onQueued(download: Download, b: Boolean) {
        Log.i(TAG, "download queued :$download")
    }

    override fun onProgress(download: Download, l: Long, l1: Long) {
        Log.i(TAG, "download progress :$download\n-->($download.downloaded/$download.total)")

        sendCommand(
            "Downloading " + (download.downloaded * 100 / download.total).toInt() + "%"
        )
    }

    override fun onCompleted(download: Download) {
        Log.i(TAG, "download complete :$download")
        sendCommand()
    }

    override fun onCancelled(download: Download) {
        Log.i(TAG, "download cancelled :" + download.url)
        sendCommand()
    }


    override fun onResumed(download: Download) {
        sendCommand()
    }

    override fun onPaused(download: Download) {
        sendCommand("Paused")
    }


    override fun onDownloadBlockUpdated(download: Download, downloadBlock: DownloadBlock, i: Int) {
    }

    override fun onDeleted(download: Download) {
        Log.i(TAG, "download deleted :$download")
        sendCommand()
    }

    private fun sendCommand(notification: String = "") {
        var cmd = DownloadService.COMMAND_UPDATE_NOTIFICATION
        if (notification.isNotBlank()) {
            cmd = DownloadService.COMMAND_NOTIFY
        }

        Intent().also { intent ->
            intent.action = DownloadService.BROADCAST_ACTION
            intent.putExtra(DownloadService.COMMAND, cmd)
            if (notification.isNotBlank())
                intent.putExtra("TXT", notification)

            MyApp.context.sendBroadcast(intent)

        }
    }


}

