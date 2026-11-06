package com.example.domain.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CoralHyper
import com.example.ui.theme.CoralHyperGlow
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanGlow
import com.example.ui.theme.EmeraldNova
import com.example.ui.theme.EmeraldNovaGlow
import com.example.ui.theme.PlasmaRose
import com.example.ui.theme.PlasmaRoseGlow
import com.example.ui.theme.SolarGold
import com.example.ui.theme.SolarGoldGlow
import com.example.ui.theme.VioletPulsar
import com.example.ui.theme.VioletPulsarGlow

/**
 * The space map has been doubled in size (4.0 x 4.0 = 16.0 viewports area).
 */
const val WORLD_WIDTH = 4.0f
const val WORLD_HEIGHT = 4.0f
const val VIEWPORT_WIDTH = 1.0f
const val VIEWPORT_HEIGHT = 1.0f

@Immutable
data class SphereColorOption(
    val id: String,
    val name: String,
    val subtitle: String,
    val primaryColor: Color,
    val glowColor: Color,
    val coreColor: Color = Color.White
)

val AvailableSphereColors = listOf(
    SphereColorOption(
        id = "cyan",
        name = "Cyan Quantique",
        subtitle = "Sillage ionique haute visibilité",
        primaryColor = CyberCyan,
        glowColor = CyberCyanGlow
    ),
    SphereColorOption(
        id = "rose",
        name = "Magenta Plasma",
        subtitle = "Énergie thermique incandescente",
        primaryColor = PlasmaRose,
        glowColor = PlasmaRoseGlow
    ),
    SphereColorOption(
        id = "gold",
        name = "Or Solaire",
        subtitle = "Rayonnement stellaire doré",
        primaryColor = SolarGold,
        glowColor = SolarGoldGlow
    ),
    SphereColorOption(
        id = "emerald",
        name = "Émeraude Nova",
        subtitle = "Harmonie bio-photonique",
        primaryColor = EmeraldNova,
        glowColor = EmeraldNovaGlow
    ),
    SphereColorOption(
        id = "violet",
        name = "Violet Pulsar",
        subtitle = "Distorsion gravitationnelle",
        primaryColor = VioletPulsar,
        glowColor = VioletPulsarGlow
    ),
    SphereColorOption(
        id = "coral",
        name = "Corail Hyper",
        subtitle = "Propulsion à fusion ardente",
        primaryColor = CoralHyper,
        glowColor = CoralHyperGlow
    )
)

fun getSphereColorById(id: String): SphereColorOption {
    return AvailableSphereColors.find { it.id == id } ?: AvailableSphereColors.first()
}

@Immutable
data class SectorTextBlueprint(
    val letter: String, // Fruit facet name, e.g. "Amour", "Joie", "Paix"
    val title: String,
    val content: String,
    val basePoints: Int,
    val fruitId: String = "amour",
    val fruitEmoji: String = "♥",
    val situationTitle: String = "Malgré la santé",
    val scriptureRef: String = "",
    val verse: String = "",
    val explain: String = "",
    val keep: String = "",
    val prayer: String = ""
)

@Immutable
data class SpaceSectorLevel(
    val levelNumber: Int,
    val name: String,
    val subtitle: String,
    val obstacleCount: Int,
    val obstacleSpeedMultiplier: Float,
    val hasLaserDrones: Boolean,
    val hasPulsarMines: Boolean,
    val completionBonusPoints: Int,
    val texts: List<SectorTextBlueprint>
)

object SpaceSectorCatalog {
    private val sectorNames = listOf(
        "Ceinture d'Orion • Voie du Fruit",
        "Nébuleuse de Cristal • Triade Céleste",
        "Forge Solaire • Sanctuaire d'Amour",
        "Vortex d'Andromède • Sentier de Paix",
        "Bastion Stellaire • Couronne de Gloire",
        "Pulsar Alpha • Voie de Patience",
        "Cœur de Nébuleuse • Grâce et Bonté",
        "Frontière Cosmique • Sphère de Douceur",
        "Horizon Éternel • Plénitude de l'Esprit"
    )

    val levels: List<SpaceSectorLevel> = (1..135).map { lvl ->
        getSector(lvl, seed = lvl * 1337L)
    }

    fun getSector(levelNumber: Int, seed: Long = 0L): SpaceSectorLevel {
        val extra = (levelNumber - 1).coerceAtLeast(0)
        val baseName = sectorNames.getOrElse((levelNumber - 1) % sectorNames.size) { "Espace Cosmique" }
        val name = baseName
        val missionMeditations = MeditationsMatrix.getMissionMeditationsForLevel(levelNumber, seed)

        val blueprints = missionMeditations.mapIndexed { index, med ->
            SectorTextBlueprint(
                letter = med.fruitName,
                title = med.displayTitle,
                content = med.fullFormattedContent,
                basePoints = 100 + index * 50 + (extra % 10) * 15,
                fruitId = med.fruitId,
                fruitEmoji = med.fruitEmoji,
                situationTitle = med.sitTitle,
                scriptureRef = med.ref,
                verse = med.verse,
                explain = med.explain,
                keep = med.keep,
                prayer = med.prayer
            )
        }

        val fruitListStr = blueprints.joinToString(" • ") { "${it.fruitEmoji} ${it.letter}" }

        return SpaceSectorLevel(
            levelNumber = levelNumber,
            name = name,
            subtitle = fruitListStr,
            obstacleCount = (16 + (extra / 2) + (extra % 6)).coerceIn(16, 36),
            obstacleSpeedMultiplier = (1.15f + (extra / 15) * 0.08f + (extra % 10) * 0.07f).coerceIn(1.15f, 2.35f),
            hasLaserDrones = levelNumber >= 2,
            hasPulsarMines = levelNumber >= 3,
            completionBonusPoints = 300 + (extra % 15) * 50,
            texts = blueprints
        )
    }
}

