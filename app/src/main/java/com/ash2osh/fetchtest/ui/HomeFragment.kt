package com.ash2osh.fetchtest.ui


import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ash2osh.fetchtest.DownloadService
import com.ash2osh.fetchtest.R
import kotlinx.android.synthetic.main.fragment_home.*
import pub.devrel.easypermissions.EasyPermissions
import java.util.*


class HomeFragment : Fragment() {


    private fun bindUI() {
        fab.setOnClickListener { view ->
            //snack bar was causing a memory leak
//            Snackbar.make(view, "Download started", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()

            val url = "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_5mb.mp4"
            val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
            val file = downloads + "/" + UUID.randomUUID().toString()
            val downloadBundle = createDownloadBundle(url, file)

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
    }


    private fun runWithPermission(code: Int, block: () -> Unit) {
        if (EasyPermissions.hasPermissions(context!!, WRITE_EXTERNAL_STORAGE))
            block()
        else
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.storage_rationale),
                code,
                WRITE_EXTERNAL_STORAGE
            )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindUI()
    }


    private fun createDownloadBundle(url: String, file: String): Bundle {
        val mBundle = Bundle()
        mBundle.putInt(DownloadService.COMMAND, DownloadService.COMMAND_ADD)
        mBundle.putString("URL", url)
        mBundle.putString("FILE", file)
        return mBundle
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_home, container, false)


}
