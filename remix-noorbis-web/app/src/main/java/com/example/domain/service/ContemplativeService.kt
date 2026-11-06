package com.example.domain.service

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

enum class FeelingType(val label: String, val emoji: String, val queryText: String) {
    TIRED("Fatigué / Découragé", "🌧️", "Je me sens fatigué, vidé ou découragé par les épreuves."),
    BURDENED("Surchargé de responsabilités", "⚡", "Je porte de trop lourdes charges et je ressens beaucoup de pression."),
    ANXIOUS("Inquiet pour l'avenir", "⚓", "J'ai peur du lendemain et je lutte contre l'inquiétude et le doute."),
    SEEKING_PEACE("En quête de paix et réconfort", "🕊️", "Mon cœur a besoin de silence, de sérénité et d'apaisement intérieur."),
    LONELY("Seul ou incompris", "🌌", "Je me sens parfois isolé ou incompris dans ce que je traverse."),
    GRATEFUL("Touché et reconnaissant", "✨", "Ce passage touche mon cœur et éveille ma reconnaissance et mon espérance.")
}

data class KeywordChallenge(
    val maskedVerse: String,
    val targetWord: String,
    val options: List<String>
)

object ContemplativeService {

    /**
     * Creates an interactive decoding step for the scripture verse by masking a central spiritual word.
     */
    fun extractKeywordChallenge(verse: String, fruitName: String): KeywordChallenge {
        if (verse.isBlank()) {
            return KeywordChallenge(
                maskedVerse = "« Demeurez dans mon [ ... ], et je demeurerai en vous. »",
                targetWord = "amour",
                options = listOf("amour", "calme", "refuge")
            )
        }

        // Keywords commonly found in biblical meditations
        val candidateWords = listOf(
            "amour" to listOf("force", "devoir"),
            "paix" to listOf("joie", "repos"),
            "joie" to listOf("paix", "gloire"),
            "grâce" to listOf("force", "justice"),
            "force" to listOf("grâce", "sagesse"),
            "confiance" to listOf("patience", "crainte"),
            "cœur" to listOf("esprit", "chemin"),
            "patience" to listOf("bonté", "silence"),
            "bonté" to listOf("douceur", "vérité"),
            "fidélité" to listOf("fermeté", "victoire"),
            "douceur" to listOf("paix", "lumière"),
            "repos" to listOf("sommeil", "secours"),
            "Dieu" to listOf("l'homme", "le monde"),
            "Seigneur" to listOf("guide", "berger"),
            "esprit" to listOf("corps", "souffle")
        )

        val cleanVerse = verse.replace("«", "").replace("»", "").trim()
        val words = cleanVerse.split(Regex("[\\s,;:.?!'’()]+")).filter { it.length >= 3 }

        for ((target, distractors) in candidateWords) {
            val matchedWord = words.find { it.equals(target, ignoreCase = true) }
            if (matchedWord != null) {
                val regex = Regex("\\b${Regex.escape(matchedWord)}\\b", RegexOption.IGNORE_CASE)
                val masked = cleanVerse.replaceFirst(regex, "[ ... ]")
                val options = (listOf(matchedWord.lowercase()) + distractors).shuffled()
                return KeywordChallenge(
                    maskedVerse = "« $masked »",
                    targetWord = matchedWord.lowercase(),
                    options = options
                )
            }
        }

        // Fallback: pick the fruit name or a significant word in the verse
        val fallbackTarget = words.maxByOrNull { it.length } ?: fruitName
        val masked = cleanVerse.replaceFirst(fallbackTarget, "[ ... ]")
        val options = listOf(fallbackTarget.lowercase(), "lumière", "espérance").shuffled()
        return KeywordChallenge(
            maskedVerse = "« $masked »",
            targetWord = fallbackTarget.lowercase(),
            options = options
        )
    }

