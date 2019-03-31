package com.ash2osh.fetchtest.ui

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.ash2osh.fetchtest.R

class QueueFragment : Fragment() {

//    companion object {
//        fun newInstance() = QueueFragment()
//    }

    private lateinit var viewModel: QueueViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.queue_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(QueueViewModel::class.java)
        // TODO: Use the ViewModel to get and view the download queue

    }

}
