package com.ash2osh.fetchtest.ui.queue

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ash2osh.fetchtest.DownloadBroadcastReceiver
import com.ash2osh.fetchtest.DownloadService
import com.ash2osh.fetchtest.R
import com.ash2osh.fetchtest.ui.base.EmptyView
import com.ash2osh.fetchtest.ui.base.ScopedFragment
import com.chad.library.adapter.base.BaseQuickAdapter
import com.tonyodev.fetch2.Status
import kotlinx.android.synthetic.main.queue_fragment.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import pub.devrel.easypermissions.EasyPermissions
import java.util.*

class QueueFragment : ScopedFragment(), KodeinAware {
    override val kodein by closestKodein()
    override val TAG: String
        get() = "QueueFragment->"

    private val viewModelFactory: QueueViewModelFactory by instance()
    private lateinit var viewModel: QueueViewModel
    private lateinit var receiver: DownloadBroadcastReceiver
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    private var adapter: QueueAdapter? = null
    private lateinit var emptyView: EmptyView
    private var isError = false

    private fun bindUI() {
        fab.setOnClickListener {
            val url = "http://192.168.10.81/1.mkv"
            val downloadUrl: String
            if (isError) {
                downloadUrl = "http://192.168.10.81/1.mkv"
                isError=false
            } else {
                downloadUrl=url
                isError=true
            }

            val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
            val file = downloads + "/" + UUID.randomUUID().toString()
            val downloadBundle = createDownloadBundle(downloadUrl, file)

            runWithPermission(111) {
                val dService = Intent(context, DownloadService::class.java)
                dService.putExtras(downloadBundle)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activity?.startForegroundService(dService)
                } else {
                    activity?.startService(dService)
                }

            }
        }

        settingFab.setOnClickListener {
            findNavController().navigate(R.id.preferenceFragment)
        }

        //bind recycler view
        adapter = QueueAdapter(mutableListOf())

        adapter?.onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.item_downloadBTN -> {
                    val download = adapter!!.getItem(position)
                    Log.d(TAG, "onItemChildClick:  $download")

                    if (download?.status == Status.COMPLETED) {
                        val action =
                            QueueFragmentDirections.actionQueueFragmentToPlayerFragment(download.file)
                        findNavController().navigate(action)
                    } else {
                        viewModel.cancelDownload(download)
                    }

                }
            }
        }
        mLayoutManager = LinearLayoutManager(context)
        queueRV.apply {
            layoutManager = this@QueueFragment.mLayoutManager
            adapter = this@QueueFragment.adapter
        }
        emptyView = EmptyView(layoutInflater, queueRV.parent as ViewGroup)
        adapter?.emptyView = this.emptyView.view
    }


    private fun createDownloadBundle(url: String, file: String): Bundle {
        val mBundle = Bundle()
        mBundle.putInt(DownloadService.COMMAND, DownloadService.COMMAND_ADD)
        mBundle.putString("URL", url)
        mBundle.putString("FILE", file)
        return mBundle
    }


    private fun runWithPermission(code: Int, block: () -> Unit) {
        if (EasyPermissions.hasPermissions(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            block()
        else
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.storage_rationale),
                code,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(QueueViewModel::class.java)
        bindUI()
        //register receiver
        receiver = DownloadBroadcastReceiver { i: Intent? -> viewModel.onDownloadBroadcast(i) }
        val filter = IntentFilter()
        filter.addAction(DownloadService.BROADCAST_ACTION)
        activity?.registerReceiver(receiver, filter)

        viewModel.downloads.observe(this, Observer {
            Log.d(TAG, "${it.size} Downloads is active")
            adapter?.setNewData(it)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.unregisterReceiver(receiver)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.queue_fragment, container, false)

}
