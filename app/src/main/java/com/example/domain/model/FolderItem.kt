package com.example.domain.model

data class FolderItem(
    val path: String,
    val name: String,
    val videoCount: Int,
    val totalSizeBytes: Long = 0L,
    val latestVideoDate: Long = 0L
)
