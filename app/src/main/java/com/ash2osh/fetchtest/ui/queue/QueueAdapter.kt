package com.ash2osh.fetchtest.ui.queue

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.widget.Button
import com.ash2osh.fetchtest.R
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Status

class QueueAdapter(data: List<Download>) : BaseQuickAdapter<Download, BaseViewHolder>(R.layout.item_download, data)
    , BaseQuickAdapter.OnItemChildClickListener {

    override fun convert(holder: BaseViewHolder?, item: Download?) {
        holder?.setText(R.id.item_downloadTV, item?.file)
        item?.progress?.let { holder?.setProgress(R.id.item_downloadPP, it) }

        when (item?.status) {
            Status.COMPLETED -> {
                holder?.setText(R.id.item_downloadBTN, R.string.play)
                setBtnColor(holder, Color.GREEN)
            }
            Status.FAILED -> {

            }
            else -> {
                holder?.setText(R.id.item_downloadBTN, R.string.cancel)
                setBtnColor(holder, Color.RED)
            }
        }

        if (item?.status == Status.COMPLETED) {


        } else {

        }
        holder?.addOnClickListener(R.id.item_downloadBTN)
        holder?.setText(R.id.item_download_statTV, item?.status.toString())
    }

    private fun setBtnColor(holder: BaseViewHolder?, color: Int) {
        holder?.getView<Button>(R.id.item_downloadBTN)?.backgroundTintList = ColorStateList.valueOf(color)
    }

    override fun onItemChildClick(adapter: BaseQuickAdapter<*, *>?, view: View?, position: Int) {
        //do nothing here
    }

}