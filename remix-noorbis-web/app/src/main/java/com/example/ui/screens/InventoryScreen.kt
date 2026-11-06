package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.service.ContemplativeService
import com.example.domain.service.FeelingType
import java.text.Normalizer
import kotlinx.coroutines.launch
import com.example.R
import com.example.ui.components.astroTouchEffect
import com.example.data.local.CollectedTextEntity
import com.example.domain.model.FruitFacet
import com.example.domain.model.MeditationsMatrix
import com.example.domain.model.getSphereColorById
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanGlow
import com.example.ui.theme.EmeraldNova
import com.example.ui.theme.EmeraldNovaGlow
import com.example.ui.theme.NebulaGray
import com.example.ui.theme.SolarGoldGlow
import com.example.ui.theme.SpaceBackground
import com.example.ui.theme.SpaceCardBorder
import com.example.ui.theme.SpaceSurface
import com.example.ui.theme.SpaceSurfaceVariant
import com.example.ui.theme.StarlightWhite
import com.example.ui.theme.VioletPulsarGlow

internal fun normalizeForInventorySearch(input: String): String {
    val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
    return normalized.replace("\\p{Mn}+".toRegex(), "").lowercase()
}

internal fun matchesInventorySearch(item: CollectedTextEntity, query: String): Boolean {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return true
    val normalizedQuery = normalizeForInventorySearch(trimmed)
    val searchableFields = listOf(
        item.title,
        item.content,
        item.letter,
        item.sectorName,
        "niveau ${item.levelNumber}",
        "${item.levelNumber}"
    )
    return searchableFields.any { field ->
        normalizeForInventorySearch(field).contains(normalizedQuery)
    }
}

