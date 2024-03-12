package ru.studiq.test.imagegallery.model.UI.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

open class CustomFragment: Fragment() {
    companion object {
        const val PARAM_ACTION = "PARAM_ACTION"
    }
    private var intRootView: View? = null;

    val rootView: View?
        get() = intRootView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        intRootView = view
        initialize(view)
        return view
    }
    open fun  initialize(view: View?) {
        intRootView = view
    }
}