package com.example.nihongo.User.data.repository

import com.example.nihongo.User.data.models.User
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepository(private val realm: Realm) {
    suspend fun createUser(user: User) {
        realm.write { copyToRealm(user) }
    }

    fun authenticate(username: String, password: String): Flow<User?> {
        return realm.query<User>("username == $0 AND password == $1", username, password)
            .first()
            .asFlow()
            .map { it.obj }
    }
}