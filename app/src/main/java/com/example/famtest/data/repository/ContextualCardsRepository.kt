package com.example.famtest.data.repository

import android.util.Log
import com.example.famtest.data.api.FamService
import com.example.famtest.data.model.GroupCardsResponse

private const val TAG = "ContextualCardsRepo"

class ContextualCardsRepository(private val famService: FamService) {
    init {
        Log.e(TAG, "ContextualCardsRepository is initialized...")
    }

    suspend fun fetchContextualCards(): GroupCardsResponse {
        return famService.fetchContextualCards()
    }
}