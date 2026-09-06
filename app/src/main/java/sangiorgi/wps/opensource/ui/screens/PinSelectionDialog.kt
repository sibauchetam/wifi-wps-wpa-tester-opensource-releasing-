package sangiorgi.wps.opensource.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
 * PIN picker dialog, built on the official Material 3 [AlertDialog] (basic
 * variant) as prescribed by the Material Components docs (components/Dialog.md):
 * the component itself supplies the extraLarge container shape, the
 * surfaceContainerHigh container color, the headlineSmall title typography, the
 * documented dialog paddings and the end-aligned action row. Per the same docs,
 * a close affordance belongs only to full-screen dialogs, so a basic dialog is
 * dismissed with the scrim tap or its action buttons.
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_pins_to_test)) },
        text = {
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
            )
        },
        confirmButton = {
            PinStartButton(
                showCustomInput = showCustomInput,
                customPin = customPin,
                selectedPins = selectedPins,
                isLoading = isLoading,
                onStart = onPinSelected,
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

/**
 * Confirm action in the dialog's confirmButton slot. Both dialog actions are
 * text buttons, matching the dialog button styles documented in
 * components/Dialog.md (Widget.Material3.Button.TextButton.Dialog).
 */
@Composable
private fun PinStartButton(
    showCustomInput: Boolean,
    customPin: String,
    selectedPins: List<String>,
    isLoading: Boolean,
    onStart: (List<String>) -> Unit,
) {
    val startEnabled = if (showCustomInput) {
        customPin.length == 8
    } else {
        selectedPins.isNotEmpty() && !isLoading
    }

    TextButton(
        onClick = {
            val finalPins = if (showCustomInput) {
                if (customPin.length == 8) listOf(customPin) else emptyList()
            } else {
                selectedPins
            }
            if (finalPins.isNotEmpty()) onStart(finalPins)
        },
        enabled = startEnabled,
    ) {
        Text(stringResource(R.string.start_testing))
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
 * Stateless `text` slot of the dialog: supporting SSID line, the mode toggle
 * and the animated mode content. Kept stateless so previews and screenshot
 * tests can render the exact production content with fake data.
 */
@Composable
internal fun PinSelectionContent(
    state: PinSelectionUiState,
    onModeChange: (Boolean) -> Unit,
    onCustomPinChange: (String) -> Unit,
    onSelectionChange: (List<String>) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (!state.ssid.isNullOrBlank()) {
            // Supporting text per Dialog.md: bodyMedium on onSurfaceVariant.
            Text(
                text = state.ssid,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        PinModeToggle(
            showCustomInput = state.showCustomInput,
            onAlgorithmMode = { onModeChange(false) },
            onCustomMode = { onModeChange(true) },
        )

        Spacer(modifier = Modifier.height(12.dp))

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
    }
}

/**
 * Mode switch as a single segmented control - the Material 3 choice for two
 * mutually exclusive segments (ToggleButtonGroup.md, singleSelection). The
 * selected segment shows the component's own default check icon.
 */
@Composable
private fun PinModeToggle(showCustomInput: Boolean, onAlgorithmMode: () -> Unit, onCustomMode: () -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        SegmentedButton(
            selected = !showCustomInput,
            onClick = onAlgorithmMode,
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            label = { Text(stringResource(R.string.algorithm_pins)) },
            modifier = Modifier.weight(1f),
        )
        SegmentedButton(
            selected = showCustomInput,
            onClick = onCustomMode,
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
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
        // Fixed-height loading state keeps the dialog from jumping while pins resolve.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
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

            // Bounded height: a long list scrolls inside the dialog while a short
            // one keeps the dialog compact.
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp),
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
 * Two-line list row per the Lists + Checkbox docs: the PIN is the headline, the
 * source is the supporting text, and a standard [Checkbox] trails the text
 * (as in the multi-select list samples of List.md / the dialog multi-choice
 * item layout of Dialog.md). The checkbox is a purely visual indicator synced
 * with the row state (`onCheckedChange = null`), so selection is communicated
 * by the checkbox's own standard state colors - no custom background wash - and
 * the whole row carries the toggle with the Checkbox role.
 */
@Composable
private fun PinSelectionItem(pinOption: PinOption, isSelected: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .toggleable(
                value = isSelected,
                role = Role.Checkbox,
                onValueChange = { onToggle() },
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pinOption.pin,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            PinBadges(pinOption = pinOption)
        }

        Checkbox(checked = isSelected, onCheckedChange = null)
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
            style = MaterialTheme.typography.bodyMedium,
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
