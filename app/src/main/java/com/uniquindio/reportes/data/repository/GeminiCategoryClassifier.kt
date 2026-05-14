package com.uniquindio.reportes.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.uniquindio.reportes.BuildConfig
import com.uniquindio.reportes.domain.model.ReportCategory
import com.uniquindio.reportes.domain.repository.CategoryClassifier
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Clasificador basado en Gemini (Google AI).
 *
 * Devuelve `null` si la API key no está configurada, si la respuesta no se puede
 * mapear a una categoría conocida, o si la llamada falla. Esto permite al
 * ViewModel hacer fallback a la heurística local sin romper la UI.
 */
@Singleton
class GeminiCategoryClassifier @Inject constructor() : CategoryClassifier {

    private val apiKey: String = BuildConfig.GEMINI_API_KEY

    private val model: GenerativeModel? by lazy {
        if (apiKey.isBlank()) return@lazy null
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0f
                maxOutputTokens = 16
            }
        )
    }

    override suspend fun classify(title: String, description: String): ReportCategory? {
        val m = model ?: return null
        val text = "Titulo: ${title.trim()}\nDescripcion: ${description.trim()}"
            .take(MAX_INPUT_CHARS)
        val prompt = buildPrompt(text)
        return runCatching {
            val response = m.generateContent(prompt)
            val raw = response.text.orEmpty().trim().uppercase()
            parseCategory(raw)
        }.getOrNull()
    }

    private fun buildPrompt(text: String): String = """
        Eres un clasificador de reportes ciudadanos. Lee el texto y responde EXCLUSIVAMENTE
        con una sola palabra que sea una de estas categorias (sin explicacion, sin punto final):
        SECURITY, MEDICAL_EMERGENCIES, INFRASTRUCTURE, PETS, COMMUNITY.

        Guia:
        - SECURITY: robos, hurtos, atracos, armas, inseguridad.
        - MEDICAL_EMERGENCIES: accidentes con heridos, urgencias medicas, ambulancia.
        - INFRASTRUCTURE: huecos, postes, alcantarillas, fugas de agua, alumbrado, vias danadas.
        - PETS: mascotas perdidas, animales en peligro o sin dueno.
        - COMMUNITY: convivencia, basura, ruido, espacio publico que no encaje en lo anterior.

        Texto:
        $text
    """.trimIndent()

    private fun parseCategory(raw: String): ReportCategory? {
        if (raw.isBlank()) return null
        ReportCategory.entries.firstOrNull { it.name == raw }?.let { return it }
        return ReportCategory.entries.firstOrNull { raw.contains(it.name) }
    }

    private companion object {
        const val MAX_INPUT_CHARS = 1_000
    }
}
