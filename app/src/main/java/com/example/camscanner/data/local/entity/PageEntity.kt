package com.example.camscanner.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pages",
    foreignKeys = [ForeignKey(
        entity = DocumentEntity::class,
        parentColumns = ["id"],
        childColumns = ["documentId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("documentId")]
)
data class PageEntity(
    @PrimaryKey val id: String,
    val documentId: String,
    val pageOrder: Int,
    val originalPath: String,
    val processedPath: String,
    val previewPath: String,
    val filter: String,
    val brightness: Int,
    val contrast: Int,
    val rotation: Int,
    val cornersJson: String
)
