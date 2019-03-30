package com.ash2osh.fetchtest

import android.os.Bundle
import android.os.Environment
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import com.afollestad.assent.Permission
import com.afollestad.assent.runWithPermissions
import com.tonyodev.fetch2.*

import kotlinx.android.synthetic.main.activity_main.*
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import android.content.Intent
import android.os.Build


class MainActivity : AppCompatActivity(), KodeinAware {
    override val kodein: Kodein by closestKodein()
    private val fetch: Fetch by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        fab.setOnClickListener { view ->
            Snackbar.make(view, "Download started", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()

            val url = "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_5mb.mp4"
            val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
            val file = downloads + "/" + java.util.UUID.randomUUID().toString()
            val downloadBundle = CreateDownloadBundle(url, file)

            runWithPermissions(Permission.WRITE_EXTERNAL_STORAGE) {
                val dService = Intent(this, DownloadService::class.java)
                dService.putExtras(downloadBundle)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    this.startForegroundService(dService)
                } else {
                    this.startService(dService)
                }


            }
        }

        fablist.setOnClickListener {
            Intent().also { intent ->
                intent.action = "com.ash2osh.fetchtest.DOWNLOAD_NOTIFACTION"
                intent.putExtra("CMD", "Notice me senpai!")
                sendBroadcast(intent)
            }
        }


    }

    fun CreateDownloadBundle(url: String, file: String): Bundle {
        val mBundle = Bundle()
        mBundle.putInt(DownloadService.COMMAND, DownloadService.COMMAND_ADD)
        mBundle.putString("URL", url)
        mBundle.putString("FILE", file)
        return mBundle
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        fetch!!.close()
    }
}



