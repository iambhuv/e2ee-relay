package com.promtuz.chat.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.promtuz.chat.data.local.entities.User
import kotlinx.coroutines.flow.Flow


@Dao
interface UserDao {
    @Query("SELECT * FROM user ORDER BY nickname ASC")
    fun getAll(): Flow<List<User>>

    @Query("SELECT * FROM user WHERE nickname LIKE '%' || :nickname || '%' ORDER BY nickname ASC")
    fun getAll(nickname: String): Flow<List<User>>

    @Insert
    suspend fun insert(vararg users: User)

    @Insert
    suspend fun insert(user: User)

    @Query("SELECT * FROM user WHERE `key` = :key")
    suspend fun get(key: ByteArray): User?

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)
}