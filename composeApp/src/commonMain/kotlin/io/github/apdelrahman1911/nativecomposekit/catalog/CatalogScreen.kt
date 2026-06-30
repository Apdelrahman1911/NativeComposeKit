package io.github.apdelrahman1911.nativecomposekit.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.apdelrahman1911.nativecomposekit.components.NativeAvatar
import io.github.apdelrahman1911.nativecomposekit.components.NativeAvatarShape
import io.github.apdelrahman1911.nativecomposekit.components.NativeAvatarSize
import io.github.apdelrahman1911.nativecomposekit.components.NativeBadge
import io.github.apdelrahman1911.nativecomposekit.components.NativeBadgedBox
import io.github.apdelrahman1911.nativecomposekit.components.NativeButton
import io.github.apdelrahman1911.nativecomposekit.components.NativeCard
import io.github.apdelrahman1911.nativecomposekit.components.NativeCardVariant
import io.github.apdelrahman1911.nativecomposekit.components.NativeCheckbox
import io.github.apdelrahman1911.nativecomposekit.components.NativeColorWell
import io.github.apdelrahman1911.nativecomposekit.components.NativeDatePicker
import io.github.apdelrahman1911.nativecomposekit.components.NativeOtpField
import io.github.apdelrahman1911.nativecomposekit.components.NativePopover
import io.github.apdelrahman1911.nativecomposekit.components.NativeContentState
import io.github.apdelrahman1911.nativecomposekit.components.NativeLoadState
import io.github.apdelrahman1911.nativecomposekit.components.NativeSheet
import io.github.apdelrahman1911.nativecomposekit.components.NativeSheetDetent
import io.github.apdelrahman1911.nativecomposekit.components.NativeDialog
import io.github.apdelrahman1911.nativecomposekit.components.NativeTopBar
import io.github.apdelrahman1911.nativecomposekit.components.nativeHeading
import io.github.apdelrahman1911.nativecomposekit.components.NativeSwipeAction
import io.github.apdelrahman1911.nativecomposekit.components.rememberNativeShare
import io.github.apdelrahman1911.nativecomposekit.components.NativeChip
import io.github.apdelrahman1911.nativecomposekit.components.NativeChipStyle
import io.github.apdelrahman1911.nativecomposekit.components.NativeDivider
import io.github.apdelrahman1911.nativecomposekit.components.NativeEmptyState
import io.github.apdelrahman1911.nativecomposekit.components.NativeIconButton
import io.github.apdelrahman1911.nativecomposekit.components.NativeListItem
import io.github.apdelrahman1911.nativecomposekit.components.NativeListSection
import io.github.apdelrahman1911.nativecomposekit.components.NativePageControl
import io.github.apdelrahman1911.nativecomposekit.components.NativeProgressIndicator
import io.github.apdelrahman1911.nativecomposekit.components.NativeProgressKind
import io.github.apdelrahman1911.nativecomposekit.components.NativeRadioGroup
import io.github.apdelrahman1911.nativecomposekit.components.NativeRating
import io.github.apdelrahman1911.nativecomposekit.components.NativeSearchBar
import io.github.apdelrahman1911.nativecomposekit.components.NativeSearchBarIosOptions
import io.github.apdelrahman1911.nativecomposekit.components.NativeSelectionStyle
import io.github.apdelrahman1911.nativecomposekit.components.NativeSkeleton
import io.github.apdelrahman1911.nativecomposekit.components.NativeSegmentedControl
import io.github.apdelrahman1911.nativecomposekit.components.NativeSlider
import io.github.apdelrahman1911.nativecomposekit.components.NativeSplitButton
import io.github.apdelrahman1911.nativecomposekit.components.NativeStepper
import io.github.apdelrahman1911.nativecomposekit.components.NativeText
import io.github.apdelrahman1911.nativecomposekit.components.NativeTextField
import io.github.apdelrahman1911.nativecomposekit.components.NativeToggle
import io.github.apdelrahman1911.nativecomposekit.components.feedback.NativeAlertAction
import io.github.apdelrahman1911.nativecomposekit.components.feedback.NativeAlertActionRole
import io.github.apdelrahman1911.nativecomposekit.components.feedback.NativeAlertIosOptions
import io.github.apdelrahman1911.nativecomposekit.components.feedback.NativeFeedbackPosition
import io.github.apdelrahman1911.nativecomposekit.components.feedback.NativeFeedbackStatus
import io.github.apdelrahman1911.nativecomposekit.components.feedback.NativeInlineStatus
import io.github.apdelrahman1911.nativecomposekit.components.feedback.NativePresentation
import io.github.apdelrahman1911.nativecomposekit.components.feedback.NativeConfirmationSheetIosOptions
import io.github.apdelrahman1911.nativecomposekit.components.feedback.NativeSheetAction
import io.github.apdelrahman1911.nativecomposekit.components.feedback.NativeToastAndroidOptions
import io.github.apdelrahman1911.nativecomposekit.components.feedback.LocalNativeFeedbackController
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonShape
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonSize
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeButtonVariant
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeCapitalization
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeCharacterLimit
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeCharacterLimitBehavior
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeClearButtonMode
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeFieldFocus
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeFieldInput
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeIcon
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeImeAction
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeKeyboardAccessory
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeKeyboardType
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeMenu
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeMenuItem
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeMenuItemRole
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeTextContentType
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeTextFieldIosOptions
import io.github.apdelrahman1911.nativecomposekit.components.model.NativeTextStyle
import io.github.apdelrahman1911.nativecomposekit.platform.platformName

