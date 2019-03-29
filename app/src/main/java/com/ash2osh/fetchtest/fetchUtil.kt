package com.ash2osh.fetchtest

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
        Log.i(TAG, "download waiting :" + download)
    }

    override fun onStarted(download: Download, list: List<DownloadBlock>, i: Int) {
        Log.i(TAG, "download started :" + download)
    }

    override fun onError(download: Download, error: Error, throwable: Throwable?) {
        Log.e(TAG, "download error :" + download + "\n " + error.name, throwable)
    }

    override fun onRemoved(download: Download) {
        Log.i(TAG, "download removed :" + download)
    }

    override fun onQueued(download: Download, b: Boolean) {
        Log.i(TAG, "download queued :" + download)
    }

    override fun onProgress(download: Download, l: Long, l1: Long) {
        Log.i(TAG, "download progress :" + download + "\n-->" + l + "/" + l1)
    }

    override fun onCompleted(download: Download) {
        Log.i(TAG, "download complete :" + download)
    }

    override fun onCancelled(download: Download) {
        Log.i(TAG, "download cancelled :" + download.url)
    }


    override fun onResumed(download: Download) {
    }

    override fun onPaused(download: Download) {
    }


    override fun onDownloadBlockUpdated(download: Download, downloadBlock: DownloadBlock, i: Int) {
    }

    override fun onDeleted(download: Download) {
        Log.i(TAG,"download deleted :" + download)
    }


}

