package com.example.domain.service

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.domain.model.NeuronCorruptionType
import com.example.domain.model.ObstacleType
import com.example.domain.model.PowerUpType
import com.example.domain.model.RescueTargetType
import com.example.domain.model.VirtueRemedyType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Style d'instrumentation et de rythme pour chaque musique de fond afin que chaque piste
 * possède une identité sonore, un timbre, un tempo et un groove radicalement différents.
 */
private enum class MusicSynthStyle {
    CELESTIAL_HARP,          // 1. Harpe céleste acoustique en arpèges rapides cristallins sans nappe lourde
    SYNTHWAVE_PULSE,         // 2. Synthwave rétro-futuriste avec basse analogique pulsée + kick/hat doux
    CRYSTAL_BELLS_CHIME,     // 3. Boîte à musique / Carillon FM aigu scintillant dans les hautes fréquences
    DEEP_BINAURAL_TEMPLE,    // 4. Bols chantants tibétains graves + bourdon méditatif profond très lent
    COSMIC_MARIMBA_GROOVE,   // 5. Percussion mélodique en bois (Marimba / Kalimba) syncopée et joyeuse
    ETHEREAL_CHOIR_ORGAN,    // 6. Grand Orgue & Chœur céleste à formants vocaux majestueux
    ORBITAL_ELECTRO_ARP,     // 7. Séquenceur électro-trance spatial rapide (doubles-croches + basse techno)
    STARLIGHT_PIANO_NOCTURNE,// 8. Piano feutré expressif en valse (3/4) poétique
    MYSTIC_DESERT_FLUTE,     // 9. Flûte orientale mystique (gamme Hijaz) avec vibrato et percussion tribale douce
    SYMPHONIC_STRINGS,       // 10. Ensemble de cordes symphoniques (violons/violoncelles) amples et héroïques
    ZEN_KOTO_BAMBOO,         // 11. Koto japonais pincé (gamme pentatonique Hirajoshi) & souffle de bambou
    GALACTIC_FUNK_BASS,      // 12. Basse groovy syncopée + accords de piano électrique Rhodes tremolo
    AMBIENT_GLACIER_ECHO,    // 13. Échos de glace polaire très aigus + chant de baleine cosmique grave
    TRIUMPHANT_APOTHEOSIS    // 14. Fanfare orchestrale radieuse, cloches solaires et pulsation triomphale
}

/**
 * Thème musical cosmique avec timbre, rythme, registre et mélodie propres.
 */
private data class CosmicMusicTheme(
    val title: String,
    val shortLabel: String,
    val style: MusicSynthStyle,
    val chords: Array<FloatArray>,
    val arpeggioOrder: IntArray, // -1 = silence rythmique (soupir), 0..3 = note de l'accord, 4..7 = octave supérieure
    val chordDurationSec: Float,
    val arpStepSec: Float,
    val bassPulseHz: Float,      // 0f = pas de pulsation de basse, >0f = fréquence rythmique de la basse/percussion
    val padLevel: Float,
    val leadLevel: Float
)

/**
 * Synthétiseur audio spatial temps-réel pour Noôrbis.
 * Génère 14 musiques de fond aux timbres, rythmes et styles radicalement distincts
 * ainsi que les effets sonores en jeu et les signatures sonores des éléments dans la zone du joueur.
 */
object AstroSoundManager {

    private const val SAMPLE_RATE = 22050
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val musicEnabled = AtomicBoolean(true)
    private val sfxEnabled = AtomicBoolean(true)
    private val soundEnabled = sfxEnabled
    private val appInForeground = AtomicBoolean(true)
    private val ambientDesired = AtomicBoolean(true)
    private val ambientPlaying = AtomicBoolean(false)
    private val ambientSessionId = AtomicInteger(0)
    private val currentLevelNumber = AtomicInteger(1)
    // -1 = automatique selon le niveau, 0..13 = thème choisi manuellement par le joueur
    private val manualThemeOverride = AtomicInteger(-1)
    private val activeSfxCount = AtomicInteger(0)
    private const val MAX_CONCURRENT_SFX = 6

    private val ambientLock = Any()
    private var ambientJob: Job? = null
    private var activeAmbientTrack: AudioTrack? = null
    private val activeSfxJobs = ConcurrentHashMap.newKeySet<Job>()
    private val activeSfxTracks = ConcurrentHashMap.newKeySet<AudioTrack>()

    private var lastHarmonizeSoundTimeMs: Long = 0L
    private var lastTickSoundTimeMs: Long = 0L
    private var lastZoneSoundGlobalTimeMs: Long = 0L
    private val zoneCategoryLastTimeMs = ConcurrentHashMap<String, Long>()

