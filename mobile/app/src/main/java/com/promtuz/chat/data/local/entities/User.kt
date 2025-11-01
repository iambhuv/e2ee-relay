package com.promtuz.chat.data.local.entities


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey val key: List<Byte>,
    @ColumnInfo(name = "nickname") val nickname: String?
)