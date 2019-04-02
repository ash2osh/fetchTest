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
        sendCommand(DownloadService.COMMAND_NOTIFY, "Waiting for network")
    }

    override fun onStarted(download: Download, list: List<DownloadBlock>, i: Int) {
        Log.i(TAG, "download started :$download")
        sendCommand(DownloadService.COMMAND_UPDATE_NOTIFICATION)
    }

    override fun onError(download: Download, error: Error, throwable: Throwable?) {
        Log.e(TAG, "download error :" + download + "\n " + error.name, throwable)
        //TODO handle
    }

    override fun onRemoved(download: Download) {
        Log.i(TAG, "download removed :$download")
        sendCommand(DownloadService.COMMAND_UPDATE_NOTIFICATION)
    }

    override fun onQueued(download: Download, b: Boolean) {
        Log.i(TAG, "download queued :$download")
    }

    override fun onProgress(download: Download, l: Long, l1: Long) {
        Log.i(TAG, "download progress :$download\n-->($download.downloaded/$download.total)")

        sendCommand(
            DownloadService.COMMAND_NOTIFY,
            "Downloading " + (download.downloaded * 100 / download.total).toInt() + "%"
        )
    }

    override fun onCompleted(download: Download) {
        Log.i(TAG, "download complete :$download")
        sendCommand(DownloadService.COMMAND_UPDATE_NOTIFICATION)
    }

    override fun onCancelled(download: Download) {
        Log.i(TAG, "download cancelled :" + download.url)
        sendCommand(DownloadService.COMMAND_UPDATE_NOTIFICATION)
    }


    override fun onResumed(download: Download) {
    }

    override fun onPaused(download: Download) {
    }


    override fun onDownloadBlockUpdated(download: Download, downloadBlock: DownloadBlock, i: Int) {
    }

    override fun onDeleted(download: Download) {
        Log.i(TAG, "download deleted :$download")
        sendCommand(DownloadService.COMMAND_UPDATE_NOTIFICATION)
    }

    private fun sendCommand(cmd: Int, notifacation: String = "") {
        Intent().also { intent ->
            intent.action = DownloadService.BROADCAST_ACTION
            intent.putExtra(DownloadService.COMMAND, cmd)
            if (notifacation.isNotBlank())
                intent.putExtra("TXT", notifacation)

            MyApp.context.sendBroadcast(intent)

        }
    }



}