    /**
     * 14 thèmes musicaux aux styles, instruments, tempos et gammes radicalement différents.
     */
    private val MUSIC_THEMES: List<CosmicMusicTheme> = listOf(
        // 1. Harpe Céleste : cordes pincées lumineuses en cascade rapide, registre médium-aigu
        CosmicMusicTheme(
            title = "Harpe Solaire",
            shortLabel = "1. Harpe Solaire",
            style = MusicSynthStyle.CELESTIAL_HARP,
            chords = arrayOf(
                floatArrayOf(261.63f, 329.63f, 392.00f, 493.88f), // C4 Maj7
                floatArrayOf(349.23f, 440.00f, 523.25f, 659.25f), // F4 Maj7
                floatArrayOf(220.00f, 261.63f, 329.63f, 392.00f), // A3 min7
                floatArrayOf(196.00f, 246.94f, 293.66f, 392.00f)  // G3 Maj
            ),
            arpeggioOrder = intArrayOf(0, 1, 2, 3, 4, 3, 2, 1, 0, 2, 4, -1),
            chordDurationSec = 2.64f,
            arpStepSec = 0.22f,
            bassPulseHz = 0f,
            padLevel = 0.08f,
            leadLevel = 0.28f
        ),
        // 2. Synthwave Pulse : basse électronique rétro-futuriste pulsée + batterie douce + lead sci-fi
        CosmicMusicTheme(
            title = "Pulsation Synthwave",
            shortLabel = "2. Synthwave",
            style = MusicSynthStyle.SYNTHWAVE_PULSE,
            chords = arrayOf(
                floatArrayOf(110.00f, 164.81f, 220.00f, 261.63f), // A2 min
                floatArrayOf(87.31f, 130.81f, 174.61f, 220.00f),  // F2 Maj
                floatArrayOf(130.81f, 196.00f, 261.63f, 329.63f), // C3 Maj
                floatArrayOf(98.00f, 146.83f, 196.00f, 246.94f)   // G2 Maj
            ),
            arpeggioOrder = intArrayOf(0, -1, 2, 3, 0, -1, 4, 3),
            chordDurationSec = 2.40f,
            arpStepSec = 0.30f,
            bassPulseHz = 3.33f,
            padLevel = 0.14f,
            leadLevel = 0.22f
        ),
        // 3. Carillon des Pléiades : boîte à musique / clochettes de cristal très aiguës et espacées
        CosmicMusicTheme(
            title = "Carillon de Cristal",
            shortLabel = "3. Carillon",
            style = MusicSynthStyle.CRYSTAL_BELLS_CHIME,
            chords = arrayOf(
                floatArrayOf(523.25f, 659.25f, 783.99f, 987.77f), // C5, E5, G5, B5 (registre très aigu)
                floatArrayOf(440.00f, 523.25f, 659.25f, 880.00f), // A4, C5, E5, A5
                floatArrayOf(349.23f, 440.00f, 523.25f, 783.99f), // F4, A4, C5, G5
                floatArrayOf(392.00f, 493.88f, 587.33f, 880.00f)  // G4, B4, D5, A5
            ),
            arpeggioOrder = intArrayOf(3, -1, 1, 2, -1, 4, -1, 0),
            chordDurationSec = 3.60f,
            arpStepSec = 0.45f,
            bassPulseHz = 0f,
            padLevel = 0.05f,
            leadLevel = 0.30f
        ),
        // 4. Sanctuaire Tibétain : bols chantants graves résonnants et bourdon de temple ultra-apaisant
        CosmicMusicTheme(
            title = "Bols Tibétains d'Orion",
            shortLabel = "4. Bols Tibétains",
            style = MusicSynthStyle.DEEP_BINAURAL_TEMPLE,
            chords = arrayOf(
                floatArrayOf(108.00f, 162.00f, 216.00f, 324.00f), // Harmoniques sacrées 108 Hz / 432 Hz
                floatArrayOf(96.00f, 144.00f, 192.00f, 288.00f),
                floatArrayOf(108.00f, 144.00f, 216.00f, 288.00f),
                floatArrayOf(81.00f, 121.50f, 162.00f, 243.00f)
            ),
            arpeggioOrder = intArrayOf(0, -1, -1, 2, -1, -1, 3, -1),
            chordDurationSec = 5.60f,
            arpStepSec = 0.70f,
            bassPulseHz = 0.35f,
            padLevel = 0.24f,
            leadLevel = 0.24f
        ),
        // 5. Danse Solaire (Marimba / Kalimba) : percussion mélodique boisée, rythmique et sautillante
        CosmicMusicTheme(
            title = "Marimba Solaire",
            shortLabel = "5. Marimba Groove",
            style = MusicSynthStyle.COSMIC_MARIMBA_GROOVE,
            chords = arrayOf(
                floatArrayOf(196.00f, 246.94f, 293.66f, 392.00f), // G3
                floatArrayOf(261.63f, 329.63f, 392.00f, 523.25f), // C4
                floatArrayOf(220.00f, 261.63f, 329.63f, 440.00f), // A3m
                floatArrayOf(146.83f, 220.00f, 293.66f, 369.99f)  // D3
            ),
            arpeggioOrder = intArrayOf(0, 2, -1, 3, 1, 4, -1, 2, 0, 3, 1, -1),
            chordDurationSec = 2.16f,
            arpStepSec = 0.18f,
            bassPulseHz = 2.77f,
            padLevel = 0.06f,
            leadLevel = 0.32f
        ),
        // 6. Cathédrale d'Andromède : Chœur céleste et grand orgue majestueux sans arpège mécanique
        CosmicMusicTheme(
            title = "Chœur d'Andromède",
            shortLabel = "6. Chœur & Orgue",
            style = MusicSynthStyle.ETHEREAL_CHOIR_ORGAN,
            chords = arrayOf(
                floatArrayOf(116.54f, 174.61f, 233.08f, 293.66f), // Bb2 Maj
                floatArrayOf(155.56f, 233.08f, 311.13f, 392.00f), // Eb3 Maj
                floatArrayOf(130.81f, 196.00f, 261.63f, 311.13f), // C3 min
                floatArrayOf(174.61f, 220.00f, 261.63f, 349.23f)  // F3 Maj
            ),
            arpeggioOrder = intArrayOf(3, 3, 2, 2, 4, 4, 3, -1),
            chordDurationSec = 4.80f,
            arpStepSec = 0.60f,
            bassPulseHz = 0f,
            padLevel = 0.30f,
            leadLevel = 0.18f
        ),
        // 7. Vitesse Lumière (Électro Orbitale) : arpégiateur trance ultra-rapide 16 notes + kick électro
        CosmicMusicTheme(
            title = "Électro Boréale",
            shortLabel = "7. Électro Rapide",
            style = MusicSynthStyle.ORBITAL_ELECTRO_ARP,
            chords = arrayOf(
                floatArrayOf(130.81f, 155.56f, 196.00f, 261.63f), // C3 min
                floatArrayOf(103.83f, 155.56f, 207.65f, 261.63f), // Ab2 Maj
                floatArrayOf(116.54f, 174.61f, 233.08f, 293.66f), // Bb2 Maj
                floatArrayOf(98.00f, 146.83f, 196.00f, 246.94f)   // G2 Maj
            ),
            arpeggioOrder = intArrayOf(0, 1, 2, 3, 4, 3, 2, 1, 0, 2, 3, 4, 3, 2, 1, 0),
            chordDurationSec = 2.08f,
            arpStepSec = 0.13f,
            bassPulseHz = 3.84f,
            padLevel = 0.10f,
            leadLevel = 0.28f
        ),
        // 8. Nocturne de Cassiopée : Valse poétique au piano feutré en 3/4 (Basse - Accord - Accord)
        CosmicMusicTheme(
            title = "Piano Nocturne (Valse)",
            shortLabel = "8. Piano Valse",
            style = MusicSynthStyle.STARLIGHT_PIANO_NOCTURNE,
            chords = arrayOf(
                floatArrayOf(146.83f, 220.00f, 261.63f, 349.23f), // D3 min7
                floatArrayOf(196.00f, 246.94f, 293.66f, 349.23f), // G3 7
                floatArrayOf(130.81f, 196.00f, 246.94f, 329.63f), // C3 Maj7
                floatArrayOf(110.00f, 164.81f, 207.65f, 261.63f)  // A2 min/Maj
            ),
            arpeggioOrder = intArrayOf(0, 2, 3, 0, 4, 3, 0, 2, 1, 0, 3, -1), // Mesure à 3 temps (valse)
            chordDurationSec = 3.60f,
            arpStepSec = 0.30f,
            bassPulseHz = 0.92f,
            padLevel = 0.08f,
            leadLevel = 0.30f
        ),
        // 9. Nébuleuse Orientale : Gamme Hijaz mystique, flûte expressive à vibrato et tambourin doux
        CosmicMusicTheme(
            title = "Oasis Stellaire (Hijaz)",
            shortLabel = "9. Flûte Orientale",
            style = MusicSynthStyle.MYSTIC_DESERT_FLUTE,
            chords = arrayOf(
                floatArrayOf(146.83f, 185.00f, 220.00f, 261.63f), // D Hijaz (D, F#, A, C)
                floatArrayOf(155.56f, 196.00f, 220.00f, 293.66f), // Eb, G, A, D
                floatArrayOf(146.83f, 185.00f, 220.00f, 311.13f), // D, F#, A, Eb
                floatArrayOf(130.81f, 196.00f, 220.00f, 293.66f)  // C, G, A, D
            ),
            arpeggioOrder = intArrayOf(1, 2, 3, 2, 1, 0, -1, 3, 4, 3, 1, -1),
            chordDurationSec = 3.36f,
            arpStepSec = 0.28f,
            bassPulseHz = 1.78f,
            padLevel = 0.12f,
            leadLevel = 0.28f
        ),
        // 10. Odyssée des Séraphins : Cordes symphoniques amples (violons & violoncelles)
        CosmicMusicTheme(
            title = "Symphonie des Séraphins",
            shortLabel = "10. Symphonie",
            style = MusicSynthStyle.SYMPHONIC_STRINGS,
            chords = arrayOf(
                floatArrayOf(146.83f, 220.00f, 293.66f, 369.99f), // D Maj
                floatArrayOf(110.00f, 164.81f, 220.00f, 277.18f), // A Maj
                floatArrayOf(123.47f, 185.00f, 246.94f, 293.66f), // B min
                floatArrayOf(196.00f, 246.94f, 293.66f, 392.00f)  // G Maj
            ),
            arpeggioOrder = intArrayOf(0, 2, 3, 4, 3, 2, 1, 3),
            chordDurationSec = 4.40f,
            arpStepSec = 0.55f,
            bassPulseHz = 0f,
            padLevel = 0.28f,
            leadLevel = 0.20f
        ),
        // 11. Jardin Zen d'Émeraude : Koto japonais pincé (gamme Hirajoshi) & flûte Shakuhachi
        CosmicMusicTheme(
            title = "Koto Zen d'Émeraude",
            shortLabel = "11. Koto Zen",
            style = MusicSynthStyle.ZEN_KOTO_BAMBOO,
            chords = arrayOf(
                floatArrayOf(164.81f, 185.00f, 246.94f, 261.63f), // Gamme Hirajoshi (E, F#, B, C)
                floatArrayOf(164.81f, 246.94f, 261.63f, 329.63f), // E, B, C, E
                floatArrayOf(123.47f, 164.81f, 185.00f, 246.94f), // B, E, F#, B
                floatArrayOf(130.81f, 164.81f, 246.94f, 329.63f)  // C, E, B, E
            ),
            arpeggioOrder = intArrayOf(0, 1, 2, 3, -1, 4, 2, 1, 0, -1, 3, -1),
            chordDurationSec = 3.12f,
            arpStepSec = 0.26f,
            bassPulseHz = 0f,
            padLevel = 0.08f,
            leadLevel = 0.30f
        ),
        // 12. Citadelle Groove : Basse Funk Galactique syncopée & Rhodes électrique chaleureux
        CosmicMusicTheme(
            title = "Groove Stellaire",
            shortLabel = "12. Groove Funk",
            style = MusicSynthStyle.GALACTIC_FUNK_BASS,
            chords = arrayOf(
                floatArrayOf(130.81f, 155.56f, 196.00f, 233.08f), // C min7
                floatArrayOf(174.61f, 220.00f, 261.63f, 311.13f), // F7 (Dorian)
                floatArrayOf(116.54f, 146.83f, 174.61f, 220.00f), // Bb Maj7
                floatArrayOf(155.56f, 196.00f, 233.08f, 293.66f)  // Eb Maj7
            ),
            arpeggioOrder = intArrayOf(0, -1, 3, 2, -1, 0, 4, -1, 2, 3, -1, 1),
            chordDurationSec = 2.40f,
            arpStepSec = 0.20f,
            bassPulseHz = 2.50f,
            padLevel = 0.16f,
            leadLevel = 0.26f
        ),
        // 13. Voûte de Saphir Polaire : Échos cristallins glaciaires haute-octave & chant sub-spatial
        CosmicMusicTheme(
            title = "Écho Polaire de Saphir",
            shortLabel = "13. Écho Polaire",
            style = MusicSynthStyle.AMBIENT_GLACIER_ECHO,
            chords = arrayOf(
                floatArrayOf(246.94f, 369.99f, 440.00f, 554.37f), // B3, F#4, A4, C#5
                floatArrayOf(196.00f, 293.66f, 369.99f, 493.88f), // G3, D4, F#4, B4
                floatArrayOf(146.83f, 220.00f, 329.63f, 440.00f), // D3, A3, E4, A4
                floatArrayOf(185.00f, 277.18f, 369.99f, 554.37f)  // F#3, C#4, F#4, C#5
            ),
            arpeggioOrder = intArrayOf(4, 3, -1, 4, 2, -1, 3, 1, -1, -1),
            chordDurationSec = 4.50f,
            arpStepSec = 0.45f,
            bassPulseHz = 0.25f,
            padLevel = 0.20f,
            leadLevel = 0.24f
        ),
        // 14. Plénitude Éternelle : Fanfare solaire triomphale, cloches dorées et rythme d'apothéose
        CosmicMusicTheme(
            title = "Fanfare de Plénitude",
            shortLabel = "14. Fanfare Solaire",
            style = MusicSynthStyle.TRIUMPHANT_APOTHEOSIS,
            chords = arrayOf(
                floatArrayOf(174.61f, 220.00f, 261.63f, 349.23f), // F Maj
                floatArrayOf(196.00f, 246.94f, 293.66f, 392.00f), // G Maj
                floatArrayOf(220.00f, 261.63f, 329.63f, 440.00f), // A min
                floatArrayOf(261.63f, 329.63f, 392.00f, 523.25f)  // C Maj
            ),
            arpeggioOrder = intArrayOf(0, 2, 3, 4, 4, 3, 2, 4, 0, 1, 2, 4),
            chordDurationSec = 2.88f,
            arpStepSec = 0.24f,
            bassPulseHz = 2.08f,
            padLevel = 0.22f,
            leadLevel = 0.28f
        )
    )

    fun isSoundEnabled(): Boolean = musicEnabled.get()

    fun isMusicEnabled(): Boolean = musicEnabled.get()

    fun isEffectsSoundEnabled(): Boolean = sfxEnabled.get()

    private fun canPlayMusic(): Boolean = musicEnabled.get() && appInForeground.get()

    private fun canPlayAudio(): Boolean = sfxEnabled.get() && appInForeground.get()

    fun setMusicEnabled(enabled: Boolean) {
        musicEnabled.set(enabled)
        if (!enabled) {
            stopAmbientSpaceMusic(clearDesired = false)
        } else if (ambientDesired.get() && appInForeground.get()) {
            startAmbientSpaceMusic()
        }
    }

