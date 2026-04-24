package com.example.camscanner.domain.model

data class Document(
    val id: String,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
    val pages: List<Page>
)
