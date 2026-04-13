package com.example.seguimiento1.core.utils

import android.util.Patterns
import com.example.seguimiento1.R

object FieldValidators {

    fun required(value: String, emptyMessage: Int): Int? {
        return if (value.isBlank()) emptyMessage else null
    }

    fun email(value: String): Int? {
        return when {
            value.isBlank() -> R.string.validator_required_email
            !Patterns.EMAIL_ADDRESS.matcher(value).matches() -> R.string.validator_invalid_email
            else -> null
        }
    }

    fun password(value: String): Int? {
        return when {
            value.isBlank() -> R.string.validator_required_password
            value.length < 8 -> R.string.validator_password_min_length
            !value.any { it.isDigit() } -> R.string.validator_password_requires_digit
            else -> null
        }
    }

    fun confirmPassword(password: String, confirm: String): Int? {
        return when {
            confirm.isBlank() -> R.string.validator_confirm_password_required
            confirm != password -> R.string.validator_password_mismatch
            else -> null
        }
    }
}