    fun setEffectsSoundEnabled(enabled: Boolean) {
        sfxEnabled.set(enabled)
        if (!enabled) {
            stopAllSfxImmediately()
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        setMusicEnabled(enabled)
    }

    /**
     * Appelé lorsque l'application passe en arrière-plan ou que l'écran du téléphone s'éteint (onPause / onStop).
     * Coupe immédiatement la musique de fond et tous les effets sonores en cours, et bloque tout nouveau son.
     */
    fun onAppBackgrounded() {
        appInForeground.set(false)
        stopAllAudioImmediately(clearDesired = false)
    }

    /**
     * Appelé lorsque l'application revient au premier plan (onResume).
     */
    fun onAppForegrounded() {
        appInForeground.set(true)
        resumeIfDesired()
    }

    private fun stopAllSfxImmediately() {
        val jobs = activeSfxJobs.toList()
        activeSfxJobs.clear()
        jobs.forEach { it.cancel() }

        val tracks = activeSfxTracks.toList()
        activeSfxTracks.clear()
        tracks.forEach { track ->
            try {
                track.pause()
                track.flush()
                track.stop()
            } catch (_: Throwable) {}
            try {
                track.release()
            } catch (_: Throwable) {}
        }
        activeSfxCount.set(0)
    }

    private fun stopAllAudioImmediately(clearDesired: Boolean = false) {
        stopAmbientSpaceMusic(clearDesired = clearDesired)
        stopAllSfxImmediately()
    }

    /**
     * Met à jour le niveau actif afin de sélectionner la musique de fond correspondante.
     * Chaque niveau change désormais d'ambiance musicale parmi les 14 thèmes distincts !
     */
    fun setLevelForMusic(levelNumber: Int) {
        currentLevelNumber.set(levelNumber.coerceAtLeast(1))
    }

    /**
     * Permet au joueur de choisir manuellement une musique de fond (-1 = Automatique selon le niveau, 0..13 = Thème fixe).
     */
    fun setManualMusicThemeOverride(themeIndex: Int) {
        manualThemeOverride.set(themeIndex.coerceIn(-1, MUSIC_THEMES.lastIndex))
        if (canPlayMusic() && ambientDesired.get()) {
            startAmbientSpaceMusic()
        }
    }

    fun getManualMusicThemeOverride(): Int = manualThemeOverride.get()

    fun getAllMusicThemeLabels(): List<String> = MUSIC_THEMES.map { it.shortLabel }

    private fun resolveActiveThemeIndex(levelNumber: Int): Int {
        val override = manualThemeOverride.get()
        if (override in MUSIC_THEMES.indices) {
            return override
        }
        // Chaque niveau consécutif fait tourner les 14 univers musicaux pour éviter toute répétition
        return (levelNumber.coerceAtLeast(1) - 1) % MUSIC_THEMES.size
    }

    fun getMusicThemeTitle(levelNumber: Int): String {
        val idx = resolveActiveThemeIndex(levelNumber)
        return MUSIC_THEMES[idx].title
    }

    /**
     * Démarre ou arrête la musique/ambiance spatiale en fonction de l'état de la partie.
     */
    fun updateAmbientState(shouldPlay: Boolean) {
        ambientDesired.set(shouldPlay)
        if (shouldPlay && canPlayMusic()) {
            startAmbientSpaceMusic()
        } else {
            stopAmbientSpaceMusic(clearDesired = false)
        }
    }

    /**
     * Vérifie que la musique de fond tourne bien si elle est activée, et la relance
     * automatiquement si un cycle de vie ou un changement d'état l'avait interrompue.
     */
    fun ensureAmbientPlaying() {
        ambientDesired.set(true)
        if (!canPlayMusic()) return
        val jobActive = synchronized(ambientLock) { ambientJob?.isActive == true }
        if (!ambientPlaying.get() || !jobActive) {
            startAmbientSpaceMusic()
        }
    }

    /**
     * Relance la musique de fond lors du retour au premier plan de l'application (onResume).
     */
    fun resumeIfDesired() {
        if (canPlayMusic() && ambientDesired.get()) {
            startAmbientSpaceMusic()
        }
    }

    fun startAmbientSpaceMusic() {
        ambientDesired.set(true)
        if (!canPlayMusic()) return

        synchronized(ambientLock) {
            if (ambientPlaying.get() && ambientJob?.isActive == true) {
                return
            }
            ambientPlaying.set(true)
            val sessionId = ambientSessionId.incrementAndGet()
            ambientJob?.cancel()

            ambientJob = scope.launch {
                var track: AudioTrack? = null
                try {
                    val chunkSamples = SAMPLE_RATE / 10 // 100ms chunks for responsive transitions
                    val minBufSize = AudioTrack.getMinBufferSize(
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                    ).coerceAtLeast(chunkSamples * 2)

                    val builtTrack = AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_GAME)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(SAMPLE_RATE)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(minBufSize * 2)
                        .setTransferMode(AudioTrack.MODE_STREAM)
                        .build()

                    track = builtTrack
                    synchronized(ambientLock) {
                        if (!canPlayMusic() || !ambientPlaying.get() || ambientSessionId.get() != sessionId) {
                            try {
                                builtTrack.release()
                            } catch (_: Throwable) {}
                            return@launch
                        }
                        activeAmbientTrack = builtTrack
                    }

                    builtTrack.play()

                    val buffer = ShortArray(chunkSamples)
                    var sampleIndex = 0L
                    var activeThemeIdx = -1
                    val twoPi = (2.0 * PI).toFloat()

                    while (isActive &&
                        appInForeground.get() &&
                        ambientPlaying.get() &&
                        musicEnabled.get() &&
                        ambientSessionId.get() == sessionId
                    ) {
                        val levelNum = currentLevelNumber.get().coerceAtLeast(1)
                        val targetThemeIdx = resolveActiveThemeIndex(levelNum)
                        if (targetThemeIdx != activeThemeIdx) {
                            activeThemeIdx = targetThemeIdx
                            sampleIndex = 0L
                        }
                        val theme = MUSIC_THEMES[activeThemeIdx]
                        val chords = theme.chords
                        val chordDurationSamples = (SAMPLE_RATE * theme.chordDurationSec).toLong().coerceAtLeast(1L)
                        val arpStepSamples = (SAMPLE_RATE * theme.arpStepSec).toLong().coerceAtLeast(1L)

                        for (i in 0 until chunkSamples) {
                            val t = sampleIndex.toFloat() / SAMPLE_RATE
                            val chordIdx = ((sampleIndex / chordDurationSamples) % chords.size).toInt()
                            val nextChordIdx = (chordIdx + 1) % chords.size
                            val posInChord = (sampleIndex % chordDurationSamples).toFloat() / chordDurationSamples

                            val crossfade = if (posInChord > 0.84f) (posInChord - 0.84f) / 0.16f else 0f
                            val c1 = chords[chordIdx]
                            val c2 = chords[nextChordIdx]

                            // 1) SYNTHÈSE DE LA NAPPE (PAD) SPÉCIFIQUE AU STYLE
                            var padSignal = 0f
                            if (theme.padLevel > 0.01f) {
                                var v1 = 0f
                                var v2 = 0f
                                when (theme.style) {
                                    MusicSynthStyle.ETHEREAL_CHOIR_ORGAN -> {
                                        // Chœur & Orgue : formants vocaux (3x, 4x, 5x) et trémolo céleste
                                        val choirVib = sin(twoPi * 5.2f * t) * 0.003f
                                        for (n in 0..3) {
                                            val f1 = c1[n] * (1f + choirVib)
                                            val f2 = c2[n] * (1f + choirVib)
                                            v1 += sin(twoPi * f1 * t) * 0.55f +
                                                sin(twoPi * f1 * 2f * t) * 0.25f +
                                                sin(twoPi * f1 * 3f * t) * 0.20f
                                            v2 += sin(twoPi * f2 * t) * 0.55f +
                                                sin(twoPi * f2 * 2f * t) * 0.25f +
                                                sin(twoPi * f2 * 3f * t) * 0.20f
                                        }
                                    }
                                    MusicSynthStyle.SYMPHONIC_STRINGS -> {
                                        // Cordes symphoniques : attaque arquée douce + désaccordage chorus (saw-like harmonics)
                                        val bowSwell = sin(posInChord * PI.toFloat()).coerceIn(0.25f, 1f)
                                        for (n in 0..3) {
                                            val f1 = c1[n]
                                            v1 += (sin(twoPi * f1 * t) +
                                                sin(twoPi * (f1 * 1.004f) * t) * 0.7f +
                                                sin(twoPi * (f1 * 2f) * t) * 0.35f +
                                                sin(twoPi * (f1 * 3f) * t) * 0.18f) * bowSwell
                                            val f2 = c2[n]
                                            v2 += (sin(twoPi * f2 * t) +
                                                sin(twoPi * (f2 * 1.004f) * t) * 0.7f +
                                                sin(twoPi * (f2 * 2f) * t) * 0.35f) * bowSwell
                                        }
                                    }
                                    MusicSynthStyle.DEEP_BINAURAL_TEMPLE -> {
                                        // Battement binaural grave (décalage de 4 Hz Thêta) + sous-octave
                                        val root1 = c1[0]
                                        val fifth1 = c1[1]
                                        v1 = sin(twoPi * root1 * t) +
                                            sin(twoPi * (root1 + 4f) * t) * 0.85f +
                                            sin(twoPi * (root1 * 0.5f) * t) * 0.75f +
                                            sin(twoPi * fifth1 * t) * 0.5f
                                        val root2 = c2[0]
                                        v2 = sin(twoPi * root2 * t) +
                                            sin(twoPi * (root2 + 4f) * t) * 0.85f +
                                            sin(twoPi * (root2 * 0.5f) * t) * 0.75f
                                    }
                                    MusicSynthStyle.GALACTIC_FUNK_BASS -> {
                                        // Piano électrique Rhodes avec trémolo stéréo rapide (6.5 Hz)
                                        val tremolo = 0.6f + 0.4f * sin(twoPi * 6.5f * t)
                                        for (n in 0..3) {
                                            v1 += (sin(twoPi * c1[n] * t) + sin(twoPi * c1[n] * 4f * t) * 0.12f * exp(-posInChord * 3f)) * tremolo
                                            v2 += sin(twoPi * c2[n] * t) * tremolo
                                        }
                                    }
                                    else -> {
                                        val lfo = 0.80f + 0.20f * sin(twoPi * 0.25f * t)
                                        for (n in 0..3) {
                                            v1 += sin(twoPi * c1[n] * t) * lfo
                                            v2 += sin(twoPi * c2[n] * t) * lfo
                                        }
                                    }
                                }
                                padSignal = ((1f - crossfade) * v1 + crossfade * v2) * 0.22f * theme.padLevel
                            }

                            // 2) SYNTHÈSE DU LEAD / ARPÈGE MÉLODIQUE SPÉCIFIQUE AU STYLE
                            val arpSeqIdx = ((sampleIndex / arpStepSamples) % theme.arpeggioOrder.size).toInt()
                            val rawSlot = theme.arpeggioOrder[arpSeqIdx]
                            val arpLocalPos = (sampleIndex % arpStepSamples).toFloat() / arpStepSamples
                            var leadSignal = 0f

                            if (rawSlot >= 0) {
                                val noteIdx = rawSlot % 4
                                val octaveMult = if (rawSlot >= 4) 2f else 1f
                                val baseNoteFreq = c1[noteIdx] * octaveMult

                                leadSignal = when (theme.style) {
                                    MusicSynthStyle.CELESTIAL_HARP -> {
                                        // Pincement de harpe cristalline : attaque franche + résonance d'harmoniques paires et impaires
                                        val env = exp(-arpLocalPos * 4.5f) * (1f - exp(-arpLocalPos * 95f))
                                        val f = baseNoteFreq
                                        (sin(twoPi * f * t) * 0.65f +
                                            sin(twoPi * f * 2f * t) * 0.25f * exp(-arpLocalPos * 6f) +
                                            sin(twoPi * f * 3f * t) * 0.10f * exp(-arpLocalPos * 10f)) * env
                                    }
                                    MusicSynthStyle.SYNTHWAVE_PULSE -> {
                                        // Lead Synthwave analogique à onde riche (saw-like detuned)
                                        val env = sin(arpLocalPos * PI.toFloat()).coerceAtLeast(0f)
                                        val f = baseNoteFreq * 2f
                                        (sin(twoPi * f * t) * 0.50f +
                                            sin(twoPi * (f * 1.006f) * t) * 0.35f +
                                            sin(twoPi * (f * 2f) * t) * 0.25f +
                                            sin(twoPi * (f * 3f) * t) * 0.12f) * env
                                    }
                                    MusicSynthStyle.CRYSTAL_BELLS_CHIME -> {
                                        // Synthèse FM de clochettes de cristal / boîte à musique (partiels inharmoniques 1 : 2.756 : 5.4)
                                        val env = exp(-arpLocalPos * 5.2f) * (1f - exp(-arpLocalPos * 140f))
                                        val mod = sin(twoPi * (baseNoteFreq * 2.756f) * t) * exp(-arpLocalPos * 9f) * 1.4f
                                        (sin(twoPi * baseNoteFreq * t + mod) * 0.78f +
                                            sin(twoPi * (baseNoteFreq * 2f) * t) * 0.22f) * env
                                    }
                                    MusicSynthStyle.DEEP_BINAURAL_TEMPLE -> {
                                        // Frappe de bol tibétain résonnant avec harmonique métallique lente (ratio 2.71)
                                        val env = exp(-arpLocalPos * 2.1f) * (1f - exp(-arpLocalPos * 45f))
                                        val bowlVib = 0.85f + 0.15f * sin(twoPi * 6.0f * t)
                                        (sin(twoPi * baseNoteFreq * t) * 0.72f +
                                            sin(twoPi * (baseNoteFreq * 2.71f) * t) * 0.28f * exp(-arpLocalPos * 3.5f)) * env * bowlVib
                                    }
                                    MusicSynthStyle.COSMIC_MARIMBA_GROOVE -> {
                                        // Marimba / Kalimba en bois : attaque percussive ultra-courte + partiel de 4e octave (ratio 4.0)
                                        val env = exp(-arpLocalPos * 9.5f) * (1f - exp(-arpLocalPos * 180f))
                                        val click = sin(twoPi * (baseNoteFreq * 4f) * t) * exp(-arpLocalPos * 32f) * 0.45f
                                        (sin(twoPi * (baseNoteFreq * 2f) * t) * 0.85f + click) * env
                                    }
                                    MusicSynthStyle.ETHEREAL_CHOIR_ORGAN -> {
                                        // Voix soprano / flûte d'orgue céleste liée (legato)
                                        val env = sin(arpLocalPos * PI.toFloat())
                                        val vib = sin(twoPi * 5.5f * t) * 3.2f
                                        val f = baseNoteFreq * 2f + vib
                                        (sin(twoPi * f * t) * 0.70f + sin(twoPi * (f * 2f) * t) * 0.30f) * env
                                    }
                                    MusicSynthStyle.ORBITAL_ELECTRO_ARP -> {
                                        // Pluck électro-trance vif et tranchant
                                        val env = exp(-arpLocalPos * 7.5f)
                                        val f = baseNoteFreq * 2f
                                        val raw = sin(twoPi * f * t) + sin(twoPi * f * 2f * t) * 0.5f + sin(twoPi * f * 3f * t) * 0.25f
                                        (raw * 0.65f) * env
                                    }
                                    MusicSynthStyle.STARLIGHT_PIANO_NOCTURNE -> {
                                        // Marteau de piano feutré en valse 3/4 (temps 1 = basse grave, temps 2 & 3 = accord/mélodie)
                                        val isWaltzBassBeat = (arpSeqIdx % 3 == 0)
                                        val f = if (isWaltzBassBeat) c1[0] else baseNoteFreq * 2f
                                        val env = exp(-arpLocalPos * (if (isWaltzBassBeat) 3.2f else 5.0f)) *
                                            (1f - exp(-arpLocalPos * 120f))
                                        (sin(twoPi * f * t) * 0.75f +
                                            sin(twoPi * f * 2f * t) * 0.20f * exp(-arpLocalPos * 4f) +
                                            sin(twoPi * f * 3f * t) * 0.05f) * env
                                    }
                                    MusicSynthStyle.MYSTIC_DESERT_FLUTE -> {
                                        // Flûte orientale / Duduk avec glissando d'attaque et vibrato expressif + harmoniques impaires
                                        val env = sin(arpLocalPos * PI.toFloat()).coerceAtLeast(0f)
                                        val pitchBend = 1f - 0.03f * exp(-arpLocalPos * 14f)
                                        val vibrato = sin(twoPi * 6.2f * t) * 4.5f * arpLocalPos
                                        val f = baseNoteFreq * 2f * pitchBend + vibrato
                                        (sin(twoPi * f * t) * 0.60f +
                                            sin(twoPi * (f * 3f) * t) * 0.28f +
                                            sin(twoPi * (f * 5f) * t) * 0.12f) * env
                                    }
                                    MusicSynthStyle.SYMPHONIC_STRINGS -> {
                                        // Violon solo lyrique au-dessus de l'orchestre
                                        val env = sin(arpLocalPos * PI.toFloat())
                                        val vib = sin(twoPi * 5.8f * t) * 3.8f
                                        val f = baseNoteFreq * 2f + vib
                                        (sin(twoPi * f * t) * 0.6f +
                                            sin(twoPi * f * 2f * t) * 0.28f +
                                            sin(twoPi * f * 3f * t) * 0.12f) * env
                                    }
                                    MusicSynthStyle.ZEN_KOTO_BAMBOO -> {
                                        // Koto japonais pincé avec léger bend de corde traditionnel
                                        val env = exp(-arpLocalPos * 6.0f) * (1f - exp(-arpLocalPos * 160f))
                                        val kotoBend = 1f + 0.025f * exp(-arpLocalPos * 12f)
                                        val f = baseNoteFreq * 2f * kotoBend
                                        (sin(twoPi * f * t) * 0.62f +
                                            sin(twoPi * f * 2f * t) * 0.26f +
                                            sin(twoPi * f * 4f * t) * 0.12f * exp(-arpLocalPos * 10f)) * env
                                    }
                                    MusicSynthStyle.GALACTIC_FUNK_BASS -> {
                                        // Synthé Funk percutant
                                        val env = exp(-arpLocalPos * 6.8f)
                                        val f = baseNoteFreq * 2f
                                        (sin(twoPi * f * t) * 0.6f +
                                            sin(twoPi * f * 2f * t) * 0.3f * exp(-arpLocalPos * 8f) +
                                            sin(twoPi * f * 3f * t) * 0.2f * exp(-arpLocalPos * 12f)) * env
                                    }
                                    MusicSynthStyle.AMBIENT_GLACIER_ECHO -> {
                                        // Cristaux polaires en double écho très haut perchés
                                        val env1 = exp(-arpLocalPos * 6.5f)
                                        val echoPos = (arpLocalPos - 0.45f).coerceAtLeast(0f)
                                        val env2 = if (arpLocalPos > 0.45f) exp(-echoPos * 8.0f) * 0.55f else 0f
                                        val f = baseNoteFreq * 2f
                                        (sin(twoPi * f * t) * env1 + sin(twoPi * (f * 1.5f) * t) * env2) * 0.75f
                                    }
                                    MusicSynthStyle.TRIUMPHANT_APOTHEOSIS -> {
                                        // Cuivres & cloches solaires triomphales
                                        val env = sin(arpLocalPos.coerceAtMost(0.2f) / 0.2f * (PI.toFloat() * 0.5f)) *
                                            exp(-arpLocalPos * 2.8f)
                                        val f = baseNoteFreq * 2f
                                        (sin(twoPi * f * t) * 0.55f +
                                            sin(twoPi * f * 2f * t) * 0.30f +
                                            sin(twoPi * f * 3f * t) * 0.15f) * env
                                    }
                                } * theme.leadLevel
                            }

                            // 3) LIGNE DE BASSE & PULSATION RYTHMIQUE (uniquement sur les thèmes rythmiques)
                            var bassAndRhythm = 0f
                            if (theme.bassPulseHz > 0.05f) {
                                val beatPhase = (t * theme.bassPulseHz) % 1f
                                val bassFreq = c1[0] * 0.5f // Sous-octave de basse
                                when (theme.style) {
                                    MusicSynthStyle.SYNTHWAVE_PULSE,
                                    MusicSynthStyle.ORBITAL_ELECTRO_ARP -> {
                                        // Kick électronique doux (sweep rapide 110Hz -> 48Hz) + basse synthé pompante
                                        val kickEnv = exp(-beatPhase * 14f)
                                        val kickSweepFreq = 48f + 65f * exp(-beatPhase * 22f)
                                        val kick = sin(twoPi * kickSweepFreq * t) * kickEnv * 0.18f
                                        val subBassEnv = sin(beatPhase * PI.toFloat()).coerceAtLeast(0f)
                                        val subBass = (sin(twoPi * bassFreq * t) + sin(twoPi * c1[0] * t) * 0.5f) * subBassEnv * 0.15f
                                        bassAndRhythm = kick + subBass
                                    }
                                    MusicSynthStyle.COSMIC_MARIMBA_GROOVE,
                                    MusicSynthStyle.GALACTIC_FUNK_BASS -> {
                                        // Basse ronde rebondissante (Slap / Sub pluck)
                                        val pluckEnv = exp(-beatPhase * 7.5f)
                                        bassAndRhythm = (sin(twoPi * bassFreq * t) * 0.7f +
                                            sin(twoPi * c1[0] * t) * 0.3f * exp(-beatPhase * 12f)) * pluckEnv * 0.18f
                                    }
                                    MusicSynthStyle.MYSTIC_DESERT_FLUTE -> {
                                        // Percussion douce de tambour sur cadre oriental (Dum grave + Tak léger)
                                        val dumEnv = exp(-beatPhase * 11f)
                                        bassAndRhythm = sin(twoPi * 72f * t) * dumEnv * 0.15f
                                    }
                                    else -> {
                                        val pulseEnv = 0.5f + 0.5f * sin(twoPi * theme.bassPulseHz * t)
                                        bassAndRhythm = sin(twoPi * bassFreq * t) * pulseEnv * 0.12f
                                    }
                                }
                            }

                            val mixed = ((padSignal + leadSignal + bassAndRhythm) * 0.68f).coerceIn(-0.92f, 0.92f)
                            buffer[i] = (mixed * Short.MAX_VALUE).toInt().toShort()
                            sampleIndex++
                        }

                        if (!canPlayMusic() || !ambientPlaying.get() || ambientSessionId.get() != sessionId) {
                            break
                        }
                        val written = track.write(buffer, 0, chunkSamples)
                        if (written < 0) {
                            break
                        }
                    }
                } catch (_: Throwable) {
                    // Ignore audio hardware exceptions in headless test environments
                } finally {
                    synchronized(ambientLock) {
                        if (activeAmbientTrack === track) {
                            activeAmbientTrack = null
                        }
                        // Never clobber ambientPlaying if a newer session has already started!
                        if (ambientSessionId.get() == sessionId) {
                            ambientPlaying.set(false)
                        }
                    }
                    try {
                        track?.pause()
                        track?.flush()
                        track?.stop()
                    } catch (_: Throwable) {}
                    try {
                        track?.release()
                    } catch (_: Throwable) {}
                }
            }
        }
    }

