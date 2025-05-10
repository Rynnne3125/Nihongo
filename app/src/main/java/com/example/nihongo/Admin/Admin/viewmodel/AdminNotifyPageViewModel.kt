package com.example.nihongo.Admin.viewmodel

import Campaign
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.concurrent.TimeUnit
import androidx.work.*

class AdminNotifyPageViewModel : ViewModel() {
    private val projectId = "nihongo-ae96a"
    private val db = FirebaseFirestore.getInstance()
    private val campaignsCollection = db.collection("campaigns")

    private val _campaigns = MutableStateFlow<List<Campaign>>(emptyList())
    val campaigns: StateFlow<List<Campaign>> = _campaigns

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadCampaigns()
    }

    fun loadCampaigns() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                campaignsCollection
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("AdminViewModel", "Error loading campaigns", error)
                            return@addSnapshotListener
                        }

                        val campaignsList = snapshot?.documents?.mapNotNull { doc ->
                            doc.toObject(Campaign::class.java)
                        } ?: emptyList()

                        _campaigns.value = campaignsList
                    }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Failed to load campaigns", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendCampaign(campaign: Campaign, context: Context) {
        viewModelScope.launch {
            try {
                // First save to Firestore
                val campaignToSave = campaign.copy(sent = true, lastSent = Timestamp.now())
                saveCampaignToFirestore(campaignToSave)

                // Then send the notification
                sendFirebaseNotification(campaign, context)
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Failed to send campaign", e)
            }
        }
    }

    fun saveCampaign(context: Context, campaign: Campaign) {
        viewModelScope.launch {
            try {
                val savedCampaign = saveCampaignToFirestore(campaign)

                if (savedCampaign.isScheduled && savedCampaign.scheduledFor != null) {
                    scheduleOneTimeNotification(context, savedCampaign)
                }

                if (savedCampaign.isDaily) {
                    scheduleDailyNotification(context, savedCampaign)
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Failed to save campaign", e)
            }
        }
    }


    private suspend fun saveCampaignToFirestore(campaign: Campaign): Campaign {
        return try {
            if (campaign.id.isEmpty()) {
                // Tạo mới document và lấy lại ID để gán
                val docRef = campaignsCollection.add(campaign).await()
                val updatedCampaign = campaign.copy(id = docRef.id)
                campaignsCollection.document(docRef.id).set(updatedCampaign).await()
                updatedCampaign
            } else {
                // Update nếu đã có ID
                campaignsCollection.document(campaign.id).set(campaign).await()
                campaign
            }
        } catch (e: Exception) {
            Log.e("AdminViewModel", "Error saving campaign to Firestore", e)
            throw e
        }
    }


    fun deleteCampaign(campaignId: String) {
        viewModelScope.launch {
            try {
                // Cancel any scheduled work for this campaign
                WorkManager.getInstance().cancelUniqueWork(campaignId)

                // Delete from Firestore
                campaignsCollection.document(campaignId).delete().await()
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Failed to delete campaign", e)
            }
        }
    }

    private suspend fun sendFirebaseNotification(campaign: Campaign, context: Context) {
        val token = getAccessToken(context) ?: return
        Log.d("FCMResponse", "Using access token: $token")

        val json = JSONObject().apply {
            put("message", JSONObject().apply {
                put("notification", JSONObject().apply {
                    put("title", campaign.title)
                    put("body", campaign.message)
                    campaign.imageUrl?.let {
                        if (it.isNotEmpty()) {
                            put("image", it)
                        }
                    }
                })
                put("topic", "all")
            })
        }

        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://fcm.googleapis.com/v1/projects/$projectId/messages:send")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FCMResponse", "Failed to send notification", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful) {
                    Log.d("FCMResponse", "Success: $responseBody")
                } else {
                    Log.e("FCMResponse", "Failure: $responseBody")
                }
            }
        })
    }

    fun sendNotificationToUser(campaign: Campaign, context: Context, token: String) {
        viewModelScope.launch {
            try {
                val accessToken = getAccessToken(context) ?: return@launch
                Log.d("FCMResponse", "Using access token: $accessToken")

                val json = JSONObject().apply {
                    put("message", JSONObject().apply {
                        put("notification", JSONObject().apply {
                            put("title", campaign.title)
                            put("body", campaign.message)
                            campaign.imageUrl?.let {
                                if (it.isNotEmpty()) {
                                    put("image", it)
                                }
                            }
                        })
                        put("topic", token) // <- gửi đến topic cá nhân
                    })
                }

                val body = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("https://fcm.googleapis.com/v1/projects/$projectId/messages:send")
                    .addHeader("Authorization", "Bearer $accessToken")
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build()

                OkHttpClient().newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e("FCMResponse", "Failed to send notification", e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseBody = response.body?.string()
                        if (response.isSuccessful) {
                            Log.d("FCMResponse", "Success: $responseBody")
                        } else {
                            Log.e("FCMResponse", "Failure: $responseBody")
                        }
                    }
                })

            } catch (e: Exception) {
                Log.e("FCMResponse", "Error sending notification", e)
            }
        }
    }


    fun scheduleOneTimeNotification(context: Context, campaign: Campaign) {
        val workRequest = OneTimeWorkRequestBuilder<CampaignWorker>()
            .setInitialDelay(
                campaign.scheduledFor!!.toDate().time - System.currentTimeMillis(),
                TimeUnit.MILLISECONDS
            )
            .setInputData(workDataOf("campaignId" to campaign.id))
            .addTag(campaign.id)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            campaign.id,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }


    fun scheduleDailyNotification(context: Context, campaign: Campaign) {
        val workRequest = PeriodicWorkRequestBuilder<CampaignWorker>(1, TimeUnit.DAYS)
            .setInputData(workDataOf("campaignId" to campaign.id))
            .addTag(campaign.id)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            campaign.id,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }


    private suspend fun getAccessToken(context: Context): String? {
        return try {
            withContext(Dispatchers.IO) {
                val inputStream: InputStream = context.assets.open("nihongo-ae96a-firebase-adminsdk-fbsvc-df1b5fe014.json")
                val googleCredentials = GoogleCredentials.fromStream(inputStream)
                    .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
                googleCredentials.refreshIfExpired()
                googleCredentials.accessToken.tokenValue
            }
        } catch (e: Exception) {
            Log.e("FCMResponse", "Failed to get access token", e)
            null
        }
    }
}
