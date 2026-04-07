package com.example.seguimiento1.core.utils

sealed class RequestResult<out T> {
    data class Success<T>(val data: T) : RequestResult<T>()
    data class Error(val message: String) : RequestResult<Nothing>()
}