@Composable
fun InventoryScreen(
    collectedTexts: List<CollectedTextEntity>,
    selectedFilterLetter: String?,
    inspectedText: CollectedTextEntity?,
    onSelectFilter: (String?) -> Unit,
    onInspectText: (CollectedTextEntity?) -> Unit,
    onGoToPlay: () -> Unit,
    onBack: () -> Unit,
    isInventoryAscending: Boolean = false,
    searchQuery: String = "",
    onSearchQueryChange: ((String) -> Unit)? = null,
    onShareTextReward: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var localSearchQuery by rememberSaveable { mutableStateOf("") }
    val activeSearchQuery = if (onSearchQueryChange != null) searchQuery else localSearchQuery
    val updateSearchQuery: (String) -> Unit = { newQuery ->
        if (onSearchQueryChange != null) {
            onSearchQueryChange(newQuery)
        } else {
            localSearchQuery = newQuery
        }
    }

    BackHandler {
        if (inspectedText != null) {
            onInspectText(null)
        } else if (activeSearchQuery.isNotEmpty()) {
            updateSearchQuery("")
        } else {
            onBack()
        }
    }

    val isBrainFilter = selectedFilterLetter == "🧠" || selectedFilterLetter == "Cerveau"
    val letterFilteredTexts = if (selectedFilterLetter == null) {
        collectedTexts
    } else if (isBrainFilter) {
        collectedTexts.filter { it.letter == "🧠" || it.id.startsWith("BRAIN_") }
    } else {
        collectedTexts.filter {
            it.letter.equals(selectedFilterLetter, ignoreCase = true) ||
                    it.title.contains(selectedFilterLetter, ignoreCase = true)
        }
    }

    val rawFilteredTexts = remember(letterFilteredTexts, activeSearchQuery) {
        if (activeSearchQuery.isBlank()) {
            letterFilteredTexts
        } else {
            letterFilteredTexts.filter { matchesInventorySearch(it, activeSearchQuery) }
        }
    }

    val filteredTexts = remember(rawFilteredTexts, isInventoryAscending) {
        if (isInventoryAscending) {
            rawFilteredTexts.sortedWith(
                compareBy<CollectedTextEntity> { it.levelNumber }
                    .thenBy { it.id }
            )
        } else {
            rawFilteredTexts.sortedWith(
                compareByDescending<CollectedTextEntity> { it.levelNumber }
                    .thenByDescending { it.id }
            )
        }
    }

    val brainTexts = collectedTexts.filter { it.letter == "🧠" || it.id.startsWith("BRAIN_") }
    val scriptureTexts = collectedTexts.filter { it.letter != "🧠" && !it.id.startsWith("BRAIN_") }
    val totalPointsFromTexts = collectedTexts.sumOf { it.pointsAwarded * it.collectedCount }
    val totalUniqueMeditations = scriptureTexts.size
    val totalBrainCount = brainTexts.size
    val totalMatrixCount = 810
    val progressFloat = (totalUniqueMeditations.toFloat() / totalMatrixCount).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceBackground),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .testTag("inventory_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header & Fruit Matrix Progress Dashboard
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = SpaceSurface),
                    border = BorderStroke(1.dp, SpaceCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = CyberCyanGlow,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = "Inventaire du Fruit de l'Esprit",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = StarlightWhite
                                )
                                Text(
                                    text = "Matrice intégrale : 9 facettes × 15 situations (810 méditations + 72 bonus 🧠)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = NebulaGray
                                )
                            }
                        }

                        // Progress bar for the 810 meditations
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$totalUniqueMeditations / $totalMatrixCount méditations • $totalBrainCount / 72 fragments 🧠",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = StarlightWhite
                                )
                                Text(
                                    text = "${(progressFloat * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = EmeraldNovaGlow
                                )
                            }
                            LinearProgressIndicator(
                                progress = { progressFloat },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = EmeraldNovaGlow,
                                trackColor = SpaceSurfaceVariant
                            )
                        }

                        Surface(
                            color = SpaceSurfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, SpaceCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Points cumulés par méditations :",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = NebulaGray
                                )
                                Text(
                                    text = "+$totalPointsFromTexts PTS",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = SolarGoldGlow
                                )
                            }
                        }
                    }
                }
            }

            // 2. Keyword Search Bar across the 810 meditations and 72 bonus fragments
            item {
                OutlinedTextField(
                    value = activeSearchQuery,
                    onValueChange = updateSearchQuery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inventory_search_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.inventory_search_placeholder),
                            style = MaterialTheme.typography.bodyMedium,
                            color = NebulaGray
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = CyberCyanGlow
                        )
                    },
                    trailingIcon = {
                        if (activeSearchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { updateSearchQuery("") },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("inventory_search_clear_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.inventory_search_clear),
                                    tint = StarlightWhite
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = StarlightWhite,
                        unfocusedTextColor = StarlightWhite,
                        focusedContainerColor = SpaceSurface,
                        unfocusedContainerColor = SpaceSurface,
                        focusedBorderColor = CyberCyanGlow,
                        unfocusedBorderColor = SpaceCardBorder,
                        cursorColor = CyberCyanGlow
                    )
                )
            }

            // 3. Single Unified Filter Chips Row for the 9 Fruits of the Spirit & Brain Bonus Fragments
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inventory_filter_row"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedFilterLetter == null,
                            onClick = { onSelectFilter(null) },
                            label = {
                                Text(
                                    text = "${stringResource(R.string.filter_all)} (${collectedTexts.size})",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberCyan.copy(alpha = 0.25f),
                                selectedLabelColor = CyberCyanGlow
                            ),
                            modifier = Modifier
                                .astroTouchEffect(glowColor = CyberCyanGlow, cornerRadius = 8.dp)
                                .testTag("filter_chip_all")
                        )
                    }

                    // Brain Bonus Filter Chip
                    item {
                        val isSelected = selectedFilterLetter == "🧠"
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectFilter(if (isSelected) null else "🧠") },
                            label = {
                                Text(
                                    text = "🧠 Cerveau ($totalBrainCount/72)",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VioletPulsarGlow.copy(alpha = 0.25f),
                                selectedLabelColor = VioletPulsarGlow
                            ),
                            modifier = Modifier
                                .astroTouchEffect(glowColor = VioletPulsarGlow, cornerRadius = 8.dp)
                                .testTag("filter_chip_brain")
                        )
                    }

                    items(MeditationsMatrix.FruitFacets) { facet ->
                        val count = collectedTexts.count {
                            it.letter.equals(facet.name, ignoreCase = true) ||
                                    it.letter.equals(facet.id, ignoreCase = true)
                        }
                        val isSelected = selectedFilterLetter.equals(facet.name, ignoreCase = true)
                        val facetColor = MeditationsMatrix.getFruitGlowColor(facet.id)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectFilter(if (isSelected) null else facet.name) },
                            label = {
                                Text(
                                    text = "${facet.emoji} ${facet.name} ($count/90)",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = facetColor.copy(alpha = 0.25f),
                                selectedLabelColor = facetColor
                            ),
                            modifier = Modifier
                                .astroTouchEffect(glowColor = facetColor, cornerRadius = 8.dp)
                                .testTag("filter_chip_${facet.id}")
                        )
                    }
                }
            }

            // 4. Empty State or List of Collected Texts
            if (filteredTexts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .testTag("inventory_empty_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SpaceSurface),
                        border = BorderStroke(1.dp, SpaceCardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = CyberCyanGlow,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = when {
                                    activeSearchQuery.isNotBlank() ->
                                        stringResource(R.string.inventory_search_empty_title, activeSearchQuery.trim())
                                    selectedFilterLetter != null ->
                                        "Aucune méditation trouvée pour « $selectedFilterLetter »"
                                    else ->
                                        stringResource(R.string.inventory_empty_title)
                                },
                                style = MaterialTheme.typography.titleLarge,
                                color = StarlightWhite,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = when {
                                    activeSearchQuery.isNotBlank() ->
                                        stringResource(R.string.inventory_search_empty_desc)
                                    selectedFilterLetter != null ->
                                        "Partez explorer les secteurs de l'espace pour collecter les orbes de cette facette !"
                                    else ->
                                        "Explorez l'espace avec votre boule pour récupérer les 3 textes de méditation scripturaire par niveau !"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = NebulaGray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = onGoToPlay,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .astroTouchEffect(glowColor = CyberCyanGlow, cornerRadius = 14.dp)
                                    .testTag("empty_inventory_play_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyberCyan,
                                    contentColor = SpaceBackground
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(imageVector = Icons.Default.RocketLaunch, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Partir explorer l'espace", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            } else {
                items(filteredTexts, key = { it.id }) { item ->
                    CollectedTextCard(
                        item = item,
                        onClick = { onInspectText(item) }
                    )
                }
            }
        }

        // 4. Detailed Reader Modal when a user clicks on a collected meditation
        if (inspectedText != null) {
            InspectedTextDialog(
                item = inspectedText,
                onDismiss = { onInspectText(null) },
                onShareTextReward = onShareTextReward
            )
        }
    }
}

@Composable
private fun CollectedTextCard(
    item: CollectedTextEntity,
    onClick: () -> Unit
) {
    val isBrain = item.letter == "🧠" || item.id.startsWith("BRAIN_")
    val fruitColor = if (isBrain) VioletPulsarGlow else MeditationsMatrix.getFruitGlowColor(item.letter)
    val sphereUsed = getSphereColorById(item.sphereColorId)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .astroTouchEffect(glowColor = fruitColor, pressScale = 0.96f, cornerRadius = 18.dp)
            .clickable { onClick() }
            .testTag("inventory_item_${item.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SpaceSurface),
        border = BorderStroke(1.dp, fruitColor.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Glowing Badge
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(fruitColor.copy(alpha = 0.18f))
                    .border(1.5.dp, fruitColor, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isBrain) "🧠" else MeditationsMatrix.getFruitEmoji(item.letter),
                        fontSize = 18.sp
                    )
                    Text(
                        text = if (isBrain) "Bonus" else item.letter.take(4),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = fruitColor
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val cleanSectorName = item.sectorName.replace(Regex("^Secteur \\d+ • "), "")
                Text(
                    text = if (isBrain) "Niveau ${item.levelNumber} • Fragment Bonus Cerveau" else "Niveau ${item.levelNumber} • $cleanSectorName",
                    style = MaterialTheme.typography.labelMedium,
                    color = fruitColor
                )

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = StarlightWhite
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(sphereUsed.primaryColor)
                        )
                        Text(
                            text = "Étincelle ${sphereUsed.name} (x${item.collectedCount})",
                            style = MaterialTheme.typography.labelMedium,
                            color = NebulaGray
                        )
                    }

                    Text(
                        text = "+${item.pointsAwarded} pts",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = SolarGoldGlow
                    )
                }
            }
        }
    }
}