enum class ObstacleType(
    val displayName: String,
    val subtitle: String,
    val emoji: String,
    val unlockLevel: Int,
    val description: String
) {
    ASTEROID(
        displayName = "Pensée Parasite",
        subtitle = "Toxine Cérébrale",
        emoji = "🦠",
        unlockLevel = 1,
        description = "Obstacle lourd et erratique : masse toxique qui encombre l'esprit et écrase les Pensées Pures."
    ),
    LASER_DRONE(
        displayName = "Foyer d'Angoisse",
        subtitle = "Pic de Stress",
        emoji = "⚡",
        unlockLevel = 1,
        description = "Obstacle traqueur à faisceau : projette des rayons d'anxiété et des pics de tension."
    ),
    PULSAR_MINE(
        displayName = "Crise de Panique",
        subtitle = "Décharge Émotive",
        emoji = "💥",
        unlockLevel = 1,
        description = "Obstacle explosif à pulsations : émet des ondes de choc émotionnelles instables."
    ),
    CONFORMISM_SWARM(
        displayName = "La Nuée Grégaire",
        subtitle = "Conformisme (L'esprit « Mouton »)",
        emoji = "🐑",
        unlockLevel = 20,
        description = "Essaim d'entités grises ternes et identiques en rangs serrés. N'inflige pas de dégâts directs, mais absorbe vos bonus actifs (Hyper-Propulsion, Aimant...) et vous ralentit pour vous forcer à rentrer dans le rang."
    ),
    DOGMATIC_PRISM(
        displayName = "Le Prisme Dogmatique",
        subtitle = "Endoctrinement",
        emoji = "🔷",
        unlockLevel = 40,
        description = "Monolithe géométrique froid et rigide aux arêtes tranchantes. Dévie/reflète les projectiles de vertu classiques (Paix, Joie, Amour). Se brise par une Onde Nova (EMP) ou par la vertu « Vérité & Discernement »."
    ),
    OBSCURANTISM_VEIL(
        displayName = "Le Voile d'Obscurantisme",
        subtitle = "L'Ignorance",
        emoji = "🌑",
        unlockLevel = 60,
        description = "Nébuleuse d'encre noire et brouillard statique dense. Brouille les capteurs de la mini-carte et dissimule les Étincelles de Lucidité et Fragments de Savoir situés à l'intérieur. Tirer des mots de vertu dedans dissipe le brouillard de façon permanente."
    ),
    MENTAL_MIRE(
        displayName = "Le Bourbier Mental",
        subtitle = "Incapacité",
        emoji = "🕸️",
        unlockLevel = 80,
        description = "Champ de force poisseux et anomalie gravitationnelle visqueuse grisâtre. Divise la vitesse de votre sphère par trois. Seul un bonus d'Hyper-Propulsion permet de s'en arracher sans pénalité."
    ),
    NARCISSUS_MIRROR(
        displayName = "Le Miroir de Narcisse",
        subtitle = "L'Ego",
        emoji = "🪞",
        unlockLevel = 100,
        description = "Astre artificiellement brillant et scintillant imitant les bonus ou les cœurs. Agit comme un Aimant Cosmique inversé qui attire votre sphère et draine un bouclier ou vos points d'énergie de purification au contact."
    )
}

enum class SimpleObstacleCategory(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val accentColor: Color,
    val coreColor: Color
) {
    BASE_NEURO(
        title = "⚡ Menaces Neuro-Émotionnelles de Base",
        subtitle = "Niv. 1+ • Obstacles simples menaçant directement les boucliers par collision physique",
        emoji = "⚡",
        accentColor = Color(0xFFA855F7),
        coreColor = Color(0xFF2E1065)
    ),
    DROIT_VICES_CONSENTEMENT(
        title = "⚖️ Droit et vices du consentement",
        subtitle = "Niv. 10 à 40 (+1 tous les 5 niveaux) • Contraintes, déséquilibres et atteintes au consentement libre",
        emoji = "⚖️",
        accentColor = Color(0xFFF59E0B),
        coreColor = Color(0xFF451A03)
    ),
    RHETORIQUE_MANIPULATION(
        title = "🎭 Rhétorique, mensonge et manipulation délibérée",
        subtitle = "Niv. 45 à 80 (+1 tous les 5 niveaux) • Sophismes, artifices et distorsion de la vérité",
        emoji = "🎭",
        accentColor = Color(0xFFC084FC),
        coreColor = Color(0xFF3B0764)
    ),
    PSYCHOLOGIE_EMPRISE(
        title = "🧠 Psychologie, contrôle mental et emprise",
        subtitle = "Niv. 85 à 110 (+1 tous les 5 niveaux) • Conditionnement et dépossession du discernement",
        emoji = "🧠",
        accentColor = Color(0xFFEC4899),
        coreColor = Color(0xFF500724)
    ),
    ATTEINTES_LIBERTES_AUTORITARISME(
        title = "⛓️ Atteintes aux libertés et autoritarisme",
        subtitle = "Niv. 115 à 135 (+1 tous les 5 niveaux) • Exclusion, censure, arbitraire et régimes d'exception",
        emoji = "⛓️",
        accentColor = Color(0xFFEF4444),
        coreColor = Color(0xFF450A0A)
    )
}

/**
 * Obstacles simples qui menacent directement les boucliers de la sphère par collision physique directe :
 * - Niveaux 1 à 9 : les 3 obstacles simples de base (Pensée Parasite, Foyer d'Angoisse, Crise de Panique).
 * - À partir du niveau 10 : +1 nouvel obstacle simple débloqué tous les 5 niveaux (Niv. 10, 15, 20 ... jusqu'au Niv. 135).
 */