    fun stopAmbientSpaceMusic(clearDesired: Boolean = false) {
        if (clearDesired) {
            ambientDesired.set(false)
        }
        val trackToStop: AudioTrack?
        synchronized(ambientLock) {
            ambientSessionId.incrementAndGet()
            ambientPlaying.set(false)
            ambientJob?.cancel()
            ambientJob = null
            trackToStop = activeAmbientTrack
            activeAmbientTrack = null
        }
        if (trackToStop != null) {
            try {
                trackToStop.pause()
                trackToStop.flush()
                trackToStop.stop()
            } catch (_: Throwable) {}
        }
    }

    // =========================================================================
    // SONS SPÉCIAUX POUR LES ÉLÉMENTS DANS LA ZONE DU JOUEUR (PROXIMITÉ)
    // =========================================================================

    private fun canTriggerZoneSound(categoryKey: String, minCategoryIntervalMs: Long, minGlobalIntervalMs: Long = 320L): Boolean {
        if (!canPlayAudio()) return false
        val now = System.currentTimeMillis()
        if (now - lastZoneSoundGlobalTimeMs < minGlobalIntervalMs) return false
        val lastCat = zoneCategoryLastTimeMs[categoryKey] ?: 0L
        if (now - lastCat < minCategoryIntervalMs) return false
        lastZoneSoundGlobalTimeMs = now
        zoneCategoryLastTimeMs[categoryKey] = now
        return true
    }

