package com.example.famtest.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.famtest.CardsComponent
import com.example.famtest.R


class ContextualCardsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_first, container, false)
        val linearLayoutContainer: LinearLayout = rootView.findViewById(R.id.ll_container)

        lifecycle.addObserver(
            CardsComponent(
                this,
                requireContext(),
                linearLayoutContainer as ViewGroup
            )
        )
        return rootView
    }
}