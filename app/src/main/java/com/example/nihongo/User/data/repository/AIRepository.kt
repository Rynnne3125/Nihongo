package com.example.nihongo.User.data.repository

import android.util.Log
import kotlinx.coroutines.CancellationException
import com.example.nihongo.User.data.models.Exercise
import com.example.nihongo.User.data.models.ExerciseType
import com.example.nihongo.User.data.models.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

data class AIResponse(
    val reply: String,
    val context: String?,
    val usage_time: String?
)
data class AIChatSession(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val messages: List<Map<String, String>> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis()
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
): Serializable

data class AITip(
    val id: String = "",
    val groupId: String = "",
    val tip: String = "",
    val category: String = "",
    val level: String = "",
    val date: Long = System.currentTimeMillis(),
    val likes: Int = 0
): Serializable


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

    // ============ GEMINI API CONFIG ============
    private val GEMINI_API_KEY = "YOUR-API-KEY"
    private val GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent"
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    // ============ GEMINI API DIRECT CALL ============
    private suspend fun executeGeminiRequest(requestBody: JSONObject): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$GEMINI_API_URL?key=$GEMINI_API_KEY")
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                if (!response.isSuccessful || responseBody.isNullOrEmpty()) {
                    Log.e("AIRepository", "Gemini API Error: ${response.code} - $responseBody")
                    return@withContext null
                }

                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.optJSONArray("candidates")

                if (candidates != null && candidates.length() > 0) {
                    val parts = candidates.getJSONObject(0)
                        .optJSONObject("content")
                        ?.optJSONArray("parts")

                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text")
                    }
                }
                null
            }
        } catch (e: Exception) {
            Log.e("AIRepository", "Gemini request error: ${e.stackTraceToString()}")
            null
        }
    }
    private suspend fun callGeminiAPI(prompt: String): String? = withContext(Dispatchers.IO) {
        try {
            val requestBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 2048)
                })
            }

            val request = Request.Builder()
                .url("$GEMINI_API_URL?key=$GEMINI_API_KEY")
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()

                Log.d("AIRepository", "Gemini Response Code: ${response.code}")
                Log.d("AIRepository", "Gemini Response: $responseBody")

                if (!response.isSuccessful || responseBody.isNullOrEmpty()) {
                    Log.e("AIRepository", "Gemini API Error: ${response.code}")
                    return@withContext null
                }

                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.optJSONArray("candidates")

                if (candidates != null && candidates.length() > 0) {
                    val content = candidates.getJSONObject(0)
                        .optJSONObject("content")
                    val parts = content?.optJSONArray("parts")

                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text")
                    }
                }

                null
            }
        } catch (e: SocketTimeoutException) {
            Log.e("AIRepository", "Gemini timeout: ${e.message}")
            null
        } catch (e: Exception) {
            Log.e("AIRepository", "Gemini error: ${e.stackTraceToString()}")
            null
        }
    }

    // ============ AI CHAT FUNCTIONS ============

    suspend fun chatWithAI(
        message: String,
        groupId: String? = null,
        userId: String? = null,
        conversationHistory: List<Pair<String, String>>? = null
    ): AIResponse? = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        try {
            val rootObj = JSONObject()

            // 1. SYSTEM INSTRUCTION: Định hình nhân vật ở tầng hệ thống (Giảm hao phí token lặp)
            val systemPrompt = """
            Bạn là Chatbot hỗ trợ học tiếng Nhật thân thiện.
            Chỉ trả lời câu hỏi liên quan đến học tiếng Nhật (từ vựng, ngữ pháp, JLPT, giao tiếp).
            Nếu hỏi ngoài lề, từ chối lịch sự.
            Người tạo ra bạn: Trần Thanh Phong (23IT211) và Trương Công Thành (23IT251).
        """.trimIndent()

            rootObj.put("system_instruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemPrompt) })
                })
            })

            // 2. CONTENTS: Cấu trúc hội thoại rõ ràng theo Role
            val contentsArray = JSONArray()

            // Nạp lịch sử (nếu có)
            conversationHistory?.forEach { (role, content) ->
                // Role của Gemini chỉ nhận "user" hoặc "model"
                val geminiRole = if (role.equals("user", true)) "user" else "model"
                contentsArray.put(JSONObject().apply {
                    put("role", geminiRole)
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", content) })
                    })
                })
            }

            // Nạp câu hỏi hiện tại
            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", message) })
                })
            })
            rootObj.put("contents", contentsArray)

            // 3. CONFIGURATION
            rootObj.put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("maxOutputTokens", 1024) // Giảm xuống 1024 để chat nhanh hơn
            })

            // GỌI API
            val aiReply = executeGeminiRequest(rootObj)

            if (aiReply.isNullOrEmpty()) return@withContext null

            val elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0
            return@withContext AIResponse(
                reply = aiReply.trim(),
                context = null,
                usage_time = String.format("%.2fs", elapsedTime)
            )

        } catch (e: Exception) {
            Log.e("AIRepository", "Chat error: ${e.stackTraceToString()}")
            null
        }
    }

    // ============ AI PARTNER RECOMMENDATIONS ============

    suspend fun getAIPartnerRecommendations(
        currentUser: User,
        limit: Int = 10
    ): List<UserRecommendation> = withContext(Dispatchers.IO) {
        try {
            val collectionRef = firestore.collection("ai_recommendations")

            // 1️⃣ LOAD EXISTING (Logic cũ giữ nguyên vì đã tối ưu số lệnh DB)
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

            if (existingRecs.size >= limit) return@withContext existingRecs.take(limit)

            // 2️⃣ LOAD OTHER USERS
            val allUsersDocs = firestore.collection("users")
                .whereNotEqualTo("id", currentUser.id)
                .get().await()

            val allRecedIds = existingRecs.map { it.userId }.toSet()

            // TỐI ƯU: Chỉ bóc xuất những trường THỰC SỰ cần cho AI chấm điểm (Bỏ qua imageUrl)
            val otherUsersMap = allUsersDocs.documents.associateBy { it.id }
            val candidateUsers = allUsersDocs.documents.filter { doc ->
                val uid = doc.id
                val jlpt = doc.getLong("jlptLevel")?.toInt()
                val rank = doc.getString("rank").orEmpty()

                uid !in allRecedIds && (jlpt == currentUser.jlptLevel || rank == currentUser.rank)
            }.take(20).map { doc ->
                mapOf(
                    "id" to doc.id,
                    "jlpt" to (doc.getLong("jlptLevel")?.toInt() ?: 0),
                    "rank" to doc.getString("rank").orEmpty()
                )
            }

            if (candidateUsers.isEmpty()) return@withContext existingRecs.take(limit)

            // 3️⃣ BUILD REQUEST JSON BẮT BUỘC TRẢ VỀ JSON
            val rootObj = JSONObject()

            val prompt = """
            Tìm bạn học phù hợp cho user hiện tại: {"jlpt":${currentUser.jlptLevel}, "rank":"${currentUser.rank}"}.
            Danh sách ứng viên: ${JSONArray(candidateUsers)}.
            Quy tắc:
            1. Đánh giá matchScore (0-100) dựa trên mức độ tương đồng JLPT và Rank.
            2. Trả về mảng JSON. Mỗi phần tử gồm: "userId", "matchScore", "matchReasons" (mảng 2 lý do ngắn gọn).
        """.trimIndent()

            rootObj.put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply { put(JSONObject().apply { put("text", prompt) }) })
                })
            })

            // TỐI ƯU CỰC MẠNH: Ép model chỉ được phép nhả ra JSON thuần
            rootObj.put("generationConfig", JSONObject().apply {
                put("temperature", 0.4) // Nhiệt độ thấp = logic chính xác hơn
                put("response_mime_type", "application/json") // Không bao giờ có mã markdown ```json
            })

            // 4️⃣ CALL GEMINI API
            val aiReply = executeGeminiRequest(rootObj) ?: return@withContext existingRecs.take(limit)

            val jsonArray = try {
                JSONArray(aiReply) // Parser ăn liền, không cần cắt chuỗi
            } catch (_: Exception) {
                Log.e("AIRepository", "Parse JSON failed: $aiReply")
                return@withContext existingRecs.take(limit)
            }

            // 5️⃣ PARSE & SAVE TO FIRESTORE
            val newRecs = mutableListOf<UserRecommendation>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val recommendedId = obj.optString("userId")
                if (recommendedId.isBlank()) continue

                // Map ngược lại dữ liệu gốc từ Firebase (để lấy Username và ImageUrl)
                val originalUser = otherUsersMap[recommendedId] ?: continue

                val rec = UserRecommendation(
                    userId = recommendedId,
                    username = originalUser.getString("username").orEmpty(),
                    jlptLevel = originalUser.getLong("jlptLevel")?.toInt(),
                    rank = originalUser.getString("rank").orEmpty(),
                    matchScore = obj.optInt("matchScore", 0),
                    imageUrl = originalUser.getString("imageUrl").orEmpty(),
                    matchReasons = (0 until (obj.optJSONArray("matchReasons")?.length() ?: 0)).map { j ->
                        obj.optJSONArray("matchReasons")!!.optString(j)
                    }
                )

                // Lưu Firestore (Batching có thể tốt hơn, nhưng giữ luồng cũ của cậu)
                val existsSnapshot = collectionRef
                    .whereEqualTo("userId", currentUser.id)
                    .whereEqualTo("recommendedUserId", rec.userId)
                    .get().await()

                if (existsSnapshot.isEmpty) {
                    collectionRef.add(
                        mapOf(
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
                    ).await()
                }
                newRecs.add(rec)
            }

            return@withContext (existingRecs + newRecs)
                .distinctBy { it.userId }
                .sortedByDescending { it.matchScore }
                .take(limit)

        } catch (e: Exception) {
            Log.e("AIRepository", "Error recommendations", e)
            emptyList()
        }
    }
    suspend fun generateChatTitle(firstMessage: String): String = withContext(Dispatchers.IO) {
        try {
            val prompt = "Tóm tắt nội dung sau thành một tiêu đề ngắn gọn (tối đa 8 chữ, không dùng dấu ngoặc kép). Nội dung: $firstMessage"

            val rootObj = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply { put(JSONObject().apply { put("text", prompt) }) })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.3)
                    put("maxOutputTokens", 20) // Chỉ cho AI sinh ra một mẩu text siêu ngắn
                })
            }

            val aiReply = executeGeminiRequest(rootObj)
            return@withContext aiReply?.trim()?.removeSurrounding("\"") ?: "Hội thoại mới"
        } catch (e: Exception) {
            Log.e("AIRepository", "Lỗi tạo tiêu đề: ", e)
            return@withContext "Hội thoại mới"
        }
    }
    suspend fun saveConversation(
        sessionId: String?,
        userId: String,
        title: String,
        messages: List<Pair<String, String>>
    ): String? = withContext(Dispatchers.IO) {
        try {
            // Chuyển List<Pair> của UI thành định dạng Map để lưu Firestore
            val dbMessages = messages.map { mapOf("role" to it.first, "content" to it.second) }
            val collectionRef = firestore.collection("aiChatHistory")

            if (sessionId == null) {
                // Tạo phiên chat mới
                val docRef = collectionRef.document()
                val newData = mapOf(
                    "userId" to userId,
                    "title" to title,
                    "messages" to dbMessages,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(newData).await()
                return@withContext docRef.id
            } else {
                // Cập nhật phiên chat cũ (ghi đè mảng tin nhắn mới nhất)
                collectionRef.document(sessionId).update(
                    mapOf(
                        "messages" to dbMessages,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                return@withContext sessionId
            }
        } catch (e: Exception) {
            Log.e("AIRepository", "Lỗi lưu hội thoại: ", e)
            return@withContext sessionId
        }
    }
    suspend fun getUserChatHistory(userId: String): List<AIChatSession> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("aiChatHistory")
                .whereEqualTo("userId", userId)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .await()

            return@withContext snapshot.documents.mapNotNull { doc ->
                val title = doc.getString("title") ?: "Hội thoại"
                // Ép kiểu an toàn từ Firestore Map về List<Map<String, String>>
                val messages = doc.get("messages") as? List<Map<String, String>> ?: emptyList()
                val updatedAt = doc.getTimestamp("updatedAt")?.toDate()?.time ?: System.currentTimeMillis()

                AIChatSession(
                    id = doc.id,
                    userId = userId,
                    title = title,
                    messages = messages,
                    updatedAt = updatedAt
                )
            }
        } catch (e: Exception) {
            Log.e("AIRepository", "Error fetching chat history", e)
            emptyList()
        }
    }
    // ============ GROUP CHALLENGE FUNCTIONS ============

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
        } catch (e: CancellationException) {
            // NẾU LÀ LỆNH HỦY DO THOÁT MÀN HÌNH -> Ném trả lại cho hệ thống tự lo, không in lỗi
            throw e
        } catch (e: Exception) {
            // CÁC LỖI THỰC SỰ (Mất mạng, sai logic, v.v.)
            Log.e("AIRepository", "Error getting challenges", e)
            emptyList()
        }
    }

    // ============ AI TIPS FUNCTIONS ============

    suspend fun generateDailyTipForGroup(
        groupId: String,
        level: String,
        description: String
    ): AITip? {
        return try {
            val now = System.currentTimeMillis()
            val dayStart = now - (now % (24 * 60 * 60 * 1000))

            // Check TODAY'S TIP
            val existingTipSnapshot = firestore.collection("aiTips")
                .whereEqualTo("groupId", groupId)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            val lastTipDoc = existingTipSnapshot.documents.firstOrNull()

            if (lastTipDoc != null) {
                val lastTipDate = lastTipDoc.getLong("date") ?: 0L
                if (lastTipDate >= dayStart) {
                    return lastTipDoc.toObject(AITip::class.java)?.copy(id = lastTipDoc.id)
                }
            }

            // Generate new tip using optimized request
            val prompt = """
            Hãy tạo một mẹo học tiếng Nhật NGẮN GỌN cho trình độ $level, tối đa 20 chữ.
            Ngữ cảnh nhóm: "$description"
            Chỉ xuất ra câu mẹo, không giải thích, không markdown.
        """.trimIndent()

            val rootObj = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply { put(JSONObject().apply { put("text", prompt) }) })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7) // Giữ mức 0.7 để câu mẹo tự nhiên, không bị cứng
                })
            }

            val aiReply = executeGeminiRequest(rootObj)

            if (aiReply.isNullOrEmpty()) return null

            val categories = listOf("grammar", "vocabulary", "kanji", "culture")

            val newTip = AITip(
                groupId = groupId,
                tip = aiReply.trim(),
                category = categories.random(),
                level = level,
                date = now
            )

            lastTipDoc?.reference?.delete()?.await()
            val docRef = firestore.collection("aiTips").add(newTip).await()

            newTip.copy(id = docRef.id)

        } catch (e: Exception) {
            Log.e("AIRepository", "Error generating tip", e)
            null
        }
    }

    suspend fun generateQuizForGroup(groupTopic: String, numberOfQuestions: Int = 5): List<Exercise> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
            Đóng vai một giáo viên tiếng Nhật JLPT. Tạo một bài trắc nghiệm $numberOfQuestions câu hỏi đơn giản về chủ đề: "$groupTopic".
            
            Yêu cầu SƯ PHẠM (Rất quan trọng):
            1. Tránh ra đề quá dễ: Nếu hỏi tìm Kanji, các đáp án BẮT BUỘC phải viết bằng Kanji có nét tương đồng nhau để gây nhiễu. KHÔNG để đáp án dưới dạng Hiragana.
            2. Các đáp án sai (distractors) phải hợp lý và dễ nhầm lẫn với đáp án đúng.
            3. Đa dạng hóa: Trộn lẫn các loại câu hỏi (Tìm nghĩa, Chọn cách đọc đúng, Điền từ vào chỗ trống).
            
            Yêu cầu KỸ THUẬT:
            Trả về MẢNG JSON thuần túy. Mỗi object gồm:
            - "question": Câu hỏi.
            - "kana": Cách đọc (nếu cần, không có để "").
            - "romanji": Cách đọc romanji (nếu cần, không có để "").
            - "options": Mảng đúng 4 đáp án.
            - "answer": Đáp án đúng nhất (nằm trong mảng options).
        """.trimIndent()

            val rootObj = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply { put(JSONObject().apply { put("text", prompt) }) })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7) // Có độ sáng tạo nhẹ để câu hỏi không bị trùng lặp
                    put("response_mime_type", "application/json") // Bắt buộc trả về JSON
                })
            }

            val aiReply = executeGeminiRequest(rootObj)
            if (aiReply.isNullOrEmpty()) return@withContext emptyList()

            val jsonArray = JSONArray(aiReply)
            val quizList = mutableListOf<Exercise>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val optionsArray = obj.optJSONArray("options")
                val optionsList = mutableListOf<String>()

                if (optionsArray != null) {
                    for (j in 0 until optionsArray.length()) {
                        optionsList.add(optionsArray.optString(j))
                    }
                }

                quizList.add(
                    Exercise(
                        id = "ai_quiz_$i",
                        question = obj.optString("question", "Câu hỏi bị lỗi"),
                        kana = obj.optString("kana", ""),
                        romanji = obj.optString("romanji", ""),
                        options = optionsList,
                        answer = obj.optString("answer", ""),
                        type = ExerciseType.PRACTICE
                    )
                )
            }
            return@withContext quizList

        } catch (e: Exception) {
            Log.e("AIRepository", "Error generating quiz: ", e)
            return@withContext emptyList()
        }
    }
}