enum class SimpleObstacleVariant(
    val displayName: String,
    val shortTag: String,
    val emoji: String,
    val unlockLevel: Int,
    val category: SimpleObstacleCategory,
    val baseArchetype: ObstacleType,
    val definition: String
) {
    // --- 3 Obstacles simples de base (Niv. 1+) ---
    PENSEE_PARASITE(
        displayName = "Pensée Parasite / Toxine Cérébrale",
        shortTag = "Pensée Parasite",
        emoji = "🦠",
        unlockLevel = 1,
        category = SimpleObstacleCategory.BASE_NEURO,
        baseArchetype = ObstacleType.ASTEROID,
        definition = "Obstacle lourd et erratique : masse organique toxique qui encombre l'esprit, menace directement vos boucliers par collision physique et écrase les Pensées Pures."
    ),
    FOYER_ANGOISSE(
        displayName = "Foyer d'Angoisse / Pic de Stress",
        shortTag = "Foyer d'Angoisse",
        emoji = "⚡",
        unlockLevel = 1,
        category = SimpleObstacleCategory.BASE_NEURO,
        baseArchetype = ObstacleType.LASER_DRONE,
        definition = "Obstacle traqueur à faisceau : noyau de tension nerveuse projetant des aiguilles d'anxiété qui menacent directement vos boucliers au contact."
    ),
    CRISE_PANIQUE(
        displayName = "Crise de Panique / Décharge Émotive",
        shortTag = "Crise de Panique",
        emoji = "💥",
        unlockLevel = 1,
        category = SimpleObstacleCategory.BASE_NEURO,
        baseArchetype = ObstacleType.PULSAR_MINE,
        definition = "Obstacle explosif à pulsations : cœur émotionnel instable émettant des ondes de choc électriques qui menacent directement vos boucliers par collision."
    ),

    // --- 1. Droit et vices du consentement (Niv. 10 à 40, tous les 5 niveaux) ---
    COERCITION(
        displayName = "Coercition",
        shortTag = "⚖️ Coercition",
        emoji = "🔨",
        unlockLevel = 10,
        category = SimpleObstacleCategory.DROIT_VICES_CONSENTEMENT,
        baseArchetype = ObstacleType.ASTEROID,
        definition = "Exercice de la force ou de la menace institutionnelle pour contraindre un individu à agir contre son gré."
    ),
    LESION(
        displayName = "Lésion",
        shortTag = "⚖️ Lésion",
        emoji = "⚖️",
        unlockLevel = 15,
        category = SimpleObstacleCategory.DROIT_VICES_CONSENTEMENT,
        baseArchetype = ObstacleType.LASER_DRONE,
        definition = "Préjudice né du déséquilibre grave entre les prestations d'un contrat conclu au détriment de l'une des parties."
    ),
    CAPTATION(
        displayName = "Captation",
        shortTag = "⚖️ Captation",
        emoji = "🕸️",
        unlockLevel = 20,
        category = SimpleObstacleCategory.DROIT_VICES_CONSENTEMENT,
        baseArchetype = ObstacleType.PULSAR_MINE,
        definition = "Manœuvres insidieuses visant à circonvenir la volonté d'une personne affaiblie pour obtenir d'elle un avantage matériel ou moral."
    ),
    VOIE_DE_FAIT(
        displayName = "Voie de fait",
        shortTag = "⚖️ Voie de fait",
        emoji = "🚧",
        unlockLevel = 25,
        category = SimpleObstacleCategory.DROIT_VICES_CONSENTEMENT,
        baseArchetype = ObstacleType.ASTEROID,
        definition = "Acte manifestement illégal commis par une autorité publique portant une atteinte grave aux libertés individuelles ou au droit de propriété."
    ),
    ABUS_DE_FAIBLESSE(
        displayName = "Abus de faiblesse",
        shortTag = "⚖️ Abus de faiblesse",
        emoji = "🪤",
        unlockLevel = 30,
        category = SimpleObstacleCategory.DROIT_VICES_CONSENTEMENT,
        baseArchetype = ObstacleType.LASER_DRONE,
        definition = "Exploitation délibérée de l'état d'ignorance ou de vulnérabilité d'une personne pour lui faire souscrire des engagements contraignants."
    ),
    SUBORNATION(
        displayName = "Subornation",
        shortTag = "⚖️ Subornation",
        emoji = "🐍",
        unlockLevel = 35,
        category = SimpleObstacleCategory.DROIT_VICES_CONSENTEMENT,
        baseArchetype = ObstacleType.PULSAR_MINE,
        definition = "Emploi de promesses, de menaces ou d'artifices pour pousser un individu à faire un faux témoignage ou à agir contre la vérité."
    ),
    SPOLIATION(
        displayName = "Spoliation",
        shortTag = "⚖️ Spoliation",
        emoji = "🏴",
        unlockLevel = 40,
        category = SimpleObstacleCategory.DROIT_VICES_CONSENTEMENT,
        baseArchetype = ObstacleType.ASTEROID,
        definition = "Action de dépouiller quelqu'un de son bien ou de ses droits par violence, ruse ou abus de droit."
    ),

    // --- 2. Rhétorique, mensonge et manipulation délibérée (Niv. 45 à 80, tous les 5 niveaux) ---
    FALLACIE(
        displayName = "Fallacie (ou sophisme)",
        shortTag = "🎭 Fallacie",
        emoji = "🎭",
        unlockLevel = 45,
        category = SimpleObstacleCategory.RHETORIQUE_MANIPULATION,
        baseArchetype = ObstacleType.LASER_DRONE,
        definition = "Raisonnement spécieux, construit avec une apparence de rigueur logique mais destiné sciemment à induire en erreur."
    ),
    NOVLANGUE(
        displayName = "Novlangue",
        shortTag = "🎭 Novlangue",
        emoji = "🗣️",
        unlockLevel = 50,
        category = SimpleObstacleCategory.RHETORIQUE_MANIPULATION,
        baseArchetype = ObstacleType.PULSAR_MINE,
        definition = "Simplification et déformation du vocabulaire destinées à neutraliser tout esprit critique et à orienter idéologiquement la pensée."
    ),
    SIMULACRE(
        displayName = "Simulacre",
        shortTag = "🎭 Simulacre",
        emoji = "🎪",
        unlockLevel = 55,
        category = SimpleObstacleCategory.RHETORIQUE_MANIPULATION,
        baseArchetype = ObstacleType.ASTEROID,
        definition = "Apparence trompeuse qui se donne pour la réalité afin de dissimuler le vide ou la contrefaçon."
    ),
    SUBVERSION(
        displayName = "Subversion",
        shortTag = "🎭 Subversion",
        emoji = "🦂",
        unlockLevel = 60,
        category = SimpleObstacleCategory.RHETORIQUE_MANIPULATION,
        baseArchetype = ObstacleType.LASER_DRONE,
        definition = "Travail méthodique de sape destiné à détruire de l'intérieur les normes, les esprits ou les institutions établies."
    ),
    FABULATION(
        displayName = "Fabulation (ou affabulation)",
        shortTag = "🎭 Fabulation",
        emoji = "📜",
        unlockLevel = 65,
        category = SimpleObstacleCategory.RHETORIQUE_MANIPULATION,
        baseArchetype = ObstacleType.PULSAR_MINE,
        definition = "Invention de récits faux présentés comme des réalités historiques ou juridiques pour mystifier autrui."
    ),
    MYSTIFICATION(
        displayName = "Mystification",
        shortTag = "🎭 Mystification",
        emoji = "🌫️",
        unlockLevel = 70,
        category = SimpleObstacleCategory.RHETORIQUE_MANIPULATION,
        baseArchetype = ObstacleType.ASTEROID,
        definition = "Tromperie collective organisée pour faire accepter une croyance fausse au détriment du bon sens."
    ),
    DUPLICITE(
        displayName = "Duplicité",
        shortTag = "🎭 Duplicité",
        emoji = "🃏",
        unlockLevel = 75,
        category = SimpleObstacleCategory.RHETORIQUE_MANIPULATION,
        baseArchetype = ObstacleType.LASER_DRONE,
        definition = "Caractère d'une personne ou d'une institution qui dissimule ses véritables desseins sous des dehors trompeurs de bienveillance."
    ),
    SUBTERFUGE(
        displayName = "Subterfuge",
        shortTag = "🎭 Subterfuge",
        emoji = "🕳️",
        unlockLevel = 80,
        category = SimpleObstacleCategory.RHETORIQUE_MANIPULATION,
        baseArchetype = ObstacleType.PULSAR_MINE,
        definition = "Ruse ou échappatoire utilisée pour fuir une obligation ou obtenir un consentement biaisé."
    ),

    // --- 3. Psychologie, contrôle mental et emprise (Niv. 85 à 110, tous les 5 niveaux) ---
    ALIENATION(
        displayName = "Aliénation",
        shortTag = "🧠 Aliénation",
        emoji = "⛓️",
        unlockLevel = 85,
        category = SimpleObstacleCategory.PSYCHOLOGIE_EMPRISE,
        baseArchetype = ObstacleType.ASTEROID,
        definition = "Processus par lequel un être humain est dépossédé de sa propre maîtrise, de sa lucidité ou de ses droits fondamentaux."
    ),
    IMPUISSANCE_APPRISE(
        displayName = "Impuissance apprise",
        shortTag = "🧠 Impuissance apprise",
        emoji = "🥀",
        unlockLevel = 90,
        category = SimpleObstacleCategory.PSYCHOLOGIE_EMPRISE,
        baseArchetype = ObstacleType.LASER_DRONE,
        definition = "État d'abandon psychologique provoqué par des contraintes répétées, conduisant l'individu à croire qu'aucune réaction n'est possible."
    ),
    CONDITIONNEMENT(
        displayName = "Conditionnement",
        shortTag = "🧠 Conditionnement",
        emoji = "🔔",
        unlockLevel = 95,
        category = SimpleObstacleCategory.PSYCHOLOGIE_EMPRISE,
        baseArchetype = ObstacleType.PULSAR_MINE,
        definition = "Modification systématique des réflexes et des croyances d'un individu par l'application de stimuli, de punitions et de récompenses."
    ),
    INFANTILISATION(
        displayName = "Infantilisation",
        shortTag = "🧠 Infantilisation",
        emoji = "🍼",
        unlockLevel = 100,
        category = SimpleObstacleCategory.PSYCHOLOGIE_EMPRISE,
        baseArchetype = ObstacleType.ASTEROID,
        definition = "Traitement imposé à des adultes pour les maintenir dans un état de dépendance et d'obéissance aveugle à une autorité."
    ),
    EMPRISE(
        displayName = "Emprise",
        shortTag = "🧠 Emprise",
        emoji = "🕷️",
        unlockLevel = 105,
        category = SimpleObstacleCategory.PSYCHOLOGIE_EMPRISE,
        baseArchetype = ObstacleType.LASER_DRONE,
        definition = "Ascendant intellectuel ou moral totalitaire exercé sur une personne, supprimant progressivement son discernement."
    ),
    ASSERVISSEMENT(
        displayName = "Asservissement",
        shortTag = "🧠 Asservissement",
        emoji = "🔒",
        unlockLevel = 110,
        category = SimpleObstacleCategory.PSYCHOLOGIE_EMPRISE,
        baseArchetype = ObstacleType.PULSAR_MINE,
        definition = "Réduction à l'état de soumission totale ou de servitude, physique ou mentale."
    ),

    // --- 4. Atteintes aux libertés et autoritarisme (Niv. 115 à 135, tous les 5 niveaux) ---
    OSTRACISME(
        displayName = "Ostracisme",
        shortTag = "⛓️ Ostracisme",
        emoji = "🚫",
        unlockLevel = 115,
        category = SimpleObstacleCategory.ATTEINTES_LIBERTES_AUTORITARISME,
        baseArchetype = ObstacleType.ASTEROID,
        definition = "Pratique d'exclusion, de relégation ou de bannissement social et numérique employée pour punir le non-conformisme."
    ),
    ARBITRAIRE(
        displayName = "Arbitraire",
        shortTag = "⛓️ Arbitraire",
        emoji = "👑",
        unlockLevel = 120,
        category = SimpleObstacleCategory.ATTEINTES_LIBERTES_AUTORITARISME,
        baseArchetype = ObstacleType.LASER_DRONE,
        definition = "Pouvoir ou décision qui ne repose sur aucune règle de droit équitable et ne dépend que du bon vouloir de l'autorité."
    ),
    INQUISITION(
        displayName = "Inquisition",
        shortTag = "⛓️ Inquisition",
        emoji = "👁️",
        unlockLevel = 125,
        category = SimpleObstacleCategory.ATTEINTES_LIBERTES_AUTORITARISME,
        baseArchetype = ObstacleType.PULSAR_MINE,
        definition = "Enquête intrusive, persécutrice et inquisitrice visant à extorquer des aveux ou à traquer les dissidences."
    ),
    BAILLONNEMENT(
        displayName = "Bâillonnement",
        shortTag = "⛓️ Bâillonnement",
        emoji = "🤐",
        unlockLevel = 130,
        category = SimpleObstacleCategory.ATTEINTES_LIBERTES_AUTORITARISME,
        baseArchetype = ObstacleType.ASTEROID,
        definition = "Mesures de censure visant à interdire l'expression d'opinions divergentes ou contestataires."
    ),
    LOIS_EXCEPTION(
        displayName = "Lois d'exception",
        shortTag = "⛓️ Lois d'exception",
        emoji = "🚨",
        unlockLevel = 135,
        category = SimpleObstacleCategory.ATTEINTES_LIBERTES_AUTORITARISME,
        baseArchetype = ObstacleType.LASER_DRONE,
        definition = "Dispositifs dérogeant aux règles fondamentales du droit commun pour restreindre drastiquement les libertés publiques."
    );

    val counterPowerUpLabel: String
        get() = when (baseArchetype) {
            ObstacleType.ASTEROID -> "🌿 Dissipateur Parasite (+60 pts)"
            ObstacleType.LASER_DRONE -> "💠 Sérénité Anti-Angoisse (+80 pts)"
            ObstacleType.PULSAR_MINE -> "🌊 Calme Anti-Panique (+100 pts)"
            else -> "💥 Onde Nova"
        }
}

