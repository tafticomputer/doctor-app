package com.doctorapp.network

import retrofit2.Response
import retrofit2.http.*

data class RegisterRequest(val full_name: String, val phone: String, val password: String, val role: String)
data class LoginRequest(val phone: String, val password: String)
data class AuthResponse(val user: UserDto, val access_token: String, val refresh_token: String)
data class UserDto(val id: Int, val full_name: String, val phone: String, val role: String)
data class SpecialistDto(val id: Int, val full_name: String, val specialty: String?, val bio: String?)
data class RequestDto(val id: Int, val patient_id: Int, val specialist_id: Int, val status: String, val note: String?)
data class NewRequestBody(val specialist_id: Int, val note: String?)
data class StatusUpdateBody(val status: String)
data class MessageDto(val id: Int, val request_id: Int, val sender_id: Int, val body: String, val created_at: String)
data class NewMessageBody(val body: String)

interface ApiService {
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @GET("specialists")
    suspend fun getSpecialists(): List<SpecialistDto>

    @POST("requests")
    suspend fun createRequest(@Body body: NewRequestBody): RequestDto

    @GET("requests/inbox")
    suspend fun getInbox(): List<RequestDto>

    @GET("requests/mine")
    suspend fun getMyRequests(): List<RequestDto>

    @GET("requests/all")
    suspend fun getAllRequests(): List<RequestDto>

    @PATCH("requests/{id}/status")
    suspend fun updateStatus(@Path("id") id: Int, @Body body: StatusUpdateBody): RequestDto

    @GET("messages/{requestId}")
    suspend fun getMessages(@Path("requestId") requestId: Int): List<MessageDto>

    @POST("messages/{requestId}")
    suspend fun sendMessage(@Path("requestId") requestId: Int, @Body body: NewMessageBody): MessageDto
}
