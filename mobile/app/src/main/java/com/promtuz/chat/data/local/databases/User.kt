package com.promtuz.chat.data.local.databases

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey val key: ByteArray,
    @ColumnInfo(name = "name") val name: String?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (!key.contentEquals(other.key)) return false

        return true
    }

    override fun hashCode(): Int {
        return key.contentHashCode()
    }
}


//@Dao
//interface UserDao {
//
//}