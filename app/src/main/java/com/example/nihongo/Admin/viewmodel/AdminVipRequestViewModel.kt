package com.example.nihongo.Admin.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class VipPaymentRequest(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val username: String = "",
    val amount: Int = 0,
    val paymentMethod: String = "",
    val reference: String = "",
    val requestDate: Timestamp? = null,
    val status: String = "pending",
    val notes: String = ""
)

class AdminVipRequestViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    // Sửa tên collection để khớp với ProfileScreen
    private val vipRequestsCollection = firestore.collection("vipPaymentRequests")
    private val usersCollection = firestore.collection("users")

    private val _vipRequests = MutableStateFlow<List<VipPaymentRequest>>(emptyList())
    val vipRequests: StateFlow<List<VipPaymentRequest>> = _vipRequests

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadVipRequests()
    }

    fun loadVipRequests() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d("VipRequestVM", "Starting to load VIP requests")
                
                // Lấy tất cả yêu cầu VIP, sắp xếp theo thời gian gần nhất
                val snapshot = vipRequestsCollection
                    .orderBy("requestDate", Query.Direction.DESCENDING)
                    .get()
                    .await()
                
                Log.d("VipRequestVM", "Firestore query completed, documents: ${snapshot.documents.size}")
                
                val requests = snapshot.documents.mapNotNull { doc ->
                    try {
                        // Log dữ liệu để debug
                        Log.d("VipRequestVM", "Document data: ${doc.data}")
                        
                        // Chuyển đổi thủ công để tránh lỗi
                        val id = doc.id
                        val data = doc.data ?: return@mapNotNull null
                        
                        VipPaymentRequest(
                            id = id,
                            userId = data["userId"] as? String ?: "",
                            userEmail = data["userEmail"] as? String ?: "",
                            username = data["username"] as? String ?: "",
                            amount = (data["amount"] as? Number)?.toInt() ?: 0,
                            paymentMethod = data["paymentMethod"] as? String ?: "",
                            reference = data["reference"] as? String ?: "",
                            requestDate = data["requestDate"] as? Timestamp,
                            status = data["status"] as? String ?: "pending",
                            notes = data["notes"] as? String ?: ""
                        )
                    } catch (e: Exception) {
                        Log.e("VipRequestVM", "Error parsing document ${doc.id}", e)
                        null
                    }
                }
                
                _vipRequests.value = requests
                Log.d("VipRequestVM", "Loaded ${requests.size} VIP requests")
            } catch (e: Exception) {
                Log.e("VipRequestVM", "Error loading VIP requests", e)
                _errorMessage.value = "Không thể tải yêu cầu VIP: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun approveVipRequest(requestId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Lấy thông tin yêu cầu
                val requestDoc = vipRequestsCollection.document(requestId).get().await()
                val data = requestDoc.data
                
                if (data != null) {
                    val userId = data["userId"] as? String ?: ""
                    
                    if (userId.isNotEmpty()) {
                        // Cập nhật trạng thái yêu cầu thành "approved"
                        vipRequestsCollection.document(requestId)
                            .update(
                                mapOf(
                                    "status" to "approved",
                                    "approvedDate" to Timestamp.now()
                                )
                            )
                            .await()
                        
                        // Cập nhật trạng thái VIP của người dùng
                        usersCollection.document(userId)
                            .update(
                                mapOf(
                                    "vip" to true,
                                    "vipExpiryDate" to calculateExpiryDate()
                                )
                            )
                            .await()
                        
                        _errorMessage.value = "Đã phê duyệt yêu cầu VIP thành công"
                        loadVipRequests() // Tải lại danh sách
                    } else {
                        _errorMessage.value = "Không tìm thấy ID người dùng"
                    }
                } else {
                    _errorMessage.value = "Không tìm thấy yêu cầu"
                }
            } catch (e: Exception) {
                Log.e("VipRequestVM", "Error approving VIP request", e)
                _errorMessage.value = "Lỗi khi phê duyệt: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun rejectVipRequest(requestId: String, reason: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Cập nhật trạng thái yêu cầu thành "rejected" và thêm lý do
                vipRequestsCollection.document(requestId)
                    .update(
                        mapOf(
                            "status" to "rejected",
                            "notes" to reason,
                            "rejectedDate" to Timestamp.now()
                        )
                    )
                    .await()
                
                _errorMessage.value = "Đã từ chối yêu cầu VIP"
                loadVipRequests() // Tải lại danh sách
            } catch (e: Exception) {
                Log.e("VipRequestVM", "Error rejecting VIP request", e)
                _errorMessage.value = "Lỗi khi từ chối: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    // Tính ngày hết hạn VIP (30 ngày từ ngày hiện tại)
    private fun calculateExpiryDate(): Timestamp {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_MONTH, 30)
        return Timestamp(calendar.time)
    }
}


