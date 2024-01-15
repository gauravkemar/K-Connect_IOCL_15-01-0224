package com.example.demorfidapp.repository

import com.example.demorfidapp.api.RetrofitInstance
import com.example.demorfidapp.model.PostRFIDReadRequest


class RFIDReaderRepository {

    suspend fun submitRFIDDetails(
        baseUrl: String,
        postRFIDReadRequest: PostRFIDReadRequest
    ) = RetrofitInstance.api(baseUrl).submitRFIDDetails(postRFIDReadRequest)

}