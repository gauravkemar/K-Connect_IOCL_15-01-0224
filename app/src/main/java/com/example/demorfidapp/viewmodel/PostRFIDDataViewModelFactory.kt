package com.kemarport.kdmsmahindra.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.demorfidapp.repository.RFIDReaderRepository


class PostRFIDDataViewModelFactory (
    private val application: Application,
    private val rfidReaderRepository: RFIDReaderRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PostRFIDDataViewModel(application, rfidReaderRepository) as T
    }
}