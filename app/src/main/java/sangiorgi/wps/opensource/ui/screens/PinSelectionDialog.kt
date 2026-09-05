package sangiorgi.wps.opensource.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import sangiorgi.wps.opensource.R
import sangiorgi.wps.opensource.algorithm.PinGeneratorService
import sangiorgi.wps.opensource.ui.motion.ExpressiveMotion
import sangiorgi.wps.opensource.ui.motion.expressivePress
import javax.inject.Inject

@HiltViewModel
class PinSelectionViewModel @Inject constructor(
    private val pinGeneratorService: PinGeneratorService,
) : ViewModel() {

    private val _pins = MutableStateFlow<List<PinOption>>(emptyList())
    val pins: StateFlow<List<PinOption>> = _pins.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadPins(bssid: String?, ssid: String?, defaultPinLabel: String) {
        if (bssid.isNullOrEmpty()) {
            _pins.value = listOf(PinOption("12345670", defaultPinLabel, false))
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val generatedPins = pinGeneratorService.generateAllPins(bssid, ssid)
                _pins.value = generatedPins.map { pinWithSource ->
                    PinOption(
                        pin = pinWithSource.pin,
                        description = pinWithSource.source,
                        isFromDatabase = pinWithSource.isFromDatabase,
                        isRecommended = pinWithSource.isRecommended,
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}

/**
 * PIN picker dialog.
 *
 * Built on Material 3 Expressive: the mode switch is a segmented button row, the
 * dismiss action is a tonal icon button, PIN rows use the selected-container
 * pattern with spring-animated tints, and every component inherits the expressive
 * motion scheme from the theme. The dialog keeps its full-height footprint but
 * reads as a light sheet rather than a stack of nested boxes.
 */
@Composable
fun PinSelectionDialog(
    bssid: String? = null,
    ssid: String? = null,
    onDismiss: () -> Unit,
    onPinSelected: (List<String>) -> Unit,
    viewModel: PinSelectionViewModel = hiltViewModel(),
) {
    val defaultWpsPinLabel = stringResource(R.string.default_wps_pin)
    val pins by viewModel.pins.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(bssid, ssid) {
        viewModel.loadPins(bssid, ssid, defaultWpsPinLabel)
    }

    var showCustomInput by remember { mutableStateOf(false) }
    var customPin by remember { mutableStateOf("") }
    var selectedPins by remember(pins) { mutableStateOf(pins.map { it.pin }) }

    Dialog(onDismissRequest = onDismiss) {
        // The sheet hugs its content and only stretches toward 90% of the
        // screen when the PIN list actually needs the room - no empty desert
        // under a short list.
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = (LocalConfiguration.current.screenHeightDp * 0.9f).dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            PinSelectionContent(
                state = PinSelectionUiState(
                    ssid = ssid,
                    pins = pins,
                    isLoading = isLoading,
                    showCustomInput = showCustomInput,
                    customPin = customPin,
                    selectedPins = selectedPins,
                ),
                onModeChange = { customMode ->
                    if (customMode) {
                        showCustomInput = true
                    } else {
                        showCustomInput = false
                        customPin = ""
                    }
                },
                onCustomPinChange = { customPin = it },
                onSelectionChange = { selectedPins = it },
                onDismiss = onDismiss,
                onStart = onPinSelected,
            )
        }
    }
}

/** Immutable snapshot of everything the PIN picker body renders. */
internal data class PinSelectionUiState(
    val ssid: String? = null,
    val pins: List<PinOption> = emptyList(),
    val isLoading: Boolean = false,
    val showCustomInput: Boolean = false,
    val customPin: String = "",
    val selectedPins: List<String> = emptyList(),
)

/**
 * Stateless body of the PIN picker: header, mode toggle, animated mode content
 * and the action footer. Kept stateless so previews and screenshot tests can
 * render the exact production dialog with fake data.
 */
@Composable
internal fun PinSelectionContent(
    state: PinSelectionUiState,
    onModeChange: (Boolean) -> Unit,
    onCustomPinChange: (String) -> Unit,
    onSelectionChange: (List<String>) -> Unit,
    onDismiss: () -> Unit,
    onStart: (List<String>) -> Unit,
) {
    PinSheetScaffold(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        header = { PinDialogHeader(ssid = state.ssid, onDismiss = onDismiss) },
        toggle = {
            PinModeToggle(
                showCustomInput = state.showCustomInput,
                onAlgorithmMode = { onModeChange(false) },
                onCustomMode = { onModeChange(true) },
            )
        },
        body = {
            AnimatedContent(
                targetState = state.showCustomInput,
                modifier = Modifier.fillMaxWidth(),
                transitionSpec = {
                    ExpressiveMotion.swapIn() togetherWith ExpressiveMotion.swapOut()
                },
                label = "pinModeContent",
            ) { customMode ->
                if (customMode) {
                    CustomPinInput(
                        customPin = state.customPin,
                        onCustomPinChange = onCustomPinChange,
                    )
                } else {
                    PinListSection(
                        pins = state.pins,
                        isLoading = state.isLoading,
                        selectedPins = state.selectedPins,
                        onSelectionChange = onSelectionChange,
                    )
                }
            }
        },
        footer = {
            PinDialogFooter(
                startEnabled = if (state.showCustomInput) {
                    state.customPin.length == 8
                } else {
                    state.selectedPins.isNotEmpty() && !state.isLoading
                },
                onCancel = onDismiss,
                onStart = {
                    val finalPins = if (state.showCustomInput) {
                        if (state.customPin.length == 8) listOf(state.customPin) else emptyList()
                    } else {
                        state.selectedPins
                    }
                    if (finalPins.isNotEmpty()) onStart(finalPins)
                },
            )
        },
    )
}

/**
 * Vertical scaffold that keeps the sheet adaptive: header, mode toggle and
 * footer measure at their natural height first, and the body receives the
 * remaining room under the dialog's height cap. A short list shrinks the
 * sheet; a long one scrolls while the footer stays visible.
 */
@Composable
private fun PinSheetScaffold(
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit,
    toggle: @Composable () -> Unit,
    body: @Composable () -> Unit,
    footer: @Composable () -> Unit,
) {
    SubcomposeLayout(modifier) { constraints ->
        val gap = 12.dp.roundToPx()
        val widthConstraints = Constraints(maxWidth = constraints.maxWidth)

        val headerPlaceables = subcompose("header", header).map { it.measure(widthConstraints) }
        val togglePlaceables = subcompose("toggle", toggle).map { it.measure(widthConstraints) }
        val footerPlaceables = subcompose("footer", footer).map { it.measure(widthConstraints) }
        val fixedHeight = headerPlaceables.sumOf { it.height } +
            togglePlaceables.sumOf { it.height } +
            footerPlaceables.sumOf { it.height } +
            gap * 3

        val bodyMaxHeight = (constraints.maxHeight - fixedHeight).coerceAtLeast(0)
        val bodyPlaceables = subcompose("body", body).map {
            it.measure(widthConstraints.copy(maxHeight = bodyMaxHeight))
        }
        val bodyHeight = bodyPlaceables.sumOf { it.height }

        val totalHeight = (fixedHeight + bodyHeight).coerceAtMost(constraints.maxHeight)
        layout(constraints.maxWidth, totalHeight) {
            var y = 0
            headerPlaceables.forEach {
                it.placeRelative(0, y)
                y += it.height
            }
            y += gap
            togglePlaceables.forEach {
                it.placeRelative(0, y)
                y += it.height
            }
            y += gap
            bodyPlaceables.forEach {
                it.placeRelative(0, y)
                y += it.height
            }
            y += gap
            footerPlaceables.forEach { it.placeRelative(0, y) }
        }
    }
}

/** Title with the network name as quiet context, and the tonal dismiss action. */
@Composable
private fun PinDialogHeader(ssid: String?, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.select_pins_to_test),
                style = MaterialTheme.typography.headlineSmall,
            )
            if (!ssid.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = ssid,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        FilledTonalIconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
        }
    }
}

