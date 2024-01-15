package  com.example.demorfidapp.api


import com.example.demorfidapp.helper.Constants
import com.example.demorfidapp.model.PostRFIDReadRequest
import com.example.demorfidapp.model.PostRFIDReadResponse

import retrofit2.Response
import retrofit2.http.*


interface RFIDReaderAPI {

 @POST(Constants.rfidTag)
 suspend fun submitRFIDDetails(
  @Body
  postRFIDReadRequest: PostRFIDReadRequest
 ): Response<String>

}