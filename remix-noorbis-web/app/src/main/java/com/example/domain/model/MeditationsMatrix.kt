package com.example.domain.model

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CyberCyanGlow
import com.example.ui.theme.EmeraldNovaGlow
import com.example.ui.theme.PlasmaRoseGlow
import com.example.ui.theme.SolarGoldGlow
import com.example.ui.theme.VioletPulsarGlow
import org.json.JSONArray
import kotlin.random.Random

data class FruitFacet(
    val id: String,
    val name: String,
    val emoji: String,
    val lead: String,
    val primaryColor: Color,
    val glowColor: Color
)

data class LifeSituation(
    val key: String,
    val title: String,
    val hint: String
)

data class MeditationScripture(
    val fruitId: String,
    val fruitName: String,
    val fruitEmoji: String,
    val sitKey: String,
    val sitTitle: String,
    val sitHint: String,
    val textIndex: Int,
    val ref: String,
    val verse: String,
    val explain: String,
    val keep: String,
    val prayer: String
) {
    val fullFormattedContent: String
        get() = buildString {
            append("« ").append(verse).append(" »\n\n")
            append("📖 Ce que ce texte dit :\n").append(explain).append("\n\n")
            append("🌱 Ce que je garde aujourd'hui :\n").append(keep).append("\n\n")
            append("🙏 Prière :\n").append(prayer)
        }

    val displayTitle: String
        get() = "$fruitName · $sitTitle ($ref)"
}

object MeditationsMatrix {

    val FRUITS: List<FruitFacet> = listOf(
        FruitFacet(
            id = "amour",
            name = "Amour",
            emoji = "♥",
            lead = "Malgré quoi veux-tu demeurer dans Son amour aujourd'hui ?",
            primaryColor = Color(0xFFE91E63),
            glowColor = PlasmaRoseGlow
        ),
        FruitFacet(
            id = "joie",
            name = "Joie",
            emoji = "✧",
            lead = "Malgré quoi veux-tu garder une joie que personne ne peut ravir ?",
            primaryColor = Color(0xFFFFD54F),
            glowColor = SolarGoldGlow
        ),
        FruitFacet(
            id = "paix",
            name = "Paix",
            emoji = "☁",
            lead = "Malgré quoi veux-tu demeurer dans Sa paix aujourd'hui ?",
            primaryColor = Color(0xFF00E5FF),
            glowColor = CyberCyanGlow
        ),
        FruitFacet(
            id = "patience",
            name = "Patience",
            emoji = "⏳",
            lead = "Malgré quoi veux-tu tenir sans te durcir aujourd'hui ?",
            primaryColor = Color(0xFFFFB74D),
            glowColor = Color(0xFFFFCC80)
        ),
        FruitFacet(
            id = "bonte",
            name = "Bonté",
            emoji = "☀",
            lead = "Malgré quoi veux-tu laisser passer la bonté de Dieu aujourd'hui ?",
            primaryColor = Color(0xFFFFEE58),
            glowColor = Color(0xFFFFF59D)
        ),
        FruitFacet(
            id = "bienveillance",
            name = "Bienveillance",
            emoji = "🤝",
            lead = "Malgré quoi veux-tu laisser le bien passer aujourd'hui ?",
            primaryColor = Color(0xFF00E676),
            glowColor = EmeraldNovaGlow
        ),
        FruitFacet(
            id = "fidelite",
            name = "Fidélité",
            emoji = "⚓",
            lead = "Malgré quoi veux-tu t'appuyer sur Sa fidélité aujourd'hui ?",
            primaryColor = Color(0xFF448AFF),
            glowColor = Color(0xFF82B1FF)
        ),
        FruitFacet(
            id = "douceur",
            name = "Douceur",
            emoji = "🌿",
            lead = "Malgré quoi veux-tu rester doux, sans te durcir ?",
            primaryColor = Color(0xFF80CBC4),
            glowColor = Color(0xFFA7FFEB)
        ),
        FruitFacet(
            id = "maitrise",
            name = "Maîtrise",
            emoji = "◇",
            lead = "Malgré quoi veux-tu laisser l'Esprit gouverner aujourd'hui ?",
            primaryColor = Color(0xFFBA68C8),
            glowColor = VioletPulsarGlow
        )
    )

    val SITUATIONS: List<LifeSituation> = listOf(
        LifeSituation("sante", "Malgré la santé", "Quand le corps fatigue, fait mal, ou inquiète."),
        LifeSituation("charges", "Malgré les responsabilités", "Quand les charges pèsent et que tout repose sur toi."),
        LifeSituation("age", "Malgré l'âge", "Quand les années passent et que les forces changent."),
        LifeSituation("solitude", "Malgré la solitude", "Quand on se sent seul, oublié, ou trop à l'écart."),
        LifeSituation("inquietude", "Malgré l'inquiétude", "Quand le cœur tourne en rond et n'arrive plus à se poser."),
        LifeSituation("fatigue", "Malgré la fatigue", "Quand il n'y a plus de force, plus de marge."),
        LifeSituation("coeur", "Malgré un cœur lourd", "Quand quelque chose pèse : peine, regret, accablement."),
        LifeSituation("deuil", "Malgré le deuil", "Quand on a perdu quelqu'un, ou quelque chose qui ne reviendra pas."),
        LifeSituation("attente", "Malgré l'attente", "Quand la prière tarde, la réponse ne vient pas, le temps s'étire."),
        LifeSituation("offense", "Malgré une offense", "Quand on a été blessé, et que le pardon coûte."),
        LifeSituation("honte", "Malgré la honte", "Quand on se sent sale, coupable, ou indigne de s'approcher."),
        LifeSituation("inutile", "Malgré le sentiment d'être inutile", "Quand on se croit de trop, trop faible, plus bon à rien."),
        LifeSituation("doute", "Malgré le doute", "Quand la foi vacille, et que les questions prennent toute la place."),
        LifeSituation("injustice", "Malgré l'injustice", "Quand on est lésé, oublié, ou que le mal semble gagner."),
        LifeSituation("incomprehension", "Malgré l'incompréhension", "Quand on se sent mal jugé, rejeté, ou incapable de faire entendre son cœur.")
    )

