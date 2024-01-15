package com.kemarport.kdmsmahindra.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.demorfidapp.helper.Constants
import com.example.demorfidapp.helper.Resource
import com.example.demorfidapp.helper.Utils
import com.example.demorfidapp.model.PostRFIDReadRequest
import com.example.demorfidapp.model.PostRFIDReadResponse
import com.example.demorfidapp.repository.RFIDReaderRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException

class PostRFIDDataViewModel  (
    application: Application,
    private val rfidRepository: RFIDReaderRepository
) : AndroidViewModel(application) {
    val postRFIDDataMutable: MutableLiveData<Resource<String>> = MutableLiveData()

    fun submitRFIDDetails(baseUrl: String, postRFIDReadRequest: PostRFIDReadRequest) {
        viewModelScope.launch {
            safeAPICallSubmitRFIDDetails(baseUrl, postRFIDReadRequest)
        }
    }

    private suspend fun safeAPICallSubmitRFIDDetails(baseUrl: String, postRFIDReadRequest: PostRFIDReadRequest) {
        postRFIDDataMutable.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = rfidRepository.submitRFIDDetails(baseUrl, postRFIDReadRequest)
                postRFIDDataMutable.postValue(handleRFIDResponse(response))
            } else {
                postRFIDDataMutable.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> postRFIDDataMutable.postValue(Resource.Error(Constants.CONFIG_ERROR))
                else -> postRFIDDataMutable.postValue(Resource.Error("${t.message}"))
            }
        }
    }

    private fun handleRFIDResponse(response: Response<String>): Resource<String> {
        return if (response.isSuccessful) {
            val message = response.body()
            Resource.Success(message ?: "")
        } else {
            val errorBody = response.errorBody()?.string()
            Resource.Error(errorBody ?: "Unknown error")
        }
    }
}