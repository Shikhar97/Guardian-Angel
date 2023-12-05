package com.example.guardianangel

import androidx.annotation.WorkerThread

class UsersRepository(private val userDao: UsersDb.UsersDao) {
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(user: UsersDb.User) {
        userDao.insert(user)
    }
}