    /**
     * Son spécial lorsqu'un Neurone Infecté est dans la zone du joueur, adapté à son type de corruption :
     * - COLERE_PEUR : Pulsation bio-électrique tendue (battement grave + crépitement synaptique)
     * - TRISTESSE : Onde grave mélancolique descendante
     * - AGITATION : Frémissement synaptique rapide et oscillant
     */
    fun playZoneInfectedNeuron(corruptionType: NeuronCorruptionType, isImmediateEntry: Boolean = false) {
        val cooldown = if (isImmediateEntry) 900L else 2400L
        if (!canTriggerZoneSound("neuron_${corruptionType.name}", cooldown)) return

        when (corruptionType) {
            NeuronCorruptionType.COLERE_PEUR -> {
                playSynthesizedEffect(durationMs = 240) { t, durationSec ->
                    val p = (t / durationSec).coerceIn(0f, 1f)
                    val pulse = sin(p * PI.toFloat() * 2f)
                    val baseFreq = 135f + 45f * pulse
                    val env = sin(p * PI.toFloat())
                    val sub = sin(2f * PI.toFloat() * baseFreq * t) * 0.65f
                    val crackle = sin(2f * PI.toFloat() * (baseFreq * 3.5f) * t) * 0.35f
                    (sub + crackle) * env * 0.26f
                }
            }
            NeuronCorruptionType.TRISTESSE -> {
                playSynthesizedEffect(durationMs = 300) { t, durationSec ->
                    val p = (t / durationSec).coerceIn(0f, 1f)
                    val freq = 246.94f * (1f - p * 0.22f) // B3 glissando mélancolique vers Sol
                    val env = sin(p * PI.toFloat())
                    val wave = sin(2f * PI.toFloat() * freq * t) * 0.75f +
                        sin(2f * PI.toFloat() * (freq * 1.5f) * t) * 0.25f
                    wave * env * 0.25f
                }
            }
            NeuronCorruptionType.AGITATION -> {
                playSynthesizedEffect(durationMs = 210) { t, durationSec ->
                    val p = (t / durationSec).coerceIn(0f, 1f)
                    val vibrato = sin(2f * PI.toFloat() * 28f * t) * 65f
                    val freq = 420f + vibrato
                    val env = sin(p * PI.toFloat())
                    val wave = sin(2f * PI.toFloat() * freq * t) * 0.7f +
                        sin(2f * PI.toFloat() * (freq * 1.75f) * t) * 0.3f
                    wave * env * 0.24f
                }
            }
            NeuronCorruptionType.RANCOEUR -> {
                playSynthesizedEffect(durationMs = 260) { t, durationSec ->
                    val p = (t / durationSec).coerceIn(0f, 1f)
                    val freq = 174.61f // Fa grave glacial avec dissonance de seconde mineure
                    val env = sin(p * PI.toFloat())
                    val wave = sin(2f * PI.toFloat() * freq * t) * 0.65f +
                        sin(2f * PI.toFloat() * (freq * 1.06f) * t) * 0.35f
                    wave * env * 0.25f
                }
            }
            NeuronCorruptionType.SURTENSION -> {
                playSynthesizedEffect(durationMs = 200) { t, durationSec ->
                    val p = (t / durationSec).coerceIn(0f, 1f)
                    val pulse = sin(2f * PI.toFloat() * 34f * t) * 85f
                    val freq = 540f + pulse
                    val env = sin(p * PI.toFloat())
                    val wave = sin(2f * PI.toFloat() * freq * t) * 0.7f +
                        sin(2f * PI.toFloat() * (freq * 2.1f) * t) * 0.3f
                    wave * env * 0.24f
                }
            }
            NeuronCorruptionType.APATHIE -> {
                playSynthesizedEffect(durationMs = 310) { t, durationSec ->
                    val p = (t / durationSec).coerceIn(0f, 1f)
                    val freq = 146.83f * (1f - p * 0.18f) // Ré grave ralenti (sidération / inertie)
                    val env = sin(p * PI.toFloat())
                    sin(2f * PI.toFloat() * freq * t) * env * 0.24f
                }
            }
            NeuronCorruptionType.RIGIDITE_OBSESSION -> {
                playSynthesizedEffect(durationMs = 240) { t, durationSec ->
                    val p = (t / durationSec).coerceIn(0f, 1f)
                    val env = sin(p * PI.toFloat())
                    // Boucle cognitive fixe à deux fréquences rigides
                    val freq = if (((p * 6f).toInt() % 2) == 0) 329.63f else 466.16f
                    sin(2f * PI.toFloat() * freq * t) * env * 0.24f
                }
            }
            NeuronCorruptionType.BURNOUT_DISSOCIATION -> {
                playSynthesizedEffect(durationMs = 320) { t, durationSec ->
                    val p = (t / durationSec).coerceIn(0f, 1f)
                    val env = sin(p * PI.toFloat()) * (1f - 0.35f * sin(2f * PI.toFloat() * 9f * t))
                    val freq = 130.81f // Do grave dissocié
                    val wave = sin(2f * PI.toFloat() * freq * t) * 0.65f +
                        sin(2f * PI.toFloat() * (freq * 1.414f) * t) * 0.35f
                    wave * env * 0.24f
                }
            }
        }
    }