/**
 * Mode switch as a single segmented control - the Material 3 choice for two
 * mutually exclusive segments, reading as one switch instead of two chips.
 */
@Composable
private fun PinModeToggle(showCustomInput: Boolean, onAlgorithmMode: () -> Unit, onCustomMode: () -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        SegmentedButton(
            selected = !showCustomInput,
            onClick = onAlgorithmMode,
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            icon = {
                Icon(Icons.Default.Pin, contentDescription = null, modifier = Modifier.size(16.dp))
            },
            label = { Text(stringResource(R.string.algorithm_pins)) },
            modifier = Modifier.weight(1f),
        )
        SegmentedButton(
            selected = showCustomInput,
            onClick = onCustomMode,
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            icon = {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            },
            label = { Text(stringResource(R.string.custom_pin)) },
            modifier = Modifier.weight(1f),
        )
    }
}

/** Selection count and the select-all action share one row instead of stacking. */
@Composable
private fun PinListSection(
    pins: List<PinOption>,
    isLoading: Boolean,
    selectedPins: List<String>,
    onSelectionChange: (List<String>) -> Unit,
) {
    if (isLoading) {
        // Fixed-height loading state keeps the sheet compact while pins resolve.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.loading_pins),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.selected_pins_count, selectedPins.size),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )

                TextButton(onClick = {
                    onSelectionChange(
                        if (selectedPins.size == pins.size) emptyList() else pins.map { it.pin },
                    )
                }) {
                    Text(stringResource(R.string.select_all))
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(pins) { pinOption ->
                    PinSelectionItem(
                        pinOption = pinOption,
                        isSelected = pinOption.pin in selectedPins,
                        onToggle = {
                            onSelectionChange(
                                if (pinOption.pin in selectedPins) {
                                    selectedPins - pinOption.pin
                                } else {
                                    selectedPins + pinOption.pin
                                },
                            )
                        },
                    )
                }
            }
        }
    }
}

