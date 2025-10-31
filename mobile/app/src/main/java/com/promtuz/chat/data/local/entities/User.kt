package com.promtuz.chat.data.local.entities


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.promtuz.chat.data.local.converters.ByteConverter

@Entity
@TypeConverters(ByteConverter::class)
data class User(
    @PrimaryKey val key: List<Byte>,
    @ColumnInfo(name = "nickname") val nickname: String?
)

