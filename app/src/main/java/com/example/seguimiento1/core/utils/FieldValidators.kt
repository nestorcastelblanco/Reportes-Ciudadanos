package com.example.seguimiento1.core.utils

import android.util.Patterns

object FieldValidators {

    fun required(value: String, emptyMessage: String): String? {
        return if (value.isBlank()) emptyMessage else null
    }

    fun email(value: String): String? {
        return when {
            value.isBlank() -> "El correo es obligatorio"
            !Patterns.EMAIL_ADDRESS.matcher(value).matches() -> "Correo invalido"
            else -> null
        }
    }

    fun password(value: String): String? {
        return when {
            value.isBlank() -> "La contrasena es obligatoria"
            value.length < 8 -> "Minimo 8 caracteres"
            !value.any { it.isDigit() } -> "Debe contener al menos un numero"
            else -> null
        }
    }

    fun confirmPassword(password: String, confirm: String): String? {
        return when {
            confirm.isBlank() -> "Confirma la contrasena"
            confirm != password -> "Las contrasenas no coinciden"
            else -> null
        }
    }
}

