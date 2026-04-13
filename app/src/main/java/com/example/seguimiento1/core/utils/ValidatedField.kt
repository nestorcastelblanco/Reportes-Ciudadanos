package com.example.seguimiento1.core.utils

data class ValidatedField(
    val value: String = "",
    val error: String? = null,
    val isTouched: Boolean = false
) {
    val isValid: Boolean get() = error == null

    fun onChange(newValue: String, validate: (String) -> String?): ValidatedField {
        return ValidatedField(
            value = newValue,
            error = validate(newValue),
            isTouched = true
        )
    }

    fun validate(validate: (String) -> String?): ValidatedField {
        return copy(error = validate(value), isTouched = true)
    }

    fun reset(): ValidatedField = ValidatedField()
}

