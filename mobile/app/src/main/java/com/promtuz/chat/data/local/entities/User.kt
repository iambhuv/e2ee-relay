package com.promtuz.chat.data.local.entities


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey val key: ByteArray,
    @ColumnInfo(name = "nickname") val nickname: String = "",
) {
    @Ignore
    var isNew: Boolean = false

    override fun equals(other: Any?) =
        other is User && key.contentEquals(other.key)

    override fun hashCode() = key.contentHashCode()
}