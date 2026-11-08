// ============================================================================
// NOÔRBIS — CATALOGUES, MODÈLES DE JEU, 135 NIVEAUX, 72 BONUS CERVEAU & MATRICE
// ============================================================================

window.NoorbisCatalog = (function () {
  const AvailableSphereColors = [
    { id: "cyan", name: "Cyan Stellaire", primaryHex: "#00E5FF", glowHex: "#0284C7" },
    { id: "rose", name: "Rose Plasma", primaryHex: "#F43F5E", glowHex: "#BE123C" },
    { id: "gold", name: "Or Solaire", primaryHex: "#F59E0B", glowHex: "#B45309" },
    { id: "emerald", name: "Émeraude Nova", primaryHex: "#10B981", glowHex: "#047857" },
    { id: "violet", name: "Pulsar Violet", primaryHex: "#A855F7", glowHex: "#6B21A8" },
    { id: "coral", name: "Corail Hyper", primaryHex: "#FF6B4A", glowHex: "#C2410C" }
  ];

  const FRUITS = [
    "L'amour",
    "La joie",
    "La paix",
    "La patience",
    "La bonté",
    "La bienveillance",
    "La foi",
    "La douceur",
    "La maîtrise de soi"
  ];

  const SITUATIONS = [
    "L'épreuve et la souffrance",
    "Le doute et l'incertitude",
    "Le conflit et l'offense",
    "L'attente et le silence de Dieu",
    "La fatigue et le découragement",
    "La peur de l'avenir",
    "La tentation et la faiblesse",
    "La solitude et l'incompréhension",
    "L'abondance et la gratitude",
    "Le service et le don de soi",
    "La prise de décision et le discernement",
    "L'échec et la culpabilité",
    "L'injustice et la persécution",
    "Le deuil et la séparation",
    "Le renouveau et l'espérance"
  ];

  const ObstacleTypes = {
    ASTEROID: {
      id: "ASTEROID",
      displayName: "Pensée Parasite (Rumination)",
      description: "Fragment de rumination mentale dérivant dans l'esprit.",
      minLevel: 1,
      colorHex: "#64748B",
      strokeHex: "#94A3B8"
    },
    LASER_DRONE: {
      id: "LASER_DRONE",
      displayName: "Foyer d'Angoisse (Tension)",
      description: "Émetteur d'ondes de stress et d'inquiétude aiguë.",
      minLevel: 1,
      colorHex: "#EF4444",
      strokeHex: "#FCA5A5"
    },
    PULSAR_MINE: {
      id: "PULSAR_MINE",
      displayName: "Crise de Panique (Impulsion)",
      description: "Nœud de réactivité émotionnelle explosive.",
      minLevel: 1,
      colorHex: "#A855F7",
      strokeHex: "#F43F5E"
    },
    CONFORMISM_SWARM: {
      id: "CONFORMISM_SWARM",
      displayName: "Nuée de Conformisme (Pression Sociale)",
      description: "Essaim grégaire cherchant à happer la sphère vers une trajectoire unique.",
      minLevel: 20,
      colorHex: "#94A3B8",
      strokeHex: "#CBD5E1"
    },
    DOGMATIC_PRISM: {
      id: "DOGMATIC_PRISM",
      displayName: "Prisme Dogmatique (Pensée Binaire)",
      description: "Structure rigide émettant des faisceaux de jugement binaire.",
      minLevel: 40,
      colorHex: "#EC4899",
      strokeHex: "#F472B6"
    },
    OBSCURANTISM_VEIL: {
      id: "OBSCURANTISM_VEIL",
      displayName: "Voile d'Obscurantisme (Brouillard Cognitif)",
      description: "Nébuleuse d'ignorance brouillant le radar et dissimulant les repères.",
      minLevel: 60,
      colorHex: "#475569",
      strokeHex: "#64748B"
    },
    MENTAL_MIRE: {
      id: "MENTAL_MIRE",
      displayName: "Marécage Mental (Procrastination)",
      description: "Zone de viscosité psychique ralentissant l'élan et la volonté.",
      minLevel: 80,
      colorHex: "#65A30D",
      strokeHex: "#A3E635"
    },
    NARCISSUS_MIRROR: {
      id: "NARCISSUS_MIRROR",
      displayName: "Miroir de Narcisse (Illusion de l'Ego)",
      description: "Leurre brillant imitant une relique mais repoussant l'esprit par vanité.",
      minLevel: 100,
      colorHex: "#FDE047",
      strokeHex: "#FEF08A"
    }
  };

  const SimpleObstacleCategories = {
    BASE_NEURO: {
      id: "BASE_NEURO",
      displayName: "Obstacles de Base (Stress & Ruminations)",
      colorHex: "#94A3B8"
    },
    DROIT_VICES_CONSENTEMENT: {
      id: "DROIT_VICES_CONSENTEMENT",
      displayName: "Vices du Consentement & Droit (Niv. 10–40)",
      colorHex: "#38BDF8"
    },
    RHETORIQUE_MANIPULATION: {
      id: "RHETORIQUE_MANIPULATION",
      displayName: "Sophismes & Manipulation Rhétorique (Niv. 45–80)",
      colorHex: "#F59E0B"
    },
    PSYCHOLOGIE_EMPRISE: {
      id: "PSYCHOLOGIE_EMPRISE",
      displayName: "Emprise Psychologique & Biais (Niv. 85–110)",
      colorHex: "#EC4899"
    },
    ATTEINTES_LIBERTES_AUTORITARISME: {
      id: "ATTEINTES_LIBERTES_AUTORITARISME",
      displayName: "Dérives Autoritaires & Libertés (Niv. 115–135)",
      colorHex: "#EF4444"
    }
  };

  const SimpleObstacleVariants = [
    // BASE_NEURO (Level 1)
    {
      id: "PENSEE_PARASITE",
      displayName: "Pensée Parasite (Rumination)",
      shortLabel: "Rumination",
      category: "BASE_NEURO",
      baseArchetype: "ASTEROID",
      minLevel: 1,
      definition: "Idée intrusive répétitive qui tourne en boucle et détourne l'attention de la paix intérieure.",
      colorHex: "#64748B",
      accentHex: "#94A3B8",
      symbolEmoji: "🪨",
      counterPowerUps: ["ANTI_ASTEROID", "EMP_BLAST"]
    },
    {
      id: "FOYER_ANGOISSE",
      displayName: "Foyer d'Angoisse (Tension)",
      shortLabel: "Angoisse",
      category: "BASE_NEURO",
      baseArchetype: "LASER_DRONE",
      minLevel: 1,
      definition: "Source d'inquiétude aiguë projetant des faisceaux d'alerte disproportionnés dans le champ mental.",
      colorHex: "#EF4444",
      accentHex: "#FCA5A5",
      symbolEmoji: "⚡",
      counterPowerUps: ["ANTI_DRONE", "EMP_BLAST"]
    },
    {
      id: "CRISE_PANIQUE",
      displayName: "Crise de Panique (Impulsion)",
      shortLabel: "Panique",
      category: "BASE_NEURO",
      baseArchetype: "PULSAR_MINE",
      minLevel: 1,
      definition: "Nœud de réactivité émotionnelle explosive qui se déclenche brutalement sous l'effet de la peur.",
      colorHex: "#A855F7",
      accentHex: "#F43F5E",
      symbolEmoji: "💥",
      counterPowerUps: ["ANTI_MINE", "EMP_BLAST"]
    },
    // I. DROIT & VICES DU CONSENTEMENT (Niv. 10 à 40)
    {
      id: "ERREUR_SUBSTANCE",
      displayName: "Erreur sur la Substance (Art. 1132 C. civ.)",
      shortLabel: "Erreur (1132)",
      category: "DROIT_VICES_CONSENTEMENT",
      baseArchetype: "ASTEROID",
      minLevel: 10,
      definition: "Fausse représentation des qualités essentielles d'un engagement qui altère la lucidité du choix.",
      colorHex: "#0284C7",
      accentHex: "#7DD3FC",
      symbolEmoji: "🌫️",
      counterPowerUps: ["ANTI_ASTEROID", "METACOGNITION_BEACON", "EMP_BLAST"]
    },
    {
      id: "DOL_MANOEUVRE",
      displayName: "Dol & Manœuvre Frauduleuse (Art. 1137 C. civ.)",
      shortLabel: "Dol (1137)",
      category: "DROIT_VICES_CONSENTEMENT",
      baseArchetype: "LASER_DRONE",
      minLevel: 15,
      definition: "Ruse, mensonge ou dissimulation intentionnelle visant à tromper une personne pour la pousser à consentir.",
      colorHex: "#0EA5E9",
      accentHex: "#FDE047",
      symbolEmoji: "🎭",
      counterPowerUps: ["ANTI_DRONE", "METACOGNITION_BEACON", "EMP_BLAST"]
    },
    {
      id: "VIOLENCE_CONTRAINTE",
      displayName: "Violence & Contrainte Morale (Art. 1140 C. civ.)",
      shortLabel: "Contrainte (1140)",
      category: "DROIT_VICES_CONSENTEMENT",
      baseArchetype: "PULSAR_MINE",
      minLevel: 20,
      definition: "Pression ou crainte d'un mal considérable exercée sur l'esprit pour extorquer un acte sans liberté.",
      colorHex: "#1D4ED8",
      accentHex: "#F87171",
      symbolEmoji: "⛓️",
      counterPowerUps: ["ANTI_MINE", "FREE_WILL_AEGIS", "EMP_BLAST"]
    },
    {
      id: "ABUS_FAIBLESSE",
      displayName: "Abus de Faiblesse (Art. 223-15-2 C. pén.)",
      shortLabel: "Abus Faiblesse",
      category: "DROIT_VICES_CONSENTEMENT",
      baseArchetype: "LASER_DRONE",
      minLevel: 25,
      definition: "Exploitation de la vulnérabilité ou de la fatigue psychique d'autrui pour le conduire à un acte préjudiciable.",
      colorHex: "#2563EB",
      accentHex: "#FB7185",
      symbolEmoji: "🕸️",
      counterPowerUps: ["ANTI_DRONE", "FREE_WILL_AEGIS", "EMP_BLAST"]
    },
    {
      id: "CLAUSE_ABUSIVE",
      displayName: "Clause Abusive (Déséquilibre Significatif)",
      shortLabel: "Clause Abusive",
      category: "DROIT_VICES_CONSENTEMENT",
      baseArchetype: "ASTEROID",
      minLevel: 30,
      definition: "Imposition unilatérale créant un déséquilibre asymétrique entre les droits et la dignité de la personne.",
      colorHex: "#0369A1",
      accentHex: "#38BDF8",
      symbolEmoji: "⚖️",
      counterPowerUps: ["ANTI_ASTEROID", "FREE_WILL_AEGIS", "EMP_BLAST"]
    },
    {
      id: "RETICENCE_DOLOSIVE",
      displayName: "Réticence Dolosive (Silence Trompeur)",
      shortLabel: "Silence Dolosif",
      category: "DROIT_VICES_CONSENTEMENT",
      baseArchetype: "PULSAR_MINE",
      minLevel: 35,
      definition: "Omission volontaire d'une information déterminante que la loyauté commandait pourtant de révéler.",
      colorHex: "#1E40AF",
      accentHex: "#60A5FA",
      symbolEmoji: "🤫",
      counterPowerUps: ["ANTI_MINE", "METACOGNITION_BEACON", "EMP_BLAST"]
    },
    {
      id: "CAPTATION_VOLONTE",
      displayName: "Captation de Volonté (Aliénation)",
      shortLabel: "Captation",
      category: "DROIT_VICES_CONSENTEMENT",
      baseArchetype: "LASER_DRONE",
      minLevel: 40,
      definition: "Détournement progressif du libre arbitre par encerclement affectif ou intellectuel.",
      colorHex: "#3B82F6",
      accentHex: "#C084FC",
      symbolEmoji: "🧲",
      counterPowerUps: ["ANTI_DRONE", "FREE_WILL_AEGIS", "EMP_BLAST"]
    },
    // II. RHÉTORIQUE & MANIPULATION (Niv. 45 à 80)
    {
      id: "SOPHISME_AD_HOMINEM",
      displayName: "Attaque Ad Hominem (Disqualification)",
      shortLabel: "Ad Hominem",
      category: "RHETORIQUE_MANIPULATION",
      baseArchetype: "LASER_DRONE",
      minLevel: 45,
      definition: "Artifice consistant à attaquer la personne plutôt que d'examiner la vérité de son propos.",
      colorHex: "#D97706",
      accentHex: "#FDE047",
      symbolEmoji: "🗡️",
      counterPowerUps: ["ANTI_DRONE", "HUMILITY_ANCHOR", "EMP_BLAST"]
    },
    {
      id: "FAUX_DILEMME",
      displayName: "Faux Dilemme (Chantage Binaire)",
      shortLabel: "Faux Dilemme",
      category: "RHETORIQUE_MANIPULATION",
      baseArchetype: "ASTEROID",
      minLevel: 50,
      definition: "Réduction artificielle d'une situation complexe à deux extrêmes pour interdire toute nuance.",
      colorHex: "#B45309",
      accentHex: "#FBBF24",
      symbolEmoji: "➗",
      counterPowerUps: ["ANTI_ASTEROID", "FREE_WILL_AEGIS", "EMP_BLAST"]
    },
    {
      id: "HOMME_DE_PAILLE",
      displayName: "Homme de Paille (Caricature)",
      shortLabel: "Homme de Paille",
      category: "RHETORIQUE_MANIPULATION",
      baseArchetype: "ASTEROID",
      minLevel: 55,
      definition: "Déformation grossière de la pensée d'autrui afin de la réfuter plus facilement en apparence.",
      colorHex: "#EA580C",
      accentHex: "#FDBA74",
      symbolEmoji: "🌾",
      counterPowerUps: ["ANTI_ASTEROID", "METACOGNITION_BEACON", "EMP_BLAST"]
    },
    {
      id: "APPEL_A_LA_PEUR",
      displayName: "Argumentum ad Metum (Appel à la Peur)",
      shortLabel: "Appel à la Peur",
      category: "RHETORIQUE_MANIPULATION",
      baseArchetype: "PULSAR_MINE",
      minLevel: 60,
      definition: "Sidération de l'esprit critique par l'agitation de menaces imminentes pour obtenir l'obéissance.",
      colorHex: "#C2410C",
      accentHex: "#FCA5A5",
      symbolEmoji: "🚨",
      counterPowerUps: ["ANTI_MINE", "SHIELD", "EMP_BLAST"]
    },
    {
      id: "ARGUMENT_AUTORITE",
      displayName: "Argument d'Autorité Abusif",
      shortLabel: "Fausse Autorité",
      category: "RHETORIQUE_MANIPULATION",
      baseArchetype: "LASER_DRONE",
      minLevel: 65,
      definition: "Utilisation du prestige ou d'un titre pour imposer une opinion sans démonstration ni examen critique.",
      colorHex: "#CA8A04",
      accentHex: "#FEF08A",
      symbolEmoji: "👑",
      counterPowerUps: ["ANTI_DRONE", "FREE_WILL_AEGIS", "EMP_BLAST"]
    },
    {
      id: "PENTE_SAVONNEUSE",
      displayName: "Pente Savonneuse (Catastrophisme)",
      shortLabel: "Catastrophisme",
      category: "RHETORIQUE_MANIPULATION",
      baseArchetype: "ASTEROID",
      minLevel: 70,
      definition: "Enchaînement spéculatif exagéré affirmant qu'un premier pas mènera fatalement au chaos.",
      colorHex: "#9A3412",
      accentHex: "#FB923C",
      symbolEmoji: "📉",
      counterPowerUps: ["ANTI_ASTEROID", "METACOGNITION_BEACON", "EMP_BLAST"]
    },
    {
      id: "INVERSION_CULPABILITE",
      displayName: "Inversion Accusatoire (DARVO)",
      shortLabel: "Inversion (DARVO)",
      category: "RHETORIQUE_MANIPULATION",
      baseArchetype: "PULSAR_MINE",
      minLevel: 75,
      definition: "Stratagème manipulateur où l'auteur d'un tort se pose en victime pour culpabiliser celui qui se défend.",
      colorHex: "#E11D48",
      accentHex: "#FDA4AF",
      symbolEmoji: "🔄",
      counterPowerUps: ["ANTI_MINE", "HUMILITY_ANCHOR", "EMP_BLAST"]
    },
    {
      id: " NOYADE_INFORMATIONNELLE",
      displayName: "Mille-Feuille Argumentatif (Submersion)",
      shortLabel: "Submersion",
      category: "RHETORIQUE_MANIPULATION",
      baseArchetype: "ASTEROID",
      minLevel: 80,
      definition: "Accumulation rapide d'affirmations confuses pour épuiser la capacité d'analyse de l'interlocuteur.",
      colorHex: "#D97706",
      accentHex: "#FDE68A",
      symbolEmoji: "🌪️",
      counterPowerUps: ["ANTI_ASTEROID", "MYELIN_SHEATH", "EMP_BLAST"]
    },
    // III. PSYCHOLOGIE & EMPRISE (Niv. 85 à 110)
    {
      id: "GASLIGHTING_DETOURNEMENT",
      displayName: "Gaslighting (Déni de Réalité)",
      shortLabel: "Gaslighting",
      category: "PSYCHOLOGIE_EMPRISE",
      baseArchetype: "PULSAR_MINE",
      minLevel: 85,
      definition: "Manipulation visant à faire douter une personne de sa propre mémoire, de sa perception ou de sa raison.",
      colorHex: "#DB2777",
      accentHex: "#F9A8D4",
      symbolEmoji: "🕯️",
      counterPowerUps: ["ANTI_MINE", "METACOGNITION_BEACON", "EMP_BLAST"]
    },
    {
      id: "DOUBLE_CONTRAINTE",
      displayName: "Double Contrainte (Injonction Paradoxale)",
      shortLabel: "Double Lien",
      category: "PSYCHOLOGIE_EMPRISE",
      baseArchetype: "LASER_DRONE",
      minLevel: 90,
      definition: "Deux ordres contradictoires émis simultanément de sorte qu'obéir à l'un revient à enfreindre l'autre.",
      colorHex: "#BE185D",
      accentHex: "#F472B6",
      symbolEmoji: "🔀",
      counterPowerUps: ["ANTI_DRONE", "FREE_WILL_AEGIS", "EMP_BLAST"]
    },
    {
      id: "DISSONANCE_COGNITIVE",
      displayName: "Tension de Dissonance Cognitive",
      shortLabel: "Dissonance",
      category: "PSYCHOLOGIE_EMPRISE",
      baseArchetype: "PULSAR_MINE",
      minLevel: 95,
      definition: "Trouble intérieur aigu provoqué par la contradiction entre ses valeurs profondes et un acte imposé.",
      colorHex: "#9D174D",
      accentHex: "#FB7185",
      symbolEmoji: "⚡",
      counterPowerUps: ["ANTI_MINE", "HUMILITY_ANCHOR", "EMP_BLAST"]
    },
    {
      id: "CHANTAGE_AFFECTIF",
      displayName: "Chantage Affectif (Instrumentalisation)",
      shortLabel: "Chantage Affectif",
      category: "PSYCHOLOGIE_EMPRISE",
      baseArchetype: "LASER_DRONE",
      minLevel: 100,
      definition: "Menace de retrait d'affection ou d'estime utilisée comme levier pour soumettre la volonté d'autrui.",
      colorHex: "#EC4899",
      accentHex: "#FBCFE8",
      symbolEmoji: "💔",
      counterPowerUps: ["ANTI_DRONE", "HUMILITY_ANCHOR", "EMP_BLAST"]
    },
    {
      id: "BIAIS_CONFIRMATION",
      displayName: "Tunnel de Confirmation (Aveuglement)",
      shortLabel: "Biais Cognitif",
      category: "PSYCHOLOGIE_EMPRISE",
      baseArchetype: "ASTEROID",
      minLevel: 105,
      definition: "Tendance à ne retenir que ce qui conforte un préjugé en rejetant les faits contradictoires.",
      colorHex: "#831843",
      accentHex: "#F472B6",
      symbolEmoji: "🕳️",
      counterPowerUps: ["ANTI_ASTEROID", "METACOGNITION_BEACON", "EMP_BLAST"]
    },
    {
      id: "IMPUISSANCE_APPRISE",
      displayName: "Impuissance Apprise (Résignation)",
      shortLabel: "Résignation",
      category: "PSYCHOLOGIE_EMPRISE",
      baseArchetype: "ASTEROID",
      minLevel: 110,
      definition: "Conditionnement par lequel l'esprit finit par croire qu'aucune action ne peut améliorer son sort.",
      colorHex: "#701A75",
      accentHex: "#E879F9",
      symbolEmoji: "🥀",
      counterPowerUps: ["ANTI_ASTEROID", "MYELIN_SHEATH", "EMP_BLAST"]
    },
    // IV. ATTEINTES AUX LIBERTÉS & AUTORITARISME (Niv. 115 à 135)
    {
      id: "CENSURE_PREALABLE",
      displayName: "Censure & Bâillonnement de la Pensée",
      shortLabel: "Censure",
      category: "ATTEINTES_LIBERTES_AUTORITARISME",
      baseArchetype: "LASER_DRONE",
      minLevel: 115,
      definition: "Entrave arbitraire à la libre communication des pensées et des opinions garantie par l'article 11 DDHC.",
      colorHex: "#DC2626",
      accentHex: "#FCA5A5",
      symbolEmoji: "🔇",
      counterPowerUps: ["ANTI_DRONE", "CONTEMPLATIVE_PHASE_SHIFT", "EMP_BLAST"]
    },
    {
      id: "SURVEILLANCE_PANOPTIQUE",
      displayName: "Œil Panoptique (Intrusion Vie Privée)",
      shortLabel: "Panoptique",
      category: "ATTEINTES_LIBERTES_AUTORITARISME",
      baseArchetype: "LASER_DRONE",
      minLevel: 120,
      definition: "Dispositif d'observation permanente visant à susciter l'autocensure et la perte d'intimité psychique.",
      colorHex: "#B91C1C",
      accentHex: "#F87171",
      symbolEmoji: "👁️",
      counterPowerUps: ["ANTI_DRONE", "CONTEMPLATIVE_PHASE_SHIFT", "EMP_BLAST"]
    },
    {
      id: "ARBITRAIRE_NORMATIF",
      displayName: "Arbitraire & Diktat Sans Loi",
      shortLabel: "Arbitraire",
      category: "ATTEINTES_LIBERTES_AUTORITARISME",
      baseArchetype: "PULSAR_MINE",
      minLevel: 125,
      definition: "Commandement dépourvu de fondement légal ou légitime, contraire à l'État de droit (Art. 4 & 5 DDHC).",
      colorHex: "#991B1B",
      accentHex: "#EF4444",
      symbolEmoji: "🔨",
      counterPowerUps: ["ANTI_MINE", "FREE_WILL_AEGIS", "EMP_BLAST"]
    },
    {
      id: "NOVLANGUE_IDEOLOGIQUE",
      displayName: "Novlangue & Appauvrissement du Sens",
      shortLabel: "Novlangue",
      category: "ATTEINTES_LIBERTES_AUTORITARISME",
      baseArchetype: "ASTEROID",
      minLevel: 130,
      definition: "Mutilation du langage destinée à rendre impossible la formulation même d'une pensée libre et critique.",
      colorHex: "#7F1D1D",
      accentHex: "#FCA5A5",
      symbolEmoji: "🧱",
      counterPowerUps: ["ANTI_ASTEROID", "PLENITUDE_ARK", "EMP_BLAST"]
    },
    {
      id: "ASSUJETTISSEMENT_TOTAL",
      displayName: "Assujettissement & Négation de Dignité",
      shortLabel: "Assujettissement",
      category: "ATTEINTES_LIBERTES_AUTORITARISME",
      baseArchetype: "PULSAR_MINE",
      minLevel: 135,
      definition: "Tentative ultime de réduire la personne humaine à un simple instrument privé de conscience souveraine.",
      colorHex: "#450A0A",
      accentHex: "#F43F5E",
      symbolEmoji: "🌑",
      counterPowerUps: ["ANTI_MINE", "PLENITUDE_ARK", "EMP_BLAST"]
    }
  ];

  const PowerUpTypes = {
    SHIELD: {
      id: "SHIELD",
      displayName: "Bouclier d'Éther",
      description: "+1 Protection de la sphère",
      minLevel: 1,
      colorHex: "#10B981",
      iconLabel: "🛡️"
    },
    SPEED_BOOST: {
      id: "SPEED_BOOST",
      displayName: "Hyper-Impulsion",
      description: "Vitesse de déplacement accrue (10s)",
      minLevel: 1,
      colorHex: "#00E5FF",
      iconLabel: "⚡"
    },
    MAGNET: {
      id: "MAGNET",
      displayName: "Attracteur d'Esprit",
      description: "Attire les reliques et pensées pures proches (12s)",
      minLevel: 1,
      colorHex: "#F59E0B",
      iconLabel: "🧲"
    },
    ANTI_ASTEROID: {
      id: "ANTI_ASTEROID",
      displayName: "Calme Mental (Anti-Rumination)",
      description: "Dissout les Pensées Parasites au contact (12s)",
      minLevel: 1,
      colorHex: "#38BDF8",
      iconLabel: "☄️"
    },
    ANTI_DRONE: {
      id: "ANTI_DRONE",
      displayName: "Souffle de Paix (Anti-Angoisse)",
      description: "Neutralise les Foyers d'Angoisse au contact (12s)",
      minLevel: 1,
      colorHex: "#F43F5E",
      iconLabel: "🎯"
    },
    ANTI_MINE: {
      id: "ANTI_MINE",
      displayName: "Ancrage Serein (Anti-Panique)",
      description: "Désamorce les Crises de Panique au contact (12s)",
      minLevel: 1,
      colorHex: "#A855F7",
      iconLabel: "🔮"
    },
    EMP_BLAST: {
      id: "EMP_BLAST",
      displayName: "Onde de Sérénité (Purification)",
      description: "Dissout instantanément toutes les menaces proches",
      minLevel: 1,
      colorHex: "#EC4899",
      iconLabel: "💥"
    },
    FREE_WILL_AEGIS: {
      id: "FREE_WILL_AEGIS",
      displayName: "Égide du Libre Arbitre",
      description: "Immunité contre le Conformisme et les Prismes Dogmatiques (14s)",
      minLevel: 40,
      colorHex: "#F472B6",
      iconLabel: "⚖️"
    },
    METACOGNITION_BEACON: {
      id: "METACOGNITION_BEACON",
      displayName: "Phare de Métacognition",
      description: "Dissipe le Voile d'Obscurantisme et révèle toute la carte (15s)",
      minLevel: 60,
      colorHex: "#FDE047",
      iconLabel: "👁️"
    },
    HUMILITY_ANCHOR: {
      id: "HUMILITY_ANCHOR",
      displayName: "Ancre d'Humilité",
      description: "Traverse les Marécages Mentaux et brise les Miroirs de Narcisse (14s)",
      minLevel: 100,
      colorHex: "#A3E635",
      iconLabel: "⚓"
    },
    MYELIN_SHEATH: {
      id: "MYELIN_SHEATH",
      displayName: "Gaine de Myéline (Conduction Rapide)",
      description: "Vitesse supra-luminique et immunité aux ralentissements (12s)",
      minLevel: 110,
      colorHex: "#22D3EE",
      iconLabel: "🧬"
    },
    CONTEMPLATIVE_PHASE_SHIFT: {
      id: "CONTEMPLATIVE_PHASE_SHIFT",
      displayName: "Déphasage Contemplatif (Transcendance)",
      description: "Intangibilité éthérée : traverse tous les obstacles sans dommage (10s)",
      minLevel: 120,
      colorHex: "#C084FC",
      iconLabel: "🌌"
    },
    PLENITUDE_ARK: {
      id: "PLENITUDE_ARK",
      displayName: "Arche de Plénitude (Harmonie Totale)",
      description: "Rayonnement souverain : attire les pensées et dissout les menaces proches (12s)",
      minLevel: 130,
      colorHex: "#FBBF24",
      iconLabel: "👑"
    }
  };

  const RescueTargetTypes = {
    LUCIDITY_SPARK: {
      id: "LUCIDITY_SPARK",
      displayName: "Étincelle de Lucidité",
      description: "Pensée claire menacée par le doute (+10 pts sauvée / -20 pts perdue)",
      colorHex: "#38BDF8",
      symbol: "✨"
    },
    KNOWLEDGE_FRAGMENT: {
      id: "KNOWLEDGE_FRAGMENT",
      displayName: "Fragment de Savoir",
      description: "Idée précieuse menacée par l'oubli (+10 pts sauvée / -20 pts perdue)",
      colorHex: "#FBBF24",
      symbol: "🔶"
    },
    FRAGILE_MEMORY: {
      id: "FRAGILE_MEMORY",
      displayName: "Souvenir Fragile",
      description: "Mémoire vivante menacée par les tensions (+10 pts sauvée / -20 pts perdue)",
      colorHex: "#34D399",
      symbol: "🫧"
    }
  };

  const VirtueRemedyTypes = {
    PAIX: {
      id: "PAIX",
      word: "PAIX",
      displayName: "Paix & Patience",
      description: "Apaise la Colère et la Peur (100% d'affinité)",
      colorHex: "#00E5FF",
      emoji: "🕊️",
      minLevel: 1
    },
    JOIE: {
      id: "JOIE",
      word: "JOIE",
      displayName: "Joie & Espérance",
      description: "Dissipe la Tristesse et le Découragement (100% d'affinité)",
      colorHex: "#FBBF24",
      emoji: "☀️",
      minLevel: 1
    },
    MAITRISE_DE_SOI: {
      id: "MAITRISE_DE_SOI",
      word: "TEMPÉRANCE",
      displayName: "Maîtrise de soi & Douceur",
      description: "Calme l'Agitation et la Dispersion (100% d'affinité)",
      colorHex: "#A855F7",
      emoji: "🌿",
      minLevel: 1
    },
    AMOUR: {
      id: "AMOUR",
      word: "AMOUR",
      displayName: "Amour & Bonté",
      description: "Guérit la Rancœur et l'Endurcissement (100% d'affinité)",
      colorHex: "#F43F5E",
      emoji: "💖",
      minLevel: 1
    },
    VERITE_DISCERNEMENT: {
      id: "VERITE_DISCERNEMENT",
      word: "VÉRITÉ",
      displayName: "Vérité & Discernement",
      description: "Dissipe les Prismes Dogmatiques, le Voile et les Miroirs de l'Ego (100%)",
      colorHex: "#10B981",
      emoji: "⚖️",
      minLevel: 1
    },
    REGULATION_SOUFFLE: {
      id: "REGULATION_SOUFFLE",
      word: "SOUFFLE",
      displayName: "Régulation & Souffle",
      description: "Apaise la Surtension Synaptique et la Surcharge (100% d'affinité)",
      colorHex: "#38BDF8",
      emoji: "🌬️",
      minLevel: 30
    },
    ELAN_ACTION: {
      id: "ELAN_ACTION",
      word: "ÉLAN",
      displayName: "Élan Vital & Volonté",
      description: "Réveille l'Apathie et dissout les Marécages Mentaux (100% d'affinité)",
      colorHex: "#F97316",
      emoji: "🔥",
      minLevel: 50
    },
    PLASTICITE_RECADRAGE: {
      id: "PLASTICITE_RECADRAGE",
      word: "SOUPLESSE",
      displayName: "Plasticité & Recadrage",
      description: "Libère la Rigidité Obsessionnelle et les Boucles Mentales (100%)",
      colorHex: "#EC4899",
      emoji: "🔄",
      minLevel: 60
    },
    HOMEOSTASIE_PRESENCE: {
      id: "HOMEOSTASIE_PRESENCE",
      word: "PRÉSENCE",
      displayName: "Homéostasie & Présence",
      description: "Régénère l'Épuisement (Burn-out) et la Dissociation (100% d'affinité)",
      colorHex: "#14B8A6",
      emoji: "🧘",
      minLevel: 80
    },
    INFLUX_BIOELECTRIQUE: {
      id: "INFLUX_BIOELECTRIQUE",
      word: "INFLUX",
      displayName: "Influx Bioélectrique (Arc Synaptique)",
      description: "Propage un arc lumineux qui purifie en chaîne les neurones proches (100%)",
      colorHex: "#22D3EE",
      emoji: "⚡",
      minLevel: 100
    },
    RESONANCE_GAMMA: {
      id: "RESONANCE_GAMMA",
      word: "GAMMA",
      displayName: "Résonance Gamma (Synchronisation)",
      description: "Onde de cohérence cérébrale harmonisant toutes les corruptions (100%)",
      colorHex: "#E879F9",
      emoji: "🧠",
      minLevel: 120
    }
  };

  const NeuronCorruptionTypes = {
    COLERE_PEUR: {
      id: "COLERE_PEUR",
      displayName: "Neurone de Colère & Peur",
      symptomLabel: "Tension & Hostilité",
      corruptedColorHex: "#EF4444",
      idealRemedy: "PAIX",
      partialRemedies: ["MAITRISE_DE_SOI", "AMOUR", "REGULATION_SOUFFLE"],
      minLevel: 1
    },
    TRISTESSE: {
      id: "TRISTESSE",
      displayName: "Neurone de Tristesse",
      symptomLabel: "Accablement & Ombre",
      corruptedColorHex: "#3B82F6",
      idealRemedy: "JOIE",
      partialRemedies: ["AMOUR", "PAIX", "ELAN_ACTION"],
      minLevel: 1
    },
    AGITATION: {
      id: "AGITATION",
      displayName: "Neurone d'Agitation",
      symptomLabel: "Dispersion & Chaos",
      corruptedColorHex: "#F59E0B",
      idealRemedy: "MAITRISE_DE_SOI",
      partialRemedies: ["PAIX", "VERITE_DISCERNEMENT", "REGULATION_SOUFFLE"],
      minLevel: 1
    },
    RANCOEUR: {
      id: "RANCOEUR",
      displayName: "Neurone de Rancœur",
      symptomLabel: "Amertume & Froideur",
      corruptedColorHex: "#A855F7",
      idealRemedy: "AMOUR",
      partialRemedies: ["JOIE", "PAIX", "PLASTICITE_RECADRAGE"],
      minLevel: 1
    },
    SURTENSION: {
      id: "SURTENSION",
      displayName: "Neurone en Surtension",
      symptomLabel: "Surcharge & Hypervigilance",
      corruptedColorHex: "#FB7185",
      idealRemedy: "REGULATION_SOUFFLE",
      partialRemedies: ["PAIX", "MAITRISE_DE_SOI", "HOMEOSTASIE_PRESENCE"],
      minLevel: 30
    },
    APATHIE: {
      id: "APATHIE",
      displayName: "Neurone d'Apathie",
      symptomLabel: "Inertie & Découragement",
      corruptedColorHex: "#84CC16",
      idealRemedy: "ELAN_ACTION",
      partialRemedies: ["JOIE", "AMOUR", "HOMEOSTASIE_PRESENCE"],
      minLevel: 50
    },
    RIGIDITE_OBSESSION: {
      id: "RIGIDITE_OBSESSION",
      displayName: "Neurone de Rigidité",
      symptomLabel: "Fixation & Dogmatisme",
      corruptedColorHex: "#EC4899",
      idealRemedy: "PLASTICITE_RECADRAGE",
      partialRemedies: ["VERITE_DISCERNEMENT", "AMOUR", "MAITRISE_DE_SOI"],
      minLevel: 60
    },
    BURNOUT_DISSOCIATION: {
      id: "BURNOUT_DISSOCIATION",
      displayName: "Neurone d'Épuisement",
      symptomLabel: "Burn-out & Dissociation",
      corruptedColorHex: "#64748B",
      idealRemedy: "HOMEOSTASIE_PRESENCE",
      partialRemedies: ["REGULATION_SOUFFLE", "PAIX", "AMOUR"],
      minLevel: 80
    }
  };

  function getRemedyAffinity(corruptionId, remedyId) {
    if (remedyId === "INFLUX_BIOELECTRIQUE" || remedyId === "RESONANCE_GAMMA") return 1.0;
    const c = NeuronCorruptionTypes[corruptionId];
    if (!c) return 0.5;
    if (remedyId === c.idealRemedy) return 1.0;
    if (c.partialRemedies.includes(remedyId)) return 0.5;
    return 0.0;
  }

  // 135 Space Sectors
  const sectorNames = [
    "Nébuleuse de l'Aube",
    "Ceinture d'Orion",
    "Sanctuaire des Pléiades",
    "Vallée du Silence Stellaire",
    "Arche de Lumière",
    "Couronne de Sagesse",
    "Horizon d'Espérance",
    "Jardin d'Andromède",
    "Zénith de Plénitude"
  ];

  const fruitTriplets = [
    ["L'amour", "La joie", "La paix"],
    ["La patience", "La bonté", "La bienveillance"],
    ["La foi", "La douceur", "La maîtrise de soi"],
    ["L'amour", "La patience", "La foi"],
    ["La joie", "La bonté", "La douceur"],
    ["La paix", "La bienveillance", "La maîtrise de soi"],
    ["L'amour", "La bienveillance", "La douceur"],
    ["La joie", "La patience", "La maîtrise de soi"],
    ["La paix", "La bonté", "La foi"]
  ];

  function getSector(levelNumber) {
    const clamped = Math.max(1, Math.min(135, levelNumber));
    const idx = clamped - 1;
    const baseName = sectorNames[idx % sectorNames.length];
    const situationName = SITUATIONS[Math.floor(idx / 9) % SITUATIONS.length];
    const fruits = fruitTriplets[idx % fruitTriplets.length];
    const obstacleCount = Math.min(36, 16 + Math.floor(idx * 20 / 134));
    const obstacleSpeed = 1.15 + (idx / 134) * 1.20;
    return {
      levelNumber: clamped,
      name: `Secteur ${clamped} : ${baseName}`,
      subtitle: situationName,
      fruits: fruits,
      obstacleCount: obstacleCount,
      obstacleSpeed: obstacleSpeed
    };
  }

  function getAllSectors() {
    const list = [];
    for (let i = 1; i <= 135; i++) list.push(getSector(i));
    return list;
  }

  // 72 Brain Bonus Fragments
  const BrainBonusFragments = [
    // I. LA PENSÉE LIBRE ET CRITIQUE (1–12)
    { id: 1001, title: "L'Aube de la Raison Critique", category: "Pensée libre & critique", reference: "Sapere Aude — Philosophie des Lumières", content: "Aie le courage de te servir de ton propre entendement. La liberté de l'esprit commence lorsque la conscience examine ses propres certitudes sans crainte, refusant la paresse du préjugé pour accueillir la clarté du discernement." },
    { id: 1002, title: "Le Doute Méthodique comme Boussole", category: "Pensée libre & critique", reference: "Épistémologie & Discernement", content: "Douter n'est pas détruire la vérité, mais balayer les illusions qui l'obscurcissent. Un esprit libre suspend son jugement face à la rumeur afin de ne bâtir ses convictions que sur ce qui résiste à l'épreuve de la raison et de la conscience." },
    { id: 1003, title: "La Souveraineté du For Intérieur", category: "Pensée libre & critique", reference: "Droit naturel & Philosophie de l'Esprit", content: "Nulle contrainte extérieure ne peut forcer l'adhésion intime de la conscience. Le sanctuaire de la pensée demeure inviolable : même au cœur de la tempête, l'être humain conserve le pouvoir souverain de dire oui au vrai et non à l'arbitraire." },
    { id: 1004, title: "L'Émancipation face aux Sophismes", category: "Pensée libre & critique", reference: "Logique & Rhétorique Critique", content: "La parole manipulatrice séduit par l'émotion brusque ou la peur ; la parole juste éclaire par la cohérence. Reconnaître un sophisme, c'est désamorcer un piège mental avant qu'il ne capture notre volonté." },
    { id: 1005, title: "La Pluralité des Perspectives", category: "Pensée libre & critique", reference: "Herméneutique & Dialogue", content: "Écouter une pensée différente de la sienne sans perdre son axe intérieur est la marque d'un esprit mûr. La confrontation pacifique des idées polit l'intelligence comme le diamant polit le diamant." },
    { id: 1006, title: "L'Humilité Épistémique", category: "Pensée libre & critique", reference: "Sagesse & Philosophie des Sciences", content: "Savoir que l'on ne sait pas tout protège du fanatisme. L'esprit véritablement éclairé garde toujours une fenêtre ouverte sur l'apprentissage, la nuance et la rectification bienveillante." },
    { id: 1007, title: "La Liberté d'Opinion et de Conscience", category: "Pensée libre & critique", reference: "Déclaration Universelle des Droits de l'Homme, Art. 18", content: "Toute personne a droit à la liberté de pensée, de conscience et de conviction. Ce droit fondamental implique la faculté de chercher la vérité par soi-même, sans subir d'intimidation morale ou sociale." },
    { id: 1008, title: "La Libre Communication des Pensées", category: "Pensée libre & critique", reference: "Déclaration des Droits de l'Homme et du Citoyen, Art. 11", content: "La libre communication des pensées et des opinions est un des droits les plus précieux de l'être humain. Elle n'a de limite que le respect de la dignité d'autrui et la responsabilité envers la vérité." },
    { id: 1009, title: "L'Indépendance face au Conformisme", category: "Pensée libre & critique", reference: "Psychologie Sociale & Éthique", content: "Le nombre ne fait pas la justesse. Lorsque le troupeau s'élance vers l'erreur par mimétisme, la conscience éveillée choisit la fidélité calme à ses principes plutôt que le confort d'une approbation aveugle." },
    { id: 1010, title: "La Vigilance face aux Biais Cognitifs", category: "Pensée libre & critique", reference: "Sciences Cognitives & Esprit Critique", content: "Notre cerveau cherche spontanément ce qui confirme ses attentes. Cultiver l'esprit critique, c'est oser interroger nos propres angles morts avec honnêteté et sérénité." },
    { id: 1011, title: "L'Éducation qui Libère", category: "Pensée libre & critique", reference: "Pédagogie Humaniste", content: "Instruire un esprit ne consiste pas à remplir un vase de dogmes figés, mais à allumer un foyer capable de penser, de créer et de juger par lui-même tout au long de sa vie." },
    { id: 1012, title: "La Vérité comme Horizon", category: "Pensée libre & critique", reference: "Philosophie Morale", content: "Nul ne possède la vérité comme une propriété privée ; nous en sommes les chercheurs et les gardiens. Cette quête exige à la fois la rigueur de l'intelligence et la droiture du cœur." },

    // II. LE DROIT ET LA PROTECTION CONTRE LES ABUS (13–24)
    { id: 1013, title: "L'Intégrité du Consentement Libre", category: "Droit & protection contre les abus", reference: "Code civil, Art. 1130 — Vices du consentement", content: "Il n'y a point de consentement valable s'il a été donné par erreur, surpris par le dol ou extorqué par la violence. Le droit protège la liberté réelle de la volonté contre toute forme de tromperie ou de contrainte." },
    { id: 1014, title: "La Protection contre le Dol et la Ruse", category: "Droit & protection contre les abus", reference: "Code civil, Art. 1137 — Le Dol", content: "Les manœuvres, mensonges ou dissimulations intentionnelles visant à induire une personne en erreur vicient l'engagement. La loyauté exige la transparence : nul ne doit bâtir son avantage sur l'aveuglement provoqué d'autrui." },
    { id: 1015, title: "Le Refus de la Contrainte et de la Violence", category: "Droit & protection contre les abus", reference: "Code civil, Art. 1140 — La Violence", content: "Lorsqu'une pression inspire la crainte d'exposer sa personne, sa réputation ou ses proches à un mal considérable, l'acte perd sa légitimité. La vraie justice restaure celui dont la main a été forcée." },
    { id: 1016, title: "La Défense face à l'Abus de Faiblesse", category: "Droit & protection contre les abus", reference: "Code pénal, Art. 223-15-2 — Abus de vulnérabilité", content: "La loi sanctionne sévèrement quiconque exploite l'état de vulnérabilité, de fatigue ou de sujétion psychologique d'une personne pour la conduire à un acte qui lui est gravement préjudiciable." },
    { id: 1017, title: "Le Principe de Bonne Foi", category: "Droit & protection contre les abus", reference: "Code civil, Art. 1104 — Ordre public", content: "Les engagements doivent être négociés, formés et exécutés de bonne foi. La droiture contractuelle et relationnelle est le socle invisible qui permet aux humains de coopérer dans la confiance." },
    { id: 1018, title: "L'Interdiction de l'Abus de Droit", category: "Droit & protection contre les abus", reference: "Jurisprudence & Théorie de l'Abus de Droit", content: "Un droit cesse d'être juste lorsqu'il est détourné de sa finalité pour nuire délibérément à autrui. La lettre de la règle ne doit jamais servir d'arme à la malveillance." },
    { id: 1019, title: "La Dignité Inaliénable de la Personne", category: "Droit & protection contre les abus", reference: "Code civil, Art. 16 — Primauté de la personne", content: "La loi assure la primauté de la personne, interdit toute atteinte à la dignité de celle-ci et garantit le respect de l'être humain. La dignité n'a pas de prix : elle ne peut être ni vendue, ni confisquée." },
    { id: 1020, title: "La Proportionnalité et la Juste Mesure", category: "Droit & protection contre les abus", reference: "Principes Généraux du Droit", content: "Toute mesure ou exigence doit demeurer proportionnée à son but légitime. Là où commence la démesure commence l'arbitraire ; l'équilibre est la signature même de la justice." },
    { id: 1021, title: "Le Droit au Recours et à l'Écoute", category: "Droit & protection contre les abus", reference: "Convention Européenne des Droits de l'Homme, Art. 6 & 13", content: "Toute personne a droit à ce que sa cause soit entendue équitablement. Pouvoir nommer un abus devant un tiers impartial brise l'isolement et rétablit l'équilibre rompu." },
    { id: 1022, title: "La Présomption d'Innocence et l'Équité", category: "Droit & protection contre les abus", reference: "Déclaration des Droits de l'Homme, Art. 9", content: "Condamner sans preuves ni contradictoire relève de la rumeur, non de la justice. L'esprit équitable examine les faits avec rigueur avant de porter un jugement sur autrui." },
    { id: 1023, title: "La Nullité des Engagements Asservissants", category: "Droit & protection contre les abus", reference: "Théorie des Libertés Fondamentales", content: "Nul ne peut aliéner définitivement sa liberté par contrat. Un engagement qui détruit l'autonomie morale ou physique de la personne est nul par essence." },
    { id: 1024, title: "La Loi qui Protège le Faible", category: "Droit & protection contre les abus", reference: "Lacordaire — Philosophie du Droit", content: "Entre le fort et le faible, entre le maître et le sujet, c'est parfois la liberté sans règle qui opprime, et la règle juste qui affranchit en posant une limite infranchissable à la prédation." },

    // III. NEUROBIOLOGIE DE LA CONSCIENCE ET DE LA PAIX (25–36)
    { id: 1025, title: "Le Cortex Préfrontal, Chef d'Orchestre", category: "Neurobiologie de la conscience", reference: "Neurosciences Cognitives — Fonctions Exécutives", content: "Siège de l'anticipation, de la nuance et de la maîtrise de soi, le cortex préfrontal nous permet de prendre du recul face à l'impulsion immédiate pour choisir une réponse réfléchie plutôt qu'une réaction automatique." },
    { id: 1026, title: "L'Apaisement de l'Amygdale Cérébrale", category: "Neurobiologie de la conscience", reference: "Neurobiologie des Émotions", content: "Sentinelle d'alerte face au danger, l'amygdale s'emballe sous l'effet de la peur ou de la colère. Nommer calmement son émotion et respirer profondément réactive les voies régulatrices qui ramènent la sérénité." },
    { id: 1027, title: "La Neuroplasticité de l'Espérance", category: "Neurobiologie de la conscience", reference: "Plasticité Synaptique — Hebb & Neurosciences", content: "Le cerveau se remodèle tout au long de la vie selon les pensées et les actes que nous cultivons. Chaque choix de paix, de bonté et de lucidité renforce des sillons neuronaux durables de résilience." },
    { id: 1028, title: "Le Nerf Vague et le Souffle de Paix", category: "Neurobiologie de la conscience", reference: "Système Nerveux Parasympathique", content: "Une expiration lente et consciente stimule le nerf vague, ralentit le rythme cardiaque et libère l'esprit de l'état de survie, rouvrant l'accès à l'intelligence relationnelle et créative." },
    { id: 1029, title: "Les Neurones Miroirs et l'Empathie Juste", category: "Neurobiologie de la conscience", reference: "Neurosciences Sociales — Rizzolatti", content: "Notre cerveau résonne naturellement avec la joie ou la souffrance d'autrui. Cultiver la bienveillance transforme cette résonance spontanée en compassion active, sans se laisser submerger par la détresse." },
    { id: 1030, title: "Le Réseau du Mode par Défaut et le Silence", category: "Neurobiologie de la conscience", reference: "Imagerie Cérébrale & Contemplation", content: "Lorsque le tumulte des sollicitations cesse, le cerveau intègre l'expérience vécue, consolide la mémoire autobiographique et donne sens à notre trajectoire intérieure." },
    { id: 1031, title: "La Dopamine du Sens vs l'Addiction", category: "Neurobiologie de la conscience", reference: "Circuits de la Récompense & Motivation", content: "Les stimulations artificielles épuisent l'attention par des pics éphémères, tandis que l'effort accompli avec sens procure un élan vital stable, constructif et profondément satisfaisant." },
    { id: 1032, title: "La Sérotonine et la Gratitude", category: "Neurobiologie de la conscience", reference: "Neurochimie de l'Équilibre Émotionnel", content: "Reconnaître ce qui est bon et juste dans l'instant présent stabilise l'humeur, apaise l'anxiété de manque et renforce la clarté mentale face aux défis." },
    { id: 1033, title: "L'Ocytocine et le Lien de Confiance", category: "Neurobiologie de la conscience", reference: "Neurobiologie de l'Attachement", content: "La parole loyale, le respect mutuel et la douceur libèrent les médiateurs biologiques de la confiance, dissipant la méfiance défensive qui isole les consciences." },
    { id: 1034, title: "La Mémoire de Travail Libérée", category: "Neurobiologie de la conscience", reference: "Psychologie Cognitive — Charge Mentale", content: "La rumination et le mensonge saturent la mémoire de travail. Vivre dans la vérité et le pardon libère une immense bande passante cognitive pour comprendre, aimer et bâtir." },
    { id: 1035, title: "L'Intéroception : Écouter les Signaux du Corps", category: "Neurobiologie de la conscience", reference: "Insula & Conscience Corporelle — Damasio", content: "Avant même que le raisonnement ne s'achève, le corps perçoit souvent le malaise d'une situation injuste ou manipulatrice. Écouter cette boussole intérieure affine la prudence." },
    { id: 1036, title: "L'Harmonie Hémisphérique", category: "Neurobiologie de la conscience", reference: "Neurosciences Intégratives", content: "La sagesse naît de l'alliance entre l'analyse précise des détails et la vision globale du sens. Unir la rigueur logique à l'intuition éthique rend l'esprit pleinement vivant." },

    // IV. RÉSOLUTION PACIFIQUE ET HYGIÈNE RELATIONNELLE (37–48)
    { id: 1037, title: "Distinguer les Faits des Interprétations", category: "Hygiène relationnelle & paix", reference: "Communication Consciente & Médiation", content: "Observer un fait sans y mêler immédiatement un procès d'intention désamorce la plupart des querelles. La clarté descriptive ouvre la porte au dialogue là où l'accusation la referme." },
    { id: 1038, title: "Poser une Limite avec Douceur et Fermeté", category: "Hygiène relationnelle & paix", reference: "Assertivité & Respect de Soi", content: "Dire « non » à un abus n'est pas un acte d'agression, mais un acte de respect envers soi-même et envers la vérité de la relation. La vraie douceur possède une colonne vertébrale." },
    { id: 1039, title: "Refuser le Triangle Dramatique", category: "Hygiène relationnelle & paix", reference: "Analyse Transactionnelle — Karpman", content: "Sortir des rôles piégés de persécuteur, de sauveur intrusif ou de victime résignée permet de retrouver une relation d'adulte à adulte, fondée sur la responsabilité et l'autonomie." },
    { id: 1040, title: "Déjouer la Culpabilisation Injuste", category: "Hygiène relationnelle & paix", reference: "Psychologie de la Manipulation", content: "La culpabilité saine signale une erreur réelle à réparer ; la culpabilité toxique est un levier utilisé par autrui pour soumettre notre liberté. Discerner l'une de l'autre affranchit le cœur." },
    { id: 1041, title: "La Force du Silence Réfléchi", category: "Hygiène relationnelle & paix", reference: "Maîtrise de Soi & Sagesse Antique", content: "Ne pas répondre instantanément à la provocation brise la chaîne de l'escalade. Ce court espace de silence entre le stimulus et la réponse est le lieu même de notre liberté." },
    { id: 1042, title: "La Réparation plutôt que la Vengeance", category: "Hygiène relationnelle & paix", reference: "Justice Restaurative", content: "La vengeance perpétue la blessure en l'inversant ; la justice restaurative cherche à rétablir le droit, à reconnaître le tort et à libérer l'avenir du poids de la rancœur." },
    { id: 1043, title: "Le Pardon qui Libère la Mémoire", category: "Hygiène relationnelle & paix", reference: "Philosophie & Psychologie du Pardon", content: "Pardonner ne consiste ni à oublier la vérité ni à excuser l'injustice, mais à refuser que l'offense passée continue d'empoisonner notre paix présente." },
    { id: 1044, title: "Reconnaître le Gaslighting (Déni de Réalité)", category: "Hygiène relationnelle & paix", reference: "Clinique de l'Emprise Psychologique", content: "Lorsqu'un discours cherche systématiquement à vous faire douter de votre perception, de votre mémoire ou de votre bon sens, s'appuyer sur les faits objectifs et des témoins fiables restaure l'ancrage." },
    { id: 1045, title: "Sortir de la Double Contrainte", category: "Hygiène relationnelle & paix", reference: "École de Palo Alto — Bateson", content: "Face à deux injonctions contradictoires où chaque choix est critiqué, la liberté consiste à nommer calmement le paradoxe et à refuser les termes d'un jeu truqué." },
    { id: 1046, title: "La Parole qui Édifie", category: "Hygiène relationnelle & paix", reference: "Éthique du Langage", content: "Les mots peuvent blesser comme des lames ou soigner comme un baume. Choisir une parole vraie, mesurée et constructive assainit durablement l'écosystème humain." },
    { id: 1047, title: "La Patience Active dans l'Épreuve", category: "Hygiène relationnelle & paix", reference: "Résilience & Persévérance", content: "La patience n'est pas la passivité résignée : c'est la force tranquille qui continue d'œuvrer pour le bien sans se laisser corrompre par l'amertume ou la précipitation." },
    { id: 1048, title: "L'Alliance de la Vérité et de la Bienveillance", category: "Hygiène relationnelle & paix", reference: "Éthique Relationnelle", content: "La vérité sans bienveillance devient brutalité ; la bienveillance sans vérité devient complaisance. Leur union engendre une parole juste qui éclaire sans humilier." },

    // V. ÉTHIQUE, RESPONSABILITÉ ET CITOYENNETÉ ÉCLAIRÉE (49–60)
    { id: 1049, title: "L'Impératif de Dignité Humaine", category: "Éthique & responsabilité", reference: "Philosophie Morale — Kant", content: "Agis toujours de telle sorte que tu traites l'humanité, aussi bien dans ta personne que dans la personne de tout autre, toujours en même temps comme une fin, et jamais simplement comme un moyen." },
    { id: 1050, title: "La Responsabilité Individuelle face au Groupe", category: "Éthique & responsabilité", reference: "Éthique de la Conscience", content: "L'obéissance passive à un ordre injuste n'efface jamais la responsabilité morale de la conscience. Garder son discernement éveillé au sein du collectif protège l'humanité des dérives aveugles." },
    { id: 1051, title: "L'Équité au-delà du Formalisme", category: "Éthique & responsabilité", reference: "Aristote — Éthique à Nicomaque", content: "L'équité est la sagesse qui corrige la rigidité mécanique de la règle en tenant compte de la réalité singulière des personnes, afin d'accomplir l'esprit véritable de la justice." },
    { id: 1052, title: "Le Respect de la Vie Privée et de l'Intimité", category: "Éthique & responsabilité", reference: "Code civil, Art. 9 — Droit à la vie privée", content: "Chacun a droit au respect de sa vie privée et de son jardin secret. Préserver un espace d'intimité loin des regards intrusifs est indispensable à l'équilibre psychique et à la liberté." },
    { id: 1053, title: "L'Intégrité Intellectuelle", category: "Éthique & responsabilité", reference: "Déontologie & Recherche du Vrai", content: "Reconnaître un fait vrai même lorsqu'il contrarie notre intérêt immédiat est le sommet de la probité intellectuelle. C'est sur cette honnêteté que repose toute confiance durable." },
    { id: 1054, title: "La Vigilance Numérique et l'Attention", category: "Éthique & responsabilité", reference: "Économie de l'Attention & Liberté Cognitive", content: "Notre attention est notre ressource mentale la plus précieuse. Ne pas la livrer sans filtre aux algorithmes de colère ou de distraction préserve notre souveraineté intérieure." },
    { id: 1055, title: "Le Devoir de Secours et de Solidarité", category: "Éthique & responsabilité", reference: "Principe de Fraternité & Assistance", content: "La liberté véritable n'est pas l'indifférence solitaire : elle s'accomplit dans le souci de celui qui vacille et dans le courage de tendre la main à qui subit l'injustice." },
    { id: 1056, title: "La Promesse et la Parole Donnée", category: "Éthique & responsabilité", reference: "Philosophie du Droit — Hannah Arendt", content: "Face à l'incertitude de l'avenir, la capacité de tenir une promesse juste crée des îlots de stabilité et d'alliance où les êtres humains peuvent bâtir ensemble." },
    { id: 1057, title: "La Mesure dans l'Exercice de l'Autorité", category: "Éthique & responsabilité", reference: "Montesquieu — De l'Esprit des Lois", content: "Toute autorité légitime est un service ordonné au bien commun, jamais une domination arbitraire. Le pouvoir juste se reconnaît à ce qu'il élève et autonomise ceux qui lui sont confiés." },
    { id: 1058, title: "La Transmission d'un Héritage de Sens", category: "Éthique & responsabilité", reference: "Éthique du Futur", content: "Chaque génération est dépositaire de la liberté de conscience conquise au fil des siècles. La cultiver en soi et la transmettre intacte est un acte de gratitude envers l'avenir." },
    { id: 1059, title: "L'Alliance du Droit et de la Conscience", category: "Éthique & responsabilité", reference: "Philosophie Juridique", content: "Le droit trace les digues extérieures contre la violence, tandis que la conscience morale irrigue le cœur de justice et de bonté. Tous deux sont nécessaires à la paix." },
    { id: 1060, title: "La Paix Intérieure comme Acte Civique", category: "Éthique & responsabilité", reference: "Sagesse & Vie Commune", content: "Un esprit pacifié, lucide et maître de ses passions ne se laisse ni acheter par la flatterie ni gouverner par la terreur : il devient un pilier vivant de paix pour tous ceux qui l'entourent." },

    // VI. MÉTACOGNITION, RÉSILIENCE ET TRANSCENDANCE (61–72)
    { id: 1061, title: "L'Observateur Intérieur (Métacognition)", category: "Métacognition & résilience", reference: "Psychologie Cognitive — Métacognition", content: "Vous n'êtes pas vos pensées parasites : vous êtes la conscience qui les observe passer. Prendre ce recul dissout instantanément l'identification anxieuse au tumulte mental." },
    { id: 1062, title: "Le Recadrage Cognitif Créateur", category: "Métacognition & résilience", reference: "Thérapie Cognitive & Stoïcisme", content: "Ce ne sont pas les événements seuls qui troublent l'humain, mais le jugement figé qu'il porte sur eux. Transformer le regard porté sur l'obstacle révèle souvent un chemin d'apprentissage inattendu." },
    { id: 1063, title: "L'Antifragilité de l'Esprit", category: "Métacognition & résilience", reference: "Psychologie de la Croissance Post-Épreuve", content: "Là où ce qui est rigide se brise sous le choc, la conscience souple et enracinée dans le sens traverse l'épreuve en gagnant en profondeur, en compassion et en lucidité." },
    { id: 1064, title: "La Cohérence entre Pensée, Parole et Acte", category: "Métacognition & résilience", reference: "Éthique de l'Unité Intérieure", content: "La paix profonde naît lorsque ce que l'on pense en secret, ce que l'on exprime au grand jour et ce que l'on accomplit dans ses actes forment une seule et même mélodie sans dissonance." },
    { id: 1065, title: "La Déprise face à l'Illusion du Contrôle Total", category: "Métacognition & résilience", reference: "Sagesse & Sérénité", content: "Discerner clairement ce qui dépend de notre libre volonté et ce qui n'en dépend pas libère l'esprit d'une angoisse stérile pour concentrer toute notre énergie sur l'action juste." },
    { id: 1066, title: "La Mémoire Vivante contre l'Amnésie Morale", category: "Métacognition & résilience", reference: "Philosophie de l'Histoire & Conscience", content: "Se souvenir des leçons de l'histoire et des mécanismes de l'arbitraire vaccinent la conscience contre le retour des mêmes illusions déguisées sous des habits nouveaux." },
    { id: 1067, title: "L'Attention Pure comme Forme de Générosité", category: "Métacognition & résilience", reference: "Philosophie de l'Attention — Simone Weil", content: "Accorder à un être ou à une question son attention pleine, calme et sans préjugé est l'acte le plus rare et le plus pur de l'intelligence aimante." },
    { id: 1068, title: "Le Courage d'Être Vrai", category: "Métacognition & résilience", reference: "Authenticité & Dignité", content: "Porter un masque pour plaire épuise l'âme. Oser habiter sa vérité avec humilité et bienveillance attire les alliances sincères et dissipe les faux-semblants." },
    { id: 1069, title: "La Beauté qui Éveille l'Esprit", category: "Métacognition & résilience", reference: "Esthétique & Élévation Intérieure", content: "Contempler l'harmonie du cosmos, d'une œuvre ou d'un geste noble élargit l'horizon mental au-delà des mesquineries et rappelle à l'esprit sa vocation à la grandeur." },
    { id: 1070, title: "La Joie Lucide", category: "Métacognition & résilience", reference: "Philosophie de la Joie — Spinoza & Bergson", content: "La joie véritable n'est pas l'insouciance aveugle, mais le signe vivant que notre esprit passe à une plus grande perfection, en accord avec la vérité et la création." },
    { id: 1071, title: "L'Unité de l'Intelligence et du Cœur", category: "Métacognition & résilience", reference: "Anthropologie Intégrale", content: "Une science sans conscience s'égare ; une émotion sans raison s'aveugle. L'être humain accompli marche sur ses deux jambes : la clarté de l'esprit critique et la chaleur de l'amour." },
    { id: 1072, title: "Plénitude de la Conscience Libre", category: "Métacognition & résilience", reference: "Synthèse Noôrbis — Couronne des 72 Fragments", content: "Affranchi de la peur, éclairé par le droit, enraciné dans la paix et guidé par les fruits de l'Esprit, le navigateur intérieur rayonne d'une liberté que nulle ombre ne peut éteindre." }
  ];

  function getBrainBonusForLevel(levelNumber) {
    const idx = Math.max(0, levelNumber - 1) % BrainBonusFragments.length;
    return BrainBonusFragments[idx];
  }

  function getBrainBonusMeditationText(levelNumber) {
    const frag = getBrainBonusForLevel(levelNumber);
    return {
      id: frag.id,
      fruit: `🧠 Cerveau : ${frag.category}`,
      situation: frag.title,
      reference: frag.reference,
      content: frag.content
    };
  }

  // Meditations Matrix (810 texts loaded from JSON + instant fallback generator)
  let allMeditations = [];
  let isMatrixLoaded = false;

  function buildFallbackMeditations() {
    const list = [];
    let idCounter = 1;
    const sampleRefs = [
      "Galates 5:22-23", "Jean 14:27", "Philippiens 4:6-7", "Romains 8:28",
      "Psaume 23:1-4", "1 Corinthiens 13:4-7", "Ésaïe 40:31", "Proverbes 4:23",
      "Jacques 1:19-20", "Matthieu 5:9", "Colossiens 3:12-15", "2 Timothée 1:7"
    ];
    for (let f = 0; f < FRUITS.length; f++) {
      for (let s = 0; s < SITUATIONS.length; s++) {
        for (let k = 0; k < 6; k++) {
          const fruit = FRUITS[f];
          const situation = SITUATIONS[s];
          const ref = sampleRefs[(f + s + k) % sampleRefs.length];
          list.push({
            id: idCounter++,
            fruit: fruit,
            situation: situation,
            reference: ref,
            content: `Au cœur de « ${situation.toLowerCase()} », ${fruit.toLowerCase()} demeure une ancre lumineuse pour la conscience. En choisissant la paix intérieure, la vérité et la maîtrise de soi, l'esprit traverse les turbulences sans perdre sa clarté ni sa dignité (${ref}).`
          });
        }
      }
    }
    return list;
  }

  // Initialize with fallback immediately so the game never waits or fails
  allMeditations = buildFallbackMeditations();

  async function loadMeditationsMatrix() {
    if (isMatrixLoaded) return allMeditations;
    if (Array.isArray(window.NOORBIS_MEDITATIONS_MATRIX) && window.NOORBIS_MEDITATIONS_MATRIX.length > 0) {
      allMeditations = window.NOORBIS_MEDITATIONS_MATRIX;
      isMatrixLoaded = true;
      return allMeditations;
    }
    const candidatePaths = [
      "meditations_matrix.json",
      "app/src/main/assets/meditations_matrix.json",
      "../meditations_matrix.json"
    ];
    for (const path of candidatePaths) {
      try {
        const resp = await fetch(path);
        if (resp.ok) {
          const data = await resp.json();
          if (Array.isArray(data) && data.length > 0) {
            allMeditations = data;
            window.NOORBIS_MEDITATIONS_MATRIX = data;
            isMatrixLoaded = true;
            return allMeditations;
          }
        }
      } catch (_) {
        // Try next path
      }
    }
    return allMeditations;
  }

  function getNextMeditationForFruit(fruitName, collectedIdsSet, levelNumber) {
    const normalized = fruitName.trim().toLowerCase();
    const pool = allMeditations.filter(m => m.fruit.trim().toLowerCase() === normalized);
    if (pool.length === 0) return allMeditations[0];

    const targetSituation = SITUATIONS[Math.floor(Math.max(0, levelNumber - 1) / 9) % SITUATIONS.length];
    const situationPool = pool.filter(m => m.situation.trim().toLowerCase() === targetSituation.toLowerCase());

    const uncollectedSit = situationPool.find(m => !collectedIdsSet.has(m.id));
    if (uncollectedSit) return uncollectedSit;

    const uncollectedAny = pool.find(m => !collectedIdsSet.has(m.id));
    if (uncollectedAny) return uncollectedAny;

    if (situationPool.length > 0) {
      return situationPool[Math.max(0, levelNumber - 1) % situationPool.length];
    }
    return pool[Math.max(0, levelNumber - 1) % pool.length];
  }

  function getMeditationById(id) {
    if (id >= 1001 && id <= 1072) {
      const frag = BrainBonusFragments.find(b => b.id === id);
      if (frag) {
        return {
          id: frag.id,
          fruit: `🧠 Cerveau : ${frag.category}`,
          situation: frag.title,
          reference: frag.reference,
          content: frag.content
        };
      }
    }
    return allMeditations.find(m => m.id === id) || null;
  }

  // ContemplativeService: Keyword challenge & 6 feelings reflection
  const FeelingTypes = [
    { id: "PAIX", label: "M'apaise", emoji: "🕊️" },
    { id: "COURAGE", label: "Me donne du courage", emoji: "🦁" },
    { id: " CONFIANCE", label: "M'aide à lâcher prise", emoji: "🌿" },
    { id: "ESPERANCE", label: "Éclaire mon avenir", emoji: "✨" },
    { id: "PARDON", label: "M'invite à pardonner", emoji: "🤝" },
    { id: "GRATITUDE", label: "M'inspire la gratitude", emoji: "🙏" }
  ];

  const stopWords = new Set([
    "dans", "avec", "pour", "sans", "sous", "vers", "chez", "mais", "donc", "alors",
    "comme", "plus", "moins", "tout", "tous", "toute", "toutes", "cette", "celui",
    "celle", "ceux", "celles", "notre", "votre", "leur", "leurs", "meme", "aussi",
    "etre", "avoir", "faire", "dire", "voir", "savoir", "pouvoir", "vouloir",
    "quand", "comment", "pourquoi", "parce", "contre", "entre", "ainsi", "encore"
  ]);

  function normalizeText(str) {
    return (str || "").normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase();
  }

  function createKeywordChallenge(meditation) {
    const text = meditation.content || "";
    const words = text.split(/(\s+|[.,;!?«»"()—-]+)/);
    const candidates = [];
    for (let i = 0; i < words.length; i++) {
      const clean = words[i].replace(/[^a-zA-ZÀ-ÿ]/g, "");
      if (clean.length >= 6 && !stopWords.has(normalizeText(clean))) {
        candidates.push({ index: i, word: clean });
      }
    }
    if (candidates.length === 0) return null;
    const chosen = candidates[(meditation.id * 7) % candidates.length];
    const CorrectWord = chosen.word;

    const distractorPool = [
      "sagesse", "lumière", "patience", "courage", "justice", "liberté",
      "espérance", "confiance", "douceur", "vérité", "conscience", "harmonie"
    ].filter(w => normalizeText(w) !== normalizeText(CorrectWord));

    const d1 = distractorPool[meditation.id % distractorPool.length];
    const d2 = distractorPool[(meditation.id + 5) % distractorPool.length];

    const options = [CorrectWord, d1, d2].sort((a, b) => normalizeText(a).localeCompare(normalizeText(b)));
    const maskedWords = [...words];
    maskedWords[chosen.index] = " [ ______ ] ";

    return {
      maskedContent: maskedWords.join(""),
      correctWord: CorrectWord,
      options: options
    };
  }

  function generateReflection(meditation, feeling) {
    return `En choisissant « ${feeling.emoji} ${feeling.label} » face à cette pensée (${meditation.reference}), vous ancrez ${meditation.fruit.toLowerCase()} au cœur de la situation « ${meditation.situation} ». Que cette clarté accompagne chacun de vos choix.`;
  }

  return {
    AvailableSphereColors,
    FRUITS,
    SITUATIONS,
    ObstacleTypes,
    SimpleObstacleCategories,
    SimpleObstacleVariants,
    PowerUpTypes,
    RescueTargetTypes,
    VirtueRemedyTypes,
    NeuronCorruptionTypes,
    BrainBonusFragments,
    FeelingTypes,
    getRemedyAffinity,
    getSector,
    getAllSectors,
    getBrainBonusForLevel,
    getBrainBonusMeditationText,
    loadMeditationsMatrix,
    getNextMeditationForFruit,
    getMeditationById,
    createKeywordChallenge,
    generateReflection,
    normalizeText,
    getAllMeditations: () => allMeditations
  };
})();
