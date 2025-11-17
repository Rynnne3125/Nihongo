package com.example.nihongo.User.data.repository

import android.util.Log
import com.example.nihongo.User.data.models.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL


data class AIResponse(
    val reply: String,
    val context: String?,
    val usage_time: String?
)

data class GroupChallenge(
    val id: String = "",
    val groupId: String = "",
    val title: String = "",
    val description: String = "",
    val targetType: String = "",
    val targetValue: Int = 0,
    val startDate: Long = 0,
    val endDate: Long = 0,
    val participants: Map<String, Int> = emptyMap(),
    val rewards: Map<String, Int> = mapOf("top1" to 300, "top2" to 200, "top3" to 100, "others" to 50),
    val status: String = "active",
    val createdAt: Long = System.currentTimeMillis()
)

data class AITip(
    val id: String = "",
    val groupId: String = "",
    val tip: String = "",
    val category: String = "",
    val level: String = "",
    val date: Long = System.currentTimeMillis(),
    val likes: Int = 0
)

data class AIDiscussionTopic(
    val id: String = "",
    val groupId: String = "",
    val topic: String = "",
    val description: String = "",
    val level: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val responses: Int = 0
)

data class UserRecommendation(
    val userId: String = "",
    val username: String = "",
    val imageUrl: String = "",
    val matchScore: Int = 0,
    val matchReasons: List<String> = emptyList(),
    val commonGoals: List<String> = emptyList(),
    val jlptLevel: Int? = null,
    val rank: Any
)

class AIRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val AI_SERVER_URL = "https://causal-noncomplaisant-jen.ngrok-free.dev" // Dùng 10.0.2.2 cho emulator

    // ============ AI Chat Functions ============

    suspend fun chatWithAI(message: String, groupId: String? = null, userId: String? = null): AIResponse? {

        if (groupId != null && userId != null) {
            Log.e("AIRepository", "Không thể chat với cả groupId và userId cùng lúc.")
            return null
        }

        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$AI_SERVER_URL/api/chat")
                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.doOutput = true
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                // ⚠️ Backend yêu cầu: message + user_id
                val jsonBody = JSONObject().apply {
                    put("message", message)
                    put("user_id", userId ?: groupId ?: "guest")
                }

                connection.outputStream.use { os ->
                    os.write(jsonBody.toString().toByteArray(Charsets.UTF_8))
                }

                val statusCode = connection.responseCode
                if (statusCode != 200) {
                    Log.e("AIRepository", "Chat error HTTP $statusCode")
                    return@withContext null
                }

                val responseText = connection.inputStream.bufferedReader().readText()
                val jsonRes = JSONObject(responseText)
                Log.d("AIRepository", "Sending JSON: $jsonBody")
                Log.d("AIRepository", "Response code: $statusCode")
                Log.d("AIRepository", "Response text: $responseText")

                return@withContext AIResponse(
                    reply = jsonRes.optString("reply"),
                    context = jsonRes.optString("context", null),
                    usage_time = jsonRes.optString("usage_time", null)
                )

            } catch (e: Exception) {
                Log.e("AIRepository", "Chat exception: ${e.message}", e)
                null
            }
        }
    }
    suspend fun getAIPartnerRecommendations(
        currentUser: User,
        limit: Int = 10
    ): List<UserRecommendation> = withContext(Dispatchers.IO) {
        try {
            val collectionRef = firestore.collection("ai_recommendations")

            // 1️⃣ Lấy các recommendation đã có cho currentUser
            val existingRecsSnapshot = collectionRef
                .whereEqualTo("userId", currentUser.id)
                .get()
                .await()

            val existingRecs = existingRecsSnapshot.documents.map { doc ->
                UserRecommendation(
                    userId = doc.getString("recommendedUserId").orEmpty(),
                    username = doc.getString("username").orEmpty(),
                    jlptLevel = doc.getLong("jlptLevel")?.toInt(),
                    rank = doc.getString("rank").orEmpty(),
                    matchScore = doc.getLong("matchScore")?.toInt() ?: 0,
                    imageUrl = doc.getString("imageUrl").orEmpty(),
                    matchReasons = doc.get("matchReasons") as? List<String> ?: emptyList()
                )
            }.sortedByDescending { it.matchScore }

            // Nếu đã đủ limit, trả về luôn
            if (existingRecs.size >= limit) return@withContext existingRecs.take(limit)

            // 2️⃣ Lấy tất cả users khác
            val allUsersDocs = firestore.collection("users")
                .whereNotEqualTo("id", currentUser.id)
                .get()
                .await()

            val otherUsers = allUsersDocs.documents.map { doc ->
                mapOf(
                    "userId" to doc.id,
                    "username" to doc.getString("username").orEmpty(),
                    "jlptLevel" to doc.getLong("jlptLevel")?.toInt(),
                    "rank" to doc.getString("rank").orEmpty(),
                    "activityScore" to (doc.getLong("activityPoints")?.toInt() ?: 0),
                    "imageUrl" to doc.getString("imageUrl").orEmpty() // <-- thêm đây
                )
            }
                .filter {
                    it["jlptLevel"] == currentUser.jlptLevel || it["rank"] == currentUser.rank
                }
                .filter { it["userId"] !in existingRecs.map { r -> r.userId } }
                .take(20)

            // Nếu không còn user mới nào để hỏi AI, trả về existing
            if (otherUsers.isEmpty()) return@withContext existingRecs.take(limit)

            // 3️⃣ Tạo payload cho AI
            val messagePayload = buildString {
                append("You are an AI that recommends study partners for Japanese learners.\n")
                append("Current user: ${JSONObject(mapOf(
                    "userId" to currentUser.id,
                    "username" to currentUser.username,
                    "jlptLevel" to currentUser.jlptLevel,
                    "rank" to currentUser.rank,
                    "activityScore" to currentUser.activityPoints
                ))}\n")
                append("Other users: ${JSONArray(otherUsers)}\n")
                append("Task: Select and return a list of users who are suitable study partners. \n")
                append("Criteria: \n")
                append("  - Must have the same JLPT level OR the same rank as the current user.\n")
                append("  - Prefer users with similar activityScore.\n")
                append("Output format: JSON array of objects with fields: userId, username, jlptLevel, rank, matchScore (0-100), matchReasons (array of strings)\n")
            }

            // 4️⃣ Gọi AI
            val aiResponse = chatWithAI(
                message = messagePayload,
                userId = currentUser.id
            ) ?: return@withContext existingRecs.take(limit)

            val replyText = aiResponse.reply.trim()

            // Xử lý trường hợp AI trả markdown code block
            val cleanedReply = replyText
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val jsonArray = JSONArray(cleanedReply)
            val newRecommendations = mutableListOf<UserRecommendation>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val userImageUrl = if (obj.has("imageUrl") && !obj.isNull("imageUrl")) {
                    obj.getString("imageUrl")
                } else {
                    // fallback từ danh sách otherUsers
                    otherUsers.find { it["userId"] == obj.optString("userId") }?.get("imageUrl") as? String ?: ""
                }
                val rec = UserRecommendation(
                    userId = obj.optString("userId"),
                    username = obj.optString("username"),
                    jlptLevel = if (obj.has("jlptLevel") && !obj.isNull("jlptLevel")) obj.getInt("jlptLevel") else null,
                    rank = obj.optString("rank"),
                    matchScore = obj.optInt("matchScore"),
                    imageUrl = userImageUrl,
                    matchReasons = mutableListOf<String>().apply {
                        val arr = obj.optJSONArray("matchReasons")
                        if (arr != null) for (j in 0 until arr.length()) add(arr.getString(j))
                    }
                )

                // 5️⃣ Push lên Firestore nếu chưa có
                val existsSnapshot = collectionRef
                    .whereEqualTo("userId", currentUser.id)
                    .whereEqualTo("recommendedUserId", rec.userId)
                    .get()
                    .await()

                if (existsSnapshot.isEmpty) {
                    val docData = mapOf(
                        "userId" to currentUser.id,
                        "recommendedUserId" to rec.userId,
                        "username" to rec.username,
                        "jlptLevel" to rec.jlptLevel,
                        "rank" to rec.rank,
                        "matchScore" to rec.matchScore,
                        "imageUrl" to rec.imageUrl,
                        "matchReasons" to rec.matchReasons,
                        "createdAt" to com.google.firebase.Timestamp.now()
                    )
                    collectionRef.add(docData).await()
                }

                newRecommendations.add(rec)
            }

            // 6️⃣ Trả về kết hợp existing + new, sắp xếp theo matchScore
            val combined = (existingRecs + newRecommendations)
                .distinctBy { it.userId } // tránh trùng
                .sortedByDescending { it.matchScore }
                .take(limit)

            return@withContext combined

        } catch (e: Exception) {
            Log.e("AIRepository", "Error getting AI partner recommendations", e)
            return@withContext emptyList<UserRecommendation>()
        }
    }


    // ============ Group Challenge Functions ============
    // (Giữ nguyên)
    suspend fun createGroupChallenge(challenge: GroupChallenge): String? {
        return try {
            val docRef = firestore.collection("groupChallenges").add(challenge).await()
            val group = firestore.collection("studyGroups").document(challenge.groupId).get().await()
            val members = group.get("members") as? List<String> ?: emptyList()

            members.forEach { memberId ->
                val notification = hashMapOf(
                    "userId" to memberId,
                    "title" to "Thử thách mới: ${challenge.title}",
                    "message" to challenge.description,
                    "type" to "group_challenge",
                    "referenceId" to docRef.id,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "read" to false
                )
                firestore.collection("notifications").add(notification).await()
            }
            docRef.id
        } catch (e: Exception) {
            Log.e("AIRepository", "Error creating challenge", e)
            null
        }
    }

    suspend fun updateChallengeProgress(challengeId: String, userId: String, progress: Int) {
        try {
            firestore.collection("groupChallenges")
                .document(challengeId)
                .update("participants.$userId", progress)
                .await()
        } catch (e: Exception) {
            Log.e("AIRepository", "Error updating challenge progress", e)
        }
    }

    suspend fun completeChallenge(challengeId: String) {
        try {
            val challenge = firestore.collection("groupChallenges")
                .document(challengeId)
                .get()
                .await()
                .toObject(GroupChallenge::class.java) ?: return

            val sortedParticipants = challenge.participants.entries
                .sortedByDescending { it.value }

            val userRepository = UserRepository()

            sortedParticipants.forEachIndexed { index, entry ->
                val points = when (index) {
                    0 -> challenge.rewards["top1"] ?: 300
                    1 -> challenge.rewards["top2"] ?: 200
                    2 -> challenge.rewards["top3"] ?: 100
                    else -> challenge.rewards["others"] ?: 50
                }
                userRepository.addActivityPoints(entry.key, points)
                val rank = when (index) {
                    0 -> "🥇 Nhất"
                    1 -> "🥈 Nhì"
                    2 -> "🥉 Ba"
                    else -> "Top ${index + 1}"
                }
                val notification = hashMapOf(
                    "userId" to entry.key,
                    "title" to "Thử thách hoàn thành!",
                    "message" to "Bạn đạt hạng $rank trong thử thách '${challenge.title}' và nhận được $points điểm!",
                    "type" to "challenge_complete",
                    "referenceId" to challengeId,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "read" to false
                )
                firestore.collection("notifications").add(notification).await()
            }
            firestore.collection("groupChallenges")
                .document(challengeId)
                .update("status", "completed")
                .await()
        } catch (e: Exception) {
            Log.e("AIRepository", "Error completing challenge", e)
        }
    }

    suspend fun getActiveChallengesForGroup(groupId: String): List<GroupChallenge> {
        return try {
            firestore.collection("groupChallenges")
                .whereEqualTo("groupId", groupId)
                .whereEqualTo("status", "active")
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(GroupChallenge::class.java)?.copy(id = it.id) }
        } catch (e: Exception) {
            Log.e("AIRepository", "Error getting challenges", e)
            emptyList()
        }
    }

    // ============ AI Tips Functions ============

     suspend fun generateDailyTipForGroup(
        groupId: String,
        level: String,
        description: String
    ): AITip? {
        return try {
            val now = System.currentTimeMillis()

            // Tính mốc đầu ngày hôm nay 00:00:00
            val dayStart = now - (now % (24 * 60 * 60 * 1000))

            // ===== 1. Check TODAY'S TIP =====
            val existingTipSnapshot = firestore.collection("aiTips")
                .whereEqualTo("groupId", groupId)
                .orderBy("date", Query.Direction.DESCENDING) // get latest
                .limit(1)
                .get()
                .await()

            val lastTipDoc = existingTipSnapshot.documents.firstOrNull()

            // Nếu có tip và vẫn là NGÀY HÔM NAY ⇒ sử dụng lại
            if (lastTipDoc != null) {
                val lastTipDate = lastTipDoc.getLong("date") ?: 0L
                if (lastTipDate >= dayStart) {
                    return lastTipDoc.toObject(AITip::class.java)
                        ?.copy(id = lastTipDoc.id)
                }
            }

            // ===== 2. Generate new tip (ONLY ONCE PER DAY) =====

            val aiResponse = chatWithAI(
                message =
                    "Generate a helpful and tailored Japanese learning tip for level $level learners. " +
                            "Here is the study group description context: \"$description\". " +
                            "Create a daily learning tip (2–3 sentences) in Vietnamese, based on this group's purpose. " +
                            "Tip must be clear, practical, and based on the topic of this learning group.",
                groupId = groupId
            )

            if (aiResponse == null) return null

            val categories = listOf("grammar", "vocabulary", "kanji", "culture")

            val newTip = AITip(
                groupId = groupId,
                tip = aiResponse.reply,
                category = categories.random(),
                level = level,
                date = now
            )

            // Xóa tip cũ (của ngày trước)
            lastTipDoc?.reference?.delete()?.await()

            // Lưu tip mới
            val docRef = firestore.collection("aiTips").add(newTip).await()

            newTip.copy(id = docRef.id)

        } catch (e: Exception) {
            Log.e("AIRepository", "Error generating tip", e)
            null
        }
    }

    suspend fun generateDiscussionTopic(groupId: String, level: String): AIDiscussionTopic? {
        // (Giữ nguyên)
        return try {
            val aiResponse = chatWithAI(
                "Generate an interesting discussion topic for Japanese learners at $level level. " +
                        "Include a topic title and a brief description to spark conversation. " +
                        "Response in Vietnamese. Format: Topic: [title]\\nDescription: [description]",
                groupId = groupId
            )

            if (aiResponse != null) {
                val lines = aiResponse.reply.split("\n")
                val topic = lines.find { it.startsWith("Topic:") }
                    ?.substringAfter("Topic:")?.trim() ?: "Thảo luận tiếng Nhật"
                val description = lines.find { it.startsWith("Description:") }
                    ?.substringAfter("Description:")?.trim() ?: aiResponse.reply

                val discussionTopic = AIDiscussionTopic(
                    groupId = groupId,
                    topic = topic,
                    description = description,
                    level = level
                )

                val docRef = firestore.collection("aiDiscussionTopics").add(discussionTopic).await()
                discussionTopic.copy(id = docRef.id)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("AIRepository", "Error generating discussion topic", e)
            null
        }
    }

    suspend fun getDiscussionTopicsForGroup(groupId: String, limit: Int = 10): List<AIDiscussionTopic> {
        // (Giữ nguyên)
        return try {
            firestore.collection("aiDiscussionTopics")
                .whereEqualTo("groupId", groupId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(AIDiscussionTopic::class.java)?.copy(id = it.id) }
        } catch (e: Exception) {
            Log.e("AIRepository", "Error getting discussion topics", e)
            emptyList()
        }
    }

}