    /**
     * Son spécial lorsqu'un Obstacle Mental est dans la zone du joueur, unique pour chacun des 8 types :
     * 1. ASTEROID (Pensée Parasite) : Grondement organique sourd
     * 2. LASER_DRONE (Foyer d'Angoisse) : Alerte radar / sifflement laser sous tension
     * 3. PULSAR_MINE (Crise de Panique) : Bip sonar pulsé accéléré
     * 4. CONFORMISM_SWARM (La Nuée Grégaire) : Cadence mécanique synchronisée d'essaim
     * 5. DOGMATIC_PRISM (Le Prisme Dogmatique) : Résonance cristalline métallique froide
     * 6. OBSCURANTISM_VEIL (Le Voile d'Obscurantisme) : Grésillement statique / brouillage radio
     * 7. MENTAL_MIRE (Le Bourbier Mental) : Succion gravitationnelle visqueuse
     * 8. NARCISSUS_MIRROR (Le Miroir de Narcisse) : Scintillement hypnotique de faux astre (sirène inversée)
     */
    fun playZoneObstacle(type: ObstacleType, isImmediateEntry: Boolean = false) {
        val cooldown = if (isImmediateEntry) 950L else 2500L
        if (!canTriggerZoneSound("obs_${type.name}", cooldown)) return

        when (type) {
            ObstacleType.ASTEROID -> {
                playSynthesizedEffect(durationMs = 190) { t, durationSec ->
                    val p = (t / durationSec).coerceIn(0f, 1f)
                    val freq = 112f - p * 28f
                    val env = sin(p * PI.toFloat())
                    val rumble = sin(2f * PI.toFloat() * freq * t) * 0.75f +
                        sin(2f * PI.toFloat() * (freq * 1.6f) * t) * 0.25f
                    rumble * env * 0.24f
                }
            }
            ObstacleType.LASER_DRONE -> {
                playSynthesizedEffect(durationMs = 175) { t, durationSec ->
                    val p = (t / durationSec).coerceIn(0f, 1f)
                    val freq = if (p < 0.5f) 740f + p * 320f else 900f - (p - 0.5f) * 260f
                    val env = sin(p * PI.toFloat())
                    val laserHum = sin(2f * PI.toFloat() * freq * t) * 0.7f +
                        sin(2f * PI.toFloat() * (freq * 2f) * t) * 0.3f
                    laserHum * env * 0.22f
                }
            }
            ObstacleType.PULSAR_MINE -> {
                playSynthesizedEffect(durationMs = 200) { t, durationSec ->
                    val p = (t / durationSec).coerceIn(0f, 1f)
                    // Double sonar ping
                    val pingEnv = if (p < 0.42f) {
                        sin((p / 0.42f) * PI.toFloat())
                    } else if (p > 0.54f) {
                        sin(((p - 0.54f) / 0.46f) * PI.toFloat())
                    } else 0f
                    val freq = if (p < 0.5f) 860f else 1080f
                    sin(2f * PI.toFloat() * freq * t) * pingEnv * 0.24f
                }
            }
            ObstacleType.CONFORMISM_SWARM -> {
                playSynthesizedEffect(durationMs = 240) { t, durationSec ->
                    val p = (t / durationSec).coerceIn(0f, 1f)
                    // Cadence robotique uniforme en 3 pas identiques ("rentrer dans le rang")
                    val step = (p * 3f).toInt().coerceIn(0, 2)
                    val localP = (p * 3f) - step
                    val stepEnv = sin(localP * PI.toFloat()).coerceAtLeast(0f)
                    val drone = sin(2f * PI.toFloat() * 220f * t) * 0.65f +
                        sin(2f * PI.toFloat() * 440f * t) * 0.35f
                    drone * stepEnv * 0.25f
                }
            }
            ObstacleType.DOGMATIC_PRISM -> {
                playSynthesizedEffect(durationMs = 250) { t, durationSec ->
                    val p = (t / durationSec).coerceIn(0f, 1f)
                    val env = exp(-p * 3.2f) * sin(p.coerceAtMost(0.15f) / 0.15f * (PI.toFloat() * 0.5f))
                    // Résonance métallique froide inharmonique (quinte froide + octave)
                    val crystal = sin(2f * PI.toFloat() * 528f * t) * 0.55f +
                        sin(2f * PI.toFloat() * 792f * t) * 0.30f +
                        sin(2f * PI.toFloat() * 1360f * t) * 0.15f
                    crystal * env * 0.26f
                }
            }
            ObstacleType.OBSCURANTISM_VEIL -> {
                playSynthesizedEffect(durationMs = 240) { t, durationSec ->
                    val p = (t / durationSec).coerceIn(0f, 1f)
                    val env = sin(p * PI.toFloat())
                    val darkHum = sin(2f * PI.toFloat() * 145f * t) * 0.6f
                    val staticNoise = (Random.nextFloat() * 2f - 1f) * 0.4f * sin(2f * PI.toFloat() * 18f * t)
                    (darkHum + staticNoise) * env * 0.23f
                }
            }
            ObstacleType.MENTAL_MIRE -> {
                playSynthesizedEffect(durationMs = 260) { t, durationSec ->
                    val p = (t / durationSec).coerceIn(0f, 1f)
                    val freq = 165f - p * 65f + sin(2f * PI.toFloat() * 9f * t) * 14f
                    val env = sin(p * PI.toFloat())
                    val mire = sin(2f * PI.toFloat() * freq * t) * 0.75f +
                        sin(2f * PI.toFloat() * (freq * 1.5f) * t) * 0.25f
                    mire * env * 0.26f
                }
            }
            ObstacleType.NARCISSUS_MIRROR -> {
                playSynthesizedEffect(durationMs = 260) { t, durationSec ->
                    val p = (t / durationSec).coerceIn(0f, 1f)
                    // Chant de sirène illusoire (arpège brillant mais légèrement dissonant qui attire)
                    val freq = 660f + sin(2f * PI.toFloat() * 6f * t) * 110f - p * 90f
                    val env = sin(p * PI.toFloat())
                    val mirror = sin(2f * PI.toFloat() * freq * t) * 0.65f +
                        sin(2f * PI.toFloat() * (freq * 1.414f) * t) * 0.35f
                    mirror * env * 0.25f
                }
            }
        }
    }

    /**
     * Son spécial lorsqu'un Sanctuaire de Méditation (Fragment du Fruit ou Texte Bonus 🧠) est dans la zone du joueur.
     */
    fun playZoneSanctuaryRelic(isBrainBonus: Boolean, isImmediateEntry: Boolean = false) {
        val key = if (isBrainBonus) "zone_brain_relic" else "zone_fruit_relic"
        val cooldown = if (isImmediateEntry) 1100L else 2800L
        if (!canTriggerZoneSound(key, cooldown)) return

        if (isBrainBonus) {
            // Onde synaptique binaurale mystique pour le Texte Bonus 🧠
            playSynthesizedEffect(durationMs = 260) { t, durationSec ->
                val p = (t / durationSec).coerceIn(0f, 1f)
                val env = sin(p * PI.toFloat())
                val f1 = 528f
                val f2 = 660f
                val wave = sin(2f * PI.toFloat() * f1 * t) * 0.5f +
                    sin(2f * PI.toFloat() * f2 * t) * 0.5f
                wave * env * 0.24f
            }
        } else {
            // Halo cristallin céleste pour un Sanctuaire du Fruit de l'Esprit
            playSynthesizedEffect(durationMs = 240) { t, durationSec ->
                val p = (t / durationSec).coerceIn(0f, 1f)
                val env = sin(p * PI.toFloat())
                val f = if (p < 0.5f) 587.33f else 880f // D5 -> A5
                val wave = sin(2f * PI.toFloat() * f * t) * 0.7f +
                    sin(2f * PI.toFloat() * (f * 2f) * t) * 0.3f
                wave * env * 0.22f
            }
        }
    }

    /**
     * Son spécial lorsqu'une Pensée Pure (Étincelle de Lucidité, Fragment de Savoir, Souvenir) est dans la zone du joueur.
     */
    fun playZoneRescueTarget(type: RescueTargetType, isImmediateEntry: Boolean = false) {
        val cooldown = if (isImmediateEntry) 1000L else 2600L
        if (!canTriggerZoneSound("target_${type.name}", cooldown)) return

        when (type) {
            RescueTargetType.LUCIDITY_SPARK -> {
                playSynthesizedEffect(durationMs = 160) { t, durationSec ->
                    val p = (t / durationSec).coerceIn(0f, 1f)
                    val freq = 1046.5f + p * 272f // C6 -> E6
                    val env = sin(p * PI.toFloat())
                    sin(2f * PI.toFloat() * freq * t) * env * 0.20f
                }
            }
            RescueTargetType.KNOWLEDGE_FRAGMENT -> {
                playSynthesizedEffect(durationMs = 180) { t, durationSec ->
                    val p = (t / durationSec).coerceIn(0f, 1f)
                    val freq = if (p < 0.5f) 659.25f else 987.77f // E5 -> B5
                    val env = sin(p * PI.toFloat())
                    val wave = sin(2f * PI.toFloat() * freq * t) * 0.75f +
                        sin(2f * PI.toFloat() * (freq * 1.5f) * t) * 0.25f
                    wave * env * 0.21f
                }
            }
            RescueTargetType.FRAGILE_MEMORY -> {
                playSynthesizedEffect(durationMs = 190) { t, durationSec ->
                    val p = (t / durationSec).coerceIn(0f, 1f)
                    // Douce bulle cristalline modulée
                    val freq = 783.99f + sin(2f * PI.toFloat() * 14f * t) * 35f
                    val env = sin(p * PI.toFloat())
                    sin(2f * PI.toFloat() * freq * t) * env * 0.21f
                }
            }
        }
    }

    /**
     * Son spécial lorsqu'un Bonus Cosmique (Bouclier, Vitesse, Aimant, Dissipateur, Onde Nova) est dans la zone du joueur.
     */
    fun playZonePowerUp(type: PowerUpType, isImmediateEntry: Boolean = false) {
        val cooldown = if (isImmediateEntry) 1100L else 2700L
        if (!canTriggerZoneSound("pup_${type.name}", cooldown)) return

        val baseFreq = when (type) {
            PowerUpType.SHIELD -> 440f
            PowerUpType.SPEED_BOOST -> 587.33f
            PowerUpType.MAGNET -> 392f
            PowerUpType.EMP_BLAST -> 659.25f
            PowerUpType.FREE_WILL_AEGIS -> 554.37f
            PowerUpType.METACOGNITION_BEACON -> 698.46f
            PowerUpType.HUMILITY_ANCHOR -> 349.23f
            PowerUpType.MYELIN_SHEATH -> 493.88f
            PowerUpType.CONTEMPLATIVE_PHASE_SHIFT -> 783.99f
            PowerUpType.PLENITUDE_ARK -> 880.00f
            else -> 523.25f
        }
        playSynthesizedEffect(durationMs = 185) { t, durationSec ->
            val p = (t / durationSec).coerceIn(0f, 1f)
            val freq = baseFreq + p * 180f
            val env = sin(p * PI.toFloat())
            val wave = sin(2f * PI.toFloat() * freq * t) * 0.75f +
                sin(2f * PI.toFloat() * (freq * 1.5f) * t) * 0.25f
            wave * env * 0.21f
        }
    }

