// ============================================================================
// NOÔRBIS — SYNTHÉTISEUR AUDIO PROCÉDURAL (14 THÈMES COSMIQUES & EFFETS SFX)
// ============================================================================

window.NoorbisAudio = (function () {
  const MUSIC_THEME_NAMES = [
    "Harpe Solaire (Arpèges Lydiens)",
    "Pulsation Synthwave (Odyssée)",
    "Carillon de Cristal (Pléiades)",
    "Bols Tibétains d'Orion (Méditatif)",
    "Marimba Solaire (Rythme Calme)",
    "Chœur d'Andromède (Nappes Célestes)",
    "Électro Boréale (Dorien Clair)",
    "Piano Nocturne (Valse 3/4)",
    "Oasis Stellaire (Mode Hijaz)",
    "Symphonie des Séraphins (Majeur 7)",
    "Koto Zen d'Émeraude (Pentatonique)",
    "Groove Stellaire (Basse & Étoiles)",
    "Écho Polaire de Saphir (Cristallin)",
    "Fanfare de Plénitude (Triomphe)"
  ];

  const THEME_SCALES = [
    [261.63, 293.66, 329.63, 369.99, 392.00, 440.00, 493.88, 523.25], // Lydian
    [220.00, 261.63, 293.66, 329.63, 392.00, 440.00, 523.25, 587.33], // Minor Pentatonic
    [293.66, 329.63, 392.00, 440.00, 523.25, 587.33, 659.25, 783.99], // Crystal
    [196.00, 220.00, 261.63, 293.66, 392.00, 440.00, 523.25, 587.33], // Tibetan
    [261.63, 293.66, 329.63, 392.00, 440.00, 523.25, 587.33, 659.25], // Major Pentatonic
    [246.94, 293.66, 329.63, 369.99, 440.00, 493.88, 587.33, 659.25], // Andromeda
    [293.66, 329.63, 349.23, 392.00, 440.00, 493.88, 523.25, 587.33], // Dorian
    [261.63, 311.13, 392.00, 466.16, 523.25, 622.25, 783.99, 932.33], // Nocturne
    [220.00, 233.08, 277.18, 293.66, 329.63, 349.23, 415.30, 440.00], // Hijaz
    [261.63, 329.63, 392.00, 493.88, 523.25, 659.25, 783.99, 987.77], // Maj7
    [293.66, 311.13, 392.00, 440.00, 466.16, 587.33, 622.25, 783.99], // Hirajoshi Zen
    [196.00, 233.08, 261.63, 293.66, 349.23, 392.00, 466.16, 523.25], // Groove
    [329.63, 369.99, 440.00, 493.88, 587.33, 659.25, 739.99, 880.00], // Polar
    [261.63, 329.63, 392.00, 523.25, 659.25, 783.99, 1046.50, 1318.51] // Fanfare
  ];

  let ctx = null;
  let sfxEnabled = true;
  let musicThemeOverride = -1; // -2 = Off, -1 = Auto, 0..13 = Manual
  let currentLevelNumber = 1;
  let musicTimer = null;
  let stepIndex = 0;

  function ensureContext() {
    if (!ctx) {
      const AudioCtx = window.AudioContext || window.webkitAudioContext;
      if (AudioCtx) ctx = new AudioCtx();
    }
    if (ctx && ctx.state === "suspended") {
      ctx.resume().catch(() => {});
    }
    return ctx;
  }

  function getActiveThemeIndex() {
    if (musicThemeOverride === -2) return -1;
    if (musicThemeOverride >= 0 && musicThemeOverride < MUSIC_THEME_NAMES.length) {
      return musicThemeOverride;
    }
    return Math.max(0, currentLevelNumber - 1) % MUSIC_THEME_NAMES.length;
  }

  function playTone(freq, durationSec, type = "sine", maxGain = 0.08, delaySec = 0) {
    const audioCtx = ensureContext();
    if (!audioCtx) return;
    const now = audioCtx.currentTime + delaySec;
    const osc = audioCtx.createOscillator();
    const gain = audioCtx.createGain();
    osc.type = type;
    osc.frequency.setValueAtTime(freq, now);

    gain.gain.setValueAtTime(0.0001, now);
    gain.gain.exponentialRampToValueAtTime(maxGain, now + 0.02);
    gain.gain.exponentialRampToValueAtTime(0.0001, now + durationSec);

    osc.connect(gain);
    gain.connect(audioCtx.destination);
    osc.start(now);
    osc.stop(now + durationSec + 0.03);
  }

  function startMusicLoop() {
    stopMusicLoop();
    const themeIdx = getActiveThemeIndex();
    if (themeIdx < 0) return;

    musicTimer = setInterval(() => {
      const activeIdx = getActiveThemeIndex();
      if (activeIdx < 0 || !ctx || ctx.state !== "running") return;
      const scale = THEME_SCALES[activeIdx % THEME_SCALES.length];
      const note = scale[(stepIndex * 3 + (stepIndex % 2)) % scale.length];
      const waveTypes = ["sine", "triangle", "sine", "sine"];
      const wave = waveTypes[activeIdx % waveTypes.length];
      playTone(note, 0.55, wave, 0.032, 0);
      if (stepIndex % 4 === 0) {
        playTone(scale[0] * 0.5, 1.1, "sine", 0.024, 0);
      }
      stepIndex++;
    }, 420);
  }

  function stopMusicLoop() {
    if (musicTimer) {
      clearInterval(musicTimer);
      musicTimer = null;
    }
  }

  function setConfig(newSfxEnabled, newMusicThemeOverride, levelNumber) {
    sfxEnabled = Boolean(newSfxEnabled);
    musicThemeOverride = Number(newMusicThemeOverride);
    if (levelNumber) currentLevelNumber = levelNumber;
    if (musicThemeOverride === -2) {
      stopMusicLoop();
    } else if (!musicTimer) {
      startMusicLoop();
    }
  }

  function playClick() {
    ensureContext();
    if (!sfxEnabled) return;
    playTone(660, 0.08, "sine", 0.05, 0);
  }

  function playRelicCollected() {
    ensureContext();
    if (!sfxEnabled) return;
    playTone(523.25, 0.18, "triangle", 0.09, 0);
    playTone(659.25, 0.18, "triangle", 0.09, 0.08);
    playTone(783.99, 0.25, "triangle", 0.10, 0.16);
    playTone(1046.50, 0.40, "sine", 0.11, 0.24);
  }

  function playTargetSaved() {
    ensureContext();
    if (!sfxEnabled) return;
    playTone(587.33, 0.14, "sine", 0.08, 0);
    playTone(880.00, 0.28, "sine", 0.09, 0.09);
  }

  function playScoreTick(isPositive) {
    ensureContext();
    if (!sfxEnabled) return;
    if (isPositive) {
      playTone(783.99, 0.10, "sine", 0.07, 0);
      playTone(1046.50, 0.14, "sine", 0.08, 0.05);
    } else {
      playTone(246.94, 0.16, "sawtooth", 0.06, 0);
      playTone(185.00, 0.22, "sawtooth", 0.06, 0.07);
    }
  }

  function playObstacleHit() {
    ensureContext();
    if (!sfxEnabled) return;
    playTone(150, 0.25, "sawtooth", 0.11, 0);
    playTone(110, 0.30, "triangle", 0.10, 0.06);
  }

  function playPowerUp() {
    ensureContext();
    if (!sfxEnabled) return;
    playTone(440, 0.12, "sine", 0.08, 0);
    playTone(554.37, 0.12, "sine", 0.08, 0.07);
    playTone(659.25, 0.24, "sine", 0.09, 0.14);
  }

  function playVirtueProjectile() {
    ensureContext();
    if (!sfxEnabled) return;
    playTone(698.46, 0.15, "sine", 0.08, 0);
    playTone(880.00, 0.25, "triangle", 0.08, 0.06);
  }

  function playNeuronPurified(affinity) {
    ensureContext();
    if (!sfxEnabled) return;
    if (affinity >= 1.0) {
      playTone(523.25, 0.15, "sine", 0.09, 0);
      playTone(783.99, 0.18, "sine", 0.09, 0.08);
      playTone(1046.50, 0.35, "triangle", 0.10, 0.16);
    } else if (affinity >= 0.5) {
      playTone(440.00, 0.15, "sine", 0.08, 0);
      playTone(587.33, 0.22, "sine", 0.08, 0.08);
    } else {
      playTone(220.00, 0.18, "triangle", 0.07, 0);
    }
  }

  function playBorderWarp() {
    ensureContext();
    if (!sfxEnabled) return;
    playTone(330.00, 0.12, "sine", 0.06, 0);
    playTone(493.88, 0.18, "sine", 0.06, 0.06);
  }

  function playLevelComplete() {
    ensureContext();
    if (!sfxEnabled) return;
    [523.25, 659.25, 783.99, 1046.50, 1318.51].forEach((f, idx) => {
      playTone(f, 0.28, "triangle", 0.09, idx * 0.09);
    });
  }

  function playGameOver() {
    ensureContext();
    if (!sfxEnabled) return;
    [392.00, 349.23, 311.13, 261.63].forEach((f, idx) => {
      playTone(f, 0.30, "sawtooth", 0.07, idx * 0.12);
    });
  }

  return {
    MUSIC_THEME_NAMES,
    ensureContext,
    setConfig,
    startMusicLoop,
    stopMusicLoop,
    getActiveThemeIndex,
    playClick,
    playRelicCollected,
    playTargetSaved,
    playScoreTick,
    playObstacleHit,
    playPowerUp,
    playVirtueProjectile,
    playNeuronPurified,
    playBorderWarp,
    playLevelComplete,
    playGameOver
  };
})();
