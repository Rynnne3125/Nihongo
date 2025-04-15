package com.example.nihongo.User.data.repository

import com.example.nihongo.User.data.models.User

class UserRepository(private val userDao: UserDao) {
    suspend fun registerUser(user: User): Boolean {
        val existing = userDao.getUserByUsername(user.username)
        return if (existing == null) {
            userDao.insertUser(user)
            true
        } else {
            false
        }
    }

    suspend fun loginUser(username: String, password: String): User? {
        return userDao.login(username, password)
    }
    suspend fun loginUserByEmail(email: String, password: String): User? {
        return userDao.getUserByEmailAndPassword(email, password)
    }
    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }



}
