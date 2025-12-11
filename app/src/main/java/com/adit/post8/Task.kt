package com.adit.post8

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val deadline: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = 0L
)