@Immutable
data class SpaceObstacle(
    val id: Int,
    val type: ObstacleType,
    val x: Float, // World coordinates 0f..WORLD_WIDTH (4.0f)
    val y: Float, // World coordinates 0f..WORLD_HEIGHT (4.0f)
    val vx: Float,
    val vy: Float,
    val radius: Float,
    val rotation: Float = 0f,
    val rotationSpeed: Float = 60f,
    val pulsePhase: Float = 0f,
    val paralyzedTimer: Float = 0f,
    val myelinSlowTimer: Float = 0f,
    val simpleVariant: SimpleObstacleVariant? = null
) {
    val effectiveDisplayName: String
        get() = simpleVariant?.displayName ?: type.displayName

    val effectiveShortTag: String
        get() = simpleVariant?.shortTag ?: type.displayName

    val effectiveEmoji: String
        get() = simpleVariant?.emoji ?: type.emoji
}

@Immutable
data class FloatingTextRelic(
    val letter: String, // Fruit Name e.g. "Amour", "Joie", "Paix", or "🧠"
    val title: String,
    val content: String,
    val points: Int,
    val x: Float, // World coordinates 0f..WORLD_WIDTH
    val y: Float, // World coordinates 0f..WORLD_HEIGHT
    val vx: Float,
    val vy: Float,
    val isCollected: Boolean = false,
    val phase: Float = 0f,
    val spawnTime: Float = 0f,
    val isSpawned: Boolean = true,
    val fruitId: String = "amour",
    val fruitName: String = "Amour",
    val fruitEmoji: String = "♥",
    val situationKey: String = "sante",
    val situationTitle: String = "Malgré la santé",
    val scriptureRef: String = "",
    val verse: String = "",
    val explain: String = "",
    val keep: String = "",
    val prayer: String = "",
    val slotIndex: Int = 0,
    val isBrainBonus: Boolean = false,
    val brainLevel: Int = 0,
    val harmonizationProgress: Float = 0f, // 0f..1f (Zone d'Apaisement)
    val isHarmonizing: Boolean = false,
    val sanctuaryRadius: Float = 0.22f // Rayon du Sanctuaire d'Harmonisation
)