/**
 * One shared catalog screen. Running it on Android shows Compose Material controls; running it on
 * iOS shows real UIKit controls — same code, native rendering per platform.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CatalogScreen(
    dark: Boolean,
    onToggleDark: (Boolean) -> Unit,
    rtl: Boolean,
    onToggleRtl: (Boolean) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("secret123") }
    var badEmail by remember { mutableStateOf("not-an-email") }
    var search by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var limited by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var wifiOn by remember { mutableStateOf(true) }
    var period by remember { mutableStateOf(0) }
    var volume by remember { mutableStateOf(0.4f) }
    var quantity by remember { mutableStateOf(2) }
    var showInlineError by remember { mutableStateOf(true) }
    // The shared feedback controller (mounted by NativeFeedbackHost in App). The existing button demos
    // keep their terse string feedback by routing this helper through controller.toast(...).
    val feedback = LocalNativeFeedbackController.current
    val toast: (String) -> Unit = { feedback.toast(it) }

    // Hosted in NativeShell's NavigationStack, which already provides the native "Catalog" nav bar — so NO
    // Compose topBar here (it would be a redundant second bar + a top gap). Zero content insets so the content
    // fills the screen and scrolls under the floating tab bar (the host applies `ignoresSafeArea(.bottom)`).
    Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0)) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .imePadding() // keyboard avoidance: scroll viewport ends above the keyboard
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text("NativeComposeKit — $platformName", style = MaterialTheme.typography.titleLarge)
            // ----- Theme / direction controls -----
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Dark")
                Switch(checked = dark, onCheckedChange = onToggleDark)
                Spacer(Modifier.width(16.dp))
                Text("RTL")
                Switch(checked = rtl, onCheckedChange = onToggleRtl)
            }

            // ----- NativeTextField -----
            Section("Text fields") {
                NativeTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notes (multiline)",
                    placeholder = "Type a few lines…",
                    input = NativeFieldInput(singleLine = false, minLines = 3, maxLines = 6),
                    ios = NativeTextFieldIosOptions(keyboardAccessory = NativeKeyboardAccessory(doneButton = true)),
                    modifier = Modifier.fillMaxWidth(),
                )
                NativeTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Name",
                    placeholder = "Jane Doe",
                    input = NativeFieldInput(capitalization = NativeCapitalization.Words),
                    modifier = Modifier.fillMaxWidth(),
                )
                NativeTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    placeholder = "you@example.com",
                    helperText = "We'll never share it.",
                    leadingIcon = NativeIcon(Icons.Default.Email, sfSymbolName = "envelope"),
                    input = NativeFieldInput(keyboardType = NativeKeyboardType.Email, imeAction = NativeImeAction.Done, autoCorrect = false),
                    focus = NativeFieldFocus(onSubmit = { /* dismiss / submit */ }),
                    contentType = NativeTextContentType.EmailAddress,
                    ios = NativeTextFieldIosOptions(
                        clearButton = NativeClearButtonMode.WhileEditing,
                        keyboardAccessory = NativeKeyboardAccessory(doneButton = true),
                    ),
                    testTag = "email",
                    modifier = Modifier.fillMaxWidth(),
                )
                NativeTextField(
                    value = badEmail,
                    onValueChange = { badEmail = it },
                    label = "Email",
                    errorText = "Invalid email format",
                    isError = true,
                    input = NativeFieldInput(keyboardType = NativeKeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                )
                NativeTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    leadingIcon = NativeIcon(Icons.Default.Lock, sfSymbolName = "lock"),
                    input = NativeFieldInput(secure = true, imeAction = NativeImeAction.Done),
                    contentType = NativeTextContentType.Password,
                    modifier = Modifier.fillMaxWidth(),
                )
                NativeTextField(
                    value = limited,
                    onValueChange = { limited = it },
                    label = "Short code (enforce)",
                    placeholder = "max 10 chars",
                    helperText = "${limited.length}/10",
                    input = NativeFieldInput(
                        characterLimit = NativeCharacterLimit(max = 10, behavior = NativeCharacterLimitBehavior.Enforce),
                        capitalization = NativeCapitalization.Characters,
                        autoCorrect = false,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                NativeTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = "Bio (warn only)",
                    placeholder = "A short bio",
                    helperText = "${bio.length}/20",
                    errorText = if (bio.length > 20) "Too long by ${bio.length - 20}" else null,
                    isError = bio.length > 20,
                    input = NativeFieldInput(
                        characterLimit = NativeCharacterLimit(max = 20, behavior = NativeCharacterLimitBehavior.WarnOnly),
                        singleLine = false,
                        minLines = 2,
                        maxLines = 4,
                    ),
                    ios = NativeTextFieldIosOptions(keyboardAccessory = NativeKeyboardAccessory(doneButton = true)),
                    modifier = Modifier.fillMaxWidth(),
                )
                NativeTextField(
                    value = "Read-only / disabled",
                    onValueChange = {},
                    label = "Disabled",
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                )
                NativeTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = "Search…",
                    leadingIcon = NativeIcon(Icons.Default.Search, sfSymbolName = "magnifyingglass"),
                    input = NativeFieldInput(imeAction = NativeImeAction.Search),
                    ios = NativeTextFieldIosOptions(clearButton = NativeClearButtonMode.WhileEditing),
                    testTag = "search",
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // ----- NativeToggle -----
            Section("Toggle") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NativeText("Wi-Fi", style = NativeTextStyle.Body)
                    NativeToggle(checked = wifiOn, onCheckedChange = { wifiOn = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NativeText("Disabled", style = NativeTextStyle.Body)
                    NativeToggle(checked = false, onCheckedChange = {}, enabled = false)
                }
            }

            // ----- NativeSegmentedControl -----
            Section("Segmented control") {
                val periods = listOf("Day", "Week", "Month")
                NativeSegmentedControl(
                    options = periods,
                    selectedIndex = period,
                    onSelectedIndexChange = { period = it },
                    modifier = Modifier.fillMaxWidth(),
                    contentDescription = "Time period",
                )
                NativeText("Selected: ${periods[period]}", style = NativeTextStyle.Label)
            }

            // ----- NativeSlider -----
            Section("Slider") {
                NativeSlider(
                    value = volume,
                    onValueChange = { volume = it },
                    modifier = Modifier.fillMaxWidth(),
                    contentDescription = "Volume",
                )
                NativeText("Value: ${(volume * 100).toInt()}%", style = NativeTextStyle.Label)
            }

            // ----- NativeStepper -----
            Section("Stepper") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NativeText("Quantity: $quantity", style = NativeTextStyle.Body)
                    NativeStepper(value = quantity, onValueChange = { quantity = it }, min = 0, max = 10, contentDescription = "Quantity")
                }
            }

            // ----- NativeText -----
            Section("Text — roles") {
                NativeText("Display style", style = NativeTextStyle.Display)
                NativeText("Title style", style = NativeTextStyle.Title)
                NativeText(
                    "Body style — the quick brown fox jumps over the lazy dog.",
                    style = NativeTextStyle.Body,
                )
                NativeText("LABEL STYLE", style = NativeTextStyle.Label)
                NativeText(
                    "Colored from the theme (primary)",
                    style = NativeTextStyle.Body,
                    color = MaterialTheme.colorScheme.primary,
                )
                NativeText(
                    "Truncated to a single line with an ellipsis when it is too long to fit.",
                    style = NativeTextStyle.Body,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // ----- NativeButton -----
            Section("Buttons — variants") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NativeButton("Primary", { toast("Clicked Primary") }, variant = NativeButtonVariant.Primary)
                    NativeButton("Secondary", { toast("Clicked Secondary") }, variant = NativeButtonVariant.Secondary)
                    NativeButton("Tertiary", { toast("Clicked Tertiary") }, variant = NativeButtonVariant.Tertiary)
                    NativeButton("Outline", { toast("Clicked Outline") }, variant = NativeButtonVariant.Outline)
                    NativeButton("Destructive", { toast("Clicked Destructive") }, variant = NativeButtonVariant.Destructive)
                }
            }

            Section("Buttons — sizes") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    NativeButton("Small", { toast("Clicked Small") }, size = NativeButtonSize.Small)
                    NativeButton("Medium", { toast("Clicked Medium") }, size = NativeButtonSize.Medium)
                    NativeButton("Large", { toast("Clicked Large") }, size = NativeButtonSize.Large)
                }
            }

            Section("Buttons — states & icons") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    NativeButton("Disabled", { toast("Clicked Disabled") }, enabled = false)
                    NativeButton("Loading", { toast("Clicked Loading") }, loading = true)
                    NativeButton(
                        "Leading",
                        { toast("Clicked Leading") },
                        leadingIcon = NativeIcon(Icons.Default.Add, sfSymbolName = "plus", contentDescription = "Add"),
                    )
                    NativeButton(
                        "Trailing",
                        { toast("Clicked Trailing") },
                        trailingIcon = NativeIcon(Icons.AutoMirrored.Filled.ArrowForward, sfSymbolName = "arrow.right"),
                    )
                    NativeButton(
                        "Both",
                        { toast("Clicked Both") },
                        leadingIcon = NativeIcon(Icons.Default.Add, sfSymbolName = "plus"),
                        trailingIcon = NativeIcon(Icons.AutoMirrored.Filled.ArrowForward, sfSymbolName = "arrow.right"),
                    )
                    NativeButton("Pill", { toast("Clicked Pill") }, shape = NativeButtonShape.Pill, variant = NativeButtonVariant.Secondary)
                }
                NativeButton("Full width", { toast("Clicked Full width") }, fullWidth = true)
            }

            // ----- NativeIconButton / pull-down menu / NativeSplitButton -----
            Section("Buttons — icon, menu & split") {
                val sampleMenu = NativeMenu(
                    items = listOf(
                        NativeMenuItem("Edit", { toast("Selected Edit") }, icon = NativeIcon(Icons.Default.Edit, sfSymbolName = "pencil")),
                        // selected = true shows a native checkmark (iOS UIMenu .on / Android trailing check).
                        NativeMenuItem("Share", { toast("Selected Share") }, icon = NativeIcon(Icons.Default.Share, sfSymbolName = "square.and.arrow.up"), selected = true),
                        NativeMenuItem(
                            "Delete",
                            { toast("Selected Delete") },
                            icon = NativeIcon(Icons.Default.Delete, sfSymbolName = "trash"),
                            role = NativeMenuItemRole.Destructive,
                        ),
                    ),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    NativeIconButton(
                        NativeIcon(Icons.Default.Add, sfSymbolName = "plus"),
                        { toast("Clicked Add") },
                        contentDescription = "Add",
                        variant = NativeButtonVariant.Primary,
                    )
                    NativeIconButton(
                        NativeIcon(Icons.Default.Edit, sfSymbolName = "pencil"),
                        { toast("Clicked Edit") },
                        contentDescription = "Edit",
                        variant = NativeButtonVariant.Outline,
                    )
                    NativeIconButton(
                        NativeIcon(Icons.Default.MoreVert, sfSymbolName = "ellipsis"),
                        { toast("Opened more") },
                        contentDescription = "More",
                        menu = sampleMenu,
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    NativeButton("Export", { toast("Opened Export") }, menu = sampleMenu)
                    NativeSplitButton(
                        "Save",
                        onPrimaryClick = { toast("Split primary clicked") },
                        menu = sampleMenu,
                        leadingIcon = NativeIcon(Icons.Default.Add, sfSymbolName = "plus"),
                    )
                }
            }

            // ----- NativeCard / NativeListSection / NativeListItem / NativeDivider -----
            Section("Cards & lists") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    NativeCard(
                        variant = NativeCardVariant.Elevated,
                        onClick = { toast("Open One Piece") },
                        modifier = Modifier.width(150.dp),
                    ) {
                        NativeText("One Piece", style = NativeTextStyle.Title)
                        NativeText("Eiichiro Oda · Ch. 1042", style = NativeTextStyle.Label)
                    }
                    NativeCard(variant = NativeCardVariant.Outlined, modifier = Modifier.width(150.dp)) {
                        NativeText("Outlined", style = NativeTextStyle.Body)
                        NativeText("Quiet separation", style = NativeTextStyle.Label)
                    }
                }
                NativeListSection(
                    header = "Reader settings",
                    rows = listOf(
                        {
                            NativeListItem(
                                "Reading direction",
                                trailingText = "Left → Right",
                                onClick = { toast("Reading direction") },
                            )
                        },
                        {
                            NativeListItem(
                                "New-chapter alerts",
                                supporting = "Notify when a followed series updates",
                                trailing = { NativeToggle(checked = wifiOn, onCheckedChange = { wifiOn = it }) },
                            )
                        },
                        {
                            NativeListItem(
                                "Download quality",
                                trailingText = "High",
                                onClick = { toast("Download quality") },
                            )
                        },
                    ),
                )
                NativeDivider()
                NativeListItem("About", supporting = "Version 1.0.0", onClick = { toast("About") })
            }

            // ----- NativeChip / NativeAvatar / NativeBadge / NativeRating -----
            Section("Chips, avatars, badges & rating") {
                val genres = listOf("Action", "Romance", "Comedy", "Horror")
                var selectedGenres by remember { mutableStateOf(setOf("Action")) }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    genres.forEach { g ->
                        NativeChip(
                            label = g,
                            style = NativeChipStyle.Filter,
                            selected = g in selectedGenres,
                            onClick = {
                                selectedGenres = if (g in selectedGenres) selectedGenres - g else selectedGenres + g
                            },
                        )
                    }
                    NativeChip(
                        label = "Ongoing",
                        style = NativeChipStyle.Input,
                        selected = true,
                        trailingIcon = Icons.Default.Close,
                        onTrailingClick = { toast("Removed filter") },
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    NativeAvatar(initials = "JD")
                    NativeAvatar(initials = "AK", size = NativeAvatarSize.Large)
                    NativeAvatar(icon = Icons.Default.Edit, shape = NativeAvatarShape.Rounded)
                    NativeBadgedBox(badge = { NativeBadge(count = 5) }) {
                        NativeAvatar(initials = "MX")
                    }
                }
                var stars by remember { mutableStateOf(3f) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    NativeText("Score 4.5", style = NativeTextStyle.Label)
                    NativeRating(4.5f)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    NativeText("Rate it", style = NativeTextStyle.Label)
                    NativeRating(stars, onRatingChange = { stars = it })
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    NativeText("Disabled", style = NativeTextStyle.Label)
                    NativeRating(3.5f, onRatingChange = { stars = it }, enabled = false) // dimmed + read-only
                }
            }

            // ----- NativeCheckbox / NativeRadioGroup / NativeSkeleton / NativeEmptyState -----
            Section("Selection & states") {
                var downloadHd by remember { mutableStateOf(true) }
                var hideSpoilers by remember { mutableStateOf(false) }
                NativeCheckbox(checked = downloadHd, onCheckedChange = { downloadHd = it }, label = "Download in HD")
                NativeCheckbox(checked = hideSpoilers, onCheckedChange = { hideSpoilers = it }, label = "Hide spoilers in comments")

                NativeText("Image quality", style = NativeTextStyle.Label)
                val qualities = listOf("Data saver", "Standard", "High")
                var quality by remember { mutableStateOf("Standard") }
                NativeRadioGroup(options = qualities, selected = quality, onSelectedChange = { quality = it })

                NativeText("Sort by (iOS checkmark style)", style = NativeTextStyle.Label)
                val sorts = listOf("Latest", "A–Z", "Popularity")
                var sort by remember { mutableStateOf("Latest") }
                NativeRadioGroup(
                    options = sorts,
                    selected = sort,
                    onSelectedChange = { sort = it },
                    style = NativeSelectionStyle.Checkmark,
                )

                NativeText("Loading (skeletons)", style = NativeTextStyle.Label)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            NativeSkeleton(Modifier.size(96.dp, 132.dp))
                            NativeSkeleton(Modifier.width(96.dp).height(12.dp))
                        }
                    }
                }

                NativeEmptyState(
                    title = "Your library is empty",
                    message = "Browse and add series to see them here.",
                    icon = Icons.Default.Search,
                    actionLabel = "Browse",
                    onAction = { toast("Browse") },
                )
            }

            // ----- NativeSearchBar / NativeProgressIndicator / NativePageControl (native-backed) -----
            Section("Progress, search & paging") {
                var query by remember { mutableStateOf("") }
                NativeSearchBar(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "Search manga…",
                    onSearch = { toast("Search: $query") },
                    ios = NativeSearchBarIosOptions(showCancelButton = true),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    NativeProgressIndicator()
                    NativeText("Loading…", style = NativeTextStyle.Label)
                }
                var downloaded by remember { mutableStateOf(0.4f) }
                NativeText("Download ${(downloaded * 100).toInt()}%", style = NativeTextStyle.Label)
                NativeProgressIndicator(
                    kind = NativeProgressKind.Linear,
                    progress = downloaded,
                    modifier = Modifier.fillMaxWidth(),
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NativeButton("-10%", { downloaded = (downloaded - 0.1f).coerceIn(0f, 1f) }, variant = NativeButtonVariant.Outline, size = NativeButtonSize.Small)
                    NativeButton("+10%", { downloaded = (downloaded + 0.1f).coerceIn(0f, 1f) }, variant = NativeButtonVariant.Outline, size = NativeButtonSize.Small)
                }
                // iOS Compose-fallback combos (no native control): determinate circular + indeterminate linear.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    NativeProgressIndicator(kind = NativeProgressKind.Circular, progress = downloaded)
                    NativeProgressIndicator(kind = NativeProgressKind.Linear, modifier = Modifier.width(120.dp))
                    NativeText("ring + looping bar", style = NativeTextStyle.Label)
                }
                var page by remember { mutableStateOf(0) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    NativePageControl(pageCount = 5, currentPage = page, onCurrentPageChange = { page = it })
                    NativeButton("Next", { page = (page + 1) % 5 }, variant = NativeButtonVariant.Tertiary, size = NativeButtonSize.Small)
                }
            }

            // ----- NativeDatePicker / ColorWell / Otp / Image / Share / Sheet / Popover / list swipe -----
            Section("Pickers, sheets, share & media") {
                var date by remember { mutableStateOf<Long?>(null) }
                NativeText("Date picker", style = NativeTextStyle.Label)
                NativeDatePicker(selectedMillis = date, onSelectedMillisChange = { date = it })

                var highlight by remember { mutableStateOf(Color(0xFF1E88E5)) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    NativeText("Highlight color", style = NativeTextStyle.Label)
                    NativeColorWell(color = highlight, onColorChange = { highlight = it })
                }

                var otp by remember { mutableStateOf("") }
                NativeText("Verification code", style = NativeTextStyle.Label)
                NativeOtpField(value = otp, onValueChange = { otp = it }, length = 6, onFilled = { toast("Code: $it") })

                val share = rememberNativeShare()
                var sheetOpen by remember { mutableStateOf(false) }
                var popoverOpen by remember { mutableStateOf(false) }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    NativeButton(
                        "Share",
                        { share.share(text = "Check out One Piece", url = "https://example.com/one-piece") },
                        variant = NativeButtonVariant.Outline,
                    )
                    NativeButton("Open sheet", { sheetOpen = true }, variant = NativeButtonVariant.Secondary)
                    NativePopover(
                        visible = popoverOpen,
                        onDismissRequest = { popoverOpen = false },
                        anchor = {
                            NativeButton("Popover", { popoverOpen = true }, variant = NativeButtonVariant.Tertiary)
                        },
                    ) {
                        NativeText("Quick info", style = NativeTextStyle.Body)
                        NativeText("Native UIPopoverPresentationController on iOS; a Compose Popup on Android.", style = NativeTextStyle.Label)
                    }
                }

                NativeListItem(
                    "Chapter 1043",
                    supporting = "Swipe ← to archive · long-press for options",
                    onClick = { toast("Open chapter 1043") },
                    onLongClick = { toast("Long-pressed Chapter 1043") },
                    swipeAction = NativeSwipeAction(
                        label = "Archive",
                        onAction = { toast("Archived 1043") },
                        icon = Icons.Default.Delete,
                    ),
                )

                NativeSheet(
                    visible = sheetOpen,
                    onDismissRequest = { sheetOpen = false },
                    detents = listOf(NativeSheetDetent.Medium, NativeSheetDetent.Large),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        NativeText("Chapter options", style = NativeTextStyle.Title)
                        NativeListItem("Download", onClick = { toast("Download"); sheetOpen = false })
                        NativeListItem("Mark as read", onClick = { toast("Marked read"); sheetOpen = false })
                        NativeButton("Close", { sheetOpen = false }, fullWidth = true)
                    }
                }
            }

            // ----- NativeFeedback: transient messages (toast / snackbar) -----
            Section("Feedback — toast & snackbar") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    NativeButton("Toast", { feedback.toast("Saved") })
                    NativeButton(
                        "Success",
                        { feedback.toast("Uploaded", status = NativeFeedbackStatus.Success) },
                        variant = NativeButtonVariant.Secondary,
                    )
                    NativeButton(
                        "Error",
                        { feedback.toast("Upload failed", status = NativeFeedbackStatus.Error) },
                        variant = NativeButtonVariant.Destructive,
                    )
                    NativeButton(
                        "System toast",
                        { feedback.toast("Native system toast", android = NativeToastAndroidOptions(useSystemToast = true)) },
                        variant = NativeButtonVariant.Outline,
                    )
                    NativeButton(
                        "Snackbar",
                        { feedback.snackbar("Message sent") },
                        variant = NativeButtonVariant.Outline,
                    )
                    NativeButton(
                        "Undo snackbar",
                        {
                            feedback.snackbar(
                                "Item deleted",
                                actionLabel = "Undo",
                                onAction = { feedback.toast("Restored", status = NativeFeedbackStatus.Success) },
                            )
                        },
                        variant = NativeButtonVariant.Outline,
                    )
                    NativeButton(
                        "Fire 3 (FIFO)",
                        {
                            feedback.toast("First")
                            feedback.toast("Second", status = NativeFeedbackStatus.Success)
                            feedback.toast("Third", status = NativeFeedbackStatus.Warning)
                        },
                        variant = NativeButtonVariant.Tertiary,
                    )
                }
            }

            // ----- NativeFeedback: banners -----
            Section("Feedback — banners") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    NativeButton("Top info", {
                        feedback.banner(
                            "Syncing your data…",
                            title = "Sync",
                            status = NativeFeedbackStatus.Info,
                            position = NativeFeedbackPosition.Top,
                        )
                    })
                    NativeButton(
                        "Success + action",
                        {
                            feedback.banner(
                                "Account updated",
                                status = NativeFeedbackStatus.Success,
                                position = NativeFeedbackPosition.Top,
                                actionLabel = "View",
                                onAction = { feedback.toast("Viewing account") },
                            )
                        },
                        variant = NativeButtonVariant.Secondary,
                    )
                    NativeButton(
                        "Bottom warning",
                        {
                            feedback.banner(
                                "You are offline",
                                status = NativeFeedbackStatus.Warning,
                                position = NativeFeedbackPosition.Bottom,
                            )
                        },
                        variant = NativeButtonVariant.Outline,
                    )
                }
            }

            // ----- NativeFeedback: alert & confirmation sheet (native by default, branded opt-in) -----
            Section("Feedback — alert & sheet") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    NativeButton("Alert", {
                        feedback.alert(
                            title = "Unsaved changes",
                            message = "Discard your edits?",
                            actions = listOf(
                                NativeAlertAction("Discard", { toast("Discarded") }, role = NativeAlertActionRole.Destructive),
                                NativeAlertAction("Keep editing", role = NativeAlertActionRole.Cancel),
                            ),
                        )
                    })
                    NativeButton(
                        "Branded alert",
                        {
                            feedback.alert(
                                title = "Delete item?",
                                message = "This cannot be undone.",
                                actions = listOf(
                                    NativeAlertAction("Delete", { toast("Deleted") }, role = NativeAlertActionRole.Destructive),
                                    NativeAlertAction("Cancel", role = NativeAlertActionRole.Cancel),
                                ),
                                ios = NativeAlertIosOptions(presentation = NativePresentation.Branded),
                            )
                        },
                        variant = NativeButtonVariant.Outline,
                    )
                    NativeButton(
                        "Action sheet",
                        {
                            feedback.confirmationSheet(
                                title = "Export as",
                                actions = listOf(
                                    NativeSheetAction("PDF", { toast("Export PDF") }, icon = NativeIcon(Icons.Default.Share, sfSymbolName = "doc")),
                                    NativeSheetAction("CSV", { toast("Export CSV") }, icon = NativeIcon(Icons.Default.Edit, sfSymbolName = "tablecells")),
                                    NativeSheetAction("Delete", { toast("Deleted") }, role = NativeAlertActionRole.Destructive, icon = NativeIcon(Icons.Default.Delete, sfSymbolName = "trash")),
                                    NativeSheetAction("Cancel", role = NativeAlertActionRole.Cancel),
                                ),
                            )
                        },
                        variant = NativeButtonVariant.Secondary,
                    )
                    NativeButton(
                        "Branded sheet",
                        {
                            feedback.confirmationSheet(
                                title = "Choose source",
                                message = "Where should we get the image?",
                                actions = listOf(
                                    NativeSheetAction("Camera", { toast("Camera") }),
                                    NativeSheetAction("Photo Library", { toast("Library") }),
                                    NativeSheetAction("Cancel", role = NativeAlertActionRole.Cancel),
                                ),
                                ios = NativeConfirmationSheetIosOptions(presentation = NativePresentation.Branded),
                            )
                        },
                        variant = NativeButtonVariant.Outline,
                    )
                }
            }

            // ----- NativeInlineStatus: in-flow (not a floating overlay) -----
            Section("Feedback — inline status") {
                NativeInlineStatus("Saved locally — will sync when you're back online.", status = NativeFeedbackStatus.Info)
                NativeInlineStatus("Profile updated successfully.", title = "Done", status = NativeFeedbackStatus.Success)
                NativeInlineStatus(
                    "Your free trial ends in 3 days.",
                    status = NativeFeedbackStatus.Warning,
                    filled = false,
                    actionLabel = "Upgrade",
                    onAction = { feedback.toast("Upgrade") },
                )
                if (showInlineError) {
                    NativeInlineStatus(
                        "Couldn't load comments.",
                        status = NativeFeedbackStatus.Error,
                        actionLabel = "Retry",
                        onAction = { feedback.toast("Retrying…", status = NativeFeedbackStatus.Info) },
                        onDismiss = { showInlineError = false },
                    )
                } else {
                    NativeButton("Reset inline error", { showInlineError = true }, variant = NativeButtonVariant.Tertiary)
                }
            }

            // ----- Structural components (T7): standalone top bar + content-state lifecycle -----
            Section("Structural — top bar & content state") {
                NativeTopBar(
                    title = "Library",
                    subtitle = "Standalone NativeTopBar (decoupled from nav)",
                    actions = {
                        NativeIconButton(
                            NativeIcon(Icons.Default.Add, sfSymbolName = "plus"),
                            { toast("Add") },
                            contentDescription = "Add",
                        )
                    },
                )
                var phase by remember { mutableStateOf(0) }
                val loadState: NativeLoadState<String> = when (phase % 4) {
                    0 -> NativeLoadState.Loading
                    1 -> NativeLoadState.Empty
                    2 -> NativeLoadState.Error("Couldn't load the library.")
                    else -> NativeLoadState.Content("Loaded 42 series")
                }
                NativeButton(
                    "Cycle content state",
                    { phase++ },
                    variant = NativeButtonVariant.Outline,
                    size = NativeButtonSize.Small,
                )
                NativeContentState(
                    state = loadState,
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    onRetry = { phase = 3 },
                    emptyTitle = "Your library is empty",
                    emptyMessage = "Add a series to see it here.",
                ) { msg ->
                    NativeText(msg, style = NativeTextStyle.Body)
                }
            }

            // ----- In-content selection (native segmented) + custom-content dialog -----
            // NativeTabBar and NativeTooltip were deprecated (kit thesis: native-per-platform). In-content tabs
            // now use the native NativeSegmentedControl (a real UISegmentedControl on iOS); iOS has no tooltip idiom.
            Section("Segmented tabs & custom-content dialog") {
                var tab by remember { mutableStateOf(0) }
                NativeSegmentedControl(
                    options = listOf("Overview", "Chapters", "Comments"),
                    selectedIndex = tab,
                    onSelectedIndexChange = { tab = it },
                    modifier = Modifier.fillMaxWidth(),
                    contentDescription = "Section tabs",
                )
                NativeText("Selected tab ${tab + 1}", style = NativeTextStyle.Label, modifier = Modifier.nativeHeading())

                var dialogOpen by remember { mutableStateOf(false) }
                NativeButton("Open custom dialog", { dialogOpen = true }, variant = NativeButtonVariant.Outline)
                if (dialogOpen) {
                    NativeDialog(
                        onDismissRequest = { dialogOpen = false },
                        title = "Custom dialog",
                        actions = {
                            NativeButton("Cancel", { dialogOpen = false }, variant = NativeButtonVariant.Tertiary, size = NativeButtonSize.Small)
                            NativeButton("OK", { dialogOpen = false }, size = NativeButtonSize.Small)
                        },
                    ) {
                        NativeText("Arbitrary content lives here — a form, a list, anything you compose.", style = NativeTextStyle.Body)
                    }
                }
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        NativeText(title, style = NativeTextStyle.Title)
        content()
    }
}
