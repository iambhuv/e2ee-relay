package com.promtuz.chat.data.local.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.promtuz.chat.data.local.converters.ByteConverter
import com.promtuz.chat.data.local.dao.UserDao
import com.promtuz.chat.data.local.entities.User

const val APP_DB_NAME = "db"

@Database(entities = [User::class], version = 1, exportSchema = false)
@TypeConverters(ByteConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}