package com.example.famtest

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.example.famtest.data.api.FamService
import com.example.famtest.state.ContextualCardsVM

class CardsComponent : LifecycleObserver {
    private lateinit var viewModel: ContextualCardsVM

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        // plot the data
        val service: FamService = FamService.create()
//        viewModel =
//            ViewModelProvider(
//                this,
//                ContextualCardsVmFactory(ContextualCardsRepository(service)) // TODO: use di
//            ).get(ContextualCardsVM::class.java)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        // fetch the data
        val service: FamService = FamService.create()
//        viewModel =
//            ViewModelProvider(
//                this.viewModelStore,
//                ContextualCardsVmFactory(ContextualCardsRepository(service)) // TODO: use di
//            ).get(ContextualCardsVM::class.java)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        // plot the data
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        // cancel any coroutine
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        // release resource
    }
}