    /**
     * Generates an empathetic pastoral reflection tailored to the user's emotional state,
     * the Scripture verse, and the Fruit of the Spirit.
     * Uses Gemini API when GEMINI_API_KEY is configured, or our rich spiritual reflection matrix.
     */
    suspend fun generatePersonalReflection(
        verse: String,
        fruit: String,
        situation: String,
        feeling: FeelingType
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val aiResponse = callGeminiApi(
                    apiKey = apiKey,
                    verse = verse,
                    fruit = fruit,
                    feeling = feeling.queryText
                )
                if (aiResponse.isNotBlank()) {
                    return@withContext aiResponse
                }
            } catch (_: Exception) {
                // Fall back to offline contemplative reflection on failure
            }
        }

        getOfflineReflection(fruit, situation, feeling)
    }

    private fun callGeminiApi(
        apiKey: String,
        verse: String,
        fruit: String,
        feeling: String
    ): String {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
        val url = URL(endpoint)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.connectTimeout = 4000
        conn.readTimeout = 5000
        conn.doOutput = true

        val prompt = """
            Tu es un accompagnateur spirituel chrétien empreint de bienveillance, de douceur et d'écoute.
            Méditation : Fruit de l'Esprit ($fruit)
            Texte biblique : "$verse"
            Ressenti du joueur : "$feeling"
            
            Rédige en français un message de 2 à 3 phrases très réconfortantes, apaisantes et personnelles reliant cette méditation à son besoin intérieur. Termine par une question douce pour l'aider à méditer en paix.
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("maxOutputTokens", 180)
                put("temperature", 0.7)
            })
        }

        OutputStreamWriter(conn.outputStream).use { writer ->
            writer.write(jsonBody.toString())
            writer.flush()
        }

        val code = conn.responseCode
        if (code in 200..299) {
            BufferedReader(InputStreamReader(conn.inputStream)).use { reader ->
                val responseStr = reader.readText()
                val json = JSONObject(responseStr)
                val candidates = json.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val content = candidates.getJSONObject(0).optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return parts.getJSONObject(0).optString("text", "").trim()
                    }
                }
            }
        }
        return ""
    }

    /**
     * Rich, graceful contemplative responses tailored to the Fruit facet and human situation.
     */
    private fun getOfflineReflection(fruit: String, situation: String, feeling: FeelingType): String {
        return when (feeling) {
            FeelingType.TIRED -> {
                "Quand la fatigue et le découragement pèsent sur vos épaules, la facette de $fruit vient vous rappeler que vous n'avez pas à tout accomplir par vous-même. Déposez cette lassitude : la grâce vous accueille sans exiger d'exploit. Pouvez-vous vous accorder, dès cet instant, le droit de simplement respirer et vous reposer ?"
            }
            FeelingType.BURDENED -> {
                "Face au poids des responsabilités quotidiennes, $fruit agit comme un doux rappel : votre valeur ne se mesure pas à votre capacité à tout porter. Dieu renouvelle votre souffle dans le silence. Quelle tâche ou attente pourriez-vous aujourd'hui confier avec confiance entre Ses mains ?"
            }
            FeelingType.ANXIOUS -> {
                "L'inquiétude tente souvent de dessiner un avenir incertain, mais $fruit offre une ancre inébranlable. Vous n'êtes pas abandonné dans la tempête ; la fidélité divine devance chacun de vos pas. Quel pas de paix pouvez-vous poser aujourd'hui sans vous préoccuper du lendemain ?"
            }
            FeelingType.SEEKING_PEACE -> {
                "Ce texte est un sanctuaire offert à votre cœur. En accueillant la dimension de $fruit, laissez la paix intérieure dissiper le tumulte environnant. Qu'est-ce qui, au fond de vous, aspire le plus à être apaisé et consolé aujourd'hui ?"
            }
            FeelingType.LONELY -> {
                "Lorsque la solitude ou l'incompréhension se fait sentir, $fruit vous rappelle une présence fidèle qui voit et comprend ce que nul autre ne perçoit. Vous êtes précieux et accompagné à chaque instant. Comment pouvez-vous accueillir cette présence bienveillante à vos côtés aujourd'hui ?"
            }
            FeelingType.GRATEFUL -> {
                "Quelle joie de sentir votre cœur résonner avec la lumière de $fruit ! La gratitude ouvre un espace intérieur où l'espérance prend racine et rayonne autour de vous. Comment aimeriez-vous partager ou prolonger cet élan de reconnaissance aujourd'hui ?"
            }
        }
    }
}