/**
 * Flat PIN row: no card container - a soft primary-container wash springs in
 * when the row is selected, so a fully-selected list stays calm instead of
 * turning into a stack of solid blocks. The checkbox is visual-only; the whole
 * row is toggleable.
 */
@Composable
private fun PinSelectionItem(pinOption: PinOption, isSelected: Boolean, onToggle: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f)
        } else {
            Color.Transparent
        },
        animationSpec = ExpressiveMotion.EffectsDefaultColor,
        label = "pinRowContainer",
    )

    Row(
        modifier = Modifier
            .expressivePress(interactionSource, pressedScale = 0.98f)
            .clip(MaterialTheme.shapes.small)
            .background(containerColor)
            .toggleable(
                value = isSelected,
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                role = Role.Checkbox,
                onValueChange = { onToggle() },
            )
            .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = isSelected, onCheckedChange = null)

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pinOption.pin,
                style = MaterialTheme.typography.titleMedium,
            )
            PinBadges(pinOption = pinOption)
        }
    }
}

/** Source description with quiet origin icons, tinted only for special PINs. */
@Composable
private fun PinBadges(pinOption: PinOption) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (pinOption.isRecommended) {
            Icon(
                Icons.Default.Star,
                contentDescription = stringResource(R.string.pin_recommended),
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        if (pinOption.isFromDatabase) {
            Icon(
                Icons.Default.Storage,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            text = pinOption.description,
            style = MaterialTheme.typography.bodySmall,
            color = if (pinOption.isFromDatabase || pinOption.isRecommended) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

/** Custom PIN entry: the text field is its own container, no extra box around it. */
@Composable
private fun CustomPinInput(customPin: String, onCustomPinChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = customPin,
            onValueChange = { value ->
                if (value.length <= 8 && value.all { it.isDigit() }) onCustomPinChange(value)
            },
            label = { Text(stringResource(R.string.custom_pin)) },
            placeholder = { Text(stringResource(R.string.enter_8_digit_pin)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            supportingText = {
                Text(stringResource(R.string.digits_count, customPin.length))
            },
            isError = customPin.isNotEmpty() && customPin.length < 7,
        )

        AnimatedVisibility(
            visible = customPin.length == 7,
            enter = ExpressiveMotion.expandEnter(),
            exit = ExpressiveMotion.collapseExit(),
        ) {
            FilledTonalButton(
                onClick = { onCustomPinChange(calculateWpsChecksum(customPin)) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    Icons.Default.Calculate,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.calculate_checksum))
            }
        }
    }
}

/**
 * Cancel plus a confident full-width start action - the Material 3 Expressive
 * emphasis pattern for the primary task of the dialog; squishes on press.
 */
@Composable
private fun PinDialogFooter(startEnabled: Boolean, onCancel: () -> Unit, onStart: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onCancel) {
            Text(stringResource(R.string.cancel))
        }

        val startInteraction = remember { MutableInteractionSource() }
        Button(
            onClick = onStart,
            enabled = startEnabled,
            interactionSource = startInteraction,
            modifier = Modifier
                .weight(1f)
                .expressivePress(startInteraction, pressedScale = 0.97f),
        ) {
            Text(stringResource(R.string.start_testing))
        }
    }
}

data class PinOption(
    val pin: String,
    val description: String,
    val isFromDatabase: Boolean = false,
    val isRecommended: Boolean = false,
)

/**
 * Calculate WPS PIN checksum
 */
fun calculateWpsChecksum(pin: String): String {
    if (pin.length != 7) return pin

    val pinInt = pin.toIntOrNull() ?: return pin
    var accum = 0

    val tempPin = pinInt * 10
    accum += 3 * ((tempPin / 10000000) % 10)
    accum += 1 * ((tempPin / 1000000) % 10)
    accum += 3 * ((tempPin / 100000) % 10)
    accum += 1 * ((tempPin / 10000) % 10)
    accum += 3 * ((tempPin / 1000) % 10)
    accum += 1 * ((tempPin / 100) % 10)
    accum += 3 * ((tempPin / 10) % 10)

    val checksum = (10 - (accum % 10)) % 10
    return pin + checksum
}