@Immutable
data class TrailPoint(
    val x: Float,
    val y: Float,
    val alpha: Float
)

@Immutable
data class BurstParticle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val life: Float = 1f
)

enum class PowerUpType(
    val displayName: String,
    val emoji: String,
    val primaryColor: Color,
    val glowColor: Color,
    val description: String,
    val targetObstacle: ObstacleType? = null,
    val unlockLevel: Int = 1
) {
    SHIELD(
        displayName = "Bouclier Stellaire",
        emoji = "🛡️",
        primaryColor = CyberCyan,
        glowColor = CyberCyanGlow,
        description = "+1 Bouclier & Aura protectrice"
    ),
    SPEED_BOOST(
        displayName = "Hyper-Propulsion",
        emoji = "⚡",
        primaryColor = SolarGold,
        glowColor = SolarGoldGlow,
        description = "Vitesse accrue x1.5 pendant 8s"
    ),
    MAGNET(
        displayName = "Aimant Cosmique",
        emoji = "🧲",
        primaryColor = VioletPulsar,
        glowColor = VioletPulsarGlow,
        description = "Attire les méditations et Pensées Pures pendant 10s"
    ),
    ANTI_ASTEROID(
        displayName = "Dissipateur Parasite",
        emoji = "🌿",
        primaryColor = EmeraldNova,
        glowColor = EmeraldNovaGlow,
        description = "Dissipe les Pensées Parasites / Toxines Cérébrales au contact (12s)",
        targetObstacle = ObstacleType.ASTEROID
    ),
    ANTI_DRONE(
        displayName = "Sérénité Anti-Angoisse",
        emoji = "💠",
        primaryColor = CyberCyan,
        glowColor = CyberCyanGlow,
        description = "Apaise les Foyers d'Angoisse / Pics de Stress au contact (12s)",
        targetObstacle = ObstacleType.LASER_DRONE
    ),
    ANTI_MINE(
        displayName = "Calme Anti-Panique",
        emoji = "🌊",
        primaryColor = CoralHyper,
        glowColor = CoralHyperGlow,
        description = "Neutralise les Crises de Panique / Décharges Émotives au contact (12s)",
        targetObstacle = ObstacleType.PULSAR_MINE
    ),
    EMP_BLAST(
        displayName = "Onde Nova",
        emoji = "💥",
        primaryColor = PlasmaRose,
        glowColor = PlasmaRoseGlow,
        description = "Dissipe tous les obstacles mentaux alentour (+Points)"
    ),
    FREE_WILL_AEGIS(
        displayName = "Égide du Libre Arbitre",
        emoji = "🔷",
        primaryColor = Color(0xFFBAE6FD),
        glowColor = Color(0xFFE0F2FE),
        description = "Niv.40 • Bouclier Réfractaire (6s) : Immunise vos bonus contre la Nuée Grégaire, fait rebondir Prisme Dogmatique & Foyer d'Angoisse sans perdre de vie (+5 énergie)",
        targetObstacle = ObstacleType.DOGMATIC_PRISM,
        unlockLevel = 40
    ),
    METACOGNITION_BEACON(
        displayName = "Phare de Métacognition",
        emoji = "🏮",
        primaryColor = SolarGold,
        glowColor = Color(0xFFFDE047),
        description = "Niv.60 • Sonar Anti-Obscurantisme (8s) : Rétablit la mini-carte, révèle les Pensées cachées sous le Voile et protège les Pensées Pures à l'écran",
        targetObstacle = ObstacleType.OBSCURANTISM_VEIL,
        unlockLevel = 60
    ),
    HUMILITY_ANCHOR(
        displayName = "Ancrage d'Humilité",
        emoji = "⚓",
        primaryColor = Color(0xFF818CF8),
        glowColor = Color(0xFFC7D2FE),
        description = "Niv.100 • Inversion Gravitationnelle (6s) : Verrouille votre énergie, repousse le Miroir de Narcisse et convertit son attraction en +1 Bouclier Stellaire (max 7)",
        targetObstacle = ObstacleType.NARCISSUS_MIRROR,
        unlockLevel = 100
    ),
    MYELIN_SHEATH(
        displayName = "Gaine de Myéline",
        emoji = "🧬",
        primaryColor = Color(0xFF2DD4BF),
        glowColor = Color(0xFF99F6E4),
        description = "Niv.110 • Sillage Isolant & Amortisseur (7s) : Ralentit de 65% et désamorce pendant 4s les obstacles traversant votre sillage ; absorbe le 1er choc direct avec onde de recul",
        unlockLevel = 110
    ),
    CONTEMPLATIVE_PHASE_SHIFT(
        displayName = "Déphasage Contemplatif",
        emoji = "👻",
        primaryColor = Color(0xFFC084FC),
        glowColor = Color(0xFFF3E8FF),
        description = "Niv.120 • Intangibilité Thêta (4s) : Traverse tous les obstacles sans dégâts ni ralentissement, divise leur taille/vitesse par 2 et accélère de +25% les Sanctuaires proches",
        unlockLevel = 120
    ),
    PLENITUDE_ARK(
        displayName = "Arche de Plénitude",
        emoji = "🏛️",
        primaryColor = EmeraldNova,
        glowColor = SolarGoldGlow,
        description = "Niv.130 • Sanctuaire Nomade (8s) : Dôme d'or et d'émeraude repoussant tous les obstacles, recueillant les Pensées Pures au contact du dôme et harmonisant les reliques 2x plus vite",
        unlockLevel = 130
    )
}

