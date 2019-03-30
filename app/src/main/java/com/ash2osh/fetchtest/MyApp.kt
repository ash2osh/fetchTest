package com.ash2osh.fetchtest

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

class MyApp : Application(), KodeinAware {
    private lateinit var fetchConfiguration: FetchConfiguration
    override val kodein = Kodein.lazy {
        import(androidXModule(this@MyApp))
        bind<CustomFetchListener>() with singleton { CustomFetchListener() }
        bind<Fetch>() with singleton { Fetch.getInstance(fetchConfiguration).addListener(instance()) }
    }

    override fun onCreate() {
        super.onCreate()
        context = this.applicationContext
        fetchConfiguration = FetchConfiguration.Builder(this.applicationContext)
            .setDownloadConcurrentLimit(1)
            .build()
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onTerminate() {
        super.onTerminate()
//        fetch.close()  ???//TODO
    }
}
