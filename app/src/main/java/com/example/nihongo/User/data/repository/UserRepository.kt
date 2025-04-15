package com.example.nihongo.User.data.repository

import com.example.nihongo.User.data.models.User

class UserRepository(private val userDao: UserDao) {

    private var currentUser: User? = null

    suspend fun registerUser(user: User): Boolean {
        val existing = userDao.getUserByUsername(user.username)
        return if (existing == null) {
            userDao.insertUser(user)
            currentUser = user  // set currentUser khi đăng ký thành công
            true
        } else {
            false
        }
    }

    suspend fun loginUser(username: String, password: String): User? {
        val user = userDao.login(username, password)
        currentUser = user // lưu user sau khi đăng nhập
        return user
    }

    suspend fun loginUserByEmail(email: String, password: String): User? {
        val user = userDao.getUserByEmailAndPassword(email, password)
        currentUser = user
        return user
    }

    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    suspend fun isVip(): Boolean {
        return getCurrentUser()?.isVip == true
    }

    suspend fun getCurrentUser(): User? {
        // Nếu currentUser đã có thì dùng luôn, chưa có thì tìm trong database.
        return currentUser ?: userDao.getLoggedInUser()?.also { currentUser = it }
    }
}