@Immutable
data class SpacePowerUp(
    val id: Int,
    val type: PowerUpType,
    val x: Float, // World coordinates 0f..WORLD_WIDTH
    val y: Float, // World coordinates 0f..WORLD_HEIGHT
    val vx: Float = 0f,
    val vy: Float = 0f,
    val radius: Float = 0.038f,
    val phase: Float = 0f,
    val isCollected: Boolean = false
)

enum class RescueTargetType(
    val displayName: String,
    val shortLabel: String,
    val emoji: String,
    val primaryColor: Color,
    val glowColor: Color,
    val concepts: List<String>
) {
    LUCIDITY_SPARK(
        displayName = "Étincelle de Lucidité",
        shortLabel = "Lucidité",
        emoji = "✨",
        primaryColor = CyberCyan,
        glowColor = CyberCyanGlow,
        concepts = listOf("Éveil", "Clarté", "Intuition", "Idée pure", "Vision", "Lumière")
    ),
    KNOWLEDGE_FRAGMENT(
        displayName = "Fragment de Savoir",
        shortLabel = "Savoir",
        emoji = "🔶",
        primaryColor = SolarGold,
        glowColor = SolarGoldGlow,
        concepts = listOf("Sagesse", "Vérité", "Logos", "Axiome", "Harmonie", "Gnose")
    ),
    FRAGILE_MEMORY(
        displayName = "Souvenir",
        shortLabel = "Souvenir",
        emoji = "🫧",
        primaryColor = PlasmaRose,
        glowColor = PlasmaRoseGlow,
        concepts = listOf("Promesse", "Enfance", "Alliance", "Tendresse", "Origine", "Grâce")
    )
}

@Immutable
data class RescueTarget(
    val id: Int,
    val type: RescueTargetType,
    val conceptLabel: String,
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val radius: Float = 0.036f,
    val phase: Float = 0f,
    val isSaved: Boolean = false,
    val isDestroyed: Boolean = false
)

