package com.ash2osh.fetchtest.ui.queue

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ash2osh.fetchtest.DownloadService
import com.ash2osh.fetchtest.ui.base.ScopedViewModel
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2core.Func

class QueueViewModel(private val fetch: Fetch) : ScopedViewModel() {
    override val TAG: String
        get() = "QueueViewModel->"

    init {
        getDownloads()
    }

    private val _downloads = MutableLiveData<List<Download>>()
    val downloads: LiveData<List<Download>> get() = _downloads

    //this updates the view model downloads
    fun onDownloadBroadcast(intent: Intent?) {
        getDownloads()
    }

    private fun getDownloads() {
        fetch.getDownloadsInGroup(DownloadService.DOWNLOAD_GROUP_ID, Func {
            Log.d("ALL DOWNLOADS \n", it.toString())
            _downloads.value = it.filter { download -> DownloadService.allDownloadsFilter(download) }
                .sortedByDescending { download -> download.status }
        })
    }

    fun cancelDownload(download: Download?) {
        download?.id?.let {
            fetch.cancel(it)
            fetch.delete(it)
        }
    }

}


class QueueViewModelFactory(private val fetch: Fetch) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return QueueViewModel(fetch) as T
    }
}