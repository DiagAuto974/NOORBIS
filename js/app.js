// ============================================================================
// NOÔRBIS — CONTRÔLEUR D'APPLICATION WEB (HANGAR, JEU, INVENTAIRE, GLOSSAIRE, EXPORT)
// ============================================================================

(function () {
  const C = window.NoorbisCatalog;
  const A = window.NoorbisAudio;
  const G = window.NoorbisGame;

  const STORAGE_KEY = "noorbis_web_state_v2";

  const appState = {
    currentTab: "hangar",
    selectedColorId: "cyan",
    unlockedLevel: 1,
    totalScore: 0,
    positiveThoughtsCount: 0,
    maxShields: 3,
    collectedItems: [], // [{ id, fruit, situation, reference, content, collectedAt }]
    levelRecords: {},   // { [level]: { isCompleted, bestScore, collectedCount, brainBonusCollected } }
    claimedShareIds: [],
    hangarFilter: "ALL", // ALL, MILESTONES, UNCOMPLETED
    inventorySearch: "",
    inventoryFilter: null, // null, "__BRAIN__", or fruit name
    glossaryCategory: null, // null, "OBSTACLE", "TARGET", "RELIC", "BONUS"
    settings: {
      sfxEnabled: true,
      musicThemeOverride: -1, // -2 = Off, -1 = Auto, 0..13 = Manual
      inventorySortAscending: false,
      joystickEnabled: true,
      directTextEnabled: true,
      showGridEnabled: false,
      showMiniMapEnabled: true
    }
  };

  function deduplicateCollectedItems() {
    if (!Array.isArray(appState.collectedItems)) {
      appState.collectedItems = [];
      return;
    }
    const seenIds = new Set();
    const uniqueList = [];
    for (let i = 0; i < appState.collectedItems.length; i++) {
      const item = appState.collectedItems[i];
      if (item && typeof item.id === "number" && !seenIds.has(item.id)) {
        seenIds.add(item.id);
        uniqueList.push(item);
      }
    }
    appState.collectedItems = uniqueList;
  }

  function loadState() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (raw) {
        const parsed = JSON.parse(raw);
        Object.assign(appState, parsed);
        if (parsed.settings) {
          appState.settings = Object.assign({}, appState.settings, parsed.settings);
        }
      }
    } catch (_) {}
    deduplicateCollectedItems();
    appState.maxShields = Math.min(7, 3 + Math.floor((appState.positiveThoughtsCount || 0) / 50));
    if (C && typeof C.syncCollectedFragments === "function") {
      C.syncCollectedFragments(appState.collectedItems.map(i => i.id));
    }
  }

  function saveState() {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(appState));
    } catch (_) {}
  }

  function showToast(msg) {
    const container = document.getElementById("toast-container");
    if (!container) return;
    const el = document.createElement("div");
    el.className = "toast-msg";
    el.textContent = msg;
    container.appendChild(el);
    setTimeout(() => {
      el.remove();
    }, 3200);
  }

  function getSphereColor() {
    return C.AvailableSphereColors.find(c => c.id === appState.selectedColorId) || C.AvailableSphereColors[0];
  }

  function getCollectedIdsSet() {
    return new Set(appState.collectedItems.map(i => i.id));
  }

  const appCallbacks = {
    getSettings: () => appState.settings,
    getSphereColor,
    getCollectedIdsSet,
    onCollectMeditation: (rawMed) => {
      if (!rawMed || typeof rawMed.id !== "number") return;
      const med = C && typeof C.enrichMeditation === "function" ? C.enrichMeditation(rawMed) : rawMed;
      if (C && typeof C.markFragmentCollected === "function") {
        C.markFragmentCollected(med.id);
      }
      const existingIdx = appState.collectedItems.findIndex(x => x.id === med.id);
      if (existingIdx === -1) {
        appState.collectedItems.push({
          ...med,
          collectedAt: Date.now()
        });
        saveState();
      } else {
        appState.collectedItems[existingIdx] = {
          ...appState.collectedItems[existingIdx],
          ...med
        };
        saveState();
      }
      if (appState.settings.directTextEnabled !== false) {
        openDirectMeditationModal(med, true);
      }
    },
    onPositiveThoughtEarned: (delta) => {
      const oldMax = appState.maxShields;
      appState.positiveThoughtsCount = (appState.positiveThoughtsCount || 0) + delta;
      appState.maxShields = Math.min(7, 3 + Math.floor(appState.positiveThoughtsCount / 50));
      if (appState.maxShields > oldMax) {
        G.state.maxShields = appState.maxShields;
        G.state.shields = Math.min(G.state.maxShields, G.state.shields + 1);
        showToast(`💖 Palier de 50 Pensées atteint ! Cœurs max : ${appState.maxShields}/7`);
      }
      saveState();
    },
    onTriggerEndMissionTally: () => {
      openEndMissionTallyModal();
    }
  };

  // ==========================================================================
  // NAVIGATION & TABS
  // ==========================================================================
  function switchTab(tabId) {
    A.playClick();
    if (tabId === "settings") {
      if (!G.state.isLevelComplete && !G.state.isGameOver && !G.state.isMissionBriefingOpen) {
        G.state.isPaused = true;
        syncGameModals();
      }
      openSettingsModal();
      return;
    }
    if (tabId !== "game" && appState.currentTab === "game" && !G.state.isLevelComplete && !G.state.isGameOver) {
      G.state.isPaused = true;
    }
    dismissLaunchModal();
    appState.currentTab = tabId;
    document.querySelectorAll(".nav-item").forEach(btn => {
      btn.classList.toggle("active", btn.dataset.tab === tabId);
    });
    document.querySelectorAll(".screen-view").forEach(view => {
      view.classList.toggle("active", view.id === `view-${tabId}`);
    });

    if (tabId === "hangar") renderHangar();
    if (tabId === "inventory") renderInventory();
    if (tabId === "game") {
      updateGameHUD();
      syncGameModals();
    }
  }

  // ==========================================================================
  // HANGAR VIEW
  // ==========================================================================
  function renderHangar() {
    const sphereColor = getSphereColor();
    const brainCount = appState.collectedItems.filter(i => i.id >= 1001 && i.id <= 1072).length;
    const fruitMedCount = appState.collectedItems.filter(i => i.id < 1001).length;
    const totalCollectedCount = appState.collectedItems.length;

    const heroCard = document.getElementById("hero-banner-card");
    if (heroCard) {
      heroCard.style.borderColor = `${sphereColor.primaryHex}99`;
      heroCard.style.boxShadow = `0 14px 40px rgba(0, 0, 0, 0.65), 0 0 26px ${sphereColor.primaryHex}28`;
    }

    const statScore = document.getElementById("stat-total-score");
    const statMeds = document.getElementById("stat-meditations");
    const statBrain = document.getElementById("stat-brain");
    const statLevel = document.getElementById("stat-level");

    if (statScore) statScore.textContent = appState.totalScore;
    if (statMeds) statMeds.textContent = `${fruitMedCount} / 810`;
    if (statBrain) statBrain.textContent = `${brainCount} / 72`;
    if (statLevel) statLevel.textContent = `#${appState.unlockedLevel} / 135`;

    const lblQuickPlay = document.getElementById("lbl-hero-quick-play");
    if (lblQuickPlay) lblQuickPlay.textContent = `Lancer Niveau ${appState.unlockedLevel}`;
    const btnQuickPlay = document.getElementById("btn-hero-quick-play");
    if (btnQuickPlay) {
      btnQuickPlay.style.background = sphereColor.primaryHex;
      btnQuickPlay.style.setProperty("--touch-glow", sphereColor.primaryHex);
    }

    const lblHeroInv = document.getElementById("lbl-hero-inventory");
    if (lblHeroInv) lblHeroInv.textContent = `Inventaire (${totalCollectedCount})`;

    const navInvBadge = document.getElementById("nav-inv-badge");
    if (navInvBadge) navInvBadge.textContent = `${totalCollectedCount}`;

    const colorLabel = document.getElementById("active-sphere-color-label");
    if (colorLabel) {
      colorLabel.textContent = `Couleur : ${sphereColor.name} • Cœurs : ${appState.maxShields}/7 (${appState.positiveThoughtsCount % 50}/50 ✨)`;
    }

    // Render Color Picker
    const colorRow = document.getElementById("color-picker-row");
    if (colorRow) {
      colorRow.innerHTML = "";
      C.AvailableSphereColors.forEach(c => {
        const isSel = c.id === appState.selectedColorId;
        const card = document.createElement("div");
        card.className = `color-card astro-touch ${isSel ? "selected" : ""}`;
        card.style.setProperty("--touch-glow", c.primaryHex);
        if (isSel) card.style.borderColor = c.primaryHex;
        card.innerHTML = `
          <div class="color-swatch" style="background:radial-gradient(circle at 32% 32%, #FFFFFF 0%, ${c.primaryHex} 55%, ${c.glowHex} 100%);box-shadow:0 0 16px ${c.primaryHex}AA">
            ${isSel ? "✓" : ""}
          </div>
          <div class="color-card-name" style="${isSel ? `color:${c.primaryHex}` : ""}">${c.name}</div>
        `;
        card.addEventListener("click", () => {
          A.playClick();
          appState.selectedColorId = c.id;
          saveState();
          renderHangar();
        });
        colorRow.appendChild(card);
      });
    }

    // Hangar filter chips
    document.querySelectorAll("[data-hangar-filter]").forEach(btn => {
      btn.classList.toggle("active", btn.dataset.hangarFilter === appState.hangarFilter);
    });

    // Render 135 Levels
    const levelsList = document.getElementById("levels-list");
    if (!levelsList) return;
    levelsList.innerHTML = "";

    const milestoneLevels = new Set([1, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100, 105, 110, 115, 120, 125, 130, 135]);
    const collectedIds = getCollectedIdsSet();

    C.getAllSectors().forEach(sector => {
      const rec = appState.levelRecords[sector.levelNumber] || null;
      const isCompleted = Boolean(rec && rec.isCompleted);
      const isUnlocked = sector.levelNumber <= appState.unlockedLevel;
      const isCurrent = sector.levelNumber === appState.unlockedLevel;
      const isMilestone = milestoneLevels.has(sector.levelNumber);

      if (appState.hangarFilter === "MILESTONES" && !isMilestone) return;
      if (appState.hangarFilter === "UNCOMPLETED" && isCompleted) return;

      const brainFrag = C.getBrainBonusForLevel(sector.levelNumber);
      const isBrainDone = collectedIds.has(brainFrag.id) || Boolean(rec && rec.brainBonusCollected);

      const card = document.createElement("div");
      card.className = `level-card ${isUnlocked ? "unlocked astro-touch" : "locked"} ${isCurrent ? "current" : ""} ${isCompleted ? "completed" : ""}`;
      if (isUnlocked) {
        card.style.setProperty("--touch-glow", isCompleted ? "#34D399" : sphereColor.primaryHex);
      }

      const fruitsHtml = sector.fruits.map(f => {
        const hasCollectedFruit = isCompleted || appState.collectedItems.some(item => item.fruit === f);
        return `<span class="mini-tag ${hasCollectedFruit ? "done" : ""}" data-sector-fruit="${f}" style="${hasCollectedFruit ? "cursor:pointer" : ""}" title="${hasCollectedFruit ? "Cliquez pour ouvrir ce fragment" : ""}">${hasCollectedFruit ? "✓ " : "✦ "}${f}</span>`;
      }).join("");

      const brainHtml = `<span class="mini-tag ${isBrainDone ? "done" : "brain"}" data-sector-brain="${isBrainDone ? brainFrag.id : ""}" style="${isBrainDone ? "cursor:pointer" : ""}" title="${isBrainDone ? "Cliquez pour ouvrir ce fragment bonus" : ""}">🧠 ${isBrainDone ? "✓ Bonus recueilli" : "Bonus"}</span>`;
      const milestoneHtml = isMilestone ? `<span class="mini-tag milestone">★ Palier Nouveauté</span>` : "";

      const badgeStyle = isUnlocked && !isCompleted
        ? `background:${sphereColor.primaryHex}26;border-color:${sphereColor.primaryHex};color:${sphereColor.primaryHex}`
        : "";
      const playBtnStyle = `background:${sphereColor.primaryHex};--touch-glow:${sphereColor.primaryHex}`;

      card.innerHTML = `
        <div class="level-badge" style="${badgeStyle}">
          ${isCompleted ? "✓" : isUnlocked ? `${sector.levelNumber}` : "🔒"}
        </div>
        <div class="level-info">
          <div class="level-title-row">
            <span class="level-name">Niveau ${sector.levelNumber} • ${sector.name}</span>
            ${milestoneHtml}
          </div>
          <div class="level-sub">
            ${sector.subtitle}${rec && rec.bestScore > 0 ? ` • ★ ${rec.bestScore} pts` : ""}
          </div>
          <div class="level-fruits-row">
            ${fruitsHtml}
            ${brainHtml}
          </div>
        </div>
        ${isUnlocked ? `<div class="level-play-icon astro-touch" style="${playBtnStyle}">▶</div>` : ""}
      `;

      // Permettre d'ouvrir directement les fragments récupérés d'un secteur harmonisé en cliquant sur leurs badges
      card.querySelectorAll("[data-sector-fruit]").forEach(tagEl => {
        const fruitName = tagEl.getAttribute("data-sector-fruit");
        const hasCollectedFruit = isCompleted || appState.collectedItems.some(item => item.fruit === fruitName);
        if (hasCollectedFruit) {
          tagEl.addEventListener("click", (e) => {
            e.stopPropagation();
            A.playClick();
            const found = appState.collectedItems.find(
              item => item.fruit === fruitName && item.situation === sector.subtitle
            ) || appState.collectedItems.find(
              item => item.fruit === fruitName
            ) || C.getNextMeditationForFruit(fruitName, null, sector.levelNumber);
            if (found) {
              openDirectMeditationModal(found, false);
            }
          });
        }
      });

      const brainTagEl = card.querySelector("[data-sector-brain]");
      if (brainTagEl && isBrainDone) {
        brainTagEl.addEventListener("click", (e) => {
          e.stopPropagation();
          A.playClick();
          const foundBrain = appState.collectedItems.find(item => item.id === brainFrag.id) || C.getMeditationById(brainFrag.id);
          if (foundBrain) {
            openDirectMeditationModal(foundBrain, false);
          }
        });
      }

      if (isUnlocked) {
        card.addEventListener("click", () => {
          startLevelAndSwitch(sector.levelNumber);
        });
      }
      levelsList.appendChild(card);
    });
  }

  function dismissLaunchModal() {
    const launchModal = document.getElementById("modal-launch-start");
    if (launchModal) {
      launchModal.classList.remove("open");
      launchModal.style.display = "none";
      launchModal.setAttribute("aria-hidden", "true");
    }
  }

  let shouldMaintainFullscreen = false;

  function enterFullscreen() {
    shouldMaintainFullscreen = true;
    try {
      const docEl = document.documentElement;
      if (!document.fullscreenElement && !document.webkitFullscreenElement) {
        if (docEl && typeof docEl.requestFullscreen === "function") {
          const p = docEl.requestFullscreen();
          if (p && typeof p.catch === "function") p.catch(() => {});
        } else if (docEl && typeof docEl.webkitRequestFullscreen === "function") {
          docEl.webkitRequestFullscreen();
        }
      }
    } catch (_) {
      // Ignorer si l'iframe ou le navigateur restreint le plein écran
    }
  }

  function startLevelAndSwitch(levelNumber, startImmediately = false) {
    dismissLaunchModal();
    enterFullscreen();
    A.ensureContext();
    A.playClick();
    A.setConfig(appState.settings.sfxEnabled, appState.settings.musicThemeOverride, levelNumber);
    try {
      G.initLevel(levelNumber, appState, startImmediately);
    } catch (err) {
      console.error("Erreur initLevel:", err);
    }
    if (startImmediately) {
      G.state.isMissionBriefingOpen = false;
      G.state.isPaused = false;
    }
    switchTab("game");
    syncGameModals();
    requestAnimationFrame(() => {
      window.dispatchEvent(new Event("resize"));
    });
  }

  // ==========================================================================
  // SPACE GAME HUD & OVERLAYS
  // ==========================================================================
  let lastShieldsHtml = "";
  let lastBannersHtml = "";

  function updateGameHUD() {
    const s = G.state;
    const titleEl = document.getElementById("hud-sector-title");
    if (titleEl && titleEl.textContent !== s.sector.name) {
      titleEl.textContent = s.sector.name;
    }

    const shieldsEl = document.getElementById("hud-shields-row");
    if (shieldsEl) {
      let hearts = "";
      for (let i = 0; i < s.maxShields; i++) {
        hearts += `<span class="heart-icon" style="opacity:${i < s.shields ? 1 : 0.25}">${i < s.shields ? "❤️" : "🖤"}</span>`;
      }
      const remainingTargets = Array.isArray(s.rescueTargets)
        ? s.rescueTargets.length
        : Math.max(0, s.totalTargetsCount - s.targetsSavedCount - s.targetsDestroyedCount);
      const remainingNeurons = Array.isArray(s.infectedNeurons)
        ? s.infectedNeurons.filter(n => !n.isPurified).length
        : Math.max(0, s.totalNeuronsCount - s.neuronsPurifiedCount);

      const nextShieldsHtml = `
        ${hearts}
        <span class="hud-badge" style="background:rgba(56,189,248,0.18);color:#38BDF8">
          ${remainingTargets > 0 ? `✨ ${remainingTargets} à sauver` : `✓ ${s.targetsSavedCount}/${s.totalTargetsCount}`}
        </span>
        <span class="hud-badge" style="background:rgba(168,85,247,0.18);color:#C084FC">
          ${remainingNeurons > 0 ? `🦠 ${remainingNeurons} neurones` : "🧠 Purifiés"}
        </span>
        <span class="hud-badge" style="background:rgba(16,185,129,0.18);color:#10B981;cursor:pointer" id="hud-energy-chip" title="Cliquez pour projeter un remède">
          ⚡ ${s.purificationEnergy} pts
        </span>
      `;
      if (nextShieldsHtml !== lastShieldsHtml) {
        lastShieldsHtml = nextShieldsHtml;
        shieldsEl.innerHTML = nextShieldsHtml;
        const energyChip = document.getElementById("hud-energy-chip");
        if (energyChip) {
          energyChip.onclick = () => {
            if (!G.canOpenVirtueMenu()) {
              showToast("Approchez-vous d'un neurone infecté dont la vertu se trouve dans la liste !");
              return;
            }
            if (G.tryOpenVirtueMenu()) {
              syncGameModals();
            }
          };
        }
      }
    }

    const relicsVal = document.getElementById("hud-relics-val");
    if (relicsVal) {
      const nextRelicsText = `${s.collectedCount}/3 ${s.isBrainBonusCollected ? "+ 🧠✓" : "+ 🧠"}`;
      if (relicsVal.textContent !== nextRelicsText) {
        relicsVal.textContent = nextRelicsText;
      }
    }

    const scoreVal = document.getElementById("hud-score-val");
    if (scoreVal) {
      const nextScoreText = String(s.score);
      if (scoreVal.textContent !== nextScoreText) {
        scoreVal.textContent = nextScoreText;
      }
    }

    // Active status banners (Sanctuary, Mire, Obscurantism, Active PowerUps)
    const bannerContainer = document.getElementById("hud-banners");
    if (bannerContainer) {
      const banners = [];
      if (s.activeSanctuaryFruit) {
        banners.push(`<div class="hud-sub-banner" style="color:#10B981;border-color:#10B981">🕊️ Sanctuaire : ${s.activeSanctuaryFruit} (${Math.round(s.sanctuaryProgress * 100)}%) — Zone Protégée</div>`);
      }
      if (s.peaceAuraTimer > 0) {
        banners.push(`<div class="hud-sub-banner" style="color:#10B981;border-color:#10B981">🛡️ Pensée Ancrée : Aura de Paix (${Math.ceil(s.peaceAuraTimer)}s)</div>`);
      }
      if (s.isInMentalMire) {
        banners.push(`<div class="hud-sub-banner" style="color:#A3E635;border-color:#A3E635">🕸️ Marécage Mental : Vitesse réduite</div>`);
      }
      if (G.canOpenVirtueMenu()) {
        banners.push(`<div class="hud-sub-banner" style="color:#00E5FF;border-color:#00E5FF;cursor:pointer" id="banner-open-remedy">✨ Neurone corrompu proche : Touchez le neurone ou cliquez ici pour projeter une Vertu (0%, 50% ou 100%)</div>`);
      }
      const nextBannersHtml = banners.join("");
      if (nextBannersHtml !== lastBannersHtml) {
        lastBannersHtml = nextBannersHtml;
        bannerContainer.innerHTML = nextBannersHtml;
        const btnRem = document.getElementById("banner-open-remedy");
        if (btnRem) {
          btnRem.onclick = () => {
            if (G.tryOpenVirtueMenu()) {
              syncGameModals();
            }
          };
        }
      }
    }
  }

  function syncGameModals() {
    const s = G.state;
    updateGameHUD();

    // 1. Mission Start Briefing Modal
    const briefingModal = document.getElementById("modal-mission-briefing");
    if (briefingModal) {
      briefingModal.classList.toggle("open", Boolean(s.isMissionBriefingOpen));
      if (s.isMissionBriefingOpen) {
        document.getElementById("briefing-sector-title").textContent = `${s.sector.name} — ${s.sector.subtitle}`;
        const listEl = document.getElementById("briefing-unlocks-list");
        listEl.innerHTML = s.newUnlockedElements.map(u => `
          <div class="glossary-item-card" style="border-color:${u.color}55">
            <div class="glossary-preview-box" style="font-size:24px;border-color:${u.color}">${u.emoji}</div>
            <div>
              <div style="font-size:11px;font-weight:700;color:${u.color}">${u.category}</div>
              <div style="font-size:14px;font-weight:700;color:#F8FAFC;margin:2px 0">${u.title}</div>
              <div style="font-size:12.5px;color:#94A3B8;line-height:1.45">${u.desc}</div>
            </div>
          </div>
        `).join("");
      }
    }

    // 2. Virtue Remedy Modal (Roue circulaire épurée, s'ouvre seulement si la vertu se trouve dans la liste)
    const virtueModal = document.getElementById("modal-virtue-remedy");
    if (virtueModal) {
      if (s.isVirtueMenuOpen && !G.canOpenVirtueMenu()) {
        s.isVirtueMenuOpen = false;
      }
      virtueModal.classList.toggle("open", Boolean(s.isVirtueMenuOpen));
      if (s.isVirtueMenuOpen) {
        const neuron = s.infectedNeurons.find(n => n.id === s.nearbyTargetNeuronId && !n.isPurified) ||
                       s.infectedNeurons.find(n => !n.isPurified);
        const symptomEl = document.getElementById("virtue-symptom-box");
        if (neuron) {
          const corr = C.NeuronCorruptionTypes[neuron.corruptionType];
          const progPct = Math.round((neuron.purificationProgress || 0) * 100);
          symptomEl.style.borderColor = corr.corruptedColorHex;
          symptomEl.innerHTML = `
            <div style="font-size:20px;line-height:1">🦠</div>
            <div style="font-weight:800;color:${corr.corruptedColorHex};font-size:11px;line-height:1.15">${corr.symptomLabel}</div>
            <span class="mini-tag" style="background:rgba(56,189,248,0.18);color:#38BDF8;font-size:10px;padding:2px 6px">${progPct}%</span>
          `;
        } else {
          symptomEl.style.borderColor = "#10B981";
          symptomEl.innerHTML = `
            <div style="font-size:20px;line-height:1">⚖️</div>
            <div style="font-weight:800;color:#10B981;font-size:11px">Vertus</div>
          `;
        }

        const remList = document.getElementById("virtue-options-list");
        let unlockedRemedies = G.getAvailableVirtues();
        if (!Array.isArray(unlockedRemedies) || unlockedRemedies.length === 0) {
          unlockedRemedies = Object.values(C.VirtueRemedyTypes).slice(0, 4);
        }
        const total = unlockedRemedies.length;
        const radius = total <= 6 ? 112 : 120;
        const isCompact = total > 6;
        remList.innerHTML = "";
        unlockedRemedies.forEach((r, idx) => {
          const angle = (Math.PI * 2 * idx) / Math.max(1, total) - Math.PI / 2;
          const x = Math.cos(angle) * radius;
          const y = Math.sin(angle) * radius;

          const btn = document.createElement("button");
          btn.type = "button";
          btn.className = `virtue-option-btn astro-touch${isCompact ? " compact-orb" : ""}`;
          btn.setAttribute("data-glow", r.colorHex);
          btn.style.setProperty("--touch-glow", r.colorHex);
          btn.style.borderColor = r.colorHex;
          btn.style.left = `calc(50% + ${x.toFixed(1)}px)`;
          btn.style.top = `calc(50% + ${y.toFixed(1)}px)`;
          btn.innerHTML = `
            <span class="virtue-orb-emoji">${r.emoji}</span>
            <span class="virtue-orb-label" style="color:${r.colorHex}">${r.word}</span>
          `;
          btn.onclick = (ev) => {
            ev.stopPropagation();
            if (!G.fireVirtueRemedy(r.id)) {
              showToast("Aucune cible à portée de tir !");
            }
            syncGameModals();
          };
          remList.appendChild(btn);
        });
      }
    }

    // 3. Pause Modal
    const pauseModal = document.getElementById("modal-pause");
    if (pauseModal) {
      pauseModal.classList.toggle("open", Boolean(s.isPaused));
    }

    // 4. Level Complete Modal ("🌟 Secteur Harmonisé !")
    const completeModal = document.getElementById("modal-level-complete");
    if (completeModal) {
      completeModal.classList.toggle("open", Boolean(s.isLevelComplete));
      if (s.isLevelComplete) {
        document.getElementById("complete-score-text").textContent = `Score final de la mission : ${s.score} pts`;
        const listEl = document.getElementById("complete-meditations-list");
        if (listEl) {
          let itemsToShow = Array.isArray(s.newlyCollectedThisLevel) && s.newlyCollectedThisLevel.length > 0
            ? s.newlyCollectedThisLevel
            : [];
          if (itemsToShow.length === 0 && s.sector && Array.isArray(s.sector.fruits)) {
            itemsToShow = s.sector.fruits.map(fruitName =>
              appState.collectedItems.find(item => item.fruit === fruitName && item.situation === s.sector.subtitle) ||
              appState.collectedItems.find(item => item.fruit === fruitName) ||
              C.getNextMeditationForFruit(fruitName, null, s.levelNumber)
            ).filter(Boolean);
          }

          listEl.innerHTML = "";
          itemsToShow.forEach(rawM => {
            const m = (C && typeof C.enrichMeditation === "function" ? C.enrichMeditation(rawM) : rawM) || rawM;
            const isBrain = m.id >= 1001 && m.id <= 1072;
            const cardEl = document.createElement("div");
            cardEl.className = "meditation-card astro-touch";
            cardEl.setAttribute("data-glow", isBrain ? "#F43F5E" : "#10B981");
            cardEl.style.padding = "12px";
            cardEl.style.cursor = "pointer";
            cardEl.innerHTML = `
              <div style="display:flex;justify-content:space-between;align-items:center;gap:8px">
                <span class="mini-tag ${isBrain ? "brain" : "done"}">${escapeHtml(m.fruit)}</span>
                <span style="font-size:11.5px;color:var(--cyan);font-weight:700">${escapeHtml(m.reference)}</span>
              </div>
              <div style="font-size:12px;color:#94A3B8">${escapeHtml(m.situation)}</div>
              <div style="font-size:13px;color:#F8FAFC;line-height:1.45">« ${escapeHtml(m.verse || m.content)} »</div>
              <div style="font-size:11.5px;font-weight:700;color:#34D399;margin-top:4px">
                📖 Ouvrir la lecture complète (Texte Biblique, Méditation & Prière) ➔
              </div>
            `;
            cardEl.addEventListener("click", () => {
              A.playClick();
              openDirectMeditationModal(m, false);
            });
            listEl.appendChild(cardEl);
          });
        }
      }
    }

    // 5. Game Over Modal
    const gameOverModal = document.getElementById("modal-game-over");
    if (gameOverModal) {
      gameOverModal.classList.toggle("open", Boolean(s.isGameOver));
    }
  }

  // ==========================================================================
  // END-MISSION TARGETS SCORE ANIMATED TALLY MODAL
  // ==========================================================================
  let tallyInterval = null;
  function openEndMissionTallyModal() {
    const s = G.state;
    const modal = document.getElementById("modal-end-tally");
    if (!modal) return;
    if (modal.classList.contains("open")) return;
    modal.classList.add("open");

    const baseScore = s.score;
    const saved = s.targetsSavedCount;
    const missed = Math.max(0, s.totalTargetsCount - saved);

    let animSaved = 0;
    let animMissed = 0;
    let displayScore = baseScore;
    let phase = "SAVED";

    const btnValidate = document.getElementById("btn-validate-tally");
    // Toujours actif immédiatement pour ne jamais bloquer le joueur en fin de mission
    btnValidate.disabled = false;

    const savedStep = Math.max(1, Math.ceil(saved / 8));
    const missedStep = Math.max(1, Math.ceil(missed / 6));

    function renderTallyStep() {
      document.getElementById("tally-saved-count").textContent = `${animSaved} / ${saved}`;
      document.getElementById("tally-saved-pts").textContent = `+${animSaved * 10} pts`;
      document.getElementById("tally-missed-count").textContent = `${animMissed} / ${missed}`;
      document.getElementById("tally-missed-pts").textContent = `-${animMissed * 20} pts`;
      document.getElementById("tally-total-score").textContent = `${displayScore} pts`;
    }

    renderTallyStep();
    if (tallyInterval) {
      clearInterval(tallyInterval);
      tallyInterval = null;
    }

    tallyInterval = setInterval(() => {
      if (phase === "SAVED") {
        if (animSaved < saved) {
          const inc = Math.min(savedStep, saved - animSaved);
          animSaved += inc;
          displayScore += inc * 10;
          A.playScoreTick(true);
          renderTallyStep();
        } else {
          phase = "MISSED";
        }
      } else if (phase === "MISSED") {
        if (animMissed < missed) {
          const inc = Math.min(missedStep, missed - animMissed);
          animMissed += inc;
          displayScore = Math.max(0, displayScore - inc * 20);
          A.playScoreTick(false);
          renderTallyStep();
        } else {
          phase = "DONE";
          clearInterval(tallyInterval);
          tallyInterval = null;
          btnValidate.disabled = false;
        }
      }
    }, 95);

    btnValidate.onclick = () => {
      if (tallyInterval) {
        clearInterval(tallyInterval);
        tallyInterval = null;
      }
      const delta = saved * 10 - missed * 20;
      s.score = Math.max(0, baseScore + delta);
      s.isEndTargetsScoreDialogOpen = false;
      s.isLevelComplete = true;
      modal.classList.remove("open");

      // Persist level completion & record
      const prevRec = appState.levelRecords[s.levelNumber] || { bestScore: 0, brainBonusCollected: false };
      appState.levelRecords[s.levelNumber] = {
        isCompleted: true,
        bestScore: Math.max(prevRec.bestScore || 0, s.score),
        collectedCount: s.collectedCount,
        brainBonusCollected: Boolean(prevRec.brainBonusCollected || s.isBrainBonusCollected)
      };
      if (s.levelNumber >= appState.unlockedLevel && appState.unlockedLevel < 135) {
        appState.unlockedLevel = s.levelNumber + 1;
      }
      appState.totalScore += s.score;
      saveState();
      A.playLevelComplete();
      syncGameModals();
    };
  }

  // ==========================================================================
  // MEDITATION READER MODAL (WITH KEYWORD CHALLENGE, ANCHOR THOUGHT & SHARE)
  // ==========================================================================
  function escapeHtml(str) {
    return String(str || "")
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;");
  }

  function openDirectMeditationModal(rawMed, isFromGameRelic = false) {
    const modal = document.getElementById("modal-meditation-reader");
    if (!modal || !rawMed) return;
    const med = (C && typeof C.enrichMeditation === "function" ? C.enrichMeditation(rawMed) : rawMed) || rawMed;
    modal.classList.add("open");

    const isBrain = med.id >= 1001 && med.id <= 1072;
    const challenge = isFromGameRelic && !isBrain ? C.createKeywordChallenge(med) : null;
    let isDecoded = !challenge;

    const fruitBadge = document.getElementById("reader-fruit-badge");
    const sitText = document.getElementById("reader-situation");
    const refText = document.getElementById("reader-reference");
    const contentBox = document.getElementById("reader-content");
    const challengeBox = document.getElementById("reader-challenge-box");
    const feelingsBox = document.getElementById("reader-feelings-box");
    const reflectionOut = document.getElementById("reader-reflection-output");
    const anchorBtn = document.getElementById("btn-reader-anchor");
    const shareBtn = document.getElementById("btn-reader-share");
    const feelingsSection = feelingsBox ? feelingsBox.parentElement : null;

    fruitBadge.textContent = med.fruit;
    sitText.textContent = `Situation : ${med.situation}`;
    refText.textContent = med.reference;
    reflectionOut.style.display = "none";
    anchorBtn.style.display = isFromGameRelic ? "inline-flex" : "none";

    function renderDecodeState() {
      if (isDecoded) {
        challengeBox.style.display = "none";
        if (feelingsSection) feelingsSection.style.display = "block";
        if (isBrain) {
          contentBox.innerHTML = `
            <div style="display:flex;flex-direction:column;gap:10px">
              <div>
                <div style="font-size:12px;font-weight:800;color:#F472B6;text-transform:uppercase;letter-spacing:0.6px;margin-bottom:4px">
                  🧠 Réflexion Neuro-Juridique (${escapeHtml(med.reference)})
                </div>
                <div style="font-size:14.5px;line-height:1.6;color:var(--star-white)">
                  « ${escapeHtml(med.verse || med.content)} »
                </div>
              </div>
            </div>
          `;
        } else {
          const verseStr = med.verse || med.content || "";
          const explainStr = med.explain || "";
          const keepStr = med.keep || "";
          const prayerStr = med.prayer || "";
          contentBox.innerHTML = `
            <div style="display:flex;flex-direction:column;gap:12px;max-height:46vh;overflow-y:auto;padding-right:4px">
              <div style="padding:12px;border-radius:12px;background:rgba(0,229,255,0.08);border:1px solid rgba(0,229,255,0.3)">
                <div style="font-size:12px;font-weight:800;color:var(--cyan);text-transform:uppercase;letter-spacing:0.6px;margin-bottom:6px">
                  📖 Texte Biblique — ${escapeHtml(med.reference)}
                </div>
                <div style="font-size:14.5px;font-style:italic;line-height:1.6;color:var(--star-white)">
                  « ${escapeHtml(verseStr)} »
                </div>
              </div>

              ${explainStr ? `
              <div style="padding:12px;border-radius:12px;background:rgba(245,158,11,0.08);border:1px solid rgba(245,158,11,0.3)">
                <div style="font-size:12px;font-weight:800;color:var(--gold);text-transform:uppercase;letter-spacing:0.6px;margin-bottom:6px">
                  💭 Méditation
                </div>
                <div style="font-size:13.8px;line-height:1.6;color:var(--star-white)">
                  ${escapeHtml(explainStr)}
                </div>
                ${keepStr ? `
                <div style="margin-top:8px;padding-top:8px;border-top:1px solid rgba(245,158,11,0.22);font-size:12.8px;color:#FDE68A;font-weight:600">
                  🌱 À garder dans le cœur : « ${escapeHtml(keepStr)} »
                </div>
                ` : ""}
              </div>
              ` : ""}

              ${prayerStr ? `
              <div style="padding:12px;border-radius:12px;background:rgba(16,185,129,0.1);border:1px solid rgba(16,185,129,0.35)">
                <div style="font-size:12px;font-weight:800;color:#34D399;text-transform:uppercase;letter-spacing:0.6px;margin-bottom:6px">
                  🙏 Prière
                </div>
                <div style="font-size:13.8px;font-style:italic;line-height:1.6;color:#ECFDF5">
                  « ${escapeHtml(prayerStr)} »
                </div>
              </div>
              ` : ""}
            </div>
          `;
        }
      } else {
        if (feelingsSection) feelingsSection.style.display = "none";
        contentBox.innerHTML = `
          <div style="display:flex;flex-direction:column;gap:8px">
            <div style="font-size:12px;font-weight:800;color:var(--cyan);text-transform:uppercase;letter-spacing:0.6px">
              📖 Texte Biblique à compléter (${escapeHtml(med.reference)})
            </div>
            <div style="font-size:14.5px;font-style:italic;line-height:1.6;color:var(--star-white)">
              « ${escapeHtml(challenge.maskedContent)} »
            </div>
            <div style="font-size:11.8px;color:var(--nebula-gray);margin-top:2px">
              🔒 Complétez le mot manquant ci-dessous pour découvrir le Texte Biblique complet, la Méditation et la Prière.
            </div>
          </div>
        `;
        challengeBox.style.display = "block";
        const optsRow = document.getElementById("reader-challenge-options");
        optsRow.innerHTML = "";
        challenge.options.forEach(opt => {
          const b = document.createElement("button");
          b.className = "chip-btn astro-touch";
          b.setAttribute("data-glow", "#10B981");
          b.textContent = opt;
          b.onclick = () => {
            if (C.normalizeText(opt) === C.normalizeText(challenge.correctWord)) {
              isDecoded = true;
              G.state.score += 25;
              A.playTargetSaved();
              showToast("✨ Mot-clé restauré ! Texte biblique, méditation et prière débloqués (+25 pts)");
              renderDecodeState();
              updateGameHUD();
            } else {
              A.playObstacleHit();
              showToast("Essayez un autre mot-clé...");
            }
          };
          optsRow.appendChild(b);
        });
      }
    }
    renderDecodeState();

    // 6 Feelings chips
    feelingsBox.innerHTML = "";
    C.FeelingTypes.forEach(f => {
      const chip = document.createElement("button");
      chip.className = "chip-btn";
      chip.textContent = `${f.emoji} ${f.label}`;
      chip.onclick = () => {
        A.playClick();
        reflectionOut.style.display = "block";
        reflectionOut.textContent = C.generateReflection(med, f);
      };
      feelingsBox.appendChild(chip);
    });

    function closeReaderAndCheckCompletion() {
      enterFullscreen();
      G.state.lastCollectedMeditation = null;
      modal.classList.remove("open");
      updateGameHUD();
      if (isFromGameRelic && typeof G.checkLevelCompletion === "function") {
        G.checkLevelCompletion(appCallbacks);
      }
    }

    // Anchor thought button (+12s Peace Aura)
    anchorBtn.onclick = () => {
      enterFullscreen();
      G.activatePeaceAura();
      showToast("🕊️ Pensée ancrée : Aura de Paix active pendant 12 secondes !");
      closeReaderAndCheckCompletion();
    };

    // Share button (+1 Point & +1 Positive Thought)
    const alreadyShared = appState.claimedShareIds.includes(med.id);
    shareBtn.textContent = alreadyShared ? "📤 Partager cette pensée" : "📤 Partager (+1 ✨)";
    shareBtn.onclick = async () => {
      shouldMaintainFullscreen = true;
      const enriched = (C && typeof C.enrichMeditation === "function" ? C.enrichMeditation(med) : med) || med;
      const verseStr = enriched.verse || enriched.content || "";
      const explainStr = enriched.explain || "";
      const keepStr = enriched.keep || "";
      const prayerStr = enriched.prayer || "";
      const gameUrl = "https://diagauto974.github.io/NOORBIS/";

      const shareLines = [
        `✨ NOÖRBIS — ${enriched.fruit} (${enriched.situation})`,
        ``,
        `📖 Verset (${enriched.reference}) :`,
        `« ${verseStr} »`
      ];
      if (explainStr) {
        shareLines.push(
          ``,
          `💭 Méditation :`,
          explainStr + (keepStr ? `\n🌱 À garder dans le cœur : « ${keepStr} »` : "")
        );
      }
      if (prayerStr) {
        shareLines.push(
          ``,
          `🙏 Prière :`,
          `« ${prayerStr} »`
        );
      }
      shareLines.push(
        ``,
        `🌌 Jouez à NOÖRBIS : ${gameUrl}`
      );
      const shareText = shareLines.join("\n");

      if (navigator.clipboard && typeof navigator.clipboard.writeText === "function") {
        try {
          await navigator.clipboard.writeText(shareText);
        } catch (_) {}
      }

      if (navigator.share) {
        try {
          await navigator.share({
            title: `NOÖRBIS — ${enriched.reference}`,
            text: shareText
          });
        } catch (_) {}
      } else {
        showToast("📋 Verset, méditation, prière et lien copiés dans le presse-papiers !");
      }
      enterFullscreen();
      if (!appState.claimedShareIds.includes(med.id)) {
        appState.claimedShareIds.push(med.id);
        appCallbacks.onPositiveThoughtEarned(1);
        appState.totalScore = (appState.totalScore || 0) + 1;
        G.state.score = (G.state.score || 0) + 1;
        G.state.purificationEnergy = (G.state.purificationEnergy || 0) + 1;
        updateGameHUD();
        shareBtn.textContent = "📤 Partager cette pensée (Bonus +1 reçu ✓)";
        showToast("🎁 +1 Point gagné pour votre partage !");
        saveState();
      }
    };

    document.getElementById("btn-reader-close").onclick = () => {
      closeReaderAndCheckCompletion();
    };
  }

  // ==========================================================================
  // INVENTORY VIEW
  // ==========================================================================
  function renderInventory() {
    const brainCount = appState.collectedItems.filter(i => i.id >= 1001 && i.id <= 1072).length;
    const fruitCount = appState.collectedItems.filter(i => i.id < 1001).length;
    const totalCount = appState.collectedItems.length;

    const countEl = document.getElementById("inv-progress-count");
    const barEl = document.getElementById("inv-progress-fill");
    const brainBadge = document.getElementById("inv-brain-badge");
    if (countEl) countEl.textContent = `${fruitCount} / 810`;
    if (barEl) barEl.style.width = `${Math.min(100, (totalCount / 882) * 100)}%`;
    if (brainBadge) brainBadge.textContent = `🧠 Bonus Cerveau (Pensée libre & Droit) : ${brainCount} / 72`;

    // Filter chips
    const filterRow = document.getElementById("inv-filter-row");
    if (filterRow) {
      const filters = [
        { key: null, label: `Tous (${totalCount})` },
        { key: "__BRAIN__", label: `🧠 Cerveau (${brainCount}/72)` },
        ...C.FRUITS.map(f => ({ key: f, label: f }))
      ];
      filterRow.innerHTML = "";
      filters.forEach(f => {
        const btn = document.createElement("button");
        btn.className = `chip-btn ${appState.inventoryFilter === f.key ? "active" : ""}`;
        btn.textContent = f.label;
        btn.onclick = () => {
          appState.inventoryFilter = f.key;
          renderInventory();
        };
        filterRow.appendChild(btn);
      });
    }

    // Filter & Sort items
    const q = C.normalizeText(appState.inventorySearch);
    let items = appState.collectedItems.filter(item => {
      const matchesFilter =
        appState.inventoryFilter === null
          ? true
          : appState.inventoryFilter === "__BRAIN__"
          ? item.id >= 1001 && item.id <= 1072
          : C.normalizeText(item.fruit) === C.normalizeText(appState.inventoryFilter);

      const matchesSearch =
        !q ||
        C.normalizeText(item.content).includes(q) ||
        C.normalizeText(item.reference).includes(q) ||
        C.normalizeText(item.situation).includes(q) ||
        C.normalizeText(item.fruit).includes(q);

      return matchesFilter && matchesSearch;
    });

    items.sort((a, b) =>
      appState.settings.inventorySortAscending
        ? (a.collectedAt || 0) - (b.collectedAt || 0)
        : (b.collectedAt || 0) - (a.collectedAt || 0)
    );

    const grid = document.getElementById("inv-meditation-grid");
    if (!grid) return;
    if (items.length === 0) {
      grid.innerHTML = `
        <div style="text-align:center;padding:48px 16px;color:#94A3B8">
          <div style="font-size:42px;margin-bottom:10px">🌌</div>
          <div style="font-weight:700;color:#F8FAFC;margin-bottom:4px">Aucune méditation trouvée</div>
          <div style="font-size:13px">Explorez les 135 secteurs spatiaux pour recueillir les textes et les 72 fragments Cerveau.</div>
        </div>
      `;
      return;
    }

    grid.innerHTML = "";
    items.forEach(item => {
      const isBrain = item.id >= 1001 && item.id <= 1072;
      const card = document.createElement("div");
      card.className = `meditation-card ${isBrain ? "brain-card" : ""}`;
      card.innerHTML = `
        <div style="display:flex;justify-content:space-between;align-items:center;gap:8px;flex-wrap:wrap">
          <span class="mini-tag ${isBrain ? "brain" : ""}">${item.fruit}</span>
          <span style="font-family:var(--font-display);font-size:12px;color:var(--cyan);font-weight:700">${item.reference}</span>
        </div>
        <div style="font-size:12px;color:#94A3B8">Situation : ${item.situation}</div>
        <div style="font-size:13.5px;color:#F8FAFC;line-height:1.5">« ${item.content} »</div>
      `;
      card.onclick = () => openDirectMeditationModal(item, false);
      grid.appendChild(card);
    });
  }

  // ==========================================================================
  // GLOSSARY MODAL (TOUTES LES ENTITÉS + 29 VARIANTES JURIDIQUES & COGNITIVES)
  // ==========================================================================
  function openGlossaryModal() {
    const modal = document.getElementById("modal-glossary");
    if (!modal) return;
    modal.classList.add("open");
    renderGlossaryContent();
  }

  function renderGlossaryContent() {
    const cat = appState.glossaryCategory;
    document.querySelectorAll("[data-glossary-cat]").forEach(btn => {
      const val = btn.dataset.glossaryCat === "ALL" ? null : btn.dataset.glossaryCat;
      btn.classList.toggle("active", val === cat);
    });

    const entries = [];

    if (!cat || cat === "RELIC") {
      entries.push(
        { emoji: "✨", title: "Étincelle de l'Esprit (Votre Sphère)", badge: "Joueur • Cliquez : Remède", color: "#00E5FF", desc: "Votre conscience lumineuse. Naviguez au doigt, à la souris, au clavier ou via le joystick circulaire. Cliquez sur la sphère près d'un neurone infecté (≥ 10 Énergie) pour projeter une Vertu !" },
        { emoji: "🕊️", title: "Relique de Fruit & Sanctuaire d'Harmonisation", badge: "Objectif (+100 pts)", color: "#F59E0B", desc: "Chaque relique est entourée d'un Sanctuaire protecteur qui repousse les obstacles et harmonise un texte des 9 Fruits de l'Esprit." },
        { emoji: "🧠", title: "Relique Cerveau (72 Fragments Droit & Pensée Libre)", badge: "Bonus Cerveau (+150 pts)", color: "#F43F5E", desc: "Présente dans chaque secteur (1 par niveau), elle débloque l'un des 72 textes sur l'esprit critique, les vices du consentement, la neurobiologie et les libertés fondamentales." }
      );
    }

    if (!cat || cat === "TARGET") {
      Object.values(C.RescueTargetTypes).forEach(t => {
        entries.push({
          emoji: t.symbol,
          title: t.displayName,
          badge: "Pensée Pure (+10 / -20 pts)",
          color: t.colorHex,
          desc: `${t.description}. Toutes les 50 pensées sauvées ou neurones purifiés, vous gagnez +1 Cœur permanent (jusqu'à 7 cœurs) !`
        });
      });
      Object.values(C.NeuronCorruptionTypes).forEach(n => {
        const ideal = C.VirtueRemedyTypes[n.idealRemedy];
        entries.push({
          emoji: "🦠",
          title: `${n.displayName} (Niv. ${n.minLevel}+)`,
          badge: `Remède 100% : ${ideal.word}`,
          color: n.corruptedColorHex,
          desc: `Symptôme : ${n.symptomLabel}. Projetez la vertu « ${ideal.displayName} » pour le purifier instantanément à 100%.`
        });
      });
    }

    if (!cat || cat === "OBSTACLE") {
      Object.values(C.ObstacleTypes).forEach(o => {
        entries.push({
          emoji: "⚠️",
          title: `${o.displayName} (Niv. ${o.minLevel}+)`,
          badge: "Archétype d'Obstacle",
          color: o.strokeHex,
          desc: o.description
        });
      });
      C.SimpleObstacleVariants.forEach(v => {
        const catInfo = C.SimpleObstacleCategories[v.category];
        entries.push({
          emoji: v.symbolEmoji,
          title: `${v.displayName} (Niv. ${v.minLevel}+)`,
          badge: catInfo ? catInfo.displayName : v.category,
          color: v.accentHex,
          desc: v.definition
        });
      });
    }

    if (!cat || cat === "BONUS") {
      Object.values(C.VirtueRemedyTypes).forEach(r => {
        entries.push({
          emoji: r.emoji,
          title: `Remède : ${r.displayName} (Niv. ${r.minLevel}+)`,
          badge: `Mot projeté : ${r.word}`,
          color: r.colorHex,
          desc: r.description
        });
      });
      Object.values(C.PowerUpTypes).forEach(p => {
        entries.push({
          emoji: p.iconLabel,
          title: `${p.displayName} (Niv. ${p.minLevel}+)`,
          badge: "Bonus Protecteur",
          color: p.colorHex,
          desc: p.description
        });
      });
    }

    const listEl = document.getElementById("glossary-entries-list");
    if (!listEl) return;
    listEl.innerHTML = entries.map(e => `
      <div class="glossary-item-card" style="border-color:${e.color}44">
        <div class="glossary-preview-box" style="font-size:24px;border-color:${e.color}88">${e.emoji}</div>
        <div style="flex:1">
          <div style="display:flex;justify-content:space-between;align-items:center;gap:8px;flex-wrap:wrap">
            <span style="font-weight:700;font-size:14px;color:#F8FAFC">${e.title}</span>
            <span class="mini-tag" style="background:${e.color}22;color:${e.color}">${e.badge}</span>
          </div>
          <div style="font-size:12.5px;color:#94A3B8;margin-top:4px;line-height:1.45">${e.desc}</div>
        </div>
      </div>
    `).join("");
  }

  // ==========================================================================
  // SETTINGS MODAL
  // ==========================================================================
  function openSettingsModal() {
    const modal = document.getElementById("modal-settings");
    if (!modal) return;
    if (!G.state.isLevelComplete && !G.state.isGameOver && !G.state.isMissionBriefingOpen) {
      G.state.isPaused = true;
      syncGameModals();
    }
    modal.classList.add("open");

    const s = appState.settings;
    document.getElementById("chk-sfx").checked = Boolean(s.sfxEnabled);
    document.getElementById("chk-sort-asc").checked = Boolean(s.inventorySortAscending);
    document.getElementById("chk-joystick").checked = Boolean(s.joystickEnabled);
    document.getElementById("chk-direct-text").checked = Boolean(s.directTextEnabled);
    document.getElementById("chk-grid").checked = Boolean(s.showGridEnabled);
    document.getElementById("chk-minimap").checked = Boolean(s.showMiniMapEnabled);

    const musicContainer = document.getElementById("settings-music-chips");
    if (musicContainer) {
      const options = [
        { val: -1, label: "🔄 Auto (Selon Secteur)" },
        { val: -2, label: "🔇 Désactivée" },
        ...A.MUSIC_THEME_NAMES.map((name, idx) => ({ val: idx, label: `${idx + 1}. ${name}` }))
      ];
      musicContainer.innerHTML = "";
      options.forEach(opt => {
        const chip = document.createElement("button");
        chip.className = `chip-btn ${s.musicThemeOverride === opt.val ? "active" : ""}`;
        chip.textContent = opt.label;
        chip.onclick = () => {
          s.musicThemeOverride = opt.val;
          A.setConfig(s.sfxEnabled, s.musicThemeOverride, G.state.levelNumber);
          saveState();
          openSettingsModal();
        };
        musicContainer.appendChild(chip);
      });
    }
  }

  // ==========================================================================
  // GITHUB PAGES EXPORTERS (PACK .ZIP MULTI-FICHIERS & INDEX.HTML AUTONOME)
  // ==========================================================================
  async function fetchTextWithFallback(paths) {
    for (const p of paths) {
      try {
        const resp = await fetch(p);
        if (resp && resp.ok) {
          return await resp.text();
        }
      } catch (_) {}
    }
    return "";
  }

  function buildCrc32Table() {
    const table = new Uint32Array(256);
    for (let i = 0; i < 256; i++) {
      let c = i;
      for (let k = 0; k < 8; k++) {
        c = (c & 1) ? (0xEDB88320 ^ (c >>> 1)) : (c >>> 1);
      }
      table[i] = c >>> 0;
    }
    return table;
  }

  const CRC32_TABLE = buildCrc32Table();

  function computeCrc32(bytes) {
    let crc = 0xFFFFFFFF;
    for (let i = 0; i < bytes.length; i++) {
      crc = (crc >>> 8) ^ CRC32_TABLE[(crc ^ bytes[i]) & 0xFF];
    }
    return (crc ^ 0xFFFFFFFF) >>> 0;
  }

  function createUncompressedZipBlob(filesMap) {
    const encoder = new TextEncoder();
    const entries = Object.entries(filesMap).map(([path, content]) => {
      const nameBytes = encoder.encode(path);
      const dataBytes = typeof content === "string" ? encoder.encode(content) : content;
      const crc = computeCrc32(dataBytes);
      return { nameBytes, dataBytes, crc };
    });

    let localSize = 0;
    let centralSize = 0;
    entries.forEach(e => {
      localSize += 30 + e.nameBytes.length + e.dataBytes.length;
      centralSize += 46 + e.nameBytes.length;
    });

    const totalSize = localSize + centralSize + 22;
    const buf = new Uint8Array(totalSize);
    const view = new DataView(buf.buffer);

    let localOffset = 0;
    let centralOffset = localSize;

    entries.forEach(e => {
      const entryLocalOffset = localOffset;

      // Local file header
      view.setUint32(localOffset, 0x04034b50, true);
      view.setUint16(localOffset + 4, 20, true);
      view.setUint16(localOffset + 6, 0x0800, true); // UTF-8
      view.setUint16(localOffset + 8, 0, true);      // Store (uncompressed)
      view.setUint16(localOffset + 10, 0, true);
      view.setUint16(localOffset + 12, 0, true);
      view.setUint32(localOffset + 14, e.crc, true);
      view.setUint32(localOffset + 18, e.dataBytes.length, true);
      view.setUint32(localOffset + 22, e.dataBytes.length, true);
      view.setUint16(localOffset + 26, e.nameBytes.length, true);
      view.setUint16(localOffset + 28, 0, true);
      buf.set(e.nameBytes, localOffset + 30);
      buf.set(e.dataBytes, localOffset + 30 + e.nameBytes.length);
      localOffset += 30 + e.nameBytes.length + e.dataBytes.length;

      // Central directory header
      view.setUint32(centralOffset, 0x02014b50, true);
      view.setUint16(centralOffset + 4, 20, true);
      view.setUint16(centralOffset + 6, 20, true);
      view.setUint16(centralOffset + 8, 0x0800, true); // UTF-8
      view.setUint16(centralOffset + 10, 0, true);
      view.setUint16(centralOffset + 12, 0, true);
      view.setUint16(centralOffset + 14, 0, true);
      view.setUint32(centralOffset + 16, e.crc, true);
      view.setUint32(centralOffset + 20, e.dataBytes.length, true);
      view.setUint32(centralOffset + 24, e.dataBytes.length, true);
      view.setUint16(centralOffset + 28, e.nameBytes.length, true);
      view.setUint16(centralOffset + 30, 0, true);
      view.setUint16(centralOffset + 32, 0, true);
      view.setUint16(centralOffset + 34, 0, true);
      view.setUint16(centralOffset + 36, 0, true);
      view.setUint32(centralOffset + 38, 0, true);
      view.setUint32(centralOffset + 42, entryLocalOffset, true);
      buf.set(e.nameBytes, centralOffset + 46);
      centralOffset += 46 + e.nameBytes.length;
    });

    // End of central directory record
    view.setUint32(centralOffset, 0x06054b50, true);
    view.setUint16(centralOffset + 4, 0, true);
    view.setUint16(centralOffset + 6, 0, true);
    view.setUint16(centralOffset + 8, entries.length, true);
    view.setUint16(centralOffset + 10, entries.length, true);
    view.setUint32(centralOffset + 12, centralSize, true);
    view.setUint32(centralOffset + 16, localSize, true);
    view.setUint16(centralOffset + 20, 0, true);

    return new Blob([buf], { type: "application/zip" });
  }

  async function collectWebProjectFiles() {
    await C.loadMeditationsMatrix();
    const [indexText, cssText, catText, audText, gameText, appText] = await Promise.all([
      fetchTextWithFallback(["index.html"]),
      fetchTextWithFallback(["css/style.css", "style.css"]),
      fetchTextWithFallback(["js/catalog.js", "catalog.js"]),
      fetchTextWithFallback(["js/audio.js", "audio.js"]),
      fetchTextWithFallback(["js/game.js", "game.js"]),
      fetchTextWithFallback(["js/app.js", "app.js"])
    ]);

    const matrixJson = JSON.stringify(C.getAllMeditations());

    // Ensure clean relative paths without leading ./ or / in index.html
    const cleanIndexHtml = (indexText || `<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
  <title>NOÖRBIS (135 Niveaux)</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Orbitron:wght@500;700;800&family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="css/style.css" />
</head>
<body>
  ${document.getElementById("app-shell").outerHTML}
  ${document.getElementById("modals-root").outerHTML}
  <div id="toast-container"></div>
  <script src="js/catalog.js"><\/script>
  <script src="js/audio.js"><\/script>
  <script src="js/game.js"><\/script>
  <script src="js/app.js"><\/script>
  <!-- Protection anti-inspection de base -->
  <script>
    // Désactive le clic droit sur toute la page
    document.addEventListener('contextmenu', function(e) {
      e.preventDefault();
    });

    // Bloque F12, Ctrl+Shift+I, Ctrl+Shift+J, Ctrl+U
    document.addEventListener('keydown', function(e) {
      if (
        e.key === 'F12' ||
        (e.ctrlKey && e.shiftKey && (e.key === 'I' || e.key === 'i' || e.key === 'J' || e.key === 'j')) ||
        (e.ctrlKey && (e.key === 'U' || e.key === 'u'))
      ) {
        e.preventDefault();
        return false;
      }
    });
  <\/script>
</body>
</html>`)
      .replace(/href="\.?\/?css\/style\.css"/g, 'href="css/style.css"')
      .replace(/href="\.?\/?style\.css"/g, 'href="css/style.css"')
      .replace(/src="\.?\/?js\/catalog\.js"/g, 'src="js/catalog.js"')
      .replace(/src="\.?\/?catalog\.js"/g, 'src="js/catalog.js"')
      .replace(/src="\.?\/?js\/audio\.js"/g, 'src="js/audio.js"')
      .replace(/src="\.?\/?audio\.js"/g, 'src="js/audio.js"')
      .replace(/src="\.?\/?js\/game\.js"/g, 'src="js/game.js"')
      .replace(/src="\.?\/?game\.js"/g, 'src="js/game.js"')
      .replace(/src="\.?\/?js\/app\.js"/g, 'src="js/app.js"')
      .replace(/src="\.?\/?app\.js"/g, 'src="js/app.js"');

    return {
      indexHtml: cleanIndexHtml,
      cssText,
      catText,
      audText,
      gameText,
      appText,
      matrixJson
    };
  }

  async function exportGitHubPagesZipPack() {
    A.playClick();
    showToast("📦 Génération de l'archive GitHub Pages (index.html, css/style.css, js/*)...");
    try {
      const files = await collectWebProjectFiles();
      const zipBlob = createUncompressedZipBlob({
        "index.html": files.indexHtml,
        "css/style.css": files.cssText,
        "js/catalog.js": files.catText,
        "js/audio.js": files.audText,
        "js/game.js": files.gameText,
        "js/app.js": files.appText,
        "meditations_matrix.json": files.matrixJson
      });

      const url = URL.createObjectURL(zipBlob);
      const a = document.createElement("a");
      a.href = url;
      a.download = "noorbis_github_pages.zip";
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
      showToast("✅ Pack GitHub Pages (noorbis_github_pages.zip) téléchargé !");
    } catch (err) {
      showToast("⚠️ Erreur lors de la création du pack .zip.");
    }
  }

  async function exportStandaloneHtmlBundle() {
    A.playClick();
    showToast("📦 Préparation du fichier index.html autonome pour GitHub Pages...");
    try {
      const files = await collectWebProjectFiles();

      const fullHtml = files.indexHtml
        .replace('<link rel="stylesheet" href="css/style.css" />', `<style>\n${files.cssText}\n</style>`)
        .replace(
          '<script src="js/catalog.js"></script>',
          `<script>window.NOORBIS_MEDITATIONS_MATRIX = ${files.matrixJson};<\/script>\n<script>\n${files.catText}\n<\/script>`
        )
        .replace('<script src="js/audio.js"></script>', `<script>\n${files.audText}\n<\/script>`)
        .replace('<script src="js/game.js"></script>', `<script>\n${files.gameText}\n<\/script>`)
        .replace('<script src="js/app.js"></script>', `<script>\n${files.appText}\n<\/script>`);

      if (window.AndroidWebBridge && typeof window.AndroidWebBridge.shareStandaloneHtml === "function") {
        window.AndroidWebBridge.shareStandaloneHtml(fullHtml);
        showToast("✅ Export index.html autonome lancé !");
        return;
      }

      const blob = new Blob([fullHtml], { type: "text/html;charset=utf-8" });
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = "index.html";
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
      showToast("✅ Fichier index.html autonome téléchargé ! Prêt pour GitHub Pages.");
    } catch (err) {
      showToast("⚠️ Vous pouvez utiliser directement les fichiers index.html, css/ et js/ du dépôt sur GitHub Pages.");
    }
  }

  // ==========================================================================
  // EXPORT & IMPORT DE LA SAUVEGARDE LOCALE (.JSON / LOCALSTORAGE)
  // ==========================================================================
  function exportLocalSaveJson() {
    A.playClick();
    try {
      saveState();
      const rawFromStorage = localStorage.getItem(STORAGE_KEY);
      const parsedState = rawFromStorage ? JSON.parse(rawFromStorage) : appState;
      const exportPayload = {
        app: "Noôrbis",
        version: 2,
        storageKey: STORAGE_KEY,
        exportedAt: new Date().toISOString(),
        ...parsedState
      };
      const jsonString = JSON.stringify(exportPayload, null, 2);

      if (window.AndroidWebBridge && typeof window.AndroidWebBridge.exportSaveJson === "function") {
        window.AndroidWebBridge.exportSaveJson(jsonString);
        showToast("📥 Sauvegarde locale (.json) prête à être exportée !");
        return;
      }

      const blob = new Blob([jsonString], { type: "application/json;charset=utf-8" });
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      const dateStamp = new Date().toISOString().slice(0, 10);
      a.href = url;
      a.download = `noorbis_sauvegarde_${dateStamp}.json`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
      showToast("📥 Sauvegarde locale téléchargée (.json) !");
    } catch (err) {
      showToast("⚠️ Impossible d'exporter la sauvegarde locale.");
    }
  }

  function applyImportedSaveObject(imported) {
    if (!imported || typeof imported !== "object") {
      throw new Error("Format JSON invalide");
    }
    const source = imported.state && typeof imported.state === "object" ? imported.state : imported;

    if (typeof source.selectedColorId === "string") {
      appState.selectedColorId = source.selectedColorId;
    }
    if (typeof source.unlockedLevel === "number") {
      appState.unlockedLevel = Math.max(1, Math.min(135, Math.floor(source.unlockedLevel)));
    }
    if (typeof source.totalScore === "number") {
      appState.totalScore = Math.max(0, Math.floor(source.totalScore));
    }
    if (typeof source.positiveThoughtsCount === "number") {
      appState.positiveThoughtsCount = Math.max(0, Math.floor(source.positiveThoughtsCount));
    }
    if (Array.isArray(source.collectedItems)) {
      appState.collectedItems = source.collectedItems;
    }
    if (source.levelRecords && typeof source.levelRecords === "object") {
      appState.levelRecords = source.levelRecords;
    }
    if (Array.isArray(source.claimedShareIds)) {
      appState.claimedShareIds = source.claimedShareIds;
    }
    if (source.settings && typeof source.settings === "object") {
      appState.settings = Object.assign({}, appState.settings, source.settings);
    }

    appState.maxShields = Math.min(7, 3 + Math.floor((appState.positiveThoughtsCount || 0) / 50));

    // Écriture dans le localStorage puis relecture pour garantir la cohérence
    localStorage.setItem(STORAGE_KEY, JSON.stringify(appState));
    loadState();

    A.setConfig(appState.settings.sfxEnabled, appState.settings.musicThemeOverride, G.state.levelNumber);
    G.initLevel(appState.unlockedLevel, appState);
    renderHangar();
    renderInventory();
    openSettingsModal();
    updateGameHUD();
  }

  function importLocalSaveJson(file) {
    if (!file) return;
    const reader = new FileReader();
    reader.onload = (event) => {
      try {
        const text = event.target.result;
        const parsed = JSON.parse(text);
        applyImportedSaveObject(parsed);
        A.playTargetSaved();
        showToast("✅ Sauvegarde importée et enregistrée dans le localStorage !");
      } catch (err) {
        A.playObstacleHit();
        showToast("❌ Fichier .json invalide ou corrompu.");
      }
    };
    reader.onerror = () => {
      showToast("❌ Erreur lors de la lecture du fichier .json.");
    };
    reader.readAsText(file, "UTF-8");
  }

  // ==========================================================================
  // INITIALIZATION
  // ==========================================================================
  window.addEventListener("DOMContentLoaded", () => {
    loadState();

    // Mettre à jour la modale de lancement avec le secteur actuel et la couleur de sphère
    const launchSubtitle = document.getElementById("launch-sector-subtitle");
    if (launchSubtitle) {
      const currentSector = C.getSector(appState.unlockedLevel);
      launchSubtitle.textContent = `Secteur #${currentSector.levelNumber} : ${currentSector.name} — ${currentSector.subtitle}. Cliquez sur Démarrer pour lancer la mission en plein écran.`;
    }
    const launchOrb = document.getElementById("launch-sphere-orb");
    if (launchOrb) {
      const sc = getSphereColor();
      launchOrb.style.background = `radial-gradient(circle at 35% 35%, #FFFFFF 0%, ${sc.primaryHex} 48%, rgba(0, 229, 255, 0) 76%)`;
      launchOrb.style.boxShadow = `0 0 32px ${sc.primaryHex}, 0 0 64px ${sc.glowHex}77`;
    }

    // Navigation buttons
    document.querySelectorAll(".nav-item").forEach(btn => {
      btn.addEventListener("click", () => switchTab(btn.dataset.tab));
    });

    // Hangar filter buttons
    document.querySelectorAll("[data-hangar-filter]").forEach(btn => {
      btn.addEventListener("click", () => {
        appState.hangarFilter = btn.dataset.hangarFilter;
        renderHangar();
      });
    });

    // Glossary buttons
    const btnOpenGlossary = document.getElementById("btn-open-glossary");
    if (btnOpenGlossary) btnOpenGlossary.onclick = openGlossaryModal;
    const btnHudGlossary = document.getElementById("btn-hud-glossary");
    if (btnHudGlossary) btnHudGlossary.onclick = () => {
      G.state.isPaused = true;
      openGlossaryModal();
    };
    const btnCloseGlossary = document.getElementById("btn-close-glossary");
    if (btnCloseGlossary) btnCloseGlossary.onclick = () => {
      document.getElementById("modal-glossary").classList.remove("open");
    };
    document.querySelectorAll("[data-glossary-cat]").forEach(btn => {
      btn.onclick = () => {
        appState.glossaryCategory = btn.dataset.glossaryCat === "ALL" ? null : btn.dataset.glossaryCat;
        renderGlossaryContent();
      };
    });

    // Export standalone HTML & GitHub Pages ZIP Pack buttons
    const btnExportHtml = document.getElementById("btn-export-html");
    if (btnExportHtml) btnExportHtml.onclick = exportStandaloneHtmlBundle;
    const btnSettingsExportHtml = document.getElementById("btn-settings-export-html");
    if (btnSettingsExportHtml) btnSettingsExportHtml.onclick = exportStandaloneHtmlBundle;
    const btnExportPack = document.getElementById("btn-export-github-pack");
    if (btnExportPack) btnExportPack.onclick = exportGitHubPagesZipPack;
    const btnSettingsExportPack = document.getElementById("btn-settings-export-pack");
    if (btnSettingsExportPack) btnSettingsExportPack.onclick = exportGitHubPagesZipPack;

    // Rétablir automatiquement le plein écran dès le retour au jeu (notamment après "Partager" qui ouvre la boîte système native)
    const restoreFullscreenIfNeeded = () => {
      if ((shouldMaintainFullscreen || appState.currentTab === "game") && !document.fullscreenElement && !document.webkitFullscreenElement) {
        enterFullscreen();
      }
    };
    document.addEventListener("click", restoreFullscreenIfNeeded, { passive: true });
    document.addEventListener("touchend", restoreFullscreenIfNeeded, { passive: true });
    window.addEventListener("focus", restoreFullscreenIfNeeded, { passive: true });
    document.addEventListener("visibilitychange", () => {
      if (document.visibilityState === "visible") {
        restoreFullscreenIfNeeded();
      }
    });

    // Écran de lancement initial : bouton "Démarrer" -> ferme immédiatement la fenêtre, bascule en plein écran via document.documentElement.requestFullscreen() et lance le jeu
    const btnLaunchStart = document.getElementById("btn-launch-start");
    if (btnLaunchStart) {
      btnLaunchStart.onclick = (e) => {
        if (e) e.stopPropagation();
        dismissLaunchModal();
        shouldMaintainFullscreen = true;
        try {
          if (document.documentElement && typeof document.documentElement.requestFullscreen === "function") {
            const p = document.documentElement.requestFullscreen();
            if (p && typeof p.catch === "function") p.catch(() => {});
          }
        } catch (_) {}
        startLevelAndSwitch(appState.unlockedLevel, true);
      };
    }
    const btnLaunchHangar = document.getElementById("btn-launch-hangar");
    if (btnLaunchHangar) {
      btnLaunchHangar.onclick = (e) => {
        if (e) e.stopPropagation();
        dismissLaunchModal();
        enterFullscreen();
        A.ensureContext();
        A.playClick();
        switchTab("hangar");
      };
    }

    // Hero Quick Play & Inventory buttons
    const btnHeroQuickPlay = document.getElementById("btn-hero-quick-play");
    if (btnHeroQuickPlay) {
      btnHeroQuickPlay.onclick = () => {
        try {
          if (document.documentElement && typeof document.documentElement.requestFullscreen === "function") {
            const p = document.documentElement.requestFullscreen();
            if (p && typeof p.catch === "function") p.catch(() => {});
          }
        } catch (_) {}
        startLevelAndSwitch(appState.unlockedLevel, true);
      };
    }
    const btnHeroInv = document.getElementById("btn-hero-inventory");
    if (btnHeroInv) {
      btnHeroInv.onclick = () => switchTab("inventory");
    }

    // HUD Buttons
    const btnHudPause = document.getElementById("btn-hud-pause");
    if (btnHudPause) btnHudPause.onclick = () => {
      G.state.isPaused = !G.state.isPaused;
      syncGameModals();
    };
    const btnHudSettings = document.getElementById("btn-hud-settings");
    if (btnHudSettings) btnHudSettings.onclick = () => {
      G.state.isPaused = true;
      openSettingsModal();
    };

    // Mission Briefing close ("Démarrer")
    const btnStartMission = document.getElementById("btn-start-mission");
    if (btnStartMission) btnStartMission.onclick = () => {
      dismissLaunchModal();
      enterFullscreen();
      A.ensureContext();
      A.playClick();
      G.state.isMissionBriefingOpen = false;
      const briefingModal = document.getElementById("modal-mission-briefing");
      if (briefingModal) briefingModal.classList.remove("open");
      syncGameModals();
    };

    // Virtue Remedy modal close
    const btnCloseVirtue = document.getElementById("btn-close-virtue");
    if (btnCloseVirtue) btnCloseVirtue.onclick = () => {
      G.closeVirtueMenu();
      syncGameModals();
    };

    // Pause modal actions
    document.getElementById("btn-resume-game").onclick = () => {
      G.state.isPaused = false;
      syncGameModals();
    };
    document.getElementById("btn-restart-level").onclick = () => {
      G.initLevel(G.state.levelNumber, appState);
      syncGameModals();
    };
    document.getElementById("btn-quit-to-hangar").onclick = () => {
      G.state.isPaused = false;
      switchTab("hangar");
      syncGameModals();
    };

    // Level Complete modal actions
    document.getElementById("btn-next-level").onclick = () => {
      const nextLvl = Math.min(135, G.state.levelNumber + 1);
      startLevelAndSwitch(nextLvl);
    };
    document.getElementById("btn-complete-hangar").onclick = () => {
      G.state.isLevelComplete = false;
      switchTab("hangar");
      syncGameModals();
    };

    // Game Over modal actions
    document.getElementById("btn-retry-gameover").onclick = () => {
      G.initLevel(G.state.levelNumber, appState);
      syncGameModals();
    };
    document.getElementById("btn-gameover-hangar").onclick = () => {
      G.state.isGameOver = false;
      switchTab("hangar");
      syncGameModals();
    };

    // Inventory Search
    const searchInput = document.getElementById("inv-search-input");
    const searchClear = document.getElementById("inv-search-clear");
    if (searchInput) {
      searchInput.addEventListener("input", () => {
        appState.inventorySearch = searchInput.value;
        renderInventory();
      });
    }
    if (searchClear) {
      searchClear.onclick = () => {
        appState.inventorySearch = "";
        if (searchInput) searchInput.value = "";
        renderInventory();
      };
    }

    // Settings checkboxes
    const bindChk = (id, key) => {
      const el = document.getElementById(id);
      if (!el) return;
      el.onchange = () => {
        appState.settings[key] = el.checked;
        A.setConfig(appState.settings.sfxEnabled, appState.settings.musicThemeOverride, G.state.levelNumber);
        saveState();
        if (appState.currentTab === "inventory") renderInventory();
      };
    };
    bindChk("chk-sfx", "sfxEnabled");
    bindChk("chk-sort-asc", "inventorySortAscending");
    bindChk("chk-joystick", "joystickEnabled");
    bindChk("chk-direct-text", "directTextEnabled");
    bindChk("chk-grid", "showGridEnabled");
    bindChk("chk-minimap", "showMiniMapEnabled");

    // Export / Import JSON save buttons in Settings
    const btnExportSaveJson = document.getElementById("btn-export-save-json");
    const btnImportSaveJson = document.getElementById("btn-import-save-json");
    const inputImportSaveJson = document.getElementById("input-import-save-json");

    if (btnExportSaveJson) {
      btnExportSaveJson.onclick = exportLocalSaveJson;
    }
    if (btnImportSaveJson && inputImportSaveJson) {
      btnImportSaveJson.onclick = () => {
        A.playClick();
        inputImportSaveJson.value = "";
        inputImportSaveJson.click();
      };
      inputImportSaveJson.onchange = (e) => {
        const file = e.target.files && e.target.files[0];
        if (file) importLocalSaveJson(file);
      };
    }

    const btnUnlockAll = document.getElementById("btn-unlock-all-levels");
    if (btnUnlockAll) {
      btnUnlockAll.onclick = () => {
        appState.unlockedLevel = 135;
        saveState();
        renderHangar();
        showToast("🔓 Les 135 secteurs spatiaux sont maintenant débloqués !");
      };
    }

    document.getElementById("btn-close-settings").onclick = () => {
      document.getElementById("modal-settings").classList.remove("open");
      syncGameModals();
    };

    // Global AstroTouchEffect handler (data-glow sync + expanding shockwave ring + press bounce)
    document.querySelectorAll("[data-glow]").forEach(el => {
      el.style.setProperty("--touch-glow", el.getAttribute("data-glow"));
    });

    const TOUCH_SELECTOR = ".astro-touch, .btn, .chip-btn, .nav-item, .icon-btn, .export-pill-btn, .glossary-cta-btn, .color-card, .level-card.unlocked, .level-play-icon, .virtue-option-btn, .meditation-card";
    document.addEventListener("pointerdown", (e) => {
      const target = e.target.closest(TOUCH_SELECTOR);
      if (!target || target.disabled) return;
      const glowAttr = target.getAttribute("data-glow");
      if (glowAttr) {
        target.style.setProperty("--touch-glow", glowAttr);
      }
      target.classList.add("is-pressed");

      const rect = target.getBoundingClientRect();
      const wave = document.createElement("span");
      wave.className = "astro-press-wave";
      wave.style.left = `${e.clientX - rect.left}px`;
      wave.style.top = `${e.clientY - rect.top}px`;
      target.appendChild(wave);
      setTimeout(() => wave.remove(), 380);

      const clearPress = () => {
        target.classList.remove("is-pressed");
        window.removeEventListener("pointerup", clearPress);
        window.removeEventListener("pointercancel", clearPress);
      };
      window.addEventListener("pointerup", clearPress);
      window.addEventListener("pointercancel", clearPress);
    });

    // Initialize Hangar animated sphere & Game Canvas
    G.startHangarSphereAnimation(document.getElementById("hangar-sphere-canvas"), getSphereColor);
    G.bindCanvasEvents(document.getElementById("space-canvas"), appCallbacks);
    G.setOnStateChange(() => {
      updateGameHUD();
      syncGameModals();
    });
    G.initLevel(appState.unlockedLevel, appState, true);
    G.startLoop(appCallbacks);

    renderHangar();
    renderInventory();

    // Charger la matrice externe en tâche de fond sans bloquer l'interface ni le bouton Démarrer
    if (C && typeof C.loadMeditationsMatrix === "function") {
      C.loadMeditationsMatrix().then(() => {
        if (C && typeof C.enrichMeditation === "function") {
          appState.collectedItems = appState.collectedItems.map(item => ({
            ...item,
            ...C.enrichMeditation(item)
          }));
          if (G && G.state && Array.isArray(G.state.relics)) {
            G.state.relics.forEach(r => {
              if (r && r.assignedMeditation) {
                r.assignedMeditation = C.enrichMeditation(r.assignedMeditation);
              }
            });
          }
        }
        renderHangar();
        renderInventory();
      }).catch(() => {});
    }
  });
})();