enum class VirtueRemedyType(
    val displayName: String,
    val projectileWord: String,
    val emoji: String,
    val affinityHint: String,
    val shortRole: String,
    val primaryColor: Color,
    val glowColor: Color,
    val unlockLevel: Int = 1
) {
    PAIX(
        displayName = "Paix",
        projectileWord = "PAIX",
        emoji = "🕊️",
        affinityHint = "Apaise Colère & Peur (100%) • Agitation, Rancœur & Surtension (50%)",
        shortRole = "Calme intérieur • Apaise les tempêtes et l'effroi",
        primaryColor = CyberCyan,
        glowColor = CyberCyanGlow,
        unlockLevel = 1
    ),
    JOIE(
        displayName = "Joie",
        projectileWord = "JOIE",
        emoji = "☀️",
        affinityHint = "Guérit Tristesse & Lourdeur (100%) • Apathie (50%)",
        shortRole = "Lumière vive • Élève l'âme face à la lourdeur",
        primaryColor = SolarGold,
        glowColor = SolarGoldGlow,
        unlockLevel = 1
    ),
    MAITRISE_DE_SOI(
        displayName = "Maîtrise de soi",
        projectileWord = "MAÎTRISE DE SOI",
        emoji = "⚖️",
        affinityHint = "Stabilise Agitation (100%) • Colère & Peur (50%)",
        shortRole = "Équilibre & ancrage • Canalise la dispersion",
        primaryColor = EmeraldNova,
        glowColor = EmeraldNovaGlow,
        unlockLevel = 1
    ),
    AMOUR(
        displayName = "Amour",
        projectileWord = "AMOUR",
        emoji = "♥",
        affinityHint = "Guérit Rancœur & Froideur (100%) • Tristesse & Burnout (50%)",
        shortRole = "Chaleur bienveillante • Fait fondre la froideur",
        primaryColor = PlasmaRose,
        glowColor = PlasmaRoseGlow,
        unlockLevel = 1
    ),
    VERITE_DISCERNEMENT(
        displayName = "Vérité & Discernement",
        projectileWord = "VÉRITÉ • DISCERNEMENT",
        emoji = "🔍",
        affinityHint = "Brise le Prisme Dogmatique & dissipe l'Ignorance • Rigidité (50%)",
        shortRole = "Clarté lucide • Brise les dogmes et l'ignorance",
        primaryColor = VioletPulsar,
        glowColor = VioletPulsarGlow,
        unlockLevel = 1
    ),
    REGULATION_SOUFFLE(
        displayName = "Régulation & Souffle",
        projectileWord = "SOUFFLE VAGAL",
        emoji = "🌬️",
        affinityHint = "Niv.30 • Surtension (100%) & fige Foyers d'Angoisse / Panique (3s)",
        shortRole = "Frein vagal • Ralentit l'alerte et fige les crises (3s)",
        primaryColor = Color(0xFFBAE6FD), // Blanc Azur Opalescent
        glowColor = Color(0xFF38BDF8), // Halo turquoise doux
        unlockLevel = 30
    ),
    ELAN_ACTION(
        displayName = "Élan & Action",
        projectileWord = "ÉLAN VITAL",
        emoji = "🔥",
        affinityHint = "Niv.50 • Apathie (100%), dissout Bourbier Mental & sprint vers Pensée Pure",
        shortRole = "Élan moteur • Lève l'inertie et dissout le bourbier",
        primaryColor = Color(0xFFF59E0B), // Ambre Ardent
        glowColor = Color(0xFFFB923C), // Halo étincelant orange feu
        unlockLevel = 50
    ),
    PLASTICITE_RECADRAGE(
        displayName = "Plasticité & Recadrage",
        projectileWord = "RECADRAGE",
        emoji = "🔄",
        affinityHint = "Niv.60 • Rigidité (100%), inverse Miroir (+1 ❤️) & se dédouble sur Prisme",
        shortRole = "Neuroplasticité • Recadre les boucles fixes et l'ego",
        primaryColor = Color(0xFF6366F1), // Indigo Profond Spectral
        glowColor = Color(0xFF818CF8), // Halo phosphorescent
        unlockLevel = 60
    ),
    HOMEOSTASIE_PRESENCE(
        displayName = "Homéostasie & Présence",
        projectileWord = "ALIGNEMENT",
        emoji = "🌿",
        affinityHint = "Niv.80 • Burnout (100%), champ harmonique (6s) & lève ralentissements/radar",
        shortRole = "Intéroception • Champ harmonique (6s) régénérant",
        primaryColor = Color(0xFF059669), // Vert Jade Bio-Lumineux
        glowColor = Color(0xFF34D399), // Halo radiant
        unlockLevel = 80
    ),
    INFLUX_BIOELECTRIQUE(
        displayName = "Influx Bioélectrique",
        projectileWord = "POTENTIEL D'ACTION",
        emoji = "⚡",
        affinityHint = "Niv.100 • Arc en chaîne (purifie 2-3 neurones) & paralyse Prisme/Angoisse (4s)",
        shortRole = "Potentiel d'action • Arc en chaîne & court-circuit (4s)",
        primaryColor = Color(0xFF38BDF8), // Cyan Électrique Haute Tension
        glowColor = Color(0xFFF0FDF4), // Halo néon blanc étincelant
        unlockLevel = 100
    ),
    RESONANCE_GAMMA(
        displayName = "Résonance Gamma",
        projectileWord = "SYNCHRONIE",
        emoji = "🌊",
        affinityHint = "Niv.120 • Onde Gamma géante (+50% neurones, dissipe Voiles & Parasites)",
        shortRole = "Onde Gamma • Vague géante qui dissipe et harmonise",
        primaryColor = Color(0xFFC084FC), // Or Violet Oscillatoire
        glowColor = Color(0xFFFDE047), // Halo ondulant doré
        unlockLevel = 120
    )
}

