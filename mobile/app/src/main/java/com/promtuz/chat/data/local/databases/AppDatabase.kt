package com.promtuz.chat.data.local.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.promtuz.chat.data.local.dao.UserDao
import com.promtuz.chat.data.local.entities.User

const val APP_DB_NAME = "db"

@Database(entities = [User::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

}