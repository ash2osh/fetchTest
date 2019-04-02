package com.ash2osh.fetchtest.ui.Queue

import android.view.View
import com.ash2osh.fetchtest.R
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.tonyodev.fetch2.Download

class QueueAdapter(data: List<Download>) : BaseQuickAdapter<Download, BaseViewHolder>(R.layout.item_download, data)
    , BaseQuickAdapter.OnItemChildClickListener {

    override fun convert(holder: BaseViewHolder?, item: Download?) {
        holder?.setText(R.id.item_downloadTV, item?.file)
        item?.progress?.let { holder?.setProgress(R.id.item_downloadPP, it) }
        holder?.addOnClickListener(R.id.item_downloadBTN)
        holder?.setText(R.id.item_download_statTV, item?.status.toString())
    }

    override fun onItemChildClick(adapter: BaseQuickAdapter<*, *>?, view: View?, position: Int) {
        //do nothing here
    }

}