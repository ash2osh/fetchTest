package com.ash2osh.fetchtest.ui.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ash2osh.fetchtest.R

class EmptyView(private val layoutInflater: LayoutInflater, private val viewGroup: ViewGroup) {

    var view: View

    init {
        view = setInflated()
    }

    private fun setInflated(): View {
        return layoutInflater.inflate(R.layout.empty_view, viewGroup, false)
    }
}