@Composable
private fun InspectedTextDialog(
    item: CollectedTextEntity,
    onDismiss: () -> Unit,
    onShareTextReward: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isBrain = item.letter == "🧠" || item.id.startsWith("BRAIN_")
    val fruitColor = if (isBrain) VioletPulsarGlow else MeditationsMatrix.getFruitGlowColor(item.letter)
    val sphereUsed = getSphereColorById(item.sphereColorId)

    var selectedFeeling by remember(item.id) { mutableStateOf<FeelingType?>(null) }
    var reflectionText by remember(item.id) { mutableStateOf<String?>(null) }
    var isLoadingReflection by remember(item.id) { mutableStateOf(false) }
    var shareRewardClaimed by remember(item.id, item.hasClaimedShareReward) {
        mutableStateOf(item.hasClaimedShareReward)
    }
    var justClaimedInThisDialog by remember(item.id) { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 460.dp)
                .testTag("inspected_text_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SpaceSurface),
            border = BorderStroke(2.dp, fruitColor)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = fruitColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, fruitColor)
                    ) {
                        Text(
                            text = if (isBrain) "FRAGMENT BONUS • 🧠 NIVEAU ${item.levelNumber}"
                            else "MÉDITATION • ${MeditationsMatrix.getFruitEmoji(item.letter)} ${item.letter.uppercase()}",
                            style = MaterialTheme.typography.labelLarge,
                            color = fruitColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .astroTouchEffect(glowColor = fruitColor, cornerRadius = 20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer la lecture",
                            tint = StarlightWhite
                        )
                    }
                }

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = StarlightWhite
                )

                val cleanOriginName = item.sectorName.replace(Regex("^Secteur \\d+ • "), "")
                Text(
                    text = if (isBrain) "Niveau ${item.levelNumber} • Fragment Bonus Juridique & Philosophique"
                    else "Niveau ${item.levelNumber} • $cleanOriginName",
                    style = MaterialTheme.typography.labelMedium,
                    color = CyberCyanGlow
                )

                // Scrollable content area
                Surface(
                    color = SpaceSurfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, SpaceCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = item.content,
                                style = MaterialTheme.typography.bodyLarge,
                                color = StarlightWhite
                            )
                        }

                        if (!isBrain) {
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "💬 Résonance : Ce texte me parle parce que...",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = CyberCyanGlow,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "↔ Glisser",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = NebulaGray
                                        )
                                    }

                                    LazyRow(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("inspected_resonance_lazy_row"),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        items(FeelingType.values().toList(), key = { it.name }) { feeling ->
                                            val isSelected = selectedFeeling == feeling
                                            Surface(
                                                color = if (isSelected) CyberCyan.copy(alpha = 0.35f) else SpaceSurface,
                                                shape = RoundedCornerShape(14.dp),
                                                border = BorderStroke(
                                                    width = if (isSelected) 1.5.dp else 1.dp,
                                                    color = if (isSelected) CyberCyanGlow else SpaceCardBorder
                                                ),
                                                modifier = Modifier
                                                    .minimumInteractiveComponentSize()
                                                    .astroTouchEffect(glowColor = CyberCyanGlow, cornerRadius = 14.dp)
                                                    .clickable {
                                                        selectedFeeling = feeling
                                                        isLoadingReflection = true
                                                        reflectionText = null
                                                        coroutineScope.launch {
                                                            val res = ContemplativeService.generatePersonalReflection(
                                                                verse = item.content,
                                                                fruit = item.letter,
                                                                situation = item.title,
                                                                feeling = feeling
                                                            )
                                                            reflectionText = res
                                                            isLoadingReflection = false
                                                        }
                                                    }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(feeling.emoji, fontSize = 18.sp)
                                                    Text(
                                                        text = feeling.label,
                                                        style = MaterialTheme.typography.labelLarge.copy(
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                        ),
                                                        color = if (isSelected) CyberCyanGlow else StarlightWhite,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (isLoadingReflection) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = CyberCyanGlow
                                            )
                                            Text(
                                                text = "Écoute contemplative et réconfort...",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = CyberCyanGlow
                                            )
                                        }
                                    }

                                    if (reflectionText != null) {
                                        Surface(
                                            color = CyberCyan.copy(alpha = 0.14f),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, CyberCyanGlow),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    text = "🕊️ Parole d'encouragement :",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = CyberCyanGlow
                                                )
                                                Text(
                                                    text = reflectionText ?: "",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = StarlightWhite
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(sphereUsed.primaryColor)
                        )
                        Text(
                            text = "Étincelle ${sphereUsed.name}",
                            style = MaterialTheme.typography.labelMedium,
                            color = NebulaGray
                        )
                    }
                    Text(
                        text = "Valeur : +${item.pointsAwarded} PTS",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = SolarGoldGlow
                    )
                }

                if (shareRewardClaimed) {
                    Surface(
                        color = EmeraldNova.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.2.dp, EmeraldNovaGlow),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (justClaimedInThisDialog) {
                                "✨ +3 Pensées Positives (+30 pts d'énergie) gagnées !"
                            } else {
                                "✓ Bonus de partage (+3 Pensées Positives) déjà obtenu pour ce texte"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldNovaGlow,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (!shareRewardClaimed) {
                                shareRewardClaimed = true
                                justClaimedInThisDialog = true
                                onShareTextReward(item.id)
                            }
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, item.title)
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    if (isBrain) "${item.title}\n\n${item.content}\n\n— Fragment Bonus 🧠 Niveau ${item.levelNumber} (Noôrbis)"
                                    else "${item.title}\n\n${item.content}\n\n— Noôrbis, Demeurer dans le Fruit de l'Esprit"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Partager le fragment"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .astroTouchEffect(glowColor = if (shareRewardClaimed) EmeraldNovaGlow else StarlightWhite, cornerRadius = 14.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (shareRewardClaimed) EmeraldNovaGlow else SpaceCardBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = if (shareRewardClaimed) EmeraldNovaGlow else StarlightWhite
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (shareRewardClaimed) "Partager (✓ +3)" else "Partager (+3 ✨)",
                            color = if (shareRewardClaimed) EmeraldNovaGlow else StarlightWhite,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .astroTouchEffect(glowColor = fruitColor, cornerRadius = 14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = fruitColor,
                            contentColor = SpaceBackground
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Fermer", style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false)
                    }
                }
            }
        }
    }
}
