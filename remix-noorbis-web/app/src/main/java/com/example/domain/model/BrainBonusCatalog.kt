package com.example.domain.model

data class BrainBonusFragment(
    val levelNumber: Int,
    val title: String,
    val source: String,
    val content: String,
    val category: String,
    val points: Int = 150
) {
    val fullDisplayText: String
        get() = buildString {
            append("🧠 FRAGMENT BONUS #").append(levelNumber).append(" • ").append(title).append("\n")
            append("📂 Catégorie : ").append(category).append("\n")
            if (source.isNotBlank()) {
                append("📜 Source : ").append(source).append("\n\n")
            } else {
                append("\n")
            }
            append(content)
        }
}

object BrainBonusCatalog {

    val fragments: List<BrainBonusFragment> = listOf(
        // 1. Les préceptes fondamentaux et droit coutumier (1 à 3)
        BrainBonusFragment(
            levelNumber = 1,
            title = "Juris praecepta sunt haec",
            source = "Droit coutumier & Lexique du Collectif",
            category = "1. Préceptes fondamentaux",
            content = "« Juris praecepta sunt haec : honeste vivere, alterum non laedere, suum cuique tribuere » :\nLes principes fondamentaux du droit sont : vivre honnêtement, ne pas faire de tort à autrui, rendre à chacun ce qui lui revient."
        ),
        BrainBonusFragment(
            levelNumber = 2,
            title = "La Nécessité faisant Loi",
            source = "Principe coutumier fondamental",
            category = "1. Préceptes fondamentaux",
            content = "« La Nécessité faisant Loi » : principe invoqué pour justifier l'établissement et la codification d'un référentiel juridique commun entre particuliers."
        ),
        BrainBonusFragment(
            levelNumber = 3,
            title = "Enlevez persona, homo tient bon",
            source = "Doyen Jean Carbonnier",
            category = "1. Préceptes fondamentaux",
            content = "« Enlevez persona, homo tient bon » : soulignant que l'être humain réel préexiste à la fiction légale ou au masque juridique."
        ),

        // 2. La charge de la preuve et l'administration du litige (4 à 9)
        BrainBonusFragment(
            levelNumber = 4,
            title = "Ei incumbit probatio qui dicit",
            source = "Maxime romaine de preuve",
            category = "2. Charge de la preuve",
            content = "« Ei incumbit probatio qui dicit, non qui negat / Affirmanti incumbit probatio / Actori incumbit probatio » :\nLa preuve incombe à celui qui allègue, ou « La preuve incombe à celui qui affirme, non à celui qui nie »."
        ),
        BrainBonusFragment(
            levelNumber = 5,
            title = "Actore non probante, reus absolvitur",
            source = "Règle d'administration du litige",
            category = "2. Charge de la preuve",
            content = "« Actore non probante, reus absolvitur » :\nSi le demandeur n'apporte pas la preuve qui lui incombe, le défendeur doit être relaxé."
        ),
        BrainBonusFragment(
            levelNumber = 6,
            title = "Factum negantis nulla probatio",
            source = "Principe probatoire",
            category = "2. Charge de la preuve",
            content = "« Factum negantis nulla probatio » :\nLes faits négatifs ne sont pas des preuves."
        ),
        BrainBonusFragment(
            levelNumber = 7,
            title = "Incerta pro nullis habentur",
            source = "Règle de certitude",
            category = "2. Charge de la preuve",
            content = "« Incerta pro nullis habentur » :\nLes choses incertaines sont considérées comme nulles."
        ),
        BrainBonusFragment(
            levelNumber = 8,
            title = "Falsus in uno, falsus in omnibus",
            source = "Intégrité du témoignage",
            category = "2. Charge de la preuve",
            content = "« Falsus in uno, falsus in omnibus » :\nFaux pour une chose, faux pour le tout."
        ),
        BrainBonusFragment(
            levelNumber = 9,
            title = "Présomption non réfutée fait état de Vérité",
            source = "Droit contractuel et administratif",
            category = "2. Charge de la preuve",
            content = "« Une présomption qui n’a pas été réfutée fait état de Vérité » : règle posée comme fondamentale dans les échanges contractuels et administratifs."
        ),

        // 3. Le contrat, le consentement et la juridiction (10 à 18)
        BrainBonusFragment(
            levelNumber = 10,
            title = "Consensus facit legem",
            source = "Principe consensuel",
            category = "3. Contrat et juridiction",
            content = "« Consensus facit legem » :\nLe consentement fait la loi."
        ),
        BrainBonusFragment(
            levelNumber = 11,
            title = "Legem Enim Contractus Dat",
            source = "Autonomie de la volonté",
            category = "3. Contrat et juridiction",
            content = "« Legem Enim Contractus Dat » :\nLe contrat fait la loi et le droit."
        ),
        BrainBonusFragment(
            levelNumber = 12,
            title = "Pacta sunt servanda",
            source = "Force obligatoire des conventions",
            category = "3. Contrat et juridiction",
            content = "« Pacta sunt servanda » :\nLe respect absolu des engagements contractuels valablement formés."
        ),
        BrainBonusFragment(
            levelNumber = 13,
            title = "Qui tacet consentire videtur",
            source = "Consentement tacite",
            category = "3. Contrat et juridiction",
            content = "« Qui tacet consentire videtur » :\n« Qui ne dit mot semble consentir » / « Qui ne dit mot, consent »."
        ),
        BrainBonusFragment(
            levelNumber = 14,
            title = "Non videntur qui errant consentire",
            source = "Vices du consentement",
            category = "3. Contrat et juridiction",
            content = "« Non videntur qui errant consentire » :\nQui commet une erreur n’est pas considéré comme avoir consenti."
        ),
        BrainBonusFragment(
            levelNumber = 15,
            title = "Quaelibet jurisdictio cancellos suos habet",
            source = "Limites juridictionnelles",
            category = "3. Contrat et juridiction",
            content = "« Quaelibet jurisdictio cancellos suos habet » :\nToute juridiction a ses limites."
        ),
        BrainBonusFragment(
            levelNumber = 16,
            title = "Disparata non debent jungi",
            source = "Règle de séparation de nature",
            category = "3. Contrat et juridiction",
            content = "« Disparata non debent jungi » :\nDes choses inégales ne doivent pas se joindre."
        ),
        BrainBonusFragment(
            levelNumber = 17,
            title = "Nullité d'un acte vicié",
            source = "Principe de nullité ab initio",
            category = "3. Contrat et juridiction",
            content = "« Ce qui est nul ne produit aucun effet » : principe de nullité des actes viciés dès l'origine."
        ),
        BrainBonusFragment(
            levelNumber = 18,
            title = "Personne morale vs Membres",
            source = "Distinction juridique de l'entité",
            category = "3. Contrat et juridiction",
            content = "« Ce qui appartient à une personne morale n’appartient pas à ses membres » : distinction fondamentale entre l'entité et l'individu."
        ),

        // 4. La coutume, la réalité et l'autorité (19 à 27)
        BrainBonusFragment(
            levelNumber = 19,
            title = "Consuetudo legis habet vigorem",
            source = "Droit coutumier",
            category = "4. Coutume et autorité",
            content = "« Consuetudo legis habet vigorem » :\nLa coutume a force de loi."
        ),
        BrainBonusFragment(
            levelNumber = 20,
            title = "Coutume passe droit",
            source = "Antoine Loisel",
            category = "4. Coutume et autorité",
            content = "« Coutume passe droit » / « Coutume vainct droit » (Antoine Loisel) : primauté accordée aux usages et au droit coutumier."
        ),
        BrainBonusFragment(
            levelNumber = 21,
            title = "Quid fas non veritas est",
            source = "Maxime philosophique",
            category = "4. Coutume et autorité",
            content = "« Quid fas non veritas est » :\nLégalité n’est pas Réalité."
        ),
        BrainBonusFragment(
            levelNumber = 22,
            title = "Force n’est pas droit",
            source = "Fondement de la légitimité",
            category = "4. Coutume et autorité",
            content = "« Force n’est pas droit » : rejet de la contrainte pure comme fondement de légitimité juridique."
        ),
        BrainBonusFragment(
            levelNumber = 23,
            title = "Nulle Servitude sans titre",
            source = "Liberté naturelle",
            category = "4. Coutume et autorité",
            content = "« Nulle Servitude sans titre » : impossibilité d'assujettir sans acte juridique initial régulier."
        ),
        BrainBonusFragment(
            levelNumber = 24,
            title = "Fiducia est audacia",
            source = "Cicéron",
            category = "4. Coutume et autorité",
            content = "« Fiducia est audacia » (Cicéron) :\nConfiance est audace."
        ),
        BrainBonusFragment(
            levelNumber = 25,
            title = "Qui s’excuse s’accuse",
            source = "Mise en garde juridique",
            category = "4. Coutume et autorité",
            content = "« Qui s’excuse s’accuse » : mise en garde contre toute justification spontanée face à une autorité."
        ),
        BrainBonusFragment(
            levelNumber = 26,
            title = "Lex non cogit impossibilia",
            source = "Maxime de bon sens universel",
            category = "4. Coutume et autorité",
            content = "« Lex non cogit impossibilia / À l'impossible nul n'est tenu » : limite matérielle des obligations imposables."
        ),
        BrainBonusFragment(
            levelNumber = 27,
            title = "Ignorer la loi n'est pas excuse",
            source = "Maxime de commerce strict",
            category = "4. Coutume et autorité",
            content = "« Ignorer la loi n'est pas excuse » : identifiée dans les textes comme une maxime commerciale stricte opposée à l'administré."
        ),

        // 5. Les principes dits de la Lex Mercatoria (28 à 37)
        BrainBonusFragment(
            levelNumber = 28,
            title = "Dignité du travail",
            source = "Lex Mercatoria (Principe 1)",
            category = "5. Lex Mercatoria",
            content = "« Un ouvrier est digne de son salaire » (A workman is worthy of his hire)."
        ),
        BrainBonusFragment(
            levelNumber = 29,
            title = "Égalité devant la Loi",
            source = "Lex Mercatoria (Principe 2)",
            category = "5. Lex Mercatoria",
            content = "« Tous sont égaux devant la Loi » (All are equal under the Law)."
        ),
        BrainBonusFragment(
            levelNumber = 30,
            title = "Souveraineté de la Vérité",
            source = "Lex Mercatoria (Principe 3)",
            category = "5. Lex Mercatoria",
            content = "« Dans la Loi, la vérité est souveraine » (In Law, truth is sovereign)."
        ),
        BrainBonusFragment(
            levelNumber = 31,
            title = "Expression sous forme d’Affidavit",
            source = "Lex Mercatoria (Principe 4)",
            category = "5. Lex Mercatoria",
            content = "« La vérité est exprimée sous la forme d’un affidavit » (Truth is expressed in the form of an Affidavit)."
        ),
        BrainBonusFragment(
            levelNumber = 32,
            title = "L'Affidavit non réfuté est la vérité",
            source = "Lex Mercatoria (Principe 5)",
            category = "5. Lex Mercatoria",
            content = "« Un Affidavit non réfuté est la vérité » (An unrebutted Affidavit stands as truth in Law)."
        ),
        BrainBonusFragment(
            levelNumber = 33,
            title = "L'Affidavit non réfuté devient jugement",
            source = "Lex Mercatoria (Principe 6)",
            category = "5. Lex Mercatoria",
            content = "« Un Affidavit non réfuté devient le jugement en Loi » (An unrebutted Affidavit becomes the judgment in Law)."
        ),
        BrainBonusFragment(
            levelNumber = 34,
            title = "Expression nécessaire pour résolution",
            source = "Lex Mercatoria (Principe 7)",
            category = "5. Lex Mercatoria",
            content = "« Toute affaire doit être exprimée afin d’être résolue » (All matters must be expressed to be resolved)."
        ),
        BrainBonusFragment(
            levelNumber = 35,
            title = "Perte par défaut",
            source = "Lex Mercatoria (Principe 8)",
            category = "5. Lex Mercatoria",
            content = "« Celui qui quitte le champ de bataille en premier perd par défaut » (He who leaves the battlefield first loses by default)."
        ),
        BrainBonusFragment(
            levelNumber = 36,
            title = "Le sacrifice mesure la crédibilité",
            source = "Lex Mercatoria (Principe 9)",
            category = "5. Lex Mercatoria",
            content = "« Le sacrifice est une mesure de crédibilité » (Sacrifice is the measure of credibility)."
        ),
        BrainBonusFragment(
            levelNumber = 37,
            title = "Résolution stricte de réclamation",
            source = "Lex Mercatoria (Principe 10)",
            category = "5. Lex Mercatoria",
            content = "« Une réclamation ou une affaire ne peut être résolue que par un Affidavit réfutant point par point, par un Jury ou par paiement »."
        ),

        // 6. Citations mettant en avant le Droit Négatif et l'autonomie naturelle (38 à 45)
        BrainBonusFragment(
            levelNumber = 38,
            title = "Droit Négatif et Non-Coercition",
            source = "Clint Richardson",
            category = "6. Droit Négatif & Autonomie",
            content = "« Ainsi, un droit négatif s’explique mieux comme le droit de ne pas être soumis à une \"procédure légale ou juridique\", à la violence ou à la coercition. Par conséquent, un droit négatif est le droit d’être laissé tranquille. »"
        ),
        BrainBonusFragment(
            levelNumber = 39,
            title = "L'Homme Libre face au Citoyen",
            source = "Clint Richardson",
            category = "6. Droit Négatif & Autonomie",
            content = "« Un droit négatif est le droit de ne pas être soumis aux actions et à la coercition d’un autre homme, personne ou gouvernement. Un homme libre jouit du droit négatif d’être libre en vertu de Dieu et de la nature, ce qui lui confère ses droits en tant que tel. »"
        ),
        BrainBonusFragment(
            levelNumber = 40,
            title = "Immunité Privée et Inaliénabilité",
            source = "Extrait de Droit Négatif",
            category = "6. Droit Négatif & Autonomie",
            content = "« Private ou privé = droit négatif = UNalienable (n’est pas en vente, avec ou sans consentement, sans-aliénation donc sans folie). C’est un état d’Être où tous les droits sont réservés à l’homme individuel seulement, comme l’immunité de toute loi positive (publique) établie, ce qui signifie que le \"droit\" ne peut être imposé à l’homme. »"
        ),
        BrainBonusFragment(
            levelNumber = 41,
            title = "Distinction Droits Négatifs vs Positifs",
            source = "Extrait de Droit Négatif",
            category = "6. Droit Négatif & Autonomie",
            content = "« Il faut toujours se souvenir de distinguer entre les droits négatifs (inaliénables/Naturels) de l’homme et les droits positifs de la personne dont il est le garant. Le négatif n’est pas appliqué, et est un choix ; le positif est appliqué sans choix. »"
        ),
        BrainBonusFragment(
            levelNumber = 42,
            title = "Le Droit Négatif est la Réalité",
            source = "Lexique du Collectif",
            category = "6. Droit Négatif & Autonomie",
            content = "« Le Droit Négatif est la réalité qui nécessite d'être Représentée, verbalisée, définie en Droit. »"
        ),
        BrainBonusFragment(
            levelNumber = 43,
            title = "Droits vs Simples Privilèges Révocables",
            source = "Thomas Paine (Les Droits de l'Homme, 1791)",
            category = "6. Droit Négatif & Autonomie",
            content = "« Les Droits ne peuvent être donnés ou acquis par aucune Charte parce que cela impliquerait légalement que ces Droits peuvent être révoqués et sous cette circonstance cela ne serait plus que de simples privilèges. »"
        ),
        BrainBonusFragment(
            levelNumber = 44,
            title = "Individualité, Liberté, Propriété",
            source = "Frédéric Bastiat (La Loi, 1850)",
            category = "6. Droit Négatif & Autonomie",
            content = "« La vie, la faculté, la production – en d'autres termes, l'individualité, la liberté, la propriété – voilà l'homme. Ces trois dons de Dieu précèdent toute législation humaine et sont supérieurs à elle. »"
        ),
        BrainBonusFragment(
            levelNumber = 45,
            title = "La Science de la Justice et de la Paix",
            source = "Lysander Spooner (Le Droit Naturel, 1882)",
            category = "6. Droit Négatif & Autonomie",
            content = "« La science de la justice est la science du mien et du tien. C'est la science de tout ce que les hommes ont le droit de faire ou d'exiger par la force, c'est-à-dire la science de la paix et la seule base sur laquelle les hommes peuvent vivre ensemble en sécurité. »"
        ),

        // 7. Citations contre l'endoctrinement, l'illusion et la servitude (46 à 57)
        BrainBonusFragment(
            levelNumber = 46,
            title = "L'Impuissance Apprise par Défaut",
            source = "Extrait de Droit Négatif",
            category = "7. Éveil & Lucidité",
            content = "« L’Éducation c’est l’Impuissance Apprise par défaut ! »"
        ),
        BrainBonusFragment(
            levelNumber = 47,
            title = "L'Enfermement Linguistique du Trivium",
            source = "Extrait de Droit Négatif",
            category = "7. Éveil & Lucidité",
            content = "« Encore un Corpus, un Monument (au) Mort, un registre de règles, un Code… pour programmer les gens/enfants… Les formules magiques du Trivium (Grammaire, Logique, Rhétorique) qui limitent (enfer-ment) la pensée, le vocabulaire et dirigent la pensée par des règles strictes. »"
        ),
        BrainBonusFragment(
            levelNumber = 48,
            title = "L'Illusion Universitaire",
            source = "Extrait de Droit Négatif",
            category = "7. Éveil & Lucidité",
            content = "« À la fin des programmes d’éducation publique et universitaire (reprogrammation mentale), nous pouvons recevoir un diplôme de crime appelé diplôme [...] comme une licence permissive de l’État pour commettre des maux contre la réalité. »"
        ),
        BrainBonusFragment(
            levelNumber = 49,
            title = "Choc entre Croyance et Réalité",
            source = "Clint Richardson",
            category = "7. Éveil & Lucidité",
            content = "« Croyance et Réalité se heurtent souvent, et je ne peux qu’espérer que la recherche de la Vérité réelle et évidente, telle qu’elle s’exprime dans la Réalité et non pas comme une simple perception de la Réalité qui satisfait l’intérêt personnel ou la dissonance cognitive, prévaudra sur l’esprit du lecteur. »"
        ),
        BrainBonusFragment(
            levelNumber = 50,
            title = "L'Illusion du Choix Électoral",
            source = "George Carlin",
            category = "7. Éveil & Lucidité",
            content = "« Les élections et les politiciens sont en place pour donner aux Américains l’illusion d’avoir la liberté de choix. Vous n’avez pas vraiment le choix dans ce pays. »"
        ),
        BrainBonusFragment(
            levelNumber = 51,
            title = "L'Injonction Paradoxale de Liberté",
            source = "Bill Hicks",
            category = "7. Éveil & Lucidité",
            content = "« Vous êtes libres de faire ce que nous vous disons ! »"
        ),
        BrainBonusFragment(
            levelNumber = 52,
            title = "Racines Magiques du Langage",
            source = "Jorge Luis Borges (El otro, el mismo)",
            category = "7. Éveil & Lucidité",
            content = "« On oublie souvent que (les dictionnaires) sont des dépôts artificiels, rassemblés bien après les langues qu’ils définissent. Les racines du langage sont irrationnelles et d’une nature magique. »"
        ),
        BrainBonusFragment(
            levelNumber = 53,
            title = "Le Consentement Implicite et l'Ignorance",
            source = "Extrait sur le consentement implicite",
            category = "7. Éveil & Lucidité",
            content = "« Le gouvernement \"agit\" (acte) comme s’il avait l’autorité, et nous donnons notre consentement implicite, parce que nous aussi \"agissons\" (acte) comme s’il avait l’autorité. Malheureusement, nous agissons comme cela depuis tellement longtemps que tout le monde est convaincu que tout autre comportement est impropre et que ces Lois sont vraies… instauration de l’Ignorance. »"
        ),
        BrainBonusFragment(
            levelNumber = 54,
            title = "Soyez Résolus de ne plus Servir",
            source = "Étienne de La Boétie (1576)",
            category = "7. Éveil & Lucidité",
            content = "« Soyez résolus de ne plus servir, et vous voilà libres. Je ne veux pas que vous le poussiez ou l'ébranliez, mais seulement ne le soutenez plus, et vous le verrez, tel un grand colosse dont on a dérobé la base, fondre sous son propre poids et se rompre. »"
        ),
        BrainBonusFragment(
            levelNumber = 55,
            title = "La Coutume, Moteur de la Servitude",
            source = "Étienne de La Boétie (1576)",
            category = "7. Éveil & Lucidité",
            content = "« La première raison de la servitude volontaire, c’est la coutume : comme les plus braves coursiers, qui d'abord mordent le frein et plus tard s'en jouent, et qui, naguère regimbaient contre la selle, se parent maintenant sous le harnais. »"
        ),
        BrainBonusFragment(
            levelNumber = 56,
            title = "Limiter le Spectre des Opinions",
            source = "Noam Chomsky",
            category = "7. Éveil & Lucidité",
            content = "« La manière intelligente de maintenir les gens passifs et obéissants est de limiter strictement le spectre des opinions acceptables, mais d'autoriser un débat très vif à l'intérieur de ce spectre. »"
        ),
        BrainBonusFragment(
            levelNumber = 57,
            title = "La Prison sans Murs",
            source = "Aldous Huxley (Retour au meilleur des mondes, 1958)",
            category = "7. Éveil & Lucidité",
            content = "« Une dictature parfaite aurait les apparences de la démocratie ; une prison sans murs dont les prisonniers ne songeraient pas à s'évader. Un système d'esclavage où, grâce à la consommation et au divertissement, les esclaves auraient l'amour de leur servitude. »"
        ),

        // 8. Fragments Finaux (58 à 72)
        BrainBonusFragment(
            levelNumber = 58,
            title = "Protocole Sui Iuris : La Fondation du Vivant",
            source = "Archive Décryptée (Serveur fantôme du Collectif)",
            category = "8. Fragment Final 1:1",
            content = "« À toi qui cherches à comprendre pourquoi tes chaînes ne laissent aucune marque sur ta peau, sache que le premier pas vers l'émancipation ne s'accomplit pas dans un tribunal, mais dans la chair et par l'effort. Un État de droit est censé garantir les droits fondamentaux des individus en tant qu'êtres capables de raisonner et d'agir, et non comme de simples sujets passifs. Pourtant, en exerçant un pouvoir d'autodétermination trompé, nous avons oublié la loi première et cédé nos droits naturels en échange d'avantages et d'obligations artificielles. »"
        ),
        BrainBonusFragment(
            levelNumber = 59,
            title = "Protocole Sui Iuris : L'Autonomie du Vivant",
            source = "Archive Décryptée (Le Premier Pilier)",
            category = "8. Fragment Final 1:2",
            content = "« Le commencement de la pleine capacité réside dans l'autonomie de l'être vivant. Pour faire valoir tes droits, il ne suffit pas de simplement revendiquer ton existence. Tu dois d'abord recouvrer ta capacité de production individuelle. Celui qui attend sa subsistance de la main qui l'administre ne pourra jamais s'en affranchir. »"
        ),
        BrainBonusFragment(
            levelNumber = 60,
            title = "Protocole Sui Iuris : Indépendance Matérielle",
            source = "Archive Décryptée (Le Premier Pilier)",
            category = "8. Fragment Final 1:3",
            content = "« La véritable capacité juridique est la faculté d'exercer ses droits de manière autonome et responsable. Pour t'émanciper de cette présomption d'aliénation, l'action primordiale est de te détacher de toute dépendance relative aux avantages proposés par l'administration. Ce détachement matériel et cette responsabilité individuelle démontrée sont les seules armes capables de neutraliser les moyens de pression qui te maintiennent sous tutelle.\n\nNous avons été créés pour nous gouverner nous-mêmes. La première rébellion n'est pas un contrat, c'est ta propre indépendance matérielle. Tant que tu as besoin de leurs faveurs pour exister, tu restes un artifice qu'ils dirigent. Produis pour toi-même, et la fiction commencera à s'effacer devant le réel. »"
        ),
        BrainBonusFragment(
            levelNumber = 61,
            title = "Émancipation de la Tutelle : Présomption d'Incapacité",
            source = "Document Intercepté (Classification Manuel Niveau 3)",
            category = "8. Fragment Final 2:1",
            content = "« Le plus grand tour de passe-passe de cette architecture légale repose sur une présomption silencieuse : celle de notre incapacité. Le système juridique présume par défaut que l'individu est un administrateur défaillant, placé sous tutelle ou curatelle, et par conséquent inapte à gouverner ses propres intérêts ou son patrimoine. Tant que nous continuons de souscrire aveuglément aux avantages de la sphère administrative, nous validons cette présomption d'aliénation et offrons à l'État le levier de pression idéal pour nous maintenir sous son autorité. »"
        ),
        BrainBonusFragment(
            levelNumber = 62,
            title = "Émancipation de la Tutelle : Vaisseaux Perdus en Mer",
            source = "Document Intercepté (Présomption d'absence)",
            category = "8. Fragment Final 2:2",
            content = "« L'artifice va plus loin : si nous ne nous manifestons pas à notre majorité pour réclamer notre titre équitable, l'administration nous considère juridiquement comme des vaisseaux perdus en mer, frappés d'une présomption d'absence, justifiant ainsi qu'elle continue de gérer notre fiducie à notre place. L'émancipation véritable ne se demande donc pas, elle s'acte par la rupture. »"
        ),
        BrainBonusFragment(
            levelNumber = 63,
            title = "Émancipation de la Tutelle : Actes de Droit Privé",
            source = "Document Intercepté (Responsabilité Individuelle)",
            category = "8. Fragment Final 2:3",
            content = "« Pour sortir de cette tutelle implicite, il est impératif de se détacher de toute dépendance matérielle envers l'administration et de démontrer activement sa pleine capacité juridique à travers la production d'actes contractuels relevant du Droit Privé. En assumant cette responsabilité individuelle, l'individu supprime les moyens de pression étatiques et renverse la charge de l'autorité. Pour consolider cette posture face aux institutions, notamment en ce qui concerne l'engagement de la partie adverse par sa propre inaction fautive, il est indispensable de se référer au fichier \"Droit Négatif\". »"
        ),
        BrainBonusFragment(
            levelNumber = 64,
            title = "Sentence Arbitrale : La Justice Privée est une Liberté",
            source = "Pièce à conviction N°4 (Cour d'arbitrage indépendante)",
            category = "8. Fragment Final 3:1",
            content = "« PAR CES MOTIFS, LE TRIBUNAL ARBITRAL DÉCLARE :\nConsidérant que par la convention d'arbitrage, les parties ont librement créé leur propre forum, choisi leurs propres juges et renoncé aux moyens de révision judiciaire étatique. Le droit commence précisément là où l'on choisit son mode de résolution ; si le procès judiciaire est un monopole, la justice privée est une liberté. »"
        ),
        BrainBonusFragment(
            levelNumber = 65,
            title = "Sentence Arbitrale : Preuve de Pleine Capacité d'Action",
            source = "Pièce à conviction N°4 (Lex Mercatoria)",
            category = "8. Fragment Final 3:2",
            content = "« Considérant que le Demandeur a formellement démontré sa pleine capacité d'action en s'affranchissant de toute dépendance administrative, renversant ainsi la présomption d'incapacité. En produisant un acte conforme aux principes de bonne foi exigés dans les rapports économiques par la lex mercatoria, il a valablement engagé le consentement de la partie adverse. »"
        ),
        BrainBonusFragment(
            levelNumber = 66,
            title = "Sentence Arbitrale : Autorité de la Chose Jugée",
            source = "Pièce à conviction N°4 (Décision finale)",
            category = "8. Fragment Final 3:3",
            content = "« En conséquence, le Tribunal rappelle que la présente sentence arbitrale, en tant que produit de la justice privée, possède dès qu'elle est rendue l'autorité de la chose jugée relativement à la contestation qu'elle tranche. Cette sentence revêt un caractère final et obligatoire pour les parties, qui s'engagent à l'exécuter sans délai. »"
        ),
        BrainBonusFragment(
            levelNumber = 67,
            title = "Sentence Arbitrale : Convention de New York de 1958",
            source = "Pièce à conviction N°4 (Force exécutoire internationale)",
            category = "8. Fragment Final 3:4",
            content = "« Si la partie adverse n'exécute pas cette sentence de manière volontaire, il est rappelé que cette décision s'inscrit dans le cadre strict de la Convention de New York de 1958, laquelle contraint les États signataires à reconnaître et à appliquer cette sentence privée dotée de la force exécutoire sur leur territoire. L'autonomie n'est plus une revendication, elle fait désormais Loi. »"
        ),
        BrainBonusFragment(
            levelNumber = 68,
            title = "Le Filtre du Trivium : Déchiffrer le Dog-Latin",
            source = "Outil de décryptage narratif",
            category = "8. Fragment Final 4:1",
            content = "« L'administration de votre univers utilise des artifices typographiques, comme la \"Glose\" ou le \"latin de cuisine\" (Dog-Latin), pour piéger les individus en altérant la grammaire et en créant de fausses présomptions. Le joueur doit employer un outil analytique in-game basé sur le Trivium (grammaire, rhétorique, dialectique) pour déchiffrer le sens occulte des mots et neutraliser la tromperie. Il pourrait également devoir interroger des terminaux équipés de bases de données étymologiques (comme le Gaffiot ou le Black's Law Dictionary) pour extraire la réalité derrière la fiction juridique. »"
        ),
        BrainBonusFragment(
            levelNumber = 69,
            title = "L'Architecture Contractuelle : Association par Mots-Clés",
            source = "Outil de décryptage narratif",
            category = "8. Fragment Final 4:2",
            content = "« Un acte juridique valable n'est pas un simple texte narratif, mais une structure fonctionnelle, intelligible et traçable. Une fois le fragment décrypté, le joueur doit l'associer au bon concept taxinomique (Personne Physique, Homme Sui Iuris, Fiction Administrative). Lier un fragment d'autonomie à la mauvaise entité génère une \"incohérence contractuelle\" qui invalide l'action. »"
        ),
        BrainBonusFragment(
            levelNumber = 70,
            title = "Mécanique d'Assemblage Chronologique",
            source = "Outil de décryptage narratif",
            category = "8. Fragment Final 4:3",
            content = "« Les fragments purifiés deviennent des blocs de Droit Privé exploitables. Le joueur doit les glisser dans l'ordre exact au sein de son acte final (la Sentence Arbitrale). S'il tente de valider \"l'émancipation de la tutelle\" avant d'avoir prouvé sa \"capacité de production\", le système signale un vice de forme, l'obligeant à intégrer la logique séquentielle de la pleine capacité. »"
        ),
        BrainBonusFragment(
            levelNumber = 71,
            title = "Séparation des Entités : Prénom NOM vs PRÉNOM NOM",
            source = "Guide pratique clandestin",
            category = "8. Fragment Final 5",
            content = "« Le repositionnement commence par un choix clair et assumé d'identité. Dans chacune de vos démarches, vous devez distinguer strictement votre personne physique, écrite en \"Prénom NOM\" et légitimement rattachée à l'acte de naissance de l'état civil, de l'entité administrative écrite en lettres capitales \"PRÉNOM NOM\". Cette entité fictive prend vie à l'instant où vous interagissez avec l'administration ou souscrivez à ses avantages. Représenter exclusivement la personne physique réellement rattachée à vous-même est la condition pour vous émanciper de la présomption d'aliénation. »"
        ),
        BrainBonusFragment(
            levelNumber = 72,
            title = "L'Armure Lexicale : Précision du Vocabulaire Juridique",
            source = "Lexique du Collectif (Annotation manuscrite)",
            category = "8. Fragment Final 6",
            content = "« Ne leur concédez jamais le confort de leur propre dialecte. Pour encadrer vos actes contractuels et bloquer toute tentative de mauvaise interprétation de vos mots par l'adversaire, l'utilisation d'un vocabulaire juridique extrêmement précis est primordiale. »"
        )
    )

    fun getFragmentForLevel(levelNumber: Int): BrainBonusFragment? {
        if (levelNumber !in 1..72) return null
        return fragments.find { it.levelNumber == levelNumber } ?: fragments.getOrNull(levelNumber - 1)
    }
}