    /**
     * Son spécial lorsque le joueur projette un Mot de Vertu depuis sa sphère vers un élément dans sa zone.
     */
    fun playVirtueProjected(virtue: VirtueRemedyType) {
        if (!soundEnabled.get()) return
        val notes = when (virtue) {
            VirtueRemedyType.PAIX -> floatArrayOf(523.25f, 659.25f, 783.99f) // Accord de Paix (Do Majeur)
            VirtueRemedyType.JOIE -> floatArrayOf(587.33f, 739.99f, 880.00f) // Accord de Joie (Ré Majeur brillant)
            VirtueRemedyType.MAITRISE_DE_SOI -> floatArrayOf(493.88f, 659.25f, 987.77f) // Quarte/Octave stable
            VirtueRemedyType.AMOUR -> floatArrayOf(440.00f, 554.37f, 659.25f, 880.00f) // Accord d'Amour chaleureux
            VirtueRemedyType.VERITE_DISCERNEMENT -> floatArrayOf(659.25f, 830.61f, 987.77f, 1318.51f) // Rayon cristallin de Vérité
            VirtueRemedyType.REGULATION_SOUFFLE -> floatArrayOf(432.00f, 528.00f, 639.00f, 528.00f) // Souffle vagal apaisant (cohérence cardiaque)
            VirtueRemedyType.ELAN_ACTION -> floatArrayOf(440.00f, 587.33f, 880.00f, 1174.66f) // Impulsion dopaminergique ascendante vive
            VirtueRemedyType.PLASTICITE_RECADRAGE -> floatArrayOf(523.25f, 622.25f, 783.99f, 1046.50f) // Modulation préfrontale de recadrage
            VirtueRemedyType.HOMEOSTASIE_PRESENCE -> floatArrayOf(396.00f, 528.00f, 792.00f) // Résonance intéroceptive harmonique profonde
            VirtueRemedyType.INFLUX_BIOELECTRIQUE -> floatArrayOf(783.99f, 1046.50f, 1567.98f, 2093.00f) // Dépolarisation bioélectrique haute tension
            VirtueRemedyType.RESONANCE_GAMMA -> floatArrayOf(528.00f, 792.00f, 1056.00f, 1584.00f) // Onde cérébrale Gamma synchronisée (40 Hz harmonique)
        }
        playSynthesizedEffect(durationMs = 280) { t, durationSec ->
            val idx = ((t / durationSec) * notes.size).toInt().coerceIn(0, notes.size - 1)
            val freq = notes[idx]
            val env = sin((t / durationSec) * PI.toFloat()).coerceAtLeast(0f)
            val wave = sin(2f * PI.toFloat() * freq * t) * 0.7f +
                sin(2f * PI.toFloat() * (freq * 2f) * t) * 0.3f
            wave * env * 0.34f
        }
    }

    /**
     * Effet sonore rapide de résultat de purification neuronale : 0%, 50% ou 100% purifié.
     */
    fun playNeuronPurificationResult(percentage: Int) {
        if (!soundEnabled.get()) return
        when {
            percentage >= 100 -> {
                // 100% purifié : Accord ascendant cristallin et triomphant
                val notes = floatArrayOf(523.25f, 659.25f, 783.99f, 1046.50f, 1318.51f)
                playSynthesizedEffect(durationMs = 360) { t, durationSec ->
                    val idx = ((t / durationSec) * notes.size).toInt().coerceIn(0, notes.size - 1)
                    val freq = notes[idx]
                    val env = (1f - (t / durationSec) * 0.65f).coerceIn(0f, 1f)
                    val wave = sin(2f * PI.toFloat() * freq * t) * 0.68f +
                        sin(2f * PI.toFloat() * (freq * 2f) * t) * 0.32f
                    wave * env * 0.38f
                }
            }
            percentage >= 50 -> {
                // 50% purifié : Double note harmonique de résonance partielle
                val notes = floatArrayOf(440.00f, 554.37f, 659.25f)
                playSynthesizedEffect(durationMs = 260) { t, durationSec ->
                    val idx = ((t / durationSec) * notes.size).toInt().coerceIn(0, notes.size - 1)
                    val freq = notes[idx]
                    val env = sin((t / durationSec) * PI.toFloat()).coerceAtLeast(0f)
                    val wave = sin(2f * PI.toFloat() * freq * t) * 0.75f +
                        sin(2f * PI.toFloat() * (freq * 1.5f) * t) * 0.25f
                    wave * env * 0.34f
                }
            }
            else -> {
                // 0% purifié : Courte dissonance descendante (remède inadapté)
                playSynthesizedEffect(durationMs = 240) { t, durationSec ->
                    val p = (t / durationSec).coerceIn(0f, 1f)
                    val freq = 260f - p * 95f
                    val env = (1f - p).coerceAtLeast(0f)
                    val wave = sin(2f * PI.toFloat() * freq * t) * 0.65f +
                        sin(2f * PI.toFloat() * (freq * 1.41f) * t) * 0.35f
                    wave * env * 0.32f
                }
            }
        }
    }

    /**
     * Son spécial lorsque le Prisme Dogmatique dévie un projectile ou se brise sous Vérité & Discernement.
     */
    fun playPrismInteraction(shattered: Boolean) {
        if (!soundEnabled.get()) return
        if (shattered) {
            val notes = floatArrayOf(587.33f, 880f, 1174.66f, 1760f)
            playSynthesizedEffect(durationMs = 420) { t, durationSec ->
                val idx = ((t / durationSec) * notes.size).toInt().coerceIn(0, notes.size - 1)
                val freq = notes[idx]
                val env = (1f - t / durationSec).coerceAtLeast(0f)
                val wave = sin(2f * PI.toFloat() * freq * t) * 0.65f +
                    sin(2f * PI.toFloat() * (freq * 1.5f) * t) * 0.35f
                wave * env * 0.38f
            }
        } else {
            // Ricochet métallique sur le Prisme Dogmatique
            playSynthesizedEffect(durationMs = 200) { t, durationSec ->
                val p = (t / durationSec).coerceIn(0f, 1f)
                val freq = 1120f - p * 480f
                val env = (1f - p) * (1f - p)
                sin(2f * PI.toFloat() * freq * t) * env * 0.32f
            }
        }
    }

    /**
     * Son spécial lorsque La Nuée Grégaire (Conformisme) absorbe les bonus dans sa zone rectangulaire
     * ou lorsque le Miroir de Narcisse draine l'énergie de purification.
     */
    fun playSpecialObstacleEffect(type: ObstacleType) {
        if (!canTriggerZoneSound("effect_${type.name}", 900L, 180L)) return
        when (type) {
            ObstacleType.CONFORMISM_SWARM -> {
                // Aspiration descendante des capacités spéciales
                playSynthesizedEffect(durationMs = 280) { t, durationSec ->
                    val p = (t / durationSec).coerceIn(0f, 1f)
                    val freq = 480f * (1f - p * 0.55f)
                    val env = sin(p * PI.toFloat())
                    val wave = sin(2f * PI.toFloat() * freq * t) * 0.7f +
                        sin(2f * PI.toFloat() * (freq * 0.5f) * t) * 0.3f
                    wave * env * 0.32f
                }
            }
            ObstacleType.NARCISSUS_MIRROR -> {
                // Drain illusoire du Miroir de Narcisse
                playSynthesizedEffect(durationMs = 320) { t, durationSec ->
                    val p = (t / durationSec).coerceIn(0f, 1f)
                    val freq = 720f * (1f - p * 0.65f)
                    val env = (1f - p)
                    val wave = sin(2f * PI.toFloat() * freq * t) * 0.65f +
                        sin(2f * PI.toFloat() * (freq * 1.41f) * t) * 0.35f
                    wave * env * 0.35f
                }
            }
            else -> {}
        }
    }

    // =========================================================================
    // SONS D'INTERACTION ET ÉVÉNEMENTS DE JEU
    // =========================================================================

    /**
     * Son cristallin évolutif lorsque le joueur harmonise un sanctuaire.
     */
    fun playHarmonizingPulse(progress: Float) {
        if (!soundEnabled.get()) return
        val now = System.currentTimeMillis()
        if (now - lastHarmonizeSoundTimeMs < 240L) return
        lastHarmonizeSoundTimeMs = now

        val baseFreq = 440f + progress.coerceIn(0f, 1f) * 440f
        playSynthesizedEffect(durationMs = 130) { t, durationSec ->
            val env = sin((t / durationSec) * PI.toFloat()).coerceAtLeast(0f)
            val wave = sin(2f * PI.toFloat() * baseFreq * t) * 0.65f +
                sin(2f * PI.toFloat() * (baseFreq * 1.5f) * t) * 0.35f
            wave * env * 0.24f
        }
    }

    /**
     * Arpège céleste lors de la collecte d'une méditation du Fruit de l'Esprit.
     */
    fun playRelicCollected() {
        if (!soundEnabled.get()) return
        val notes = floatArrayOf(523.25f, 659.25f, 783.99f, 987.77f, 1174.66f) // C5, E5, G5, B5, D6
        playSynthesizedEffect(durationMs = 520) { t, durationSec ->
            val step = (t / durationSec * notes.size).toInt().coerceIn(0, notes.size - 1)
            val freq = notes[step]
            val localT = (t % (durationSec / notes.size))
            val env = exp(-localT * 9f) * (1f - t / durationSec * 0.3f)
            val fundamental = sin(2f * PI.toFloat() * freq * t)
            val overtone = sin(2f * PI.toFloat() * (freq * 2f) * t) * 0.35f
            (fundamental + overtone) * env * 0.36f
        }
    }