    private var allMeditations: List<MeditationScripture> = emptyList()
    private var isLoaded: Boolean = false

    val FruitFacets: List<FruitFacet> get() = FRUITS

    fun getFruitById(fruitIdOrName: String): FruitFacet {
        return FRUITS.find {
            it.id.equals(fruitIdOrName, ignoreCase = true) || it.name.equals(fruitIdOrName, ignoreCase = true)
        } ?: FRUITS[0]
    }

    fun getFruitEmoji(fruitIdOrName: String): String {
        return getFruitById(fruitIdOrName).emoji
    }

    fun getFruitColor(fruitIdOrName: String): Color {
        return getFruitById(fruitIdOrName).primaryColor
    }

    fun getFruitGlowColor(fruitIdOrName: String): Color {
        return getFruitById(fruitIdOrName).glowColor
    }

    fun ensureLoaded(context: Context) {
        if (isLoaded && allMeditations.isNotEmpty()) return
        synchronized(this) {
            if (isLoaded && allMeditations.isNotEmpty()) return
            try {
                val jsonText = context.assets.open("meditations_matrix.json").bufferedReader().use { it.readText() }
                val array = JSONArray(jsonText)
                val list = ArrayList<MeditationScripture>(array.length())
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        MeditationScripture(
                            fruitId = obj.getString("fruitId"),
                            fruitName = obj.getString("fruitName"),
                            fruitEmoji = obj.getString("fruitEmoji"),
                            sitKey = obj.getString("sitKey"),
                            sitTitle = obj.getString("sitTitle"),
                            sitHint = obj.optString("sitHint", ""),
                            textIndex = obj.getInt("textIndex"),
                            ref = obj.getString("ref"),
                            verse = obj.getString("verse"),
                            explain = obj.getString("explain"),
                            keep = obj.getString("keep"),
                            prayer = obj.getString("prayer")
                        )
                    )
                }
                allMeditations = list
                isLoaded = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getAllMeditations(): List<MeditationScripture> = allMeditations

    fun getMeditationsForFruit(fruitId: String): List<MeditationScripture> {
        val target = fruitId.lowercase()
        return allMeditations.filter { it.fruitId == target }
    }

    fun getMeditationsForCombination(fruitId: String, sitKey: String): List<MeditationScripture> {
        val targetFruit = fruitId.lowercase()
        val targetSit = sitKey.lowercase()
        return allMeditations.filter { it.fruitId == targetFruit && it.sitKey == targetSit }
    }

    /**
     * Selects 3 Fruit Facets for the mission level, paired randomly with situations and scripture meditations.
     */
    fun getMissionMeditationsForLevel(levelNumber: Int, seed: Long = 0L): List<MeditationScripture> {
        val rand = if (seed != 0L) Random(seed) else Random(levelNumber * 10007L + System.currentTimeMillis())

        // Rotation cycles through all 9 fruits so every facet is discovered across levels
        val fruitMissionRotations = listOf(
            listOf("amour", "joie", "paix"),
            listOf("patience", "bonte", "bienveillance"),
            listOf("fidelite", "douceur", "maitrise"),
            listOf("amour", "patience", "fidelite"),
            listOf("joie", "bonte", "douceur"),
            listOf("paix", "bienveillance", "maitrise"),
            listOf("amour", "bonte", "maitrise"),
            listOf("joie", "patience", "douceur"),
            listOf("paix", "bienveillance", "fidelite")
        )

        val rotation = fruitMissionRotations[(levelNumber - 1).coerceAtLeast(0) % fruitMissionRotations.size]

        return rotation.mapIndexed { slot, fruitId ->
            val pool = getMeditationsForFruit(fruitId)
            if (pool.isNotEmpty()) {
                pool[rand.nextInt(pool.size)]
            } else {
                val fObj = getFruitById(fruitId)
                MeditationScripture(
                    fruitId = fObj.id,
                    fruitName = fObj.name,
                    fruitEmoji = fObj.emoji,
                    sitKey = "sante",
                    sitTitle = "Malgré la santé",
                    sitHint = "Quand le corps fatigue, fait mal, ou inquiète.",
                    textIndex = slot,
                    ref = "Galates 5.22-23",
                    verse = "Le fruit de l'Esprit, c'est l'amour, la joie, la paix, la patience, la bonté, la bienveillance, la foi, la douceur, la maîtrise de soi.",
                    explain = "La grâce de Dieu demeure souveraine en Jésus-Christ.",
                    keep = "Je demeure en Jésus aujourd'hui.",
                    prayer = "Seigneur, fais grandir en moi le fruit de Ton Esprit. Amen."
                )
            }
        }
    }
}
