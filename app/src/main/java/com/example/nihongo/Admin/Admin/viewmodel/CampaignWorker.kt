package com.example.nihongo.Admin.viewmodel

import Campaign
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CampaignWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    private val db = FirebaseFirestore.getInstance()

    override suspend fun doWork(): Result {
        try {
            val campaignId = inputData.getString("campaignId")
            Log.d("CampaignWorker", "Campaign ID = $campaignId")
            if (campaignId.isNullOrEmpty()) {
                Log.e("CampaignWorker", "Campaign ID is null or empty")
                return Result.failure()
            }

            val campaignDoc = db.collection("campaigns").document(campaignId).get().await()
            val campaign = campaignDoc.toObject(Campaign::class.java) ?: return Result.failure()

            val viewModel = AdminNotifyPageViewModel()
            viewModel.sendCampaign(campaign, applicationContext)

            val updates = hashMapOf<String, Any>(
                "lastSent" to Timestamp.now(),
                "sent" to true
            )

            db.collection("campaigns").document(campaignId).update(updates).await()

            return Result.success()
        } catch (e: Exception) {
            Log.e("CampaignWorker", "Error executing campaign worker", e)
            return Result.retry()
        }
    }

}