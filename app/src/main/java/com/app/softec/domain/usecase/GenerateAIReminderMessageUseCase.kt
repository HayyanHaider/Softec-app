package com.app.softec.domain.usecase

import com.app.softec.domain.model.Account
import com.app.softec.domain.model.Customer
import com.app.softec.domain.model.ReminderTemplates
import com.app.softec.domain.repository.SettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import org.json.JSONObject

/**
 * Use case for generating AI-powered reminder messages using Google Gemini API.
 * Provides context-aware, professional messages tailored to account severity.
 * 
 * This implementation uses direct HTTP calls to the Gemini API.
 */
class GenerateAIReminderMessageUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    /**
     * Generates a personalized AI message based on account details.
     *
     * @param account The overdue account/invoice.
     * @param customer The customer associated with the account.
     * @param apiKey The Gemini API key (or null to use default).
     * @return The AI-generated message string.
     * @throws IllegalArgumentException if API key is missing.
     */
    suspend operator fun invoke(
        account: Account,
        customer: Customer,
        apiKey: String? = null
    ): String = try {
        val key = apiKey ?: settingsRepository.geminiApiKey.first()
        
        if (key.isNullOrBlank()) {
            "Error: Gemini API key not configured. Please add your API key in Settings → Update Reminders → Gemini API Key"
        } else {
            val templates = settingsRepository.reminderTemplates.first()
            val urgencyLevel = determineUrgency(account.daysOverdue)
            val prompt = buildPrompt(
                promptTemplate = templates.aiPromptTemplate,
                customer = customer,
                account = account,
                urgency = urgencyLevel
            )

            callGeminiAPI(key, prompt)
        }
    } catch (e: Exception) {
        "Error generating AI message: ${e.message ?: e.toString()}"
    }

    /**
     * Generates a message with a specific urgency level override.
     */
    suspend fun generateWithUrgency(
        account: Account,
        customer: Customer,
        urgencyLevel: String,
        apiKey: String? = null
    ): String = try {
        val key = apiKey ?: settingsRepository.geminiApiKey.first()
        
        if (key.isNullOrBlank()) {
            "Error: Gemini API key not configured. Please add your API key in Settings → Update Reminders → Gemini API Key"
        } else {
            val templates = settingsRepository.reminderTemplates.first()
            val prompt = buildPrompt(
                promptTemplate = templates.aiPromptTemplate,
                customer = customer,
                account = account,
                urgency = urgencyLevel
            )

            callGeminiAPI(key, prompt)
        }
    } catch (e: Exception) {
        "Error generating AI message: ${e.message ?: e.toString()}"
    }

    private suspend fun callGeminiAPI(apiKey: String, prompt: String): String {
        return withContext(Dispatchers.IO) {
            try {
                if (apiKey.isBlank()) {
                    return@withContext "Error: Gemini API key is empty. Please configure it in Settings."
                }

                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
                val requestBody = JSONObject().apply {
                    put("contents", JSONObject().apply {
                        put("parts", org.json.JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                }

                val connection = URL(url).openConnection() as java.net.HttpURLConnection
                connection.apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    connectTimeout = 10000
                    readTimeout = 10000
                    doOutput = true
                }

                val requestBytes = requestBody.toString().toByteArray(Charsets.UTF_8)
                
                try {
                    connection.outputStream.use { os ->
                        os.write(requestBytes)
                        os.flush()
                    }

                    val responseCode = connection.responseCode
                    
                    if (responseCode == 200) {
                        val response = connection.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                            reader.readText()
                        }
                        
                        val jsonResponse = JSONObject(response)
                        val candidates = jsonResponse.optJSONArray("candidates")
                        
                        if (candidates != null && candidates.length() > 0) {
                            val content = candidates.getJSONObject(0).optJSONObject("content")
                            if (content != null) {
                                val parts = content.optJSONArray("parts")
                                if (parts != null && parts.length() > 0) {
                                    val text = parts.getJSONObject(0).optString("text")
                                    if (text.isNotBlank()) {
                                        return@withContext text
                                    }
                                }
                            }
                        }
                        
                        return@withContext "Unable to generate message. Please try again."
                    } else {
                        val errorStream = connection.errorStream
                        val errorMessage = if (errorStream != null) {
                            errorStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                        } else {
                            connection.responseMessage ?: "Unknown error"
                        }
                        
                        return@withContext "API Error ($responseCode): $errorMessage"
                    }
                } finally {
                    connection.disconnect()
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: e.toString()
                return@withContext "Error calling Gemini API: $errorMsg"
            }
        }
    }

    private fun buildPrompt(
        promptTemplate: String,
        customer: Customer,
        account: Account,
        urgency: String
    ): String {
        val amountStr = formatAmount(account.amountRemaining)
        return promptTemplate
            .replace("{name}", customer.customerName)
            .replace("{amount}", amountStr)
            .replace("{urgency}", urgency)
    }

    private fun determineUrgency(daysOverdue: Int): String {
        return when {
            daysOverdue <= 7 -> "Low (early reminder)"
            daysOverdue in 8..30 -> "Medium (moderate delay)"
            else -> "High (urgent action required)"
        }
    }

    private fun formatAmount(amount: Double): String {
        return String.format("$%.2f", amount)
    }
}