    /**
     * Accord mystique lors de la collecte d'un Fragment Bonus (🧠).
     */
    fun playBrainBonusCollected() {
        if (!soundEnabled.get()) return
        val notes = floatArrayOf(440f, 554.37f, 659.25f, 880f, 1108.73f) // A4 Major/Lydian arpeggio
        playSynthesizedEffect(durationMs = 600) { t, durationSec ->
            val step = (t / durationSec * notes.size).toInt().coerceIn(0, notes.size - 1)
            val freq = notes[step]
            val env = (1f - (t / durationSec)).coerceAtLeast(0f)
            val wave = sin(2f * PI.toFloat() * freq * t) * 0.7f +
                sin(2f * PI.toFloat() * (freq * 1.5f) * t) * 0.3f
            wave * env * 0.38f
        }
    }

    /**
     * Son énergétique futuriste lors du ramassage d'un bonus (Bouclier, Aimant, Vitesse, Arme).
     */
    fun playPowerUpCollected() {
        if (!soundEnabled.get()) return
        playSynthesizedEffect(durationMs = 340) { t, durationSec ->
            val progress = (t / durationSec).coerceIn(0f, 1f)
            val freq = 360f + progress * progress * 880f
            val env = sin(progress * PI.toFloat())
            val wave = sin(2f * PI.toFloat() * freq * t) * 0.75f +
                sin(2f * PI.toFloat() * (freq * 1.25f) * t) * 0.25f
            wave * env * 0.34f
        }
    }

    /**
     * Son cristallin poétique lorsqu'une cible (Étincelle de Lucidité, Fragment de Savoir, Souvenir) est sauvée (+10 pts).
     */
    fun playTargetSaved() {
        if (!soundEnabled.get()) return
        val notes = floatArrayOf(783.99f, 1046.50f, 1318.51f) // G5, C6, E6
        playSynthesizedEffect(durationMs = 240) { t, durationSec ->
            val step = (t / durationSec * notes.size).toInt().coerceIn(0, notes.size - 1)
            val freq = notes[step]
            val env = (1f - t / durationSec).coerceAtLeast(0f)
            val wave = sin(2f * PI.toFloat() * freq * t) * 0.7f +
                sin(2f * PI.toFloat() * (freq * 2f) * t) * 0.3f
            wave * env * 0.32f
        }
    }

    /**
     * Fanfare de vie supplémentaire (+1 Cœur toutes les 50 cibles sauvées, max 7 cœurs).
     */
    fun playExtraHeartEarned() {
        if (!soundEnabled.get()) return
        val notes = floatArrayOf(587.33f, 783.99f, 987.77f, 1174.66f, 1567.98f) // D5, G5, B5, D6, G6
        playSynthesizedEffect(durationMs = 640) { t, durationSec ->
            val step = (t / durationSec * notes.size).toInt().coerceIn(0, notes.size - 1)
            val freq = notes[step]
            val env = (1f - (t / durationSec) * 0.55f).coerceAtLeast(0f)
            val wave = sin(2f * PI.toFloat() * freq * t) * 0.65f +
                sin(2f * PI.toFloat() * (freq * 1.5f) * t) * 0.35f
            wave * env * 0.40f
        }
    }

    /**
     * Éclat fragile lorsqu'une cible (ex: bulle de Souvenir) est percutée et détruite par un astéroïde.
     */
    fun playTargetDestroyed() {
        if (!soundEnabled.get()) return
        playSynthesizedEffect(durationMs = 220) { t, durationSec ->
            val progress = (t / durationSec).coerceIn(0f, 1f)
            val freq = 640f * (1f - progress * 0.65f)
            val env = (1f - progress) * (1f - progress)
            val chime = sin(2f * PI.toFloat() * freq * t) * 0.6f
            val glass = sin(2f * PI.toFloat() * (freq * 2.4f) * t) * 0.4f
            (chime + glass) * env * 0.28f
        }
    }

    /**
     * Désintégration d'un astéroïde, drone ou mine pulsar.
     */
    fun playObstacleDestroyed() {
        if (!soundEnabled.get()) return
        playSynthesizedEffect(durationMs = 260) { t, durationSec ->
            val progress = (t / durationSec).coerceIn(0f, 1f)
            val freq = 520f * (1f - progress * 0.75f)
            val env = (1f - progress) * (1f - progress)
            val tone = sin(2f * PI.toFloat() * freq * t)
            val noise = (Random.nextFloat() * 2f - 1f) * 0.45f
            (tone * 0.65f + noise * 0.35f) * env * 0.35f
        }
    }

    /**
     * Impact sur le bouclier lors d'une collision avec un obstacle.
     */
    fun playShieldHit() {
        if (!soundEnabled.get()) return
        playSynthesizedEffect(durationMs = 320) { t, durationSec ->
            val progress = (t / durationSec).coerceIn(0f, 1f)
            val freq = 190f * (1f - progress * 0.55f)
            val env = (1f - progress)
            val sub = sin(2f * PI.toFloat() * freq * t)
            val grit = sin(2f * PI.toFloat() * (freq * 2.7f) * t) * 0.4f
            (sub + grit) * env * 0.45f
        }
    }

    /**
     * Fanfare lumineuse lorsque le niveau est complété.
     */
    fun playLevelComplete() {
        if (!soundEnabled.get()) return
        val notes = floatArrayOf(523.25f, 659.25f, 783.99f, 1046.50f, 1318.51f, 1567.98f)
        playSynthesizedEffect(durationMs = 780) { t, durationSec ->
            val idx = ((t / durationSec) * notes.size).toInt().coerceIn(0, notes.size - 1)
            val freq = notes[idx]
            val env = (1f - (t / durationSec) * 0.65f).coerceAtLeast(0f)
            val chord = sin(2f * PI.toFloat() * freq * t) * 0.6f +
                sin(2f * PI.toFloat() * (freq * 0.5f) * t) * 0.25f +
                sin(2f * PI.toFloat() * (freq * 1.5f) * t) * 0.15f
            chord * env * 0.40f
        }
    }

    /**
     * Cadence descendante lors de la perte de tous les boucliers (Game Over).
     */
    fun playGameOver() {
        if (!soundEnabled.get()) return
        val notes = floatArrayOf(392.00f, 349.23f, 311.13f, 261.63f)
        playSynthesizedEffect(durationMs = 680) { t, durationSec ->
            val idx = ((t / durationSec) * notes.size).toInt().coerceIn(0, notes.size - 1)
            val freq = notes[idx]
            val env = (1f - (t / durationSec) * 0.8f).coerceAtLeast(0f)
            sin(2f * PI.toFloat() * freq * t) * env * 0.38f
        }
    }

    /**
     * Petit retour sonore cristallin lors d'une action importante ou d'un ancrage de pensée.
     */
    fun playUiTap() {
        if (!soundEnabled.get()) return
        playSynthesizedEffect(durationMs = 90) { t, durationSec ->
            val env = (1f - t / durationSec).coerceAtLeast(0f)
            sin(2f * PI.toFloat() * 880f * t) * env * 0.25f
        }
    }

    /**
     * Tic-tac numérique rapide pour le déroulement animé des points en fin de mission.
     */
    fun playScoreTick(isPositive: Boolean) {
        if (!soundEnabled.get()) return
        val now = System.currentTimeMillis()
        if (now - lastTickSoundTimeMs < 48L) return
        lastTickSoundTimeMs = now

        val freq = if (isPositive) 980f else 340f
        playSynthesizedEffect(durationMs = 36) { t, durationSec ->
            val env = (1f - t / durationSec).coerceAtLeast(0f)
            val wave = sin(2f * PI.toFloat() * freq * t) * 0.75f +
                sin(2f * PI.toFloat() * (freq * 1.5f) * t) * 0.25f
            wave * env * 0.22f
        }
    }

    /**
     * Effet sonore de distorsion toroïdale lorsque la sphère traverse la bordure rouge de la carte.
     */
    fun playBorderWarp() {
        if (!soundEnabled.get()) return
        playSynthesizedEffect(durationMs = 240) { t, durationSec ->
            val p = (t / durationSec).coerceIn(0f, 1f)
            // Glissando dimensionnel ascendant puis cristallin (320 Hz -> 860 Hz)
            val sweepFreq = 320f + 540f * sin(p * PI.toFloat() * 0.85f)
            val shimmer = sin(2f * PI.toFloat() * 24f * t) * 0.25f + 0.75f
            val env = sin(p * PI.toFloat()).coerceAtLeast(0f)
            val wave = sin(2f * PI.toFloat() * sweepFreq * t) * 0.65f +
                sin(2f * PI.toFloat() * (sweepFreq * 1.5f) * t) * 0.35f
            wave * shimmer * env * 0.24f
        }
    }

    private fun playSynthesizedEffect(
        durationMs: Int,
        generator: (tSec: Float, durationSec: Float) -> Float
    ) {
        if (!canPlayAudio()) return
        if (activeSfxCount.get() >= MAX_CONCURRENT_SFX) return
        activeSfxCount.incrementAndGet()
        lateinit var job: Job
        job = scope.launch {
            var track: AudioTrack? = null
            try {
                if (!canPlayAudio()) return@launch
                val totalSamples = (SAMPLE_RATE * durationMs) / 1000
                if (totalSamples <= 0) return@launch
                val durationSec = durationMs / 1000f
                val pcm = ShortArray(totalSamples)

                for (i in 0 until totalSamples) {
                    val t = i.toFloat() / SAMPLE_RATE
                    val sample = generator(t, durationSec).coerceIn(-0.95f, 0.95f)
                    pcm[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                }

                if (!canPlayAudio()) return@launch

                val bufferBytes = totalSamples * 2
                val builtTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferBytes.coerceAtLeast(
                        AudioTrack.getMinBufferSize(
                            SAMPLE_RATE,
                            AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT
                        )
                    ))
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track = builtTrack
                activeSfxTracks.add(builtTrack)
                if (!canPlayAudio()) return@launch

                builtTrack.write(pcm, 0, totalSamples)
                builtTrack.play()
                kotlinx.coroutines.delay(durationMs.toLong() + 40L)
            } catch (_: Throwable) {
                // Safe fallback on test JVM or devices without audio output
            } finally {
                activeSfxJobs.remove(job)
                if (track != null) {
                    activeSfxTracks.remove(track)
                }
                activeSfxCount.decrementAndGet()
                try {
                    track?.pause()
                    track?.flush()
                    track?.stop()
                } catch (_: Throwable) {}
                try {
                    track?.release()
                } catch (_: Throwable) {}
            }
        }
        activeSfxJobs.add(job)
    }
}