enum class NeuronCorruptionType(
    val categoryTitle: String,
    val negativeAffects: List<String>,
    val remedyVirtue: VirtueRemedyType,
    val secondaryVirtue: VirtueRemedyType,
    val infectedCoreColor: Color,
    val infectedAuraColor: Color,
    val purifiedCoreColor: Color,
    val purifiedAuraColor: Color,
    val unlockLevel: Int = 1
) {
    COLERE_PEUR(
        categoryTitle = "Colère & Peur",
        negativeAffects = listOf("Colère", "Peur", "Angoisse", "Fureur"),
        remedyVirtue = VirtueRemedyType.PAIX,
        secondaryVirtue = VirtueRemedyType.MAITRISE_DE_SOI,
        infectedCoreColor = Color(0xFF7F1D1D), // Rouge sombre abrasif
        infectedAuraColor = Color(0xFFDC2626), // Rouge corrompu
        purifiedCoreColor = CyberCyan,
        purifiedAuraColor = CyberCyanGlow, // Aura lumineuse cyan
        unlockLevel = 1
    ),
    TRISTESSE(
        categoryTitle = "Tristesse & Lourdeur",
        negativeAffects = listOf("Tristesse", "Accablement", "Chagrin", "Lourdeur"),
        remedyVirtue = VirtueRemedyType.JOIE,
        secondaryVirtue = VirtueRemedyType.AMOUR,
        infectedCoreColor = Color(0xFF3B0764), // Violacé terne et lourd
        infectedAuraColor = Color(0xFF6B21A8), // Violet sombre
        purifiedCoreColor = SolarGold,
        purifiedAuraColor = SolarGoldGlow, // Aura lumineuse dorée
        unlockLevel = 1
    ),
    AGITATION(
        categoryTitle = "Agitation & Égarement",
        negativeAffects = listOf("Agitation", "Égarement", "Déchaînement", "Trouble"),
        remedyVirtue = VirtueRemedyType.MAITRISE_DE_SOI,
        secondaryVirtue = VirtueRemedyType.PAIX,
        infectedCoreColor = Color(0xFF701A75), // Rouge-violacé abrasif erratique
        infectedAuraColor = Color(0xFFBE185D), // Magenta sombre agité
        purifiedCoreColor = EmeraldNova,
        purifiedAuraColor = EmeraldNovaGlow, // Aura lumineuse émeraude
        unlockLevel = 1
    ),
    RANCOEUR(
        categoryTitle = "Rancœur & Froideur",
        negativeAffects = listOf("Rancœur", "Haine", "Mépris", "Froideur"),
        remedyVirtue = VirtueRemedyType.AMOUR,
        secondaryVirtue = VirtueRemedyType.PAIX,
        infectedCoreColor = Color(0xFF4C0519), // Bordeaux glacial et durci
        infectedAuraColor = Color(0xFF9F1239), // Cramoisi sombre
        purifiedCoreColor = PlasmaRose,
        purifiedAuraColor = PlasmaRoseGlow, // Aura lumineuse rose chaleureuse
        unlockLevel = 1
    ),
    SURTENSION(
        categoryTitle = "Surtension & Hyperventilation",
        negativeAffects = listOf("Surtension", "Hyperventilation", "Tachycardie", "Alerte Sympathique"),
        remedyVirtue = VirtueRemedyType.REGULATION_SOUFFLE,
        secondaryVirtue = VirtueRemedyType.PAIX,
        infectedCoreColor = Color(0xFF7C2D12), // Rouge-orangé contracté sous haute tension
        infectedAuraColor = Color(0xFFEA580C), // Orange électrique tendu
        purifiedCoreColor = Color(0xFFBAE6FD), // Blanc Azur Opalescent
        purifiedAuraColor = Color(0xFF38BDF8), // Halo turquoise doux
        unlockLevel = 30
    ),
    APATHIE(
        categoryTitle = "Apathie & Procrastination",
        negativeAffects = listOf("Apathie", "Procrastination", "Sidération", "Inertie"),
        remedyVirtue = VirtueRemedyType.ELAN_ACTION,
        secondaryVirtue = VirtueRemedyType.JOIE,
        infectedCoreColor = Color(0xFF1E293B), // Gris ardoise éteint et figé
        infectedAuraColor = Color(0xFF475569), // Halo cendré léthargique
        purifiedCoreColor = Color(0xFFF59E0B), // Ambre Ardent
        purifiedAuraColor = Color(0xFFFB923C), // Halo orange feu
        unlockLevel = 50
    ),
    RIGIDITE_OBSESSION(
        categoryTitle = "Rigidité Mentale & Obsession",
        negativeAffects = listOf("Rigidité", "Obsession", "Biais Cognitif", "Rumination"),
        remedyVirtue = VirtueRemedyType.PLASTICITE_RECADRAGE,
        secondaryVirtue = VirtueRemedyType.VERITE_DISCERNEMENT,
        infectedCoreColor = Color(0xFF311042), // Prune cristallisé rigide
        infectedAuraColor = Color(0xFF7E22CE), // Pourpre obsessionnel
        purifiedCoreColor = Color(0xFF6366F1), // Indigo Profond Spectral
        purifiedAuraColor = Color(0xFF818CF8), // Halo phosphorescent
        unlockLevel = 60
    ),
    BURNOUT_DISSOCIATION(
        categoryTitle = "Burnout & Dissociation Somatique",
        negativeAffects = listOf("Burnout", "Dissociation", "Épuisement", "Déconnexion"),
        remedyVirtue = VirtueRemedyType.HOMEOSTASIE_PRESENCE,
        secondaryVirtue = VirtueRemedyType.AMOUR,
        infectedCoreColor = Color(0xFF18181B), // Noir calciné dissocié
        infectedAuraColor = Color(0xFF52525B), // Gris spectral déconnecté
        purifiedCoreColor = Color(0xFF059669), // Vert Jade Bio-Lumineux
        purifiedAuraColor = Color(0xFF34D399), // Halo radiant jade
        unlockLevel = 80
    )
}

@Immutable
data class InfectedNeuron(
    val id: Int,
    val corruptionType: NeuronCorruptionType,
    val negativeAffectLabel: String,
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val radius: Float = 0.044f,
    val phase: Float = 0f,
    val purificationProgress: Float = 0f, // 0f (infecté) .. 1f (purifié)
    val isPurified: Boolean = false,
    val purifiedGlowTimer: Float = 0f,
    val hitFlashTimer: Float = 0f,
    val lastVirtueWord: String? = null
)

@Immutable
data class VirtueWordProjectile(
    val id: Int,
    val virtue: VirtueRemedyType,
    val wordLabel: String,
    val targetNeuronId: Int,
    val targetObstacleId: Int = -1,
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val primaryColor: Color,
    val glowColor: Color,
    val life: Float = 1.8f,
    val hasSplit: Boolean = false
)

@Immutable
data class BioelectricArcEffect(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val life: Float = 0.65f
)

@Immutable
data class GammaWaveEffect(
    val x: Float,
    val y: Float,
    val currentRadius: Float = 0.05f,
    val maxRadius: Float = 1.8f,
    val life: Float = 1.0f,
    val hitNeuronIds: Set<Int> = emptySet()
)

@Immutable
data class PurificationFeedbackEffect(
    val id: Int,
    val x: Float,
    val y: Float,
    val percentage: Int, // 0, 50, or 100
    val label: String,   // "0% PURIFIÉ !", "50% PURIFIÉ !", "100% PURIFIÉ !"
    val subLabel: String,
    val primaryColor: Color,
    val glowColor: Color,
    val life: Float = 1.65f,
    val maxLife: Float = 1.65f
)


