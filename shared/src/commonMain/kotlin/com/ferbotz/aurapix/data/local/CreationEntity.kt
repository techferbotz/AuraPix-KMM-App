package com.ferbotz.aurapix.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "creations")
data class CreationEntity(
    @PrimaryKey val id: String,
    val status: String,
    val generatedImageUrl: String?,
    val templateTitleSnapshot: String,
    val templateThumbnailSnapshot: String?,
    val createdAt: String,
    val templateId: String,
    val failureReason: String?,
)
