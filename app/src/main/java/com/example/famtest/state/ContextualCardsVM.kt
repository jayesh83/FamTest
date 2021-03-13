package com.example.famtest.state

import android.util.Log
import androidx.lifecycle.*
import com.example.famtest.data.model.GroupCardsResponse
import com.example.famtest.data.repository.ContextualCardsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "ContextualCardsVM"

class ContextualCardsVM(private val repository: ContextualCardsRepository) : ViewModel() {
    val _contextualCards: MutableLiveData<GroupCardsResponse> = MutableLiveData()
    val contextualCards: LiveData<GroupCardsResponse> get() = _contextualCards

    init {
        Log.e(TAG, "ContextualCardsVM initialized...")
        fetchContextualCards()
    }

    private fun fetchContextualCards() {
        viewModelScope.launch(Dispatchers.IO) {
            val cards = repository.fetchContextualCards()
            _contextualCards.postValue(cards)
        }
    }
}

private const val FactoryTAG = "ContextualCardsVmFact"

class ContextualCardsVmFactory(private val repository: ContextualCardsRepository) :
    ViewModelProvider.Factory {
    init {
        Log.e(FactoryTAG, "ContextualCardsVmFactory initialized...")
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ContextualCardsVM::class.java)) {
            Log.e("ContextualCardsVmFact", "Create called...")
            ContextualCardsVM(repository) